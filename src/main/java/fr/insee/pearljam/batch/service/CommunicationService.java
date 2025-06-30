package fr.insee.pearljam.batch.service;

import fr.insee.pearljam.batch.exception.MissingCommunicationException;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import org.springframework.stereotype.Service;

@Service
public interface CommunicationService {

    BatchErrorCode handleCommunications() throws SynchronizationException, MissingCommunicationException;

}
