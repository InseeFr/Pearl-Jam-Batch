package fr.insee.pearljam.batch.exception;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PublicationException extends Exception {
    private static final Logger logger = LogManager.getLogger(PublicationException.class);

    public PublicationException(String errorMessage, Exception e) {
        super(errorMessage, e);
        logger.error("Message : {}. Error: {}", errorMessage, e.getMessage());

    }
}
