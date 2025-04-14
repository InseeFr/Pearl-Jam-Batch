package fr.insee.pearljam.batch.dao;

public interface CommunicationRequestStatusDao {

    void addStatus(String communicationRequestId, String status, long timestamp);

    void deleteBySurveyUnitId(String surveyUnitId);

}
