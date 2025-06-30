package fr.insee.pearljam.batch.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.dao.MessageDao;
import fr.insee.pearljam.batch.dao.StateDao;
import fr.insee.pearljam.batch.dao.SurveyUnitDao;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.exception.TooManyReaffectationsException;
import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.service.synchronization.InterviewersAffectationsSynchronizationService;
import fr.insee.pearljam.batch.service.synchronization.InterviewersSynchronizationService;
import fr.insee.pearljam.batch.service.synchronization.OrganizationalUnitsAffectationsSynchronizationService;
import fr.insee.pearljam.batch.service.synchronization.OrganizationalUnitsSynchronizationService;
import fr.insee.pearljam.batch.service.synchronization.SynchronizationUtilsService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;

@Service
public class TriggerService {

	private static final Logger logger = LogManager.getLogger(TriggerService.class);

	@Autowired
	AnnotationConfigApplicationContext context;

	@Autowired
	@Qualifier("pilotageConnection")
	Connection pilotageConnection;
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

	@Autowired
	SynchronizationUtilsService synchronizationUtilsService;

	private Clock clock;

	public void setClock(Clock newClock) {
		clock = newClock;
	}

	public void initDefaultClock() {
		setClock(
				Clock.system(
						ZoneId.of("UTC")));
	}

	{
		initDefaultClock();
	}

	private long now() {
		return LocalDateTime.now(clock).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
	}

	public BatchErrorCode synchronizeWithOpale(String folderOut) throws SQLException {

		BatchErrorCode returnCode = checkExternalServices();
		if (!returnCode.equals(BatchErrorCode.OK))
			return returnCode;

		BatchErrorCode itwSyncReturnCode = synchronizeIntervewers(folderOut);
		returnCode = updateErrorCode(returnCode, itwSyncReturnCode);

		BatchErrorCode itwAffSyncReturnCode = synchronizeIntervewersAffectations(folderOut);
		returnCode = updateErrorCode(returnCode, itwAffSyncReturnCode);

		// temporary bypass
		// BatchErrorCode ouSyncReturnCode = synchronizeOrganizationUnits(folderOut);
		// returnCode = updateErrorCode(returnCode, ouSyncReturnCode);

		BatchErrorCode ouAffSyncReturnCode = synchronizeOrganizationUnitsAffectations(folderOut);
		returnCode = updateErrorCode(returnCode, ouAffSyncReturnCode);

		return returnCode;
	}

	public BatchErrorCode checkExternalServices() {
		try {
			synchronizationUtilsService.checkServices();
		} catch (SynchronizationException e) {
			logger.error("Error during checking external services, synchronization aborted : {}", e.getMessage());
			return BatchErrorCode.KO_TECHNICAL_ERROR;
		} catch (Exception e) {
			logger.error("Unexpected error during checking external services, synchronization aborted : {}",
					e.getMessage());
			return BatchErrorCode.KO_TECHNICAL_ERROR;
		}
		return BatchErrorCode.OK;
	}

	public BatchErrorCode synchronizeIntervewers(String folderOut) throws SQLException {
		BatchErrorCode returnedCode;
		pilotageConnection.setAutoCommit(false);
		try {
			returnedCode = interviewersSynchronizationService.synchronizeInterviewers(folderOut);
		} catch (Exception e) {
			pilotageConnection.rollback();
			logger.error("Error during interviewers synchronization, rolling back : {}", e.getMessage());
			returnedCode = BatchErrorCode.KO_TECHNICAL_ERROR;
		} finally {
			pilotageConnection.setAutoCommit(true);
		}
		return returnedCode;

	}

	public BatchErrorCode synchronizeIntervewersAffectations(String folderOut)  throws SQLException{
		BatchErrorCode returnedCode;
		pilotageConnection.setAutoCommit(false);
		try {
			returnedCode = interviewersAffectationsSynchronizationService
					.synchronizeSurveyUnitInterviewerAffectation(folderOut);
		} catch (TooManyReaffectationsException e) {
			returnedCode = BatchErrorCode.KO_FONCTIONAL_ERROR;
			pilotageConnection.rollback();
			logger.error("Error during survey-unit / interviewers affectation synchronization, rolling back : {}",
					e.getMessage());
		} catch (Exception e) {
			returnedCode = BatchErrorCode.KO_TECHNICAL_ERROR;
			pilotageConnection.rollback();
			logger.error("Error during survey-unit / interviewers affectation synchronization, rolling back : {}",
					e.getMessage());
		} finally {
			pilotageConnection.setAutoCommit(true);
		}
		return returnedCode;
	}

