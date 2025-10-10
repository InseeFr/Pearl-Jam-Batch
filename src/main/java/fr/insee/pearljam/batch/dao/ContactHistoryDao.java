package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.InformationCollectePrecedenteType;

public interface ContactHistoryDao {

    void createContactHistory(InformationCollectePrecedenteType informationCollectePrecedente, String surveyUnitId);

    InformationCollectePrecedenteType findBySurveyUnitId(String surveyUnitId);

    void deleteBySurveyUnitId(String surveyUnitId);

}
