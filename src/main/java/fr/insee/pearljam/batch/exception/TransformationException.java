package fr.insee.pearljam.batch.exception;

/**
 * Exception used when transforming xml to object or object to xml
 * @author scorcaud
 *
 */
public class TransformationException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new TransformationException with the specified detail message and cause.
	 *
	 * @param message the detail message
	 * @param cause   the underlying cause of the exception
	 */
	public TransformationException(String message, Throwable cause) {
		super(message, cause);
	}
}
