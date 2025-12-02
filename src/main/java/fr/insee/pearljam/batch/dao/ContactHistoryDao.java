package fr.insee.pearljam.batch.dao;


import fr.insee.pearljam.batch.campaign.PreviousCollectionInformationType;

public interface ContactHistoryDao {

    void createContactHistory(PreviousCollectionInformationType previousCollectionInformation, String surveyUnitId);

    PreviousCollectionInformationType findBySurveyUnitId(String surveyUnitId);

    void deleteBySurveyUnitId(String surveyUnitId);

    void deletePreviousContactTypeBySurveyUnitId(String surveyUnitId);

}
