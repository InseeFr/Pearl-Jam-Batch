package fr.insee.pearljam.batch.utils;

import java.util.stream.Collectors;

import fr.insee.pearljam.batch.Constants;
import fr.insee.pearljam.batch.campaign.Campaign;
import fr.insee.pearljam.batch.campaign.CommentType;
import fr.insee.pearljam.batch.campaign.CommentsType;
import fr.insee.pearljam.batch.campaign.InseeAddressType;
import fr.insee.pearljam.batch.campaign.InseeSampleIdentiersType;
import fr.insee.pearljam.batch.campaign.PersonType;
import fr.insee.pearljam.batch.campaign.PersonsType;
import fr.insee.pearljam.batch.campaign.PhoneNumberType;
import fr.insee.pearljam.batch.campaign.PhoneNumbersType;
import fr.insee.pearljam.batch.campaign.SurveyUnitType;
import fr.insee.pearljam.batch.campaign.SurveyUnitsType;
import fr.insee.pearljam.batch.sampleprocessing.Campagne;
import fr.insee.pearljam.batch.sampleprocessing.Campagne.Questionnaires.Questionnaire.InformationsGenerales.Contacts;
import fr.insee.pearljam.batch.sampleprocessing.Campagne.Questionnaires.Questionnaire.InformationsGenerales.Contacts.Contact;
import fr.insee.pearljam.batch.sampleprocessing.Campagne.Questionnaires.Questionnaire.InformationsGenerales.Contacts.Contact.Telephones;
import fr.insee.pearljam.batch.sampleprocessing.Campagne.Questionnaires.Questionnaire.InformationsGenerales.Contacts.Contact.Telephones.Telephone;
import fr.insee.pearljam.batch.sampleprocessing.Campagne.Questionnaires.Questionnaire.InformationsGenerales.UniteEnquetee.Commentaires;
import fr.insee.pearljam.batch.sampleprocessing.Campagne.Questionnaires.Questionnaire.InformationsGenerales.UniteEnquetee.IdentifiantsInsee;

/**
 * Operation on XML Content
 * - getXmlNodeFile
 * - validateXMLSchema
 * - xmlToObject
 * - objectToXML
 * - removeSurveyUnitNode
 * - updateSampleFileErrorList
 * 
 * @author Claudel Benjamin
 * 
 */
public class PilotageMapper {
	private PilotageMapper() {
		throw new IllegalStateException("Utility class");
	}

	public static Campaign mapSampleProcessingToPilotageCampaign(Campagne c) {
		Campaign campaign = new Campaign();
		campaign.setId(c.getIdSource() + c.getMillesime() + c.getIdPeriode());
		campaign.setSurveyUnits(new SurveyUnitsType());
		campaign.getSurveyUnits().getSurveyUnit().addAll(
				c.getQuestionnaires().getQuestionnaire().stream().map(su -> {
					SurveyUnitType surveyUnitType = new SurveyUnitType();
					surveyUnitType.setId(su.getIdInterrogation());
					surveyUnitType.setDisplayName(su.getInformationsGenerales().getUniteEnquetee().getIdentifiant());
					surveyUnitType.setPriority(su.getInformationsGenerales().getUniteEnquetee().isPrioritaire());
					surveyUnitType.setInterviewerId(
							su.getInformationsGenerales().getUniteEnquetee().getAffectation().getEnqueteurId());
					surveyUnitType.setOrganizationalUnitId(su.getInformationsGenerales().getUniteEnquetee()
							.getAffectation().getUniteOrganisationnelleId());
					surveyUnitType.setInseeAddress(
							getInseeAddressFromSampleProcessing(su.getInformationsGenerales().getContacts()));
					surveyUnitType.setInseeSampleIdentiers(getInseeSampleIdentiersFromSampleProcessing(
							su.getInformationsGenerales().getUniteEnquetee().getIdentifiantsInsee()));
					surveyUnitType
							.setPersons(getPersonsFromSampleProcessing(su.getInformationsGenerales().getContacts()));
					surveyUnitType.setComments(getCommentsFromSampleProcessing(
							su.getInformationsGenerales().getUniteEnquetee().getCommentaires()));
					return surveyUnitType;
				}).collect(Collectors.toList()));
		return campaign;
	}

	private static CommentsType getCommentsFromSampleProcessing(Commentaires commentaires) {
		CommentsType comments = new CommentsType();

		if (commentaires == null || commentaires.getCommentaire().isEmpty())
			return comments;

		commentaires.getCommentaire().stream().forEach(commentaire -> {
			CommentType comment = new CommentType();
			comment.setType(convertCommentType(commentaire.getType()));
			comment.setValue(commentaire.getValeur());
			comments.getComment().add(comment);
		});
		return comments;
	}

