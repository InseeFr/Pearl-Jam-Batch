package fr.insee.pearljam.batch.exception;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PublicationException extends Exception {
    private static final Logger logger = LogManager.getLogger(PublicationException.class);

    public PublicationException(String communicationModele, Exception e) {
        super(String.format("Failed to publish communication with model %s. Error: %s", communicationModele, e.getMessage()));
        logger.error("Failed to fetch template for ID: {}. Error: {}", communicationModele, e.getMessage());

    }
}
