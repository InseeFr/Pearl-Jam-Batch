package fr.insee.pearljam.batch.enums;

public enum BatchOption {
	DELETECAMPAIGN("DELETECAMPAIGN"),
	LOADCONTEXT("LOADCONTEXT"),
	DAILYUPDATE("DAILYUPDATE"),
	SYNCHRONIZE("SYNCHRONIZE"),
	EXTRACT("EXTRACT"),
	COMMUNICATION("COMMUNICATION"),
	SAMPLEPROCESSING("SAMPLEPROCESSING");

	/**
	 * label of the BatchOption
	 */
	private final String label;

	/**
	 * Defaut constructor for BatchOption
	 *
	 * @param label
	 */
	BatchOption(String label) {
		this.label = label;
	}

	/**
	 * Get the label for BatchOption
	 *
	 * @return label
	 */
	public String getLabel() {
		return label;
	}
}