	public BatchErrorCode synchronizeOrganizationUnitsAffectations(String folderOut)  throws SQLException {
		BatchErrorCode returnedCode;
		pilotageConnection.setAutoCommit(false);
		try {
			returnedCode = organizationalUnitsAffectationsSynchronizationService
					.synchronizeSurveyUnitOrganizationUnitAffectation(folderOut);
		} catch (TooManyReaffectationsException e) {
			returnedCode = BatchErrorCode.KO_FONCTIONAL_ERROR;
			pilotageConnection.rollback();
			logger.error("Error during survey-unit / organization-units affectation synchronization, rolling back : {}",
					e.getMessage());
		} catch (Exception e) {
			returnedCode = BatchErrorCode.KO_TECHNICAL_ERROR;
			pilotageConnection.rollback();
			logger.error("Error during survey-unit / organization-units affectation synchronization, rolling back : {}",
					e.getMessage());
		} finally {
			pilotageConnection.setAutoCommit(true);
		}
		return returnedCode;
	}

	public BatchErrorCode updateErrorCode(BatchErrorCode current, BatchErrorCode lastResult) {
		BatchErrorCode[] codeCriticityOrder = {
				BatchErrorCode.KO_TECHNICAL_ERROR,
				BatchErrorCode.KO_FONCTIONAL_ERROR,
				BatchErrorCode.OK_TECHNICAL_WARNING,
				BatchErrorCode.OK_FONCTIONAL_WARNING,
				BatchErrorCode.OK_WITH_STOP,
		};
		for (BatchErrorCode code : codeCriticityOrder) {
			if (current.equals(code) || lastResult.equals(code)) {
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
		List<String> lstSu;
		List<Long> lstIdMsg;
		try {
			pilotageConnection.setAutoCommit(false);
			// Get the list of Survey unit id to update from state NVM to ANV or NNS
			surveyUnitDao.getSurveyUnitNVM(now()).stream().forEach(suId -> {
				if (StringUtils.isNotBlank(surveyUnitDao.getSurveyUnitById(suId).getInterviewerId())) {
					stateDao.createState(now(), "ANV", suId);
					lstSuANV.add(suId);
				} else {
					stateDao.createState(now(), "NNS", suId);
					lstSuNNS.add(suId);
				}
			});
			String strLstANV = String.join(",", lstSuANV);
			String strLstNNS = String.join(",", lstSuNNS);
			logger.log(Level.INFO, "There are {} survey-units updated from state NVM to ANV : [{}]", lstSuANV.size(),
					strLstANV);
			logger.log(Level.INFO, "There are {} survey-units updated from state NVM to NNS : [{}]", lstSuNNS.size(),
					strLstNNS);


			// Get the list of Survey unit id to update from state NNS to ANV
			lstSu = surveyUnitDao.getSurveyUnitNNS(now()).stream().filter(suId -> StringUtils.isNotBlank(surveyUnitDao.getSurveyUnitById(suId).getInterviewerId())).collect(Collectors.toList());
			lstSu.stream().forEach(suId -> stateDao.createState(now(), "ANV", suId));
			String strLstNnsToAnv = String.join(",", lstSu);
			logger.log(Level.INFO, "There are {} survey-units updated from state NNS to ANV : [{}]", lstSu.size(), strLstNnsToAnv);

			// Get the list of Survey unit id to update from state ANV to VIN
			lstSu = surveyUnitDao.getSurveyUnitANV(now());
			lstSu.stream().forEach(suId -> stateDao.createState(now(), "VIN", suId));
			String strLstSu = String.join(",", lstSu);
			logger.log(Level.INFO, "There are {} survey-units updated from state ANV to VIN : [{}]", lstSu.size(), strLstSu);

			// Get the list of Survey unit id to update to state NVA
			lstSu = surveyUnitDao.getSurveyUnitForNVA(now());
			lstSu.stream().forEach(suId -> {
				stateDao.createState(now(), "NVA", suId);
				logger.log(Level.INFO, "Update survey-unit {} state NVA", suId);
			});
			strLstSu = String.join(",", lstSu);
			logger.log(Level.INFO, "There are {} survey-units updated to state NVA : [{}]", lstSu.size(), strLstSu);

			// Get the list of notifications to delete
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.add(Calendar.MONTH, -1);
			lstIdMsg = messageDao.getIdsToDelete(c.getTimeInMillis());
			lstIdMsg.stream().forEach(id -> {
				messageDao.deleteCampaignMessageById(id);
				messageDao.deleteOuMessageById(id);
				messageDao.deleteStatusMessageById(id);
				messageDao.deleteById(id);
			});
			String strLstIdMsg = String.join(",", String.valueOf(lstIdMsg));
			logger.log(Level.INFO, "There are {} messages deleted : [{}]", lstIdMsg.size(), strLstIdMsg);

			pilotageConnection.commit();
		} catch (Exception e) {
			pilotageConnection.setAutoCommit(true);
			throw new ValidateException("Error during process, error update states : " + e.getMessage());
		}
		return BatchErrorCode.OK;

	}
}
