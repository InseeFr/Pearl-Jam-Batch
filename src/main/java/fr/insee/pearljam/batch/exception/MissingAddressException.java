package fr.insee.pearljam.batch.exception;

public class MissingAddressException extends RuntimeException {
	public MissingAddressException(String surveyUnitId) {
		super("Missing address for survey unit: " + surveyUnitId);
	}
}
