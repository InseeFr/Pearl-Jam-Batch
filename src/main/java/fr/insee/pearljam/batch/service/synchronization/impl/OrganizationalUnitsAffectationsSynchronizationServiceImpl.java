package fr.insee.pearljam.batch.service.synchronization.impl;

import fr.insee.pearljam.batch.dao.OrganizationalUnitTypeDao;
import fr.insee.pearljam.batch.dao.SurveyUnitDao;
import fr.insee.pearljam.batch.dto.OrganizationUnitAffectationsDto;
import fr.insee.pearljam.batch.dto.SimpleIdDto;
import fr.insee.pearljam.batch.enums.ContextReferentialSyncLogIds;
import fr.insee.pearljam.batch.exception.BatchException;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.exception.TooManyReaffectationsException;
import fr.insee.pearljam.batch.service.ContextReferentialService;
import fr.insee.pearljam.batch.service.synchronization.OrganizationalUnitsAffectationsSynchronizationService;
import fr.insee.pearljam.batch.template.*;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.Utils;
import fr.insee.pearljam.batch.utils.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



@Service
public class OrganizationalUnitsAffectationsSynchronizationServiceImpl implements OrganizationalUnitsAffectationsSynchronizationService {
	private static final Logger logger = LogManager.getLogger(OrganizationalUnitsAffectationsSynchronizationServiceImpl.class);

	@Value("${fr.insee.pearljam.context.synchronization.log.elements:#{null}}")
	private String logIds;
	
	@Value("${fr.insee.pearljam.context.synchronization.organization.reaffectation.threshold.absolute:#{null}}")
	private Float absThreshold;
	
	@Value("${fr.insee.pearljam.context.synchronization.organization.reaffectation.threshold.relative:#{null}}")
	private Float relThreshold;
	
	@Autowired
	ContextReferentialService opaleService;
	
	@Autowired
	OrganizationalUnitTypeDao organizationalUnitTypeDao;
	
	@Autowired
	SurveyUnitDao surveyUnitDao;
	
	public BatchErrorCode synchronizeSurveyUnitOrganizationUnitAffectation(String out) throws TooManyReaffectationsException, SynchronizationException, BatchException {
		logger.info("Organizational units affectation synchronization started");
		// processed, created, updated
		Long[] counters = {0L, 0L, 0L};
		List<OrganizationUnitAffectation> createdIds = new ArrayList<>();
		List<OrganizationUnitAffectation> updatedIds = new ArrayList<>();
		List<OrganizationUnitAffectationSynchronizationError> errors = new ArrayList<>();
		BatchErrorCode code = BatchErrorCode.OK;
		
		List<OrganizationUnitAffectationsDto> organizationUnitsAffectations = opaleService.getOrganizationUnitsAffectationsFromOpale();
		for(OrganizationUnitAffectationsDto organizationUnitAffectations : organizationUnitsAffectations) {
			code = processOrganizationUnitAffectation(organizationUnitAffectations, counters,
					createdIds, updatedIds, errors, code);
		}
		
		if(absThreshold != null && absThreshold > 0 && counters[2] > absThreshold) {
			throw new TooManyReaffectationsException(
					"This synchronization would have reaffected " 
					+ counters[2].toString()
					+ " survey units to an other organization, "
					+ "but the maximum number of reaffectations allowed is "
					+ absThreshold.toString()
					+". (Defined in properties file: fr.insee.pearljam.context.synchronization.organization.reaffectation.threshold.absolute)"
					);
		}
		
		if(relThreshold != null && relThreshold > 0 && (float)counters[2]/counters[0] > relThreshold/100) {
			throw new TooManyReaffectationsException(
					"This synchronization would have reaffected " 
					+ ((float)counters[2]*100/counters[0])
					+ "% of survey units processed to an other organization, "
					+ "but the maximum percentage of reaffectation allowed is "
					+ relThreshold.toString()
					+"%. (Defined in properties file: fr.insee.pearljam.context.synchronization.organization.reaffectation.threshold.relative)"
					);
		}
		
		if(logIds != null && logIds.equals(ContextReferentialSyncLogIds.YES.getLabel())) {
			OrganizationUnitsAffectationsSynchronizationResult sync = new OrganizationUnitsAffectationsSynchronizationResult(
					code.getLabel(), 
					counters[0], counters[1], counters[2],  
					new CreatedOrganizationUnitsAffectations(createdIds),
					new OrganizationUnitsReaffectations(updatedIds),
					new OrganizationUnitAffectationsSynchronizationErrors(errors)
				);
			XmlUtils.objectToXML(out+"/synchro/sync.SU_OU." + Utils.getTimestamp() + ".xml", sync);
		}
		else {
			OrganizationUnitsAffectationsSynchronizationResult sync = new OrganizationUnitsAffectationsSynchronizationResult(
					code.getLabel(), 
					counters[0], counters[1], counters[2],  
					null,
					null,
					new OrganizationUnitAffectationsSynchronizationErrors(errors)
				);

			String timestamp = Utils.getTimestamp();
			XmlUtils.objectToXML(out+"/synchro/sync.SU_OU." + timestamp + ".xml", sync);
			if(logIds != null && logIds.equals(ContextReferentialSyncLogIds.IN_SEPARATE_FILES.getLabel())) {
				XmlUtils.objectToXML(out+"/synchro/sync.SU_OU.created." + timestamp + ".xml", new CreatedOrganizationUnitsAffectations(createdIds));
				XmlUtils.objectToXML(out+"/synchro/sync.SU_OU.reassigned." + timestamp + ".xml", new OrganizationUnitsReaffectations(updatedIds));
			}
		}

		
		logger.info("Organizational units affectation synchronization ended - result {}",code.getCode());
		logger.info("Created [{}] - Updated [{}] - Error [{}]",counters[1],counters[2],errors.size());
		return code;
	}
	
