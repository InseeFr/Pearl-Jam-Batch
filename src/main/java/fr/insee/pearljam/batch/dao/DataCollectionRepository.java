package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.dto.CampaignDataCollectionDto;
import fr.insee.pearljam.batch.dto.InterrogationDataCollectionDto;
import fr.insee.pearljam.batch.exception.DataCollectionApiException;

import java.util.List;

public interface DataCollectionRepository {
    CampaignDataCollectionDto retrieveCampaign(String campaignId) throws DataCollectionApiException;
    void saveInterrogations(List<InterrogationDataCollectionDto> interrogations, String campaignId);
}
