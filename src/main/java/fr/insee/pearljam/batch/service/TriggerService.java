package fr.insee.pearljam.batch.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.dao.MessageDao;
import fr.insee.pearljam.batch.dao.StateDao;
import fr.insee.pearljam.batch.dao.SurveyUnitDao;
import fr.insee.pearljam.batch.exception.TooManyReaffectationsException;
import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.utils.BatchErrorCode;

@Service
public class TriggerService {

	private static final Logger logger = LogManager.getLogger(TriggerService.class);

	@Autowired
	AnnotationConfigApplicationContext context;

	@Autowired
	Connection connection;
	StateDao stateDao;
	SurveyUnitDao surveyUnitDao;
	MessageDao messageDao;
	
	
	@Autowired
	InterviewersSynchronizationService interviewersSynchronizationService;
	
	@Autowired
	InterviewersAffectationsSynchronizationService interviewersAffectationsSynchronizationService;
	
	@Autowired
	OrganizationalUnitsSynchronizationService organizationalUnitsSynchronizationService;
	
	@Autowired
	OrganizationalUnitsAffectationsSynchronizationService organizationalUnitsAffectationsSynchronizationService;
	
	public BatchErrorCode synchronizeWithOpale(String folderOut) throws SQLException {
		BatchErrorCode returnCode = BatchErrorCode.OK;
		connection.setAutoCommit(false);
		try {
			returnCode = updateErrorCode(returnCode, interviewersSynchronizationService.synchronizeInterviewers(folderOut));
			returnCode = updateErrorCode(returnCode, interviewersAffectationsSynchronizationService.synchronizeSurveyUnitInterviewerAffectation(folderOut));
			returnCode = updateErrorCode(returnCode, organizationalUnitsSynchronizationService.synchronizeOrganizationUnits(folderOut));
			returnCode = updateErrorCode(returnCode, organizationalUnitsAffectationsSynchronizationService.synchronizeSurveyUnitOrganizationUnitAffectation(folderOut));
		} catch (TooManyReaffectationsException e) {
			returnCode = BatchErrorCode.KO_FONCTIONAL_ERROR;
			connection.rollback();
			connection.setAutoCommit(true);
			logger.error("Error during survey-unit / interviewers affectation synchronization, rolling back : {}", e.getMessage());
		} catch (Exception e) {
			returnCode = BatchErrorCode.KO_TECHNICAL_ERROR;
			connection.rollback();
			connection.setAutoCommit(true);
			logger.error("Error during organizational units synchronization, rolling back : {}", e.getMessage());
		} finally {
			connection.setAutoCommit(true);
		}
		

		return returnCode;
	}
	
	public BatchErrorCode updateErrorCode(BatchErrorCode current, BatchErrorCode lastResult) {
		BatchErrorCode[] codeCriticityOrder = {
				BatchErrorCode.KO_TECHNICAL_ERROR,
				BatchErrorCode.KO_FONCTIONAL_ERROR,
				BatchErrorCode.OK_TECHNICAL_WARNING,
				BatchErrorCode.OK_FONCTIONAL_WARNING,
				BatchErrorCode.OK_WITH_STOP,
		};
		for(BatchErrorCode code : codeCriticityOrder) {
			if(current.equals(code) || lastResult.equals(code)) {
				return code;
			}
		}
		return BatchErrorCode.OK;
	}
	
	public BatchErrorCode updateStates() throws SQLException, ValidateException {
		stateDao = context.getBean(StateDao.class);
		surveyUnitDao = context.getBean(SurveyUnitDao.class);
		messageDao = context.getBean(MessageDao.class);
		List<String> lstSuANV = new ArrayList<>();
		List<String> lstSuNNS = new ArrayList<>();
		List<String> lstSu = new ArrayList<>();
		List<Long> lstIdMsg = new ArrayList<>();
		try {
			connection.setAutoCommit(false);
			// Get the list of Survey unit id to update from state NVM to ANV or NNS
			surveyUnitDao.getSurveyUnitNVM().stream().forEach(suId -> {
				if (StringUtils.isNotBlank(surveyUnitDao.getSurveyUnitById(suId).getInterwieverId())) {
					stateDao.createState(System.currentTimeMillis(), "ANV", suId);
					lstSuANV.add(suId);
				} else {
					stateDao.createState(System.currentTimeMillis(), "NNS", suId);
					lstSuNNS.add(suId);
				}
			});
			String strLstANV =String.join(",", lstSuANV);
			String strLstNNS =String.join(",", lstSuNNS);
			logger.log(Level.INFO, "There is {} survey-units updated from state NVM to ANV : [{}]", lstSuANV.size(), strLstANV);
			logger.log(Level.INFO, "There is {} survey-units updated from state NVM to NNS : [{}]", lstSuNNS.size(), strLstNNS);

			// Get the list of Survey unit id to update from state ANV to VIN
			lstSu = surveyUnitDao.getSurveyUnitANVToVIN();
			lstSu.stream().forEach(suId -> 
				stateDao.createState(System.currentTimeMillis(), "VIN", suId)
			);
			String strLstSu = String.join(",", lstSu);
			logger.log(Level.INFO, "There is {} survey-units updated from state ANV to VIN : [{}]", lstSu.size(), strLstSu);

			// Get the list of Survey unit id to update to state QNA
			lstSu = surveyUnitDao.getSurveyUnitForQNA();
			lstSu.stream().forEach(suId -> 
				stateDao.createState(System.currentTimeMillis(), "QNA", suId)
			);
			strLstSu = String.join(",", lstSu);
			logger.log(Level.INFO, "There is {} survey-units updated to state QNA : [{}]", lstSu.size(), strLstSu);

			// Get the list of Survey unit id to update to state NVA
			lstSu = surveyUnitDao.getSurveyUnitForNVA();
			lstSu.stream().forEach(suId -> {
				stateDao.createState(System.currentTimeMillis(), "NVA", suId);
				logger.log(Level.INFO, "Update survey-unit {} state NVA", suId);
			});
			strLstSu = String.join(",", lstSu);
			logger.log(Level.INFO, "There is {} survey-units updated to state NVA : [{}]", lstSu.size(), strLstSu);

			// Get the list of notifications to delete
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.add(Calendar.MONTH, -1);
			lstIdMsg = messageDao.getIdsToDelete(c.getTimeInMillis());
			lstIdMsg.stream().forEach(id -> {
				messageDao.deleteCampaignMessageById(id);
				messageDao.deleteOuMessageById(id);
				messageDao.deleteInterviewerMessageById(id);
				messageDao.deleteStatusMessageById(id);
				messageDao.deleteById(id);
			});
			String strLstIdMsg = String.join(",", String.valueOf(lstIdMsg));
			logger.log(Level.INFO, "There is {} messages deleted : [{}]", lstIdMsg.size(), strLstIdMsg);

			connection.commit();
		} catch (Exception e) {
			connection.setAutoCommit(true);
			throw new ValidateException("Error during process, error update states : " + e.getMessage());
		}
		return BatchErrorCode.OK;
			
	}

}