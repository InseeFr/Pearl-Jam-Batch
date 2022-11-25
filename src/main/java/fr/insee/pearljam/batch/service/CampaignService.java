package fr.insee.pearljam.batch.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.Campaign;
import fr.insee.pearljam.batch.campaign.CommentType;
import fr.insee.pearljam.batch.campaign.CommentsType;
import fr.insee.pearljam.batch.campaign.ContactAttemptType;
import fr.insee.pearljam.batch.campaign.ContactAttemptsType;
import fr.insee.pearljam.batch.campaign.OrganizationalUnitType;
import fr.insee.pearljam.batch.campaign.OrganizationalUnitsType;
import fr.insee.pearljam.batch.campaign.PersonType;
import fr.insee.pearljam.batch.campaign.PersonsType;
import fr.insee.pearljam.batch.campaign.PhoneNumberType;
import fr.insee.pearljam.batch.campaign.PhoneNumbersType;
import fr.insee.pearljam.batch.campaign.StateType;
import fr.insee.pearljam.batch.campaign.StatesType;
import fr.insee.pearljam.batch.campaign.SurveyUnitType;
import fr.insee.pearljam.batch.campaign.SurveyUnitsType;
import fr.insee.pearljam.batch.dao.AddressDao;
import fr.insee.pearljam.batch.dao.CampaignDao;
import fr.insee.pearljam.batch.dao.ClosingCauseDao;
import fr.insee.pearljam.batch.dao.CommentDao;
import fr.insee.pearljam.batch.dao.ContactAttemptDao;
import fr.insee.pearljam.batch.dao.ContactOutcomeDao;
import fr.insee.pearljam.batch.dao.IdentificationDao;
import fr.insee.pearljam.batch.dao.InterviewerTypeDao;
import fr.insee.pearljam.batch.dao.MessageDao;
import fr.insee.pearljam.batch.dao.OrganizationalUnitTypeDao;
import fr.insee.pearljam.batch.dao.PersonDao;
import fr.insee.pearljam.batch.dao.PhoneNumberDao;
import fr.insee.pearljam.batch.dao.PreferenceDao;
import fr.insee.pearljam.batch.dao.SampleIdentifierDao;
import fr.insee.pearljam.batch.dao.StateDao;
import fr.insee.pearljam.batch.dao.SurveyUnitDao;
import fr.insee.pearljam.batch.dao.UserTypeDao;
import fr.insee.pearljam.batch.dao.VisibilityDao;
import fr.insee.pearljam.batch.exception.BatchException;
import fr.insee.pearljam.batch.exception.DataBaseException;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.XmlUtils;

/**
 * CampaignService : Contains all functions needed to load or delete a Campaign
 * 
 * @author bclaudel
 *
 */
@Service
public class CampaignService {

	@Autowired
	AnnotationConfigApplicationContext context;
	@Autowired
	@Qualifier("pilotageConnection")
	Connection pilotageConnection;
	@Autowired
	PersonDao personDao;
	@Autowired
	ClosingCauseDao closingCauseDao;
	@Autowired
	CampaignDao campaignDao;
	@Autowired
	SurveyUnitDao surveyUnitDao;
	@Autowired
	StateDao stateDao;
	@Autowired
	AddressDao addressDao;
	@Autowired
	SampleIdentifierDao sampleIdentifierDao;
	@Autowired
	VisibilityDao visibilityDao;
	@Autowired
	PhoneNumberDao phoneNumberDao;
	@Autowired
	CommentDao commentDao;
	@Autowired
	ContactAttemptDao contactAttemptDao;
	@Autowired
	ContactOutcomeDao contactOutcomeDao;
	@Autowired
	OrganizationalUnitTypeDao organizationalUnitTypeDao;
	@Autowired
	PreferenceDao preferenceDao;
	@Autowired
	UserTypeDao userDao;
	@Autowired
	MessageDao messageDao;
	@Autowired
	InterviewerTypeDao interviewerTypeDao;
	@Autowired
	IdentificationDao identificationDao;

