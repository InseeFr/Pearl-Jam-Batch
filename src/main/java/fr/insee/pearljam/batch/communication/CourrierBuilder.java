package fr.insee.pearljam.batch.communication;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
public class CourrierBuilder {

	private CourrierBuilder() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Builds a {@link Courrier} object from communication data and template.
	 *
	 * @param data          the communication data
	 * @param template      the communication template
	 * @param editionId     the edition ID
	 * @param documentIndex the sequential index for NumeroDocument
	 * @return Courrier
	 */
	public static Courrier buildFrom(
			CommunicationData data,
			CommunicationTemplate template,
			String editionId,
			int documentIndex
	) {

		Courrier courrier = new Courrier();
		Variables variables = new Variables();
		courrier.setVariables(variables);

		String numeroDocument = String.format("%08d", documentIndex);
		variables.setNumeroDocument(numeroDocument);
		variables.setBddIdentifiantUniteEnquetee(data.getCommunicationRequestId()); // replaced by su.businessId soon
		variables.setCodePostalDestinataire(data.getRecipientPostCode());

		// Address fields
		variables.setBddAdressePosteeL1(data.getBddL1());
		variables.setBddAdressePosteeL2(data.getBddL2());
		variables.setBddAdressePosteeL3(data.getBddL3());
		variables.setBddAdressePosteeL4(data.getBddL4());
		variables.setBddAdressePosteeL5(data.getBddL5());
		variables.setBddAdressePosteeL6(data.getBddL6());
		variables.setBddAdressePosteeL7(data.getBddL7());

		// Interviewer fields
		variables.addAdditionalField("Ue_DateEdition", data.getEditionDate());
		variables.addAdditionalField("Ue_CiviliteEnqueteur", data.getInterviewerTitle());
		variables.addAdditionalField("Ue_NomEnqueteur", data.getInterviewerLastName());
		variables.addAdditionalField("Ue_PrenomEnqueteur", data.getInterviewerFirstName());
		variables.addAdditionalField("Ue_MailEnqueteur", data.getInterviewerEmail());
		variables.addAdditionalField("Ue_TelEnqueteur", data.getInterviewerTel());

		// Conditional reminder reason
		if ("REMINDER".equals(template.getCommunicationType())) {
			variables.addAdditionalField("Ue_TypeRelance", data.getReminderReason());
		}

		// OU contact
		variables.addAdditionalField("Ue_MailAssistance", data.getMailAssistance());
		variables.addAdditionalField("Ue_TelAssistance", data.getTelAssistance());

		// Barcode
		// will be replaced by su.businessId soon
		variables.setBarcode(generateBarCode(editionId, data.getCommunicationRequestId()));

		// Acknowledgement flag
		variables.setInitAccuseReception(template.isInitAccuseReception() ? "oui" : "non");

		// Metadata
		if (data.getTemplateMetadata() != null) {
			data.getTemplateMetadata().forEach(meta -> variables.addAdditionalField(meta.getKey(), meta.getValue()));
		} else {
			log.warn("{} : Template metadata {} is null", data.getSurveyUnitBusinessId(),
					template.getCommunicationId());
		}

		return courrier;
	}

	private static String generateBarCode(String idEdition, String surveyUnitBusinessId) {
		// Segment 1: Code identifiant de l'Insee
		StringBuilder barcode = new StringBuilder("IS");

		// Segment 2: Date de dépôt du courrier
		int dayOfYear = LocalDate.now().getDayOfYear();
		barcode.append(String.format("%03d", dayOfYear));  // Ensure it's 3 digits long

		// Segment 3: Édition (id_edition with the first two characters removed, up to 11 characters)
		String truncatedEdition = idEdition.substring(2);
		barcode.append(truncatedEdition);

		// Segment 4: Destinataire courrier (surveyUnit business ID, filled up to 14 chars with spaces)
		String truncatedCommunicationId = surveyUnitBusinessId.length() > 14 ? surveyUnitBusinessId.substring(0,
				14) : String.format("%-14s", surveyUnitBusinessId);
		barcode.append(truncatedCommunicationId);

		// Segment 5: Ventilation (fixed 2 spaces)
		barcode.append("  ");

		// Segment 6: Code application (fixed 3 spaces)
		barcode.append("   ");

		return barcode.toString();
	}
}
