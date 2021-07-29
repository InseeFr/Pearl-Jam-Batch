package fr.insee.pearljam.batch.service;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import fr.insee.pearljam.batch.Constants;
import fr.insee.pearljam.batch.campaign.Campaign;
import fr.insee.pearljam.batch.campaign.OrganizationalUnitType;
import fr.insee.pearljam.batch.campaign.SurveyUnitType;
import fr.insee.pearljam.batch.config.ApplicationConfig;
import fr.insee.pearljam.batch.context.Context;
import fr.insee.pearljam.batch.dao.CampaignDao;
import fr.insee.pearljam.batch.dao.OrganizationalUnitTypeDao;
import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.exception.BatchException;
import fr.insee.pearljam.batch.exception.DataBaseException;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.sampleprocessing.Campagne;
import fr.insee.pearljam.batch.sampleprocessing.Campagne.Steps.Step;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.DataCollectionMapper;
import fr.insee.pearljam.batch.utils.PathUtils;
import fr.insee.pearljam.batch.utils.PilotageMapper;
import fr.insee.pearljam.batch.utils.XmlUtils;
import fr.insee.queen.batch.object.Sample;
import fr.insee.queen.batch.object.SurveyUnit;
import fr.insee.queen.batch.service.LoadService;

/**
 * Launcher Service : this service contains all steps of Batch : 
 * - Validate 
 * - Load
 * - Clean & reset contents
 * 
 * @author bclaudel
 * 
 */
@Service
public class PilotageLauncherService {

	@Autowired
	AnnotationConfigApplicationContext context;
	
	@Autowired
	PilotageFolderService pilotageFolderService;
	
	private static final Logger logger = LogManager.getLogger(PilotageLauncherService.class);
	private static final String CAMPAIGN_PATH_IN = "/campaign/campaign.xml";
	private static final String SAMPLE_PATH_IN = "/sample/sample.xml";

	/**
	 * Global function that structure the batch execution depends on batchOption
	 * 
	 * @param batchOption
	 * @param folderIn
	 * @param folderOut
	 * @return BatchErrorCode
	 * @throws BatchException
	 * @throws IOException
	 * @throws ValidateException
	 * @throws XMLStreamException 
	 */
	public BatchErrorCode validateLoadClean(BatchOption batchOption, String folderIn, String folderOut) throws IOException, ValidateException, XMLStreamException {
		BatchErrorCode returnCode = BatchErrorCode.OK;
		ValidateException ve = null;
		String name = getName(batchOption);
		String processingFolder = folderIn + "/processing";
		if (PathUtils.isDirContainsFileExtension(Path.of(folderIn), name+".xml")) {
			try {
				switch (batchOption) {
					case LOADCONTEXT:
						XmlUtils.validateXMLSchema(Constants.MODEL_CONTEXT, folderIn + "/" + name +".xml");
						break;
					case LOADCAMPAIGN:
					case DELETECAMPAIGN:
					case EXTRACT:
						XmlUtils.validateXMLSchema(Constants.MODEL_CAMPAIGN, folderIn + "/" + name +".xml");
						break;
					case SAMPLEPROCESSING:
						XmlUtils.validateXMLSchema(Constants.MODEL_SAMPLEPROCESSING, folderIn + "/" + name +".xml");
						break;
					default:
						throw new ValidateException("Error validating "+name+".xml : unknown model");
				}
			} catch (ValidateException e) {
				cleanAndReset(name, folderIn +"/"+ name +".xml", folderOut, processingFolder, BatchErrorCode.KO_FONCTIONAL_ERROR, batchOption);
				throw new ValidateException("Error validating "+name+".xml : "+e.getMessage());
			}
			try {
				logger.log(Level.INFO, "Start {}", batchOption.getLabel());
				returnCode = load(batchOption, folderIn +"/"+ name +".xml", folderOut, processingFolder);
				logger.log(Level.INFO, "Finish {}", batchOption.getLabel());
			} catch (SynchronizationException e) {
				ve = new ValidateException("Error during process, error loading "+name+" : "+e.getMessage());
				returnCode = BatchErrorCode.KO_TECHNICAL_ERROR;
			} catch (Exception e) {
				ve = new ValidateException("Error during process, error loading "+name+" : "+e.getMessage());
				returnCode = BatchErrorCode.KO_FONCTIONAL_ERROR;
			} finally {
				try {
					returnCode = cleanAndReset(name, folderIn +"/"+ name +".xml", folderOut, processingFolder, returnCode, batchOption);
				} catch (IOException e) {
					logger.log(Level.ERROR, "Error during process, error files have been created : {}", e.getMessage());
					returnCode=BatchErrorCode.OK_TECHNICAL_WARNING;
				}
			}
			if(ve != null) throw ve;
		}else {
			logger.log(Level.WARN, "No {} file to treat in '{}'", name, folderIn);
			returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
		}
		return returnCode;
		
	}

