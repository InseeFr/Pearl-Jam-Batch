package fr.insee.pearljam.batch.dao;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.pearljam.batch.dto.CampaignDataCollectionDto;
import fr.insee.pearljam.batch.exception.DataCollectionApiException;

public interface DataCollectionRepository {
    CampaignDataCollectionDto retrieveCampaign(String campaignId) throws DataCollectionApiException;
    void deleteInterrogation(String interrogationId);
    void createOrUpdateInterrogation(String interrogationId, String surveyUnitId, String questionnaireModelId, String campaignId, ObjectNode data);
}