	boolean deleteAllSurveyUnits = false;

	private static final Logger logger = LogManager.getLogger(CampaignService.class);

	/**
	 * Initialization of all DAO needed
	 */
	private void initDaos() {
		campaignDao = context.getBean(CampaignDao.class);
		surveyUnitDao = context.getBean(SurveyUnitDao.class);
		stateDao = context.getBean(StateDao.class);
		addressDao = context.getBean(AddressDao.class);
		sampleIdentifierDao = context.getBean(SampleIdentifierDao.class);
		visibilityDao = context.getBean(VisibilityDao.class);
		phoneNumberDao = context.getBean(PhoneNumberDao.class);
		commentDao = context.getBean(CommentDao.class);
		contactAttemptDao = context.getBean(ContactAttemptDao.class);
		contactOutcomeDao = context.getBean(ContactOutcomeDao.class);
		organizationalUnitTypeDao = context.getBean(OrganizationalUnitTypeDao.class);
		preferenceDao = context.getBean(PreferenceDao.class);
		userDao = context.getBean(UserTypeDao.class);
		messageDao = context.getBean(MessageDao.class);
		interviewerTypeDao = context.getBean(InterviewerTypeDao.class);
	}

	/**
	 * Delete Campaign : delete and archive a campaign and all data associated
	 * 
	 * @param campaign
	 * @param in
	 * @param out
	 * @return BatchErrorCode
	 * @throws BatchException
	 * @throws SQLException
	 * @throws DataBaseException
	 */
	public BatchErrorCode deleteCampaign(Campaign campaign, String out)
			throws BatchException, SQLException, DataBaseException {
		initDaos();
		BatchErrorCode returnCode = BatchErrorCode.OK;
		// Archive datas in XML file
		returnCode = archiveCampaign(campaign, returnCode, true);
		XmlUtils.objectToXML(out + "/campaign." + getTimestampForPath() + ".delete.archive.xml", campaign);
		// Complete campaign deletion
		deleteCampaign(campaign, this.deleteAllSurveyUnits);
		return returnCode;
	}