	/**
	 * Get the reference name of batch execution depends on batchOption
	 * @param batchOption
	 * @return the reference name of batch execution
	 */
	private String getName(BatchOption batchOption) {
		switch(batchOption) {
			case LOADCAMPAIGN:
				return Constants.CAMPAIGN;
			case DELETECAMPAIGN:
				return Constants.CAMPAIGN_TO_DELETE;
			case EXTRACT:
				return Constants.CAMPAIGN_TO_EXTRACT;
			case LOADCONTEXT:
				return Constants.CONTEXT;
			case SAMPLEPROCESSING:
				return Constants.SAMPLEPROCESSING;
			default:
				return null;
		}
	}

	
	/**
	 * Call load function depends on batchOption
	 * 
	 * @param batchOption
	 * @param in
	 * @param out
	 * @return BatchErrorCode
	 * @throws IOException 
	 * @throws SynchronizationException 
	 * @throws ValidateException 
	 * @throws DataBaseException 
	 * @throws SQLException 
	 * @throws BatchException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws Exception 
	 * @throws ParseException 
	 */
	public BatchErrorCode load(BatchOption batchOption, String in, String out, String processing) throws SQLException, DataBaseException, ValidateException, SynchronizationException, IOException, BatchException, ParserConfigurationException, SAXException{
		switch(batchOption) {
			case LOADCAMPAIGN:
				return loadCampaign(in, out, processing);
			case DELETECAMPAIGN:
				return deleteCampaign(in, out);
			case EXTRACT:
				return extractCampaign(in, out);
			case LOADCONTEXT:
				return loadContext(in);
			case SAMPLEPROCESSING:
				return loadSampleProcessing(in, processing);
			default:
				return null;
		}
	}
	
	/**
	 * Global function of clean and reset.
	 * Filenames depends on return code and batchOption
	 * @param name
	 * @param in
	 * @param out
	 * @param returnCode
	 * @param batchOption
	 * @return BatchErrorCode
	 * @throws IOException
	 * @throws ValidateException
	 */
	public BatchErrorCode cleanAndReset(String name, String in, String out, String processing, BatchErrorCode returnCode, BatchOption batchOption) throws IOException, ValidateException {
		String fileName = getFileName(name, batchOption, returnCode);
		
		String location;
		String processedFilename = pilotageFolderService.getFilename();
		if((batchOption==BatchOption.LOADCAMPAIGN || batchOption==BatchOption.SAMPLEPROCESSING) && !processedFilename.isBlank()) {
			location = processing + "/" + processedFilename;
		}
		else {
			location = in;
		}
		
		File file = new File(location);
		
		if(file.exists()) {
			Path temp = Files.move(Paths.get(location),
					Paths.get(out + "/" + fileName));
			if (temp != null) {
				logger.log(Level.INFO, Constants.MSG_FILE_MOVE_SUCCESS, fileName);
			} else {
				logger.log(Level.WARN, Constants.MSG_FAILED_MOVE_FILE);
				if(returnCode != BatchErrorCode.KO_FONCTIONAL_ERROR) {
					returnCode = BatchErrorCode.OK_TECHNICAL_WARNING;
				}
			}
		} else {
			logger.log(Level.ERROR, Constants.MSG_FAILED_MOVE_FILE + " does not exists", fileName);
			if(returnCode != BatchErrorCode.KO_FONCTIONAL_ERROR) {
				returnCode = BatchErrorCode.OK_TECHNICAL_WARNING;
			}
		}
		return returnCode;
	}
  
