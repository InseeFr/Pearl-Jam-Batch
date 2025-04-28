package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationRequestType;

import java.util.List;

public interface CommunicationRequestDao {

    List<CommunicationRequestType> findAll();

    void delete(String communicationRequestId);

    void save(CommunicationRequestType request);

    void deleteBySurveyUnitId(String surveyUnitId;
}
