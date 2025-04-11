package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationRequestType;

import java.util.List;

public interface CommunicationRequestDao {

    public List<CommunicationRequestType> findAll();

    public void delete(String communicationRequestId);

    void save(CommunicationRequestType request);
}