	private static String convertCommentType(String input) {
		String output = "";
		switch (input) {
			case "enqueteur":
				output = "INTERVIEWER";
				break;
			case "pilotage":
				output = "MANAGEMENT";
				break;
			default:
				break;
		}
		return output;
	}

	private static PersonsType getPersonsFromSampleProcessing(Contacts contacts) {
		PersonsType persons = new PersonsType();
		for (Contact contact : contacts.getContact()) {
			PersonType person = new PersonType();
			person.setTitle(contact.getCiviliteReferent());
			person.setFirstName(contact.getPrenomReferent());
			person.setLastName(contact.getNomReferent());
			person.setEmail(contact.getMailReferent());
			person.setFavoriteEmail(contact.isMailFavori() != null ? contact.isMailFavori() : false);
			person.setPrivileged(contact.isPrincipal());
			person.setDateOfBirth(contact.getDateNaissance());
			person.setPhoneNumbers(getPhoneNumbersFromSampleProcessing(contact.getTelephones()));
			persons.getPerson().add(person);
		}
		return persons;
	}

	private static PhoneNumbersType getPhoneNumbersFromSampleProcessing(Telephones telephones) {
		PhoneNumbersType phoneNumbers = new PhoneNumbersType();
		for (Telephone tel : telephones.getTelephone()) {
			PhoneNumberType phoneNumber = new PhoneNumberType();
			phoneNumber.setNumber(tel.getNumero());
			phoneNumber.setSource(tel.getSource());
			phoneNumber.setFavorite(tel.isFavori());
			phoneNumbers.getPhoneNumber().add(phoneNumber);
		}
		return phoneNumbers;
	}

	private static InseeAddressType getInseeAddressFromSampleProcessing(Contacts contacts) {
		InseeAddressType address = new InseeAddressType();
		for (Contact contact : contacts.getContact()) {
			if (contact.getAdresse() != null) {
				address.setL1(new StringBuilder().append(contact.getCiviliteReferent())
						.append(Constants.ESPACE)
						.append(contact.getPrenomReferent())
						.append(Constants.ESPACE)
						.append(contact.getNomReferent()).toString());
				if (contact.getAdresse().getComplementAdresse().length() > 38) {
					address.setL2(contact.getAdresse().getComplementAdresse().substring(0, 38));
					address.setL3(contact.getAdresse().getComplementAdresse().substring(39));
				} else {
					address.setL2(contact.getAdresse().getComplementAdresse());
				}
				address.setL4(new StringBuilder().append(contact.getAdresse().getNumeroVoie())
						.append(Constants.ESPACE)
						.append(contact.getAdresse().getIndiceRepetition())
						.append(Constants.ESPACE)
						.append(contact.getAdresse().getTypeVoie())
						.append(Constants.ESPACE)
						.append(contact.getAdresse().getLibelleVoie()).toString());
				address.setL5(contact.getAdresse().getMentionSpeciale());
				address.setL6(new StringBuilder().append(contact.getAdresse().getCodePostal())
						.append(Constants.ESPACE)
						.append(contact.getAdresse().getLibelleCommune()).toString());
				address.setL7(contact.getAdresse().getLibellePays());
				address.setBuilding(contact.getAdresse().getBatiment());
				address.setFloor(contact.getAdresse().getEtage());
				address.setDoor(contact.getAdresse().getPorte());
				address.setStaircase(contact.getAdresse().getEscalier());
				address.setElevator(contact.getAdresse().isAscenseur());
				address.setCityPriorityDistrict(contact.getAdresse().isQPV());
			}
		}
		return address;
	}

	private static InseeSampleIdentiersType getInseeSampleIdentiersFromSampleProcessing(
			IdentifiantsInsee identifier) {
		InseeSampleIdentiersType res = new InseeSampleIdentiersType();
		res.setAutre(identifier.getAutre());
		res.setBs(identifier.getBs().intValue());
		res.setEc(identifier.getEc());
		res.setLe(identifier.getLe().intValue());
		res.setNograp(identifier.getNograp());
		res.setNoi(identifier.getNoi().intValue());
		res.setNole(identifier.getNole().intValue());
		res.setNolog(identifier.getNolog().intValue());
		res.setNumfa(identifier.getNumfa().intValue());
		res.setRges(identifier.getRges().intValue());
		res.setSsech(identifier.getSsech().intValue());
		return res;
	}
}
