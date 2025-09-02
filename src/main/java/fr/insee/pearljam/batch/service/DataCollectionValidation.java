package fr.insee.pearljam.batch.service;

import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.sampleprocessing.Campagne;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
@NoArgsConstructor
@Slf4j
public class DataCollectionValidation {

    /**
     * Validate questionnaire models. Check that questionnaires in flow data exist in campaign
     * @param questionnaireIdsFromRepository questionnaire ids retrieved from repository
     * @param campaign sample processing campaign
     * @throws ValidateException exception when flow data contains wrong questionnaire models
     */
    public void validateQuestionnaireModels(List<String> questionnaireIdsFromRepository, Campagne campaign) throws ValidateException {
        String campaignId = campaign.getIdSource() + campaign.getMillesime() + campaign.getIdPeriode();

        // check if questionnaire models in the sample exist in questionnaire api
        if(questionnaireIdsFromRepository == null || questionnaireIdsFromRepository.isEmpty()) {
            throw new ValidateException(String.format("Campaign %s has no questionnaire models", campaignId));
        }

        List<String> questionnaireIdsSample = campaign
                .getQuestionnaires()
                .getQuestionnaire()
                .stream()
                .map(Campagne.Questionnaires.Questionnaire::getIdModele)
                .distinct()
                .toList();

        if(questionnaireIdsSample.isEmpty()) {
            throw new ValidateException(String.format("Sample for campaign %s has no questionnaire models", campaignId));
        }

        boolean allQuestionnairesFoundInRepository = new HashSet<>(questionnaireIdsFromRepository)
                .containsAll(questionnaireIdsSample);

        if(log.isInfoEnabled()) {
            log.info("All questionnaire models in sample are found in repository: {}", allQuestionnairesFoundInRepository);
            questionnaireIdsSample
                    .forEach(id -> log.info("Questionnaire model sampleProcessing file found: {}", id));
            questionnaireIdsFromRepository
                    .forEach(id -> log.info("Questionnaire model repository found: {}", id));
        }

        if (!allQuestionnairesFoundInRepository) {
            throw new ValidateException("At least one questionnaire model wasn't found in data-collection repository");
        }
    }
}
