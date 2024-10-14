package fr.insee.pearljam.batch.exception;


import fr.insee.pearljam.batch.service.impl.CommunicationServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MissingCommunicationException extends Exception {
    private static final Logger logger = LogManager.getLogger(MissingCommunicationException.class);
    private static final long serialVersionUID = 1L;

    /**
     * Defaut constructor of a FolderException
     */
    public MissingCommunicationException() {
        super();
    }

    /**
     * Constructor for a FolderException,
     * @param message
     */
    public MissingCommunicationException(String templateId,Exception e) {
        super(String.format("Failed to fetch template for ID: %s. Error: %s", templateId, e.getMessage()));
        logger.error("Failed to fetch template for ID: {}. Error: {}", templateId, e.getMessage());

    }
}