	private BatchErrorCode processOrganizationUnitAffectation(
			OrganizationUnitAffectationsDto organizationUnitAffectations,
			Long[] counters,
			List<OrganizationUnitAffectation> createdIds,
			List<OrganizationUnitAffectation> updatedIds,
			List<OrganizationUnitAffectationSynchronizationError> errors,
			BatchErrorCode code) {
		BatchErrorCode returnCode = code;
		try {
			if(organizationalUnitTypeDao.existOrganizationalUnit(organizationUnitAffectations.getId())) {
				List<String> ids = organizationUnitAffectations.getSurveyUnits().stream()
						.map(SimpleIdDto::getId)
						.collect(Collectors.toList());
				for(String id : ids) {
					returnCode = assignSurveyUnit(id, organizationUnitAffectations, counters,
							createdIds, updatedIds, errors, returnCode);
				}
			}
			else {
				errors.add(new OrganizationUnitAffectationSynchronizationError(
						organizationUnitAffectations.getId(),
						null,
						"Could not assign: missing organization unit",
						"Organization unit with id " + organizationUnitAffectations.getId() + " does not exist in Sabiane"
					));
				logger.error("Organizational unit '{}' does not exist in Sabiane", organizationUnitAffectations.getId());
				if(returnCode == BatchErrorCode.OK) {
					returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
				}
			}
		} catch (Exception e) {
			errors.add(new OrganizationUnitAffectationSynchronizationError(
					organizationUnitAffectations.getId(),
					null,
					"Unexpected error while processing organization unit",
					"An exception occured while processing the affectations of interviewer with id " + organizationUnitAffectations.getId() + " : " + e.getMessage()
				));
			returnCode = BatchErrorCode.OK_TECHNICAL_WARNING;
		}
		return returnCode;
	}
	
	private BatchErrorCode assignSurveyUnit(
			String id,
			OrganizationUnitAffectationsDto organizationUnitAffectations,
			Long[] counters,
			List<OrganizationUnitAffectation> createdIds,
			List<OrganizationUnitAffectation> updatedIds,
			List<OrganizationUnitAffectationSynchronizationError> errors,
			BatchErrorCode code) {
			BatchErrorCode returnCode = code;
		if(surveyUnitDao.existSurveyUnit(id)) {
			String currentAffectation = surveyUnitDao.getSurveyUnitOrganizationUnitAffectation(id);
			
			if(!organizationUnitAffectations.getId().equals(currentAffectation)) {
				if(currentAffectation != null) {
					updatedIds.add(new OrganizationUnitAffectation(organizationUnitAffectations.getId(), id));
					counters[2] += 1;
				}
				else {
					createdIds.add(new OrganizationUnitAffectation(organizationUnitAffectations.getId(), id));
					counters[1] += 1;
				}
				surveyUnitDao.setSurveyUnitOrganizationUnitAffectation(id, organizationUnitAffectations.getId());
			}
			
			counters[0] += 1;
		}
		else {
			errors.add(new OrganizationUnitAffectationSynchronizationError(
					organizationUnitAffectations.getId(),
					id,
					"Could not assign: missing survey unit",
					"Survey unit with id " + id + " does not exist in Sabiane"
				));
			logger.error("Survey unit with id '{}' does not exist in Sabiane", id);
			if(returnCode == BatchErrorCode.OK) {
				returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
			}
		}
		return returnCode;
	}
}
