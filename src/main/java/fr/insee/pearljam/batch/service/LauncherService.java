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
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.Constants;
import fr.insee.pearljam.batch.campaign.Campaign;
import fr.insee.pearljam.batch.campaign.OrganizationalUnitType;
import fr.insee.pearljam.batch.context.Context;
import fr.insee.pearljam.batch.dao.CampaignDao;
import fr.insee.pearljam.batch.dao.OrganizationalUnitTypeDao;
import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.exception.BatchException;
import fr.insee.pearljam.batch.exception.DataBaseException;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.PathUtils;
import fr.insee.pearljam.batch.utils.XmlUtils;

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
public class LauncherService {

	@Autowired
	AnnotationConfigApplicationContext context;
	
	@Autowired
	FolderService folderService;
	
	private static final Logger logger = LogManager.getLogger(LauncherService.class);
	

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
	public BatchErrorCode validateLoadClean(BatchOption batchOption, String folderIn, String folderOut) throws BatchException, IOException, ValidateException, XMLStreamException {
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
						XmlUtils.validateXMLSchema(Constants.MODEL_CAMPAIGN, folderIn + "/" + name +".xml");
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
			case DELETECAMPAIGN:
				return "campaign";
			case LOADCONTEXT:
				return "context";
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
	 * @throws BatchException
	 * @throws SQLException
	 * @throws DataBaseException
	 * @throws ValidateException
	 * @throws SynchronizationException 
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public BatchErrorCode load(BatchOption batchOption, String in, String out, String processing) throws BatchException, SQLException, DataBaseException, ValidateException, SynchronizationException, IOException{
		switch(batchOption) {
			case LOADCAMPAIGN:
				return loadCampaign(in, out, processing);
			case DELETECAMPAIGN:
				return deleteCampaign(in, out);
			case LOADCONTEXT:
				return loadContext(in);
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
		String fileName = "";
		switch(returnCode) {
		case KO_TECHNICAL_ERROR: 
		case KO_FONCTIONAL_ERROR:
			if(batchOption==BatchOption.DELETECAMPAIGN) {
				fileName = name + PathUtils.getTimestampForPath() + ".delete.error.xml";
			} else {
				fileName = name + PathUtils.getTimestampForPath() + ".error.xml";
			}
			break;
		case OK_TECHNICAL_WARNING:
		case OK_FONCTIONAL_WARNING:
			if(batchOption==BatchOption.DELETECAMPAIGN) {
				fileName = name + PathUtils.getTimestampForPath() + ".delete.error.xml";
			} else {
				fileName = name + PathUtils.getTimestampForPath() + ".warning.xml";
			}
			break;
		case OK:
			if(batchOption==BatchOption.DELETECAMPAIGN) {
				fileName = name + PathUtils.getTimestampForPath() + ".delete.done.xml";
			} else {
				fileName = name + PathUtils.getTimestampForPath() + ".done.xml";
			}
			break;
		default:
			throw new ValidateException("Unknown return code");
		}
		
		String location;
		String processedFilename = folderService.getFilename();
		if(batchOption==BatchOption.LOADCAMPAIGN && !processedFilename.isBlank()) {
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
  
  public void moveCampaignFileToProcessing(String name, String in, 
		  String processing, String campaignId) throws IOException, ValidateException {
	  	String fileName = "";

	  	fileName = new StringBuilder(name)
                .append(".")
                .append(campaignId)
                .append(".")
                .append(PathUtils.getTimestampForPath())
                .append(".xml")
                .toString();
    	folderService.setFilename(fileName);
    
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
			String campaignId = campaign.getId().toUpperCase();
		    moveCampaignFileToProcessing("campaign", in, processing, campaignId);
		    
			if(checkOrganizationUnits(campaign) && checkDateConsistency(campaign)) {
				return campaignService.createOrUpdateCampaign(
						campaign, 
						campaignDao.existCampaign(campaignId), 
						processing + "/" + folderService.getFilename(), 
						out);
			}else{
				throw new ValidateException("Error during load campaign " + campaignId);
			}
		}else {
			throw new ValidateException("Error : campaign is null");
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
			throw new ValidateException("Error : campaign is null");
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