	private String getFileName(String name, BatchOption batchOption, BatchErrorCode returnCode) throws ValidateException {
		String ending = getEnding(returnCode) ;
		  
		String designation = null;
		String finalName = name;
		switch(batchOption) {
		case DELETECAMPAIGN:
			designation = "delete";
			finalName = Constants.CAMPAIGN;
			break;
		case EXTRACT:
			designation = "extract";
			finalName = Constants.CAMPAIGN;
			break;
		default:
			designation = "";
			break;
		}
		return new StringBuilder(finalName)
				.append(".")
				.append(PathUtils.getTimestampForPath())
				.append(".")
				.append(designation)
				.append(".")
				.append(ending).toString();
	}

	private String getEnding(BatchErrorCode returnCode) throws ValidateException {
		switch(returnCode) {
		case KO_TECHNICAL_ERROR: 
		case KO_FONCTIONAL_ERROR:
			return "error.xml";
		case OK_TECHNICAL_WARNING:
		case OK_FONCTIONAL_WARNING:
			return "warning.xml";
		case OK:
			return "done.xml";
		default:
			throw new ValidateException("Unknown return code");
		}
	}

	public void moveFileToProcessing(String name, String in, 
		  String processing, String campaignId) throws IOException {
	  	String fileName = "";
	  	fileName = new StringBuilder(name)
                .append(".")
                .append(campaignId)
                .append(".")
                .append(PathUtils.getTimestampForPath())
                .append(".xml")
                .toString();
    	pilotageFolderService.setFilename(fileName);
		File file = new File(in);
		if(file.exists()) {
			Path temp = Files.move(Paths.get(in),
			Paths.get(processing + "/" + fileName));
			if (temp != null) {
				logger.log(Level.INFO, Constants.MSG_FILE_MOVE_SUCCESS, fileName);
				
			} else {
				logger.log(Level.ERROR, Constants.MSG_FAILED_MOVE_FILE);
			}
		} else {
			logger.log(Level.ERROR, Constants.MSG_FAILED_MOVE_FILE + " does not exists", fileName);
		}
	}

	/**
	 * Specific function for load Campaign (Create or update)
	 * @param in
	 * @param out
	 * @return BatchErrorCode
	 * @throws BatchException
	 * @throws SQLException
	 * @throws DataBaseException
	 * @throws ValidateException
	 * @throws SynchronizationException 
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public BatchErrorCode loadCampaign(String in, String out, String processing) throws SQLException, DataBaseException, ValidateException, SynchronizationException, IOException {
		CampaignDao campaignDao = context.getBean(CampaignDao.class);
		Campaign campaign = XmlUtils.xmlToObject(in, Campaign.class);
		CampaignService campaignService = context.getBean(CampaignService.class);
		if(campaign!=null) {
			String campaignId = campaign.getId();
		    moveFileToProcessing(Constants.CAMPAIGN, in, processing, campaignId);
			if(checkOrganizationUnits(campaign) && checkDateConsistency(campaign)) {
				return campaignService.createOrUpdateCampaign(
						campaign, 
						campaignDao.existCampaign(campaign.getId()), 
						processing + "/" + pilotageFolderService.getFilename(), 
						out);
			}else{
				throw new ValidateException("Error during load campaign " + campaign.getId());
			}
		}else {
			throw new ValidateException(Constants.ERROR_CAMPAIGN_NULL);
		}
	}
	
	/**
	 * Specific function for delete Campaign
	 * 
	 * @param in
	 * @param out
	 * @return BatchErrorCode
	 * @throws BatchException
	 * @throws ValidateException
	 * @throws SQLException
	 * @throws DataBaseException
	 */
	public BatchErrorCode deleteCampaign(String in, String out) throws BatchException, ValidateException, SQLException, DataBaseException {
		Campaign campaign = XmlUtils.xmlToObject(in, Campaign.class);
		CampaignDao campaignDao = context.getBean(CampaignDao.class);
		CampaignService campaignService = context.getBean(CampaignService.class);
		if(campaign!=null) {
			if(campaignDao.existCampaign(campaign.getId())) {
				return campaignService.deleteCampaign(campaign, out);
			}else{
				logger.log(Level.ERROR, "The campaign {} does not exist", campaign.getId());
				return BatchErrorCode.OK_FONCTIONAL_WARNING;
			}
		}else {
			throw new ValidateException(Constants.ERROR_CAMPAIGN_NULL);
		}
	}
	