	/**
	 * Archive Campaign and all data associated
	 * 
	 * @param campaign
	 * @param returnCode
	 * @param delete
	 * @return BatchErrorCode
	 * @throws DataBaseException
	 */
	private BatchErrorCode archiveCampaign(Campaign campaign, BatchErrorCode returnCode, boolean delete)
			throws DataBaseException {
		this.deleteAllSurveyUnits = false;
		OrganizationalUnitsType ou = new OrganizationalUnitsType();
		List<SurveyUnitType> listSurveyUnit = new ArrayList<>();
		List<OrganizationalUnitType> listOrganizationalUnit = visibilityDao
				.getAllVisibilitiesByCampaignId(campaign.getId());
		ou.getOrganizationalUnit().addAll(listOrganizationalUnit);
		Campaign dbCampaign = campaignDao.findById(campaign.getId());
		campaign.setContactAttemptsConfiguration(dbCampaign.getContactAttemptsConfiguration());
		campaign.setContactOutcomeConfiguration(dbCampaign.getContactOutcomeConfiguration());
		campaign.setIdentificationConfiguration(dbCampaign.getIdentificationConfiguration());
		campaign.setOrganizationalUnits(ou);
		if (campaign.getSurveyUnits() == null || campaign.getSurveyUnits().getSurveyUnit() == null
				|| campaign.getSurveyUnits().getSurveyUnit().isEmpty() || checkListAllSurveyUnit(campaign)) {
			this.deleteAllSurveyUnits = true;
			SurveyUnitsType su = new SurveyUnitsType();
			campaign.setSurveyUnits(new SurveyUnitsType());
			for (String surveyUnitTypeId : surveyUnitDao.getAllSurveyUnitByCampaignId(campaign.getId())) {
				listSurveyUnit.add(surveyUnitDao.getSurveyUnitById(surveyUnitTypeId));
			}
			su.getSurveyUnit().addAll(listSurveyUnit);
			campaign.setSurveyUnits(su);
		}
		for (SurveyUnitType surveyUnitType : campaign.getSurveyUnits().getSurveyUnit()) {
			if (surveyUnitDao.existSurveyUnit(surveyUnitType.getId())) {
				CommentsType com = new CommentsType();
				ContactAttemptsType contAtt = new ContactAttemptsType();
				StatesType state = new StatesType();
				PersonsType persons = new PersonsType();
				List<CommentType> listComment = new ArrayList<>();
				List<ContactAttemptType> listContactAttempt = new ArrayList<>();
				List<StateType> listState = new ArrayList<>();
				List<PersonType> listPersons = new ArrayList<>();
				try {
					if (!this.deleteAllSurveyUnits) {
						surveyUnitType = surveyUnitDao.getSurveyUnitById(surveyUnitType.getId());
					}

					// Persons
					List<Entry<Long, PersonType>> listPersonEntries = personDao
							.getPersonsBySurveyUnitId(surveyUnitType.getId());
					for (Entry<Long, PersonType> personEntry : listPersonEntries) {
						Long personId = personEntry.getKey();
						PersonType person = personEntry.getValue();
						PhoneNumbersType phoneNumbers = new PhoneNumbersType();
						phoneNumbers.getPhoneNumber().addAll(phoneNumberDao.getPhoneNumbersByPersonId(personId));
						person.setPhoneNumbers(phoneNumbers);
						listPersons.add(person);
					}

					// Comment
					listComment = commentDao.getCommentBySurveyUnitId(surveyUnitType.getId());

					// Contact Attempt
					listContactAttempt = contactAttemptDao.getContactAttemptTypeBySurveyUnitId(surveyUnitType.getId());

					// States
					listState = stateDao.getStateBySurveyUnitId(surveyUnitType.getId());

					// Address
					surveyUnitType.setInseeAddress(addressDao.getAddressBySurveyUnitId(surveyUnitType.getId()));

					// Contact Outcome
					surveyUnitType.setContactOutcome(
							contactOutcomeDao.getContactOutcomeTypeBySurveyUnitId(surveyUnitType.getId()));

					// Closing Cause
					surveyUnitType
							.setClosingCause(closingCauseDao.getClosingCauseTypeBySurveyUnitId(surveyUnitType.getId()));

					// InseeSampleIdentifiers
					surveyUnitType.setInseeSampleIdentiers(
							sampleIdentifierDao.getSampleIdentiersBySurveyUnitId(surveyUnitType.getId()));

					surveyUnitType.setIdentification(identificationDao.getIdentificationTypeBySurveyUnitId(surveyUnitType.getId()));

				} catch (Exception e) {
					throw new DataBaseException("Error during the archiving of the file campaign."
							+ getTimestampForPath() + ".delete.archive.xml : " + e.getMessage());
				}

				// Persons
				persons.getPerson().addAll(listPersons);
				surveyUnitType.setPersons(persons);

				// Comments
				com.getComment().addAll(listComment);
				surveyUnitType.setComments(com);

				// Contact Attempt
				contAtt.getContactAttempt().addAll(listContactAttempt);
				surveyUnitType.setContactAttempts(contAtt);

				// States
				state.getState().addAll(listState);
				surveyUnitType.setStates(state);

				// SU
				if (!this.deleteAllSurveyUnits || !delete) {
					listSurveyUnit.add(surveyUnitType);
				}
			} else {
				logger.log(Level.WARN, "The following survey unit : {}",
						surveyUnitType.getId() + " doesn't exist in database and has been remove");
				returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
			}
		}
		if (!this.deleteAllSurveyUnits && delete) {
			campaign.getSurveyUnits().getSurveyUnit().removeAll(campaign.getSurveyUnits().getSurveyUnit());
			campaign.getSurveyUnits().getSurveyUnit().addAll(listSurveyUnit);
		}
		return returnCode;
	}

