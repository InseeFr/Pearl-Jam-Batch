package fr.insee.pearljam.batch.service;

import fr.insee.pearljam.batch.communication.CommunicationTemplate;
import fr.insee.pearljam.batch.exception.MissingCommunicationException;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.utils.BatchErrorCode;

@Service
public interface CommunicationService {

    BatchErrorCode handleCommunications() throws SynchronizationException, MissingCommunicationException;

    // from Meshuggah
    CommunicationTemplate getCommunicationTemplate(String communicationTemplateId) throws MissingCommunicationException, SynchronizationException;

}