	/**
	 * Specific function for extract Campaign
	 * 
	 * @param in
	 * @param out
	 * @return BatchErrorCode
	 * @throws BatchException
	 * @throws ValidateException
	 * @throws SQLException
	 * @throws DataBaseException
	 */
	public BatchErrorCode extractCampaign(String in, String out) throws ValidateException, DataBaseException, BatchException {
		Campaign campaign = XmlUtils.xmlToObject(in, Campaign.class);
		CampaignDao campaignDao = context.getBean(CampaignDao.class);
		CampaignService campaignService = context.getBean(CampaignService.class);
		if(campaign!=null) {
			if(campaignDao.existCampaign(campaign.getId())) {
				return campaignService.extractCampaign(campaign, out);
			}else{
				logger.log(Level.ERROR, "The campaign {} does not exist", campaign.getId());
				return BatchErrorCode.OK_FONCTIONAL_WARNING;
			}
		}else {
			throw new ValidateException(Constants.ERROR_CAMPAIGN_NULL);
		}
	}
	
	/**
	 * Specific function for load Context
	 * 
	 * @param in
	 * @return BatchErrorCode
	 * @throws SQLException
	 * @throws DataBaseException
	 * @throws BatchException
	 * @throws ValidateException
	 */
	public BatchErrorCode loadContext(String in) throws SQLException, DataBaseException, ValidateException {
		Context contextXml = XmlUtils.xmlToObject(in, Context.class);
		ContextService contextService = context.getBean(ContextService.class);
		if(contextXml!=null) {
			return contextService.createContext(contextXml);
		}else {
			throw new ValidateException("Error : context is null");
		}
	}
	
	public BatchErrorCode loadSampleProcessing(String in, String processing) throws ParserConfigurationException, SAXException, IOException, ValidateException, BatchException, SynchronizationException, SQLException {
		BatchErrorCode returnCode = BatchErrorCode.OK;
		CampaignService pilotageCampaignService = context.getBean(CampaignService.class);
		LoadService dataCollectionloadService = context.getBean(LoadService.class);
				
		// Move SampleProcessing File to processing folder and unmarshall
		pilotageFolderService.setCampaignName(in);
		moveFileToProcessing("sampleprocessing", in, processing, pilotageFolderService.getCampaignName());
		Campagne sampleProcessing = XmlUtils.xmlToObject(processing + "/" + pilotageFolderService.getFilename(), Campagne.class);
		
		// Extract campaignId, list of steps and list of survey-unit id from sampleprocessing
		String campaignId = sampleProcessing.getIdSource() + sampleProcessing.getMillesime() + sampleProcessing.getIdPeriode();
		List<String> steps = sampleProcessing.getSteps().getStep().stream().map(Step::getName).collect(Collectors.toList());
		List<String> surveyUnits = sampleProcessing.getQuestionnaires().getQuestionnaire().stream().map(su -> su.getInformationsGenerales().getUniteEnquetee().getIdentifiant()).collect(Collectors.toList());
		
		logger.log(Level.INFO, "Start split sample processing content");
		Map<String, SurveyUnit> mapDataCollectionSu = extractAndValidateDatacollectionFromSamplProcessing(steps, campaignId, sampleProcessing);
		Map<String, SurveyUnitType> mapPilotageSu = extractAndValidatePilotageFromSamplProcessing(steps, campaignId, sampleProcessing);
		logger.log(Level.INFO, "End split sample processing content");
		
		// Create or update survey-units on pilotage and/or data-collection
		boolean pilotageValidate;
		SurveyUnitType oldSu;
		for(String su : surveyUnits) {
			pilotageValidate = true;
			oldSu = null;
			if(steps.contains(Constants.PILOTAGE)){
				pilotageValidate = pilotageCampaignService.validateInput(mapPilotageSu.get(su), campaignId);
				if(pilotageValidate) {
					oldSu = pilotageCampaignService.createOrUpdateSurveyUnit(mapPilotageSu.get(su), campaignId);
					logger.log(Level.INFO, "Survey-unit {} created in pilotage", su);
				} else {
					returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
				}
			}
			try{
				if(pilotageValidate && steps.contains(Constants.DATACOLLECTION)){
					dataCollectionloadService.createOrUpdateSurveyUnit(mapDataCollectionSu.get(su));
					logger.log(Level.INFO, "Survey-unit {} created in data-collection", su);
				}
			}catch(SQLException e){
				logger.log(Level.ERROR, "Error during create Survey-unit {} in data collection", su);
				if(steps.contains(Constants.PILOTAGE)){
					//Rollback Survey unit creation/update on pearl BB
					logger.log(Level.WARN, "Roll back for Survey-unit {} created in pilotage ...", su);
					pilotageCampaignService.rollbackSurveyUnit(su, oldSu,  campaignId);
					logger.log(Level.WARN, "Roll back ok");
				}
				returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
			}
		}
		
		// Move files in out folder
		moveFilesInOutFolders(returnCode);
		return returnCode;
	}

