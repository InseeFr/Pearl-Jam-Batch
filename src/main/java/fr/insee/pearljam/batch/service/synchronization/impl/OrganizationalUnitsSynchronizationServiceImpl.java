package fr.insee.pearljam.batch.service.synchronization.impl;

import fr.insee.pearljam.batch.dao.OrganizationalUnitTypeDao;
import fr.insee.pearljam.batch.dto.OrganizationUnitDto;
import fr.insee.pearljam.batch.enums.ContextReferentialSyncLogIds;
import fr.insee.pearljam.batch.exception.BatchException;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.service.ContextReferentialService;
import fr.insee.pearljam.batch.service.synchronization.OrganizationalUnitsSynchronizationService;
import fr.insee.pearljam.batch.template.CreatedOrganizationUnits;
import fr.insee.pearljam.batch.template.OrganizationUnitSynchronizationError;
import fr.insee.pearljam.batch.template.OrganizationUnitSynchronizationErrors;
import fr.insee.pearljam.batch.template.OrganizationUnitsSynchronizationResult;
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



@Service
public class OrganizationalUnitsSynchronizationServiceImpl implements OrganizationalUnitsSynchronizationService {
	private static final Logger logger = LogManager.getLogger(OrganizationalUnitsSynchronizationServiceImpl.class);

	@Value("${fr.insee.pearljam.context.synchronization.log.elements:#{null}}")
	private String logIds;
	
	@Autowired
	ContextReferentialService opaleService;
	
	@Autowired
	OrganizationalUnitTypeDao organizationalUnitTypeDao;
	
	
	public BatchErrorCode synchronizeOrganizationUnits(String out) throws SynchronizationException, BatchException {
		logger.info("Organizational units synchronization started");
		// processed, created
		Long[] counters = {0L, 0L};
		List<String> createdIds = new ArrayList<>();
		List<OrganizationUnitSynchronizationError> errors = new ArrayList<>();
		BatchErrorCode code = BatchErrorCode.OK;
				
		List<OrganizationUnitDto> organizationUnits = opaleService.getOrganizationUnitsFromOpale();
		for(OrganizationUnitDto organizationUnit : organizationUnits) {
			code = processOrganizationUnit(organizationUnit, counters, createdIds, errors, code);
		}

		
			if(logIds != null && logIds.equals(ContextReferentialSyncLogIds.YES.getLabel())) {
				OrganizationUnitsSynchronizationResult sync = new OrganizationUnitsSynchronizationResult(
						code.getLabel(), 
						counters[0], counters[1], 
						new CreatedOrganizationUnits(createdIds),
						new OrganizationUnitSynchronizationErrors(errors)
					);
				XmlUtils.objectToXML(out+"/synchro/sync.OU." + Utils.getTimestamp() + ".xml", sync);
			}
			else {
				OrganizationUnitsSynchronizationResult sync = new OrganizationUnitsSynchronizationResult(
						code.getLabel(), 
						counters[0], counters[1], 
						null,
						new OrganizationUnitSynchronizationErrors(errors)
					);
				String timestamp = Utils.getTimestamp();
				XmlUtils.objectToXML(out+"/synchro/sync.OU." + timestamp + ".xml", sync);
				if(logIds != null && logIds.equals(ContextReferentialSyncLogIds.IN_SEPARATE_FILES.getLabel())) {
					XmlUtils.objectToXML(out+"/synchro/sync.OU.created." + timestamp + ".xml", new CreatedOrganizationUnits(createdIds));
				}
			}

		logger.info("Organizational units synchronization ended - result {}",code.getCode());
		logger.info("Organizational units created [{}] - errors [{}]",counters[1],errors.size());
		return code;
	}
	
	public BatchErrorCode processOrganizationUnit(
			OrganizationUnitDto organizationUnit,
			Long[] counters,
			List<String> createdIds,
			List<OrganizationUnitSynchronizationError> errors,
			BatchErrorCode code) {
		BatchErrorCode returnCode = code;
		try {
			if(!organizationalUnitTypeDao.existOrganizationalUnit(organizationUnit.getCodeEtab())) {
				if(organizationalUnitTypeDao.existOrganizationalUnit(organizationUnit.getOrigineEtab())) {
					organizationalUnitTypeDao.createOrganizationalUnitFromDto(organizationUnit);
					counters[1] += 1;
					createdIds.add(organizationUnit.getCodeEtab());
				}
				else {
					errors.add(
								new OrganizationUnitSynchronizationError(
								organizationUnit.getCodeEtab(),
								"Could not create",
								String.format("Cannot import organizational unit '%s' because its parent unit '%s' is not present in Sabiane", 
										organizationUnit.getCodeEtab(), organizationUnit.getOrigineEtab())
								)
							);
					if(returnCode == BatchErrorCode.OK) {
						returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
					}
				}
			}
			counters[0] += 1;
		}
		catch(Exception e) {
			errors.add(new OrganizationUnitSynchronizationError(
					organizationUnit.getCodeEtab(),
					"Unexpected error while processing organization unit",
					"An exception occured while processing the organization unit with code " + organizationUnit.getCodeEtab() + " : " + e.getMessage()
				));
			returnCode = BatchErrorCode.OK_TECHNICAL_WARNING;
		}
		return returnCode;
	}
	
}
