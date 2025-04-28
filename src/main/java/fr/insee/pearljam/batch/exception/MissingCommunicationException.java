package fr.insee.pearljam.batch.exception;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serial;

public class MissingCommunicationException extends Exception {
	private static final Logger logger = LogManager.getLogger(MissingCommunicationException.class);
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for a MissingCommunicationException,
	 *
	 * @param templateId template id
	 * @param e          exception
	 */
	public MissingCommunicationException(String templateId, Exception e) {
		super(String.format("Failed to fetch template for ID: %s. Error: %s", templateId, e.getMessage()));
		logger.error("Failed to fetch template for ID: {}. Error: {}", templateId, e.getMessage());

	}
}