	/**
	 * Delete Campaign and all data associated
	 * 
	 * @param campaign
	 * @param allSurveyUnitAndCampaign
	 * @throws SQLException
	 * @throws DataBaseException
	 */
	private void deleteCampaign(Campaign campaign, boolean allSurveyUnitAndCampaign)
			throws SQLException, DataBaseException {
		pilotageConnection.setAutoCommit(false);
		try {
			deleteSurveyUnit(campaign, allSurveyUnitAndCampaign);
			if (allSurveyUnitAndCampaign) {
				logger.log(Level.INFO, "All survey units of campaign {}", campaign.getId() + " have been deleted");
				preferenceDao.deletePreferenceByCampaignId(campaign.getId());
				visibilityDao.deleteVisibilityByCampaignId(campaign.getId());
				campaignDao.deleteCampaign(campaign);
				logger.log(Level.INFO, "Campaign {}", campaign.getId() + ", have been deleted");
			} else {
				String strList = String.join(",",
						campaign.getSurveyUnits().getSurveyUnit().stream().map(SurveyUnitType::getId)
								.collect(Collectors.toList()));
				logger.log(Level.INFO,
						"The following survey units of campaign {} : {}, have been deleted",
						campaign.getId(),
						strList);
			}
		} catch (Exception e) {
			pilotageConnection.rollback();
			pilotageConnection.setAutoCommit(true);
			throw new DataBaseException("Error during delete of datas in DB ... Rollback : " + e.getMessage());
		} finally {
			pilotageConnection.setAutoCommit(true);
		}
	}

	/**
	 * Delete Survey unit and all data associated
	 * 
	 * @param campaign
	 * @param allSurveyUnit
	 * @throws SQLException
	 */
	private void deleteSurveyUnit(Campaign campaign, boolean allSurveyUnit) throws SQLException {
		messageDao.deleteByCampaign(campaign.getId());
		for (SurveyUnitType surveyUnit : campaign.getSurveyUnits().getSurveyUnit()) {
			deleteSurveyUnit(surveyUnit, allSurveyUnit);
		}
		if (allSurveyUnit) {
			surveyUnitDao.deleteSurveyUnitByCampaignId(campaign.getId());
		}
	}

	private void deleteSurveyUnit(SurveyUnitType surveyUnit, boolean allSurveyUnit) throws SQLException {
		Long addressId = surveyUnitDao.getAddressIdBySurveyUnitId(surveyUnit.getId());
		Long sampleIdentifirId = surveyUnitDao.getSampleIdentifiersIdBySurveyUnitId(surveyUnit.getId());
		commentDao.deleteCommentBySurveyUnitId(surveyUnit.getId());
		contactAttemptDao.deleteContactAttemptBySurveyUnitId(surveyUnit.getId());
		contactOutcomeDao.deleteContactOutcomeBySurveyUnitId(surveyUnit.getId());
		phoneNumberDao.deletePhoneNumbersBySurveyUnitId(surveyUnit.getId());
		closingCauseDao.deleteAllClosingCausesOfSurveyUnit(surveyUnit.getId());
		personDao.deletePersonBySurveyUnitId(surveyUnit.getId());
		stateDao.deleteStateBySurveyUnitId(surveyUnit.getId());
		surveyUnitDao.deleteSurveyUnitById(surveyUnit.getId());
		addressDao.deleteAddressById(addressId);
		sampleIdentifierDao.deleteSampleIdentifiersById(sampleIdentifirId);
		if (!allSurveyUnit) {
			surveyUnitDao.deleteSurveyUnitById(surveyUnit.getId());
		}
	}

