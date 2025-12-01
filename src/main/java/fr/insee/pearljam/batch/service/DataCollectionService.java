package fr.insee.pearljam.batch.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.pearljam.batch.dao.DataCollectionRepository;
import fr.insee.pearljam.batch.dto.CampaignDataCollectionDto;
import fr.insee.pearljam.batch.dto.InterrogationDataCollectionDto;
import fr.insee.pearljam.batch.exception.DataCollectionApiException;
import fr.insee.pearljam.batch.exception.TransformationException;
import fr.insee.pearljam.batch.exception.ValidateException;

import fr.insee.pearljam.batch.sampleprocessing.Campagne;
import fr.insee.pearljam.batch.utils.XmlToJsonLunaticConverter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DataCollectionService {
    private static final String LOG_CONTEXT = "context";
    private final DataCollectionValidation dataCollectionValidation;
    private final DataCollectionRepository dataCollectionRepository;
    private final XmlToJsonLunaticConverter lunaticConverter;
    private static final Logger log = LogManager.getLogger(DataCollectionService.class);


    public void validate(Campagne campaign) throws ValidateException {
        String campaignId = campaign.getIdSource() + campaign.getMillesime() + campaign.getIdPeriode();
        log.info(
                "************************************ Start DATACOLLECTION API Step validation ****************************");

        CampaignDataCollectionDto campaignApi;
        try {
            campaignApi = dataCollectionRepository.retrieveCampaign(campaignId);
        } catch(DataCollectionApiException e) {
            throw new ValidateException(e.getMessage(), e);
        }

        List<String> questionnaireIds = campaignApi.questionnaireIds();

        if(questionnaireIds == null || questionnaireIds.isEmpty()) {
            throw new ValidateException(
                    String.format("Campaign %s does not have questionnaire ids, aborting data collection api step.", campaignId)
            );
        }
        dataCollectionValidation.validateQuestionnaireModels(questionnaireIds, campaign);

        log.info("************************************  DATACOLLECTION API Step Validation OK ****************************");
    }

    /**
     * Create interrogations from queen sample
     *
     * @param interrogations survey units in platine batch format
     * @param campaignId campaign id
     */
    public void saveInterrogations(List<InterrogationDataCollectionDto> interrogations, String campaignId) {
        log.info("Starting creating/updating interrogations");
        dataCollectionRepository.saveInterrogations(interrogations, campaignId);
    }

    public InterrogationDataCollectionDto buildInterrogation(Campagne.Questionnaires.Questionnaire questionnaire) throws TransformationException {
        String surveyUnitId = questionnaire
                .getInformationsGenerales()
                .getUniteEnquetee()
                .getIdentifiant();

        log.info("Building interrogation for survey unit id {}", surveyUnitId);

        ObjectNode data = lunaticConverter.convertInterrogationData(questionnaire.getInformationsPersonnalisees().getData());
        return new InterrogationDataCollectionDto(questionnaire.getIdInterrogation(), surveyUnitId, questionnaire.getIdModele(), data);
    }
}