	private void moveFilesInOutFolders(BatchErrorCode returnCode) throws IOException, ValidateException {
		if(new File(ApplicationConfig.FOLDER_IN_QUEEN + SAMPLE_PATH_IN).exists()) {
			Files.move(Paths.get(ApplicationConfig.FOLDER_IN_QUEEN + SAMPLE_PATH_IN), 
					Paths.get(new StringBuilder(ApplicationConfig.FOLDER_OUT_QUEEN)
					.append("/sample/")
					.append("sample")
					.append(".")
					.append(PathUtils.getTimestampForPath())
					.append(".")
					.append(getEnding(returnCode)).toString()));
			
		}
		if(new File(ApplicationConfig.FOLDER_IN + CAMPAIGN_PATH_IN).exists()) {
			Files.move(Paths.get(ApplicationConfig.FOLDER_IN + CAMPAIGN_PATH_IN), 
					Paths.get(new StringBuilder(ApplicationConfig.FOLDER_OUT)
							.append("/campaign/")
							.append("campaign")
							.append(".")
							.append(PathUtils.getTimestampForPath())
							.append(".")
							.append(getEnding(returnCode)).toString()));
		}
	}

	private Map<String, SurveyUnitType> extractAndValidatePilotageFromSamplProcessing(List<String> steps,
			String campaignId, Campagne sampleProcessing) throws BatchException, ValidateException {
		if(!steps.contains(Constants.PILOTAGE)) {
			return new HashMap<>();
		}
		CampaignDao pilotageCampaignDao = context.getBean(CampaignDao.class);
		if(!pilotageCampaignDao.existCampaign(campaignId)){
			logger.log(Level.INFO, "Campaign {} does not exist in Pilotage", campaignId);
			throw new ValidateException("Campaign does not exist in Pilotage DB");
		}
		logger.log(Level.INFO, "Extract Pilotage content");
		Campaign pilotageCampaign = PilotageMapper.mapSampleProcessingToPilotageCampaign(sampleProcessing);
		XmlUtils.objectToXML(ApplicationConfig.FOLDER_IN + CAMPAIGN_PATH_IN, pilotageCampaign);
		logger.log(Level.INFO, "Validate Pilotage input");
		return pilotageCampaign.getSurveyUnits().getSurveyUnit()
				.stream()
				.collect(Collectors.toMap(SurveyUnitType::getId, su-> su));
	}

