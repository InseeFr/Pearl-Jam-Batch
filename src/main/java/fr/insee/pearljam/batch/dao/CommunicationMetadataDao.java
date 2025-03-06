package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationMetadataType;

public interface CommunicationMetadataDao {

    void createMetadata(CommunicationMetadataType metadata);

    void updateMetadata(CommunicationMetadataType metadata);


}