	/**
	 * Check if delete concern all survey unit or not
	 * 
	 * @param campaign
	 * @return true if all survey unit have to be deleted
	 */
	private boolean checkListAllSurveyUnit(Campaign campaign) {
		List<String> listSurveyUnitDb = surveyUnitDao.getAllSurveyUnitByCampaignId(campaign.getId());
		List<String> listSurveyUnitXml = campaign.getSurveyUnits().getSurveyUnit().stream().map(SurveyUnitType::getId)
				.collect(Collectors.toList());
		listSurveyUnitDb.removeAll(listSurveyUnitXml);
		if (listSurveyUnitDb.isEmpty()) {
			logger.log(Level.INFO, "Delete all survey units of campaign {}", campaign.getId());
			return true;
		} else {
			String strList = String.join(",", listSurveyUnitXml);
			logger.log(Level.INFO, "Delete only the following survey units of campaign {} : {}", campaign.getId(),
					strList);
			return false;
		}
	}

	SurveyUnitType createOrUpdateSurveyUnit(SurveyUnitType surveyUnitType, String campaignId)
			throws SynchronizationException {
		SurveyUnitType oldSu = null;
		if (!surveyUnitDao.existSurveyUnit(surveyUnitType.getId())) {
			createSurveyUnit(surveyUnitType, campaignId);
			logger.log(Level.INFO, "The Survey Unit {} has been created",
					surveyUnitType.getId());
		} else {
			oldSu = surveyUnitDao.getSurveyUnitById(surveyUnitType.getId());
			updateSurveyUnit(surveyUnitType, campaignId);
			logger.log(Level.INFO, "The Survey Unit {} has been updated",
					surveyUnitType.getId());
		}
		return oldSu;
	}

	private void createSurveyUnit(SurveyUnitType surveyUnitType, String campaignId) throws SynchronizationException {
		// Create address
		Long addressId = addressDao.createAddress(surveyUnitType.getInseeAddress());
		// Create sample identifier
		Long sampleIdentifierId = sampleIdentifierDao.createSampleIdentifier(surveyUnitType.getInseeSampleIdentiers());

		String interviewerAffectation = getInterviewerAffectation(surveyUnitType);
		String organizationUnitAffectation = getOrganizationUnitAffectation(surveyUnitType);

		// Create Survey Unit
		surveyUnitDao.createSurveyUnit(campaignId, surveyUnitType, addressId, sampleIdentifierId,
				interviewerAffectation, organizationUnitAffectation);

		// Create persons
		if (surveyUnitType.getPersons() != null) {
			for (PersonType person : surveyUnitType.getPersons().getPerson()) {
				Long personId = personDao.createPerson(person, surveyUnitType.getId());
				// Create phone numbers
				for (PhoneNumberType phoneNumber : person.getPhoneNumbers().getPhoneNumber()) {
					phoneNumberDao.createPhoneNumber(phoneNumber, personId);
				}
			}
		}

		// Create State for the Survey Unit
		stateDao.createState(System.currentTimeMillis(), "NVM", surveyUnitType.getId());

		// Create Comments
		if (surveyUnitType.getComments() != null) {
			for (CommentType comment : surveyUnitType.getComments().getComment()) {
				commentDao.createComment(comment, surveyUnitType.getId());
			}
		}
	}

	private void updateSurveyUnit(SurveyUnitType surveyUnitType, String campaignId) throws SynchronizationException {
		// Update address
		addressDao.updateAddress(surveyUnitType.getInseeAddress(), surveyUnitType.getId());
		// Update sample identifier
		sampleIdentifierDao.updateSampleIdentifier(surveyUnitType.getInseeSampleIdentiers(),
				surveyUnitType.getId());
		surveyUnitDao.updateSurveyUnitById(campaignId, surveyUnitType);

		// Replace persons
		phoneNumberDao.deletePhoneNumbersBySurveyUnitId(surveyUnitType.getId());
		personDao.deletePersonBySurveyUnitId(surveyUnitType.getId());
		for (PersonType person : surveyUnitType.getPersons().getPerson()) {
			Long personId = personDao.createPerson(person, surveyUnitType.getId());
			// Create phone numbers
			for (PhoneNumberType phoneNumber : person.getPhoneNumbers().getPhoneNumber()) {
				phoneNumberDao.createPhoneNumber(phoneNumber, personId);
			}
		}

	}

