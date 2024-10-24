package fr.insee.pearljam.batch.service;

import fr.insee.pearljam.batch.communication.CommunicationTemplate;
import fr.insee.pearljam.batch.exception.MissingCommunicationException;
import fr.insee.pearljam.batch.exception.SynchronizationException;

import java.io.File;


public interface MeshuggahService {

    CommunicationTemplate getCommunicationTemplate(String templateId) throws MissingCommunicationException, SynchronizationException;
    String getNewEditionNumber() throws SynchronizationException;
    boolean postPublication(File fileToPublish, String communicationModele) ;
}
