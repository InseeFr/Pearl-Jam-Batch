package fr.insee.pearljam.batch.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.pearljam.batch.dao.DataCollectionRepository;
import fr.insee.pearljam.batch.dto.CampaignDataCollectionDto;
import fr.insee.pearljam.batch.exception.DataCollectionApiException;
import fr.insee.pearljam.batch.exception.TransformationException;
import fr.insee.pearljam.batch.exception.ValidateException;

import fr.insee.pearljam.batch.sampleprocessing.Campagne;
import fr.insee.pearljam.batch.utils.XmlToJsonLunaticConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.xml.transform.TransformerException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class DataCollectionService {
    private static final String LOG_CONTEXT = "context";
    private final DataCollectionValidation dataCollectionValidation;
    private final DataCollectionRepository dataCollectionRepository;
    private final XmlToJsonLunaticConverter lunaticConverter;

    public void validate(Campagne campaign) throws ValidateException {
        String campaignId = campaign.getIdSource() + campaign.getMillesime() + campaign.getIdPeriode();
        log.info(
                "************************************ Start DATACOLLECTION API Step validation ****************************");

        CampaignDataCollectionDto campaignApi;
        try {
            campaignApi = dataCollectionRepository.retrieveCampaign(campaignId);
        } catch(DataCollectionApiException e) {
            throw new ValidateException(e.getMessage());
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
     * Create interrogation from queen sample
     *
     * @param questionnaire interrogation in platine batch format
     * @param campaignId campaign id
     * @return true if success, false otherwise
     */
    public void createOrUpdateInterrogation(Campagne.Questionnaires.Questionnaire questionnaire, String campaignId) throws DataCollectionApiException, TransformationException {
        String surveyUnitId = questionnaire
                .getInformationsGenerales()
                .getUniteEnquetee()
                .getIdentifiant();

        ObjectNode data = lunaticConverter.convertInterrogationData(questionnaire.getInformationsPersonnalisees().getData());
        dataCollectionRepository.createOrUpdateInterrogation(questionnaire.getIdInterrogation(), surveyUnitId, questionnaire.getIdModele(), campaignId, data);
    }

    /**
     * Delete interrogation
     * @param questionnaire questionnaire to delete
     * @return true if success, false otherwise
     */
    public boolean deleteInterrogation(Campagne.Questionnaires.Questionnaire questionnaire) {
        String idInterrogation = questionnaire.getIdInterrogation();
        try {
            dataCollectionRepository.deleteInterrogation(idInterrogation);
            return true;
        } catch(DataCollectionApiException e) {
            log.error(LOG_CONTEXT, e);
            log.error("Error when deleting interrogation: {} and step DATACOLLECTION", idInterrogation);
            return false;
        }
    }
}
