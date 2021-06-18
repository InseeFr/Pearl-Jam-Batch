package fr.insee.pearljam.batch.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
import fr.insee.pearljam.batch.dao.GeographicalLocationDao;
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
import fr.insee.pearljam.batch.dto.InterviewerDto;
import fr.insee.pearljam.batch.dto.SimpleIdDto;
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
	Connection connection;
	
	@Autowired
	ContextReferentialService contextReferentialService;
	
	@Autowired
	PersonDao personDao;
	
	@Autowired
	ClosingCauseDao closingCauseDao;
	
	CampaignDao campaignDao;
	SurveyUnitDao surveyUnitDao;
	StateDao stateDao;
	AddressDao addressDao;
	SampleIdentifierDao sampleIdentifierDao;
	GeographicalLocationDao geographicalLocationDao;
	VisibilityDao visibilityDao;
	PhoneNumberDao phoneNumberDao;
	CommentDao commentDao;
	ContactAttemptDao contactAttemptDao;
	ContactOutcomeDao contactOutcomeDao;
	OrganizationalUnitTypeDao organizationalUnitTypeDao;
	PreferenceDao preferenceDao;
	UserTypeDao userDao;
	MessageDao messageDao;
	
	InterviewerTypeDao interviewerTypeDao;

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
		geographicalLocationDao = context.getBean(GeographicalLocationDao.class);
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
	public BatchErrorCode deleteCampaign(Campaign campaign, String out) throws BatchException, SQLException, DataBaseException {
		initDaos();
		BatchErrorCode returnCode = BatchErrorCode.OK;
		// Archive datas in XML file
		returnCode = archiveCampaign(campaign, returnCode);
		XmlUtils.objectToXML(out+"/campaign." + getTimestampForPath() + ".delete.archive.xml", campaign);
		// Complete campaign deletion
		deleteCampaign(campaign, this.deleteAllSurveyUnits);
		return returnCode;
	}

	/**
	 * Archive Campaign and all data associated
	 * 
	 * @param campaign
	 * @param returnCode
	 * @return BatchErrorCode
	 * @throws DataBaseException
	 */
	private BatchErrorCode archiveCampaign(Campaign campaign, BatchErrorCode returnCode) throws DataBaseException {
		this.deleteAllSurveyUnits = false;
		OrganizationalUnitsType ou = new OrganizationalUnitsType();
		List<SurveyUnitType> listSurveyUnit = new ArrayList<>();
		List<OrganizationalUnitType> listOrganizationalUnit = visibilityDao.getAllVisibilitiesByCampaignId(campaign.getId());
		ou.getOrganizationalUnit().addAll(listOrganizationalUnit);
		campaign.setOrganizationalUnits(ou);
		if (campaign.getSurveyUnits() == null || campaign.getSurveyUnits().getSurveyUnit() == null
				|| campaign.getSurveyUnits().getSurveyUnit().isEmpty() || checkListAllSurveyUnit(campaign)) {
			this.deleteAllSurveyUnits = true;
			SurveyUnitsType su = new SurveyUnitsType();
			campaign.setSurveyUnits(new SurveyUnitsType());
			for(String surveyUnitTypeId : surveyUnitDao.getAllSurveyUnitByCampaignId(campaign.getId())) {
				listSurveyUnit.add(surveyUnitDao.getSurveyUnitById(surveyUnitTypeId));
			}
			su.getSurveyUnit().addAll(listSurveyUnit);
			campaign.setSurveyUnits(su);
		}
		for(SurveyUnitType surveyUnitType : campaign.getSurveyUnits().getSurveyUnit()) {
			if(surveyUnitDao.existSurveyUnit(surveyUnitType.getId())) {
				CommentsType com = new CommentsType();
				ContactAttemptsType contAtt = new ContactAttemptsType();
				StatesType state = new StatesType();
				PersonsType persons = new PersonsType();
				List<CommentType> listComment = new ArrayList<>();
				List<ContactAttemptType> listContactAttempt = new ArrayList<>();
				List<StateType> listState = new ArrayList<>();
				List<PersonType> listPersons = new ArrayList<>();
				try {
					if(!this.deleteAllSurveyUnits) {
						surveyUnitType = surveyUnitDao.getSurveyUnitById(surveyUnitType.getId());
					}
					
					// Persons
					List<Entry<Long,PersonType>> listPersonEntries = personDao.getPersonsBySurveyUnitId(surveyUnitType.getId());
					for(Entry<Long,PersonType> personEntry : listPersonEntries) {
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
					surveyUnitType.setContactOutcome(contactOutcomeDao.getContactOutcomeTypeBySurveyUnitId(surveyUnitType.getId()));
					
					// InseeSampleIdentifiers
					surveyUnitType.setInseeSampleIdentiers(sampleIdentifierDao.getSampleIdentiersBySurveyUnitId(surveyUnitType.getId()));
				
				} catch (Exception e) {
					throw new DataBaseException("Error during the archiving of the file campaign."+ getTimestampForPath() +".delete.archive.xml : " + e.getMessage());
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
				if(!this.deleteAllSurveyUnits) {
					listSurveyUnit.add(surveyUnitType);
				}
			} else {
				logger.log(Level.WARN, "The following survey unit : {}", surveyUnitType.getId() + " doesn't exist in database and has been remove");
				returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
			}
		}
		if(!this.deleteAllSurveyUnits) {
			campaign.getSurveyUnits().getSurveyUnit().removeAll(campaign.getSurveyUnits().getSurveyUnit());
			campaign.getSurveyUnits().getSurveyUnit().addAll(listSurveyUnit);
		}
		return returnCode;
	}
	
	/**
	 * Delete Campaign and all data associated

	 * @param campaign
	 * @param allSurveyUnitAndCampaign
	 * @throws SQLException
	 * @throws DataBaseException
	 */
	private void deleteCampaign(Campaign campaign, boolean allSurveyUnitAndCampaign) throws SQLException, DataBaseException {
		connection.setAutoCommit(false);
		try {
			if(allSurveyUnitAndCampaign) {
				deleteSurveyUnit(campaign, allSurveyUnitAndCampaign);
				logger.log(Level.INFO, "All survey units of campaign {}", campaign.getId() + " have been deleted");
				preferenceDao.deletePreferenceByCampaignId(campaign.getId());
				visibilityDao.deleteVisibilityByCampaignId(campaign.getId());
				campaignDao.deleteCampaign(campaign);
				logger.log(Level.INFO, "Campaign {}", campaign.getId() + ", have been deleted");
			} else {
				deleteSurveyUnit(campaign, allSurveyUnitAndCampaign);
				String strList = String.join(",", campaign.getSurveyUnits().getSurveyUnit().stream().map(SurveyUnitType::getId)
						.collect(Collectors.toList()));
				logger.log(Level.INFO, 
						"The following survey units of campaign {} : {}, have been deleted", 
						campaign.getId(),
						strList);
			}
		} catch (Exception e) {
			connection.rollback();
			connection.setAutoCommit(true);
			throw new DataBaseException("Error during delete of datas in DB ... Rollback : " + e.getMessage());
		} finally {
			connection.setAutoCommit(true);
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
		for(SurveyUnitType surveyUnit : campaign.getSurveyUnits().getSurveyUnit()) {
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
			if(!allSurveyUnit) {
				surveyUnitDao.deleteSurveyUnitById(surveyUnit.getId());
			}
		}
		if(allSurveyUnit) {
			surveyUnitDao.deleteSurveyUnitByCampaignId(campaign.getId());
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
			logger.log(Level.INFO, "Delete only the following survey units of campaign {} : {}", campaign.getId(), strList);
			return false;
		}

	}

	/**
	 * Create or update a campaign and all data associated
	 * 
	 * @param campaign
	 * @param campaignExist
	 * @param in
	 * @param out
	 * @return BatchErrorCode
	 * @throws SQLException
	 * @throws DataBaseException
	 * @throws SynchronizationException 
	 */
	public BatchErrorCode createOrUpdateCampaign(Campaign campaign, boolean campaignExist, String in, String out)
			throws SQLException, DataBaseException, SynchronizationException {
		BatchErrorCode returnCode = BatchErrorCode.OK;
		initDaos();
		connection.setAutoCommit(false);
		try {
			String campaignId = campaign.getId().toUpperCase();
			if (campaignExist) {
				campaignDao.updateCampaignById(campaign);
				logger.log(Level.INFO, "The Campaign {} has been updated", campaignId);
				for (OrganizationalUnitType organizationalUnitType : campaign.getOrganizationalUnits()
						.getOrganizationalUnit()) {
					if (visibilityDao.existVisibility(campaignId, organizationalUnitType.getId())) {
						visibilityDao.updateDateVisibilityByCampaignIdAndOrganizationalUnitId(campaign,
								organizationalUnitType);
						logger.log(Level.INFO, "The Visibility for the Organizational Unit {} has been updated",
								organizationalUnitType.getId());
					} else {
						visibilityDao.createVisibility(campaign, organizationalUnitType);
						logger.log(Level.INFO, "The Visibility for the Organizational Unit {} has been created",
								organizationalUnitType.getId());
					}
				}
			} else {
				if(campaign.getOrganizationalUnits()!=null && campaign.getOrganizationalUnits().getOrganizationalUnit()!=null && 
						organizationalUnitTypeDao.existOrganizationUnitNational(campaign.getOrganizationalUnits().getOrganizationalUnit().stream().map(OrganizationalUnitType::getId).collect(Collectors.toList()))) {
					logger.log(Level.WARN, "campaign can not be associated to an organization unit with type NATIONAL");
					throw new BatchException("Campaign can not be associated to an organization unit with type NATIONAL");
				}
				List<String> lstUserId =new ArrayList<>(); 
				campaignDao.createCampaign(campaign);
				logger.log(Level.INFO, "The Campaign {} has been created", campaign.getId());
				if(campaign.getOrganizationalUnits() != null) {
					for(OrganizationalUnitType organizationalUnitType : campaign.getOrganizationalUnits()
							.getOrganizationalUnit()) {
						// Create Visibilities
						visibilityDao.createVisibility(campaign, organizationalUnitType);
						logger.log(Level.INFO, "The Visibility for the Organizational Unit {} has been created", organizationalUnitType.getId());
						for(String userId : userDao.findAllUsersByOrganizationUnit(organizationalUnitType.getId())) {
							if(!lstUserId.contains(userId)) {
								lstUserId.add(userId);
							}
						}
					}
				}
				for(String userId : lstUserId) {
					preferenceDao.createPreference(userId, campaignId);
				}
			}
			returnCode = createSurveyUnits(campaign, in, out, returnCode);
			

			connection.commit();
		} 
		catch (SynchronizationException e) {
			connection.rollback();
			connection.setAutoCommit(true);
			throw new SynchronizationException(e.getMessage());
		}
		catch (Exception e) {
			connection.rollback();
			connection.setAutoCommit(true);
			throw new DataBaseException("Error during insert datas in DB ... Rollback : " + e.getMessage());
		} finally {
			connection.setAutoCommit(true);
		}
		return returnCode;
	}


	/**
	 * Create a survey unit and all data associated
	 * @param campaign
	 * @param in
	 * @param out
	 * @param returnCode2 
	 * @return BatchErrorCode
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws SynchronizationException 
	 */
	private BatchErrorCode createSurveyUnits(Campaign campaign, String in, String out, BatchErrorCode returnCode)
			throws IOException, SAXException, ParserConfigurationException, XPathExpressionException,
			TransformerFactoryConfigurationError, TransformerException, SynchronizationException {
		String fileNameList = out + "/campaign." + getTimestampForPath() + ".error.list.xml";
		String campaignId = campaign.getId().toUpperCase();
		FileUtils.copyFile(new File(in), new File(fileNameList));
		File file = new File(fileNameList);
		InputSource is = new InputSource(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		StreamResult sr = new StreamResult(new StringWriter());
		List<String> lstSUError = new ArrayList<>();
		List<String> lstSUSuccess = new ArrayList<>();
		if (campaign.getSurveyUnits() != null) {
			if (!campaign.getSurveyUnits().getSurveyUnit().isEmpty()) {
				for (SurveyUnitType surveyUnitType : campaign.getSurveyUnits().getSurveyUnit()) {
					// check if survey unit exist and associated to an other campaign
					if (!surveyUnitDao.existSurveyUnitForCampaign(surveyUnitType.getId(), campaignId)) {
						if (StringUtils.isNotBlank(surveyUnitType.getInseeAddress().getGeographicalLocationId())) {
							if (geographicalLocationDao.existGeographicalLocation(
									String.valueOf(surveyUnitType.getInseeAddress().getGeographicalLocationId()))) {
								if (!surveyUnitDao.existSurveyUnit(surveyUnitType.getId())) {
									// Create address
									Long addressId = addressDao.createAddress(surveyUnitType.getInseeAddress());
									// Create sample identifier
									Long sampleIdentifierId = sampleIdentifierDao
											.createSampleIdentifier(surveyUnitType.getInseeSampleIdentiers());
									
									String interviewerAffectation = getInterviewerAffectation(surveyUnitType);
									String organizationUnitAffectation = getOrganizationUnitAffectation(surveyUnitType);
									
									// Create Survey Unit
									surveyUnitDao.createSurveyUnit(campaignId, surveyUnitType, addressId,
											sampleIdentifierId, interviewerAffectation, organizationUnitAffectation);
									
									// Create persons
									if(surveyUnitType.getPersons() != null) {
										for(PersonType person : surveyUnitType.getPersons().getPerson()) {
											Long personId = personDao.createPerson(person, surveyUnitType.getId());
											// Create phone numbers
											for (PhoneNumberType phoneNumber : person.getPhoneNumbers().getPhoneNumber()) {
												phoneNumberDao.createPhoneNumber(phoneNumber, personId);
											}
										}
									}
									
									
									// Create State for the Survey Unit
									if(interviewerAffectation != null) {
										stateDao.createState(System.currentTimeMillis(), "NVM", surveyUnitType.getId());
									}
									
									logger.log(Level.INFO, "The Survey Unit {} has been created",
											surveyUnitType.getId());
									logger.log(Level.INFO, "The State for the Survey Unit {} has been created",
											surveyUnitType.getId());
								} else {
									// Update address
									addressDao.updateAddress(surveyUnitType.getInseeAddress(), surveyUnitType.getId());
									// Update sample identifier
									sampleIdentifierDao.updateSampleIdentifier(surveyUnitType.getInseeSampleIdentiers(),
											surveyUnitType.getId());
									// Update Survey Unit
									String interviewerAffectation = getInterviewerAffectation(surveyUnitType);
									String organizationUnitAffectation = getOrganizationUnitAffectation(surveyUnitType);
									surveyUnitDao.updateSurveyUnitById(campaignId, surveyUnitType, interviewerAffectation, organizationUnitAffectation);
									
									// Replace persons
									phoneNumberDao.deletePhoneNumbersBySurveyUnitId(surveyUnitType.getId());
									personDao.deletePersonBySurveyUnitId(surveyUnitType.getId());
									for(PersonType person : surveyUnitType.getPersons().getPerson()) {
										Long personId = personDao.createPerson(person, surveyUnitType.getId());
										// Create phone numbers
										for (PhoneNumberType phoneNumber : person.getPhoneNumbers().getPhoneNumber()) {
											phoneNumberDao.createPhoneNumber(phoneNumber, personId);
										}
									}
							
									logger.log(Level.INFO, "The Survey Unit {} has been updated",
											surveyUnitType.getId());
								}
								// Remove SU from file error.list
								removeSurveyUnitNode(doc, surveyUnitType.getId());
								lstSUSuccess.add(surveyUnitType.getId());
							} else {
								logger.log(Level.WARN, "The Geographical Location {} for the survey unit {}, is not in database",
										surveyUnitType.getInseeAddress().getGeographicalLocationId(), surveyUnitType.getId());
								lstSUError.add(surveyUnitType.getId());
							}
						} else {
							logger.log(Level.WARN,
									"The GeographicalLocationId is null or equals to 0");
							lstSUError.add(surveyUnitType.getId());
						}
					} else {
						logger.log(Level.WARN, "The Survey Unit {} is already associated to an other campaign",
								surveyUnitType.getId());
						lstSUError.add(surveyUnitType.getId());
					}
				}
			} else {
				logger.log(Level.INFO, " No survey unit found in the file campaign.xml");
			}
		} else {
			logger.log(Level.INFO, " No survey unit found in the file campaign.xml");
		}
		if (lstSUError.isEmpty()) {
			// No error in reporting units : error file do not have to be created
			FileUtils.forceDelete(new File(fileNameList));
		} else {
			// At least 1 error on reporting units : file must be created
			updateCampaignFileErrorList(doc, sr, fileNameList);
			returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
		}
		if (campaign.getSurveyUnits() == null) {
			logger.log(Level.INFO, " Total survey unit treated : 0");
		} else {
			logger.log(Level.INFO, " Total survey unit treated : {}", campaign.getSurveyUnits().getSurveyUnit().size());
		}
		logger.log(Level.INFO, " list of survey unit with success : [{}]", String.join(",", lstSUSuccess));
		logger.log(Level.INFO, " list of survey unit with error : [{}]", String.join(",", lstSUError));
		return returnCode;
	}
	
	private String getInterviewerAffectation(SurveyUnitType surveyUnitType) throws SynchronizationException {
		String affectation = null;
		
		if (surveyUnitType.getInterwieverId() != null && surveyUnitType.getInterwieverId().equalsIgnoreCase("none")) {
			return null;
		}
		if(surveyUnitType.getInterwieverId() != null && !surveyUnitType.getInterwieverId().isBlank()) {
			return surveyUnitType.getInterwieverId();
		}
		
		InterviewerDto intwDto = contextReferentialService.getSurveyUnitInterviewerAffectation(surveyUnitType.getId());
		if(intwDto != null && intwDto.getIdep() != null) {
			if(interviewerTypeDao.existInterviewer(intwDto.getIdep())) {
				affectation = intwDto.getIdep();
			}
			else {
				logger.error("Survey unit {} is affected to interviewer {} that does not exist in Sabianne", surveyUnitType.getId(), intwDto.getIdep());
			}
		}
		
		
		return affectation;
	}
	
	private String getOrganizationUnitAffectation(SurveyUnitType surveyUnitType) throws SynchronizationException {
		String affectation = null;
		
		if (surveyUnitType.getOrganizationalUnitId() != null && surveyUnitType.getOrganizationalUnitId().equalsIgnoreCase("none")) {
			return null;
		}
		if(surveyUnitType.getOrganizationalUnitId() != null && !surveyUnitType.getOrganizationalUnitId().isBlank()) {
			return surveyUnitType.getOrganizationalUnitId();
		}
		SimpleIdDto idDto = contextReferentialService.getSurveyUnitOUAffectation(surveyUnitType.getId());
		if(idDto != null && idDto.getId() != null) {
			if(organizationalUnitTypeDao.existOrganizationalUnit(idDto.getId())) {
				affectation = idDto.getId();
			}
			else {
				logger.error("Survey unit {} is affected to organizational unit {} that does not exist in Sabianne", surveyUnitType.getId(), idDto.getId());
			}
		}

		return affectation;
	}

	/**
	 * Get the current date with a specific format for path
	 * @return the current date
	 */
	public static String getTimestampForPath() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Date dateNow = new Date();
		return formatter.format(dateNow);
	}

	/**
	 * update the campaign file error list for delete all survey unit without error
	 * 
	 * @param doc
	 * @param sr
	 * @param fileName
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	private static void updateCampaignFileErrorList(Document doc, StreamResult sr, String fileName)
			throws TransformerFactoryConfigurationError, TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(new DOMSource(doc), sr);
		// writing to file
		File fileNew = new File(fileName);
		try (FileOutputStream fop = new FileOutputStream(fileNew)) {
			// if file doesnt exists, then create it
			if (!fileNew.exists() && !fileNew.createNewFile()) {
				logger.log(Level.ERROR, "Failed to create file %s", fileName);
			}
			// get the content in bytes
			String xmlString = sr.getWriter().toString();
			byte[] contentInBytes = xmlString.getBytes();
			fop.write(contentInBytes);
			fop.flush();
		} catch (IOException e) {
			logger.log(Level.ERROR, e.getMessage());
		}
	}

	/**
	 * Remove a specific Survey unit node from xml file
	 * 
	 * @param doc
	 * @param id
	 * @param sr
	 * @throws XPathExpressionException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	private static void removeSurveyUnitNode(Document doc, String id)
			throws XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile("//SurveyUnit[Id = \"" + id + "\"]");
		Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
		Node prevElement = node.getPreviousSibling();
		if(prevElement != null && prevElement.getNodeType() == Node.TEXT_NODE && prevElement.getNodeValue().trim().length() == 0 ) {
			node.getParentNode().removeChild(prevElement);
		}
		Node parent = node.getParentNode();
		parent.removeChild(node);
		DOMSource domSource = new DOMSource(doc);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult(sw);
		transformer.transform(domSource, sr);
	}
	
	
	public void getOrganizationUnits(List<String> organizationUnits, String currentOu, boolean saveAllLevels) {
		List<String> lstOu = organizationalUnitTypeDao.findChildren(currentOu);
		if(lstOu.isEmpty()) {
			if(!organizationUnits.contains(currentOu)){
				organizationUnits.add(currentOu);
			}
		}else {
			if(saveAllLevels && !organizationUnits.contains(currentOu)) {
				organizationUnits.add(currentOu);
			}
			for (String ou : lstOu) {
				getOrganizationUnits(organizationUnits, ou, saveAllLevels);
			}
		}
	}

}