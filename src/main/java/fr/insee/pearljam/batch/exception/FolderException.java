package fr.insee.pearljam.batch.exception;

import java.io.Serial;

/**
 * Class to throw a FolderException
 * @author scorcaud
 *
 */
public class FolderException extends Exception {
	
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for a FolderException
	 * @param message
	 */
	public FolderException(String message) {
		super(message);
	}
}