	private Map<String, SurveyUnit> extractAndValidateDatacollectionFromSamplProcessing(List<String> steps, String campaignId, Campagne sampleProcessing) throws BatchException, ValidateException {
		fr.insee.queen.batch.utils.XmlUtils dataCollectionXmlUtils = context.getBean(fr.insee.queen.batch.utils.XmlUtils.class);
		fr.insee.queen.batch.dao.CampaignDao dataCollectionCampaignDao = context.getBean(fr.insee.queen.batch.dao.CampaignDao.class);
		fr.insee.queen.batch.sample.Campaign dataCollectionCampaign = null;
		if(!steps.contains(Constants.DATACOLLECTION)) {
			return new HashMap<>();
		}
		if(!dataCollectionCampaignDao.exist(campaignId)){
			logger.log(Level.INFO, "Campaign {} does not exist in Data-collection", campaignId);
			throw new ValidateException("Campaign does not exist in Data-collection DB");
		}
		logger.log(Level.INFO, "Get Data-collection content");
		dataCollectionCampaign = DataCollectionMapper.mapSampleProcessingToDataCollectionCampaign(sampleProcessing);
		XmlUtils.objectToXML(ApplicationConfig.FOLDER_IN_QUEEN + SAMPLE_PATH_IN, dataCollectionCampaign);
		Sample dataCollectionSample = null;
		try {
			dataCollectionSample = dataCollectionXmlUtils.createSample(XmlUtils.objectToXML(ApplicationConfig.FOLDER_IN_QUEEN + "/sample.xml", dataCollectionCampaign).getPath());
		} catch (Exception e) {
			throw new ValidateException("Error on getting sample entity : " + e.getMessage());
		}
		return dataCollectionSample.getSurveyUnits()
				.stream()
				.collect(Collectors.toMap(SurveyUnit::getId, su-> su));
	}

	/**
	 * check existence in delimited organiaztion units
	 * @param campaign
	 * @return false if at least one Organizational Unit does not exist 
	 */
	private boolean checkOrganizationUnits(Campaign campaign){
		List<String> lstOrganizationalUnitMissing = new ArrayList<>();
		if(campaign.getOrganizationalUnits()!=null) {
			OrganizationalUnitTypeDao organizationalUnitTypeDao = context.getBean(OrganizationalUnitTypeDao.class);
			for(OrganizationalUnitType organizationalUnitType : campaign.getOrganizationalUnits().getOrganizationalUnit()) {
				if(!organizationalUnitTypeDao.existOrganizationalUnit(organizationalUnitType.getId())) {
					lstOrganizationalUnitMissing.add(organizationalUnitType.getId());
				}
			}
		}
		if(!lstOrganizationalUnitMissing.isEmpty()) {
			String strList = String.join(",", lstOrganizationalUnitMissing);
			logger.log(Level.ERROR, "Organizational Unit [{}] does not exist", strList);
			return false;
		}else {
			return true;
		}
		
	}
	
	private boolean checkDateConsistency(Campaign campaign) throws ValidateException {
		boolean returnCode = false;
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
		if(campaign.getOrganizationalUnits() == null) {
			return true;
		}
		for(OrganizationalUnitType organizationalUnitType : campaign.getOrganizationalUnits().getOrganizationalUnit()) {
			try {
				Date collectionStartDate = sdf.parse(organizationalUnitType.getCollectionStartDate());
				Date collectionEndDate = sdf.parse(organizationalUnitType.getCollectionEndDate());
				Date identificationPhaseStartDate = sdf.parse(organizationalUnitType.getIdentificationPhaseStartDate());
				Date interviewerStartDate = sdf.parse(organizationalUnitType.getInterviewerStartDate());
				Date managerStartDate = sdf.parse(organizationalUnitType.getManagementStartDate());
				Date endDate = sdf.parse(organizationalUnitType.getEndDate());
				if(managerStartDate.before(interviewerStartDate) && interviewerStartDate.before(identificationPhaseStartDate) && identificationPhaseStartDate.before(collectionStartDate) 
						&& collectionStartDate.before(collectionEndDate) && collectionEndDate.before(endDate)) {
					returnCode = true;
				}else {
					throw new ValidateException("Error no coherency between the dates for the Organizational Unit : " + organizationalUnitType.getId());
				}
			} catch (Exception e) {
				throw new ValidateException("Error during process, error checking dates coherency for "+organizationalUnitType.getId()+" : "+e.getMessage());
			}
			
		}
		return returnCode;
	}
}