	private String getInterviewerAffectation(SurveyUnitType surveyUnitType) throws SynchronizationException {
		String affectation = null;

		if (surveyUnitType.getInterviewerId() != null && surveyUnitType.getInterviewerId().equalsIgnoreCase("none")) {
			return null;
		}
		if (surveyUnitType.getInterviewerId() != null && !surveyUnitType.getInterviewerId().isBlank()) {
			return surveyUnitType.getInterviewerId();
		}

		return affectation;
	}

	private String getOrganizationUnitAffectation(SurveyUnitType surveyUnitType) throws SynchronizationException {
		String affectation = null;

		if (surveyUnitType.getOrganizationalUnitId() != null
				&& surveyUnitType.getOrganizationalUnitId().equalsIgnoreCase("none")) {
			return null;
		}
		if (surveyUnitType.getOrganizationalUnitId() != null && !surveyUnitType.getOrganizationalUnitId().isBlank()) {
			return surveyUnitType.getOrganizationalUnitId();
		}

		return affectation;
	}

	/**
	 * Get the current date with a specific format for path
	 * 
	 * @return the current date
	 */
	public static String getTimestampForPath() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Date dateNow = new Date();
		return formatter.format(dateNow);
	}

	public void getOrganizationUnits(List<String> organizationUnits, String currentOu, boolean saveAllLevels) {
		List<String> lstOu = organizationalUnitTypeDao.findChildren(currentOu);
		if (lstOu.isEmpty()) {
			if (!organizationUnits.contains(currentOu)) {
				organizationUnits.add(currentOu);
			}
		} else {
			if (saveAllLevels && !organizationUnits.contains(currentOu)) {
				organizationUnits.add(currentOu);
			}
			for (String ou : lstOu) {
				getOrganizationUnits(organizationUnits, ou, saveAllLevels);
			}
		}
	}

	public BatchErrorCode extractCampaign(Campaign campaign, String out) throws DataBaseException, BatchException {
		initDaos();
		BatchErrorCode returnCode = BatchErrorCode.OK;
		// Archive datas in XML file
		returnCode = archiveCampaign(campaign, returnCode, false);
		XmlUtils.objectToXML(out + "/campaign." + getTimestampForPath() + ".extract.xml", campaign);
		return returnCode;
	}

	public boolean validateInput(SurveyUnitType surveyUnitType, String campaignId) {
		if (surveyUnitDao.existSurveyUnitForCampaign(surveyUnitType.getId(), campaignId)) {
			logger.log(Level.WARN, "The Survey Unit {} is already associated to an other campaign",
					surveyUnitType.getId());
			return false;
		}
		if (StringUtils.isNotBlank(surveyUnitType.getInterviewerId())
				&& !"none".equals(surveyUnitType.getInterviewerId())
				&& !interviewerTypeDao.existInterviewer(surveyUnitType.getInterviewerId())) {
			logger.log(Level.WARN, "The interviewer {} for the survey unit {}, not exist in database",
					surveyUnitType.getInterviewerId(), surveyUnitType.getId());
			return false;
		}
		// OU
		return true;
	}

	public void rollbackSurveyUnit(String surveyUnitId, SurveyUnitType oldSu, String campaignId)
			throws SynchronizationException, SQLException {
		if (oldSu == null) {
			deleteSurveyUnit(surveyUnitDao.getSurveyUnitById(surveyUnitId), false);
		} else {
			updateSurveyUnit(oldSu, campaignId);
		}
	}

}