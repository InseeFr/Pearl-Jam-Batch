package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationMetadataType;
import java.util.List;
import java.util.Map;

public interface CommunicationMetadataDao {

	List<CommunicationMetadataType> findMetadataByCampaignIdAndMeshuggahIdAndSurveyUnitId (String campaignId, String meshuggahId, String surveyUnitId);
	void createAllMetadataForSurveyUnits(Map<String, List<CommunicationMetadataType>> metadataBySurveyUnit);

	void deleteBySurveyUnitId(String surveyUnitId);
}
