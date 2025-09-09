package fr.insee.pearljam.batch.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.Constants;
import fr.insee.pearljam.batch.context.Context;
import fr.insee.pearljam.batch.context.InerviewersType;
import fr.insee.pearljam.batch.context.InterviewerType;
import fr.insee.pearljam.batch.context.OrganizationalUnitType;
import fr.insee.pearljam.batch.context.OrganizationalUnitsRefType;
import fr.insee.pearljam.batch.context.OrganizationalUnitsType;
import fr.insee.pearljam.batch.context.UserType;
import fr.insee.pearljam.batch.context.UsersRefType;
import fr.insee.pearljam.batch.context.UsersType;
import fr.insee.pearljam.batch.dao.InterviewerTypeDao;
import fr.insee.pearljam.batch.dao.OrganizationalUnitTypeDao;
import fr.insee.pearljam.batch.dao.UserTypeDao;
import fr.insee.pearljam.batch.exception.BatchException;
import fr.insee.pearljam.batch.exception.DataBaseException;
import fr.insee.pearljam.batch.utils.BatchErrorCode;

/**
 * ContextService : Contains all functions needed to load context
 * 
 * @author bclaudel
 *
 */
@Service
@RequiredArgsConstructor
public class ContextService {

	@Qualifier("pilotageConnection")
	private final Connection pilotageConnection;

	private final UserTypeDao userDao;

	private final InterviewerTypeDao interviewerDao;

	private final OrganizationalUnitTypeDao organizationalUnitDao;

	private static final Logger logger = LogManager.getLogger(ContextService.class);

	/**
	 * Create context and all data associated
	 * 
	 * @param context
	 * @return BatchErrorCode
	 * @throws SQLException
	 * @throws DataBaseException
	 */
	public BatchErrorCode createContext(Context context) throws SQLException, DataBaseException {
		BatchErrorCode returnCode = BatchErrorCode.OK;
		pilotageConnection.setAutoCommit(false);
		try{
			// Create Users
			returnCode = createUsers(context.getUsers(), returnCode);
			// Create Interviewers
			returnCode = createInterviewers(context.getInterviewers(), returnCode);
			// Create Organizational Units
			returnCode = createOrganizationalUnits(context.getOrganizationalUnits(), returnCode);
			pilotageConnection.commit();
			      
      returnCode = checkUsersOUAssociations(returnCode);
		}catch (Exception e) {
			logger.log(Level.ERROR, "Rollback ... Error during creating Context : {}", e.getMessage());
			pilotageConnection.rollback();
			pilotageConnection.setAutoCommit(true);
			throw new DataBaseException("Error during creating Context : " + e.getMessage());
		}finally {
			pilotageConnection.setAutoCommit(true);
		}
		return returnCode;
	}

	private BatchErrorCode checkUsersOUAssociations(BatchErrorCode returnCode) {
		List<String> lstUserId = userDao.findAllUsersWithoutOrganizationUnit();
		if(!lstUserId.isEmpty()) {
			String strList = StringUtils.join(lstUserId, ", ");
			logger.log(Level.WARN, "Following users are not associated to an organization unit : {}", strList);
			returnCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
		}
		return returnCode;
	}


	/**
	 * Create OrganizationalUnits and all data associated
	 * 
	 * @param organizationalUnitsType
	 * @param returnCode
	 * @return BatchErrorCode
	 * @throws DataBaseException
	 * @throws BatchException 
	 */
	private BatchErrorCode createOrganizationalUnits(OrganizationalUnitsType organizationalUnitsType, BatchErrorCode returnCode) throws DataBaseException {
		BatchErrorCode returnCreateCode = returnCode;
		if (organizationalUnitsType == null) {
			logger.log(Level.WARN, "There is no organizational units to treat");
			return BatchErrorCode.OK_FONCTIONAL_WARNING;
		}
		for (OrganizationalUnitType organizationalUnit : organizationalUnitsType.getOrganizationalUnit()) {
			if (organizationalUnitDao.existOrganizationalUnit(organizationalUnit.getId())) {
				logger.log(Level.WARN, "The organizational Unit {} already exists", organizationalUnit.getId());
				returnCreateCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
			}else {
				organizationalUnitDao.createOrganizationalUnit(organizationalUnit);
				// Associate Organizational Unit to Organizational Unit children
				returnCreateCode = updateOrganizationalUnitRef(organizationalUnit.getOrganizationalUnitsRef(), organizationalUnit.getId(), organizationalUnit.getType(), returnCreateCode);
				
				// Associate Organizational Unit to Users
				returnCreateCode = updateUsersRef(organizationalUnit.getUsersRef(), organizationalUnit.getId(), returnCreateCode);
				logger.log(Level.INFO, "The organizational Unit {} has been created", organizationalUnit.getId());
			}
		}
		return returnCreateCode;
	}

	/**
	 * Update UserRefs: users associated to Organizational unit
	 * 
	 * @param usersRefType
	 * @param organizationalUnitId
	 * @param returnCreateCode 
	 * @return 
	 * @throws DataBaseException
	 */
	private BatchErrorCode updateUsersRef(UsersRefType usersRefType, String organizationalUnitId, BatchErrorCode returnCreateCode) throws DataBaseException {
		if (usersRefType != null && usersRefType.getUserId()!=null && !usersRefType.getUserId().isEmpty()) {
			if(userDao.userAlreadyAssociated(usersRefType.getUserId(), organizationalUnitId)) {
				logger.log(Level.WARN, "At least one user is already associated to an other organizational unit");
				return BatchErrorCode.OK_FONCTIONAL_WARNING;
			}
			for(String userId : usersRefType.getUserId()) {
				if(!userDao.existUser(userId)) {
					throw new DataBaseException("the user "+userId+" does not exist in DB");
				}
				if(userDao.userAlreadyAssociatedToOrganizationUnitId(userId, organizationalUnitId)) {
					logger.log(Level.WARN, "User {} is already associated to organizational unit {}", userId, organizationalUnitId);
					return BatchErrorCode.OK_FONCTIONAL_WARNING;
				}
				userDao.updateOrganizationalUnitByUserId(userId, organizationalUnitId);
			}
		}else {
			logger.log(Level.WARN, "There is no UsersRef for the organizational unit {}", organizationalUnitId);
			return BatchErrorCode.OK_FONCTIONAL_WARNING;
		}
		return returnCreateCode;
	}

	/**
	 * Update OrganizationalUnitRefs: OrganizationalUnit associated to Organizational unit
	 * 
	 * @param organizationalUnitsRefType
	 * @param organizationalUnitId
	 * @param returnCreateCode 
	 * @return 
	 * @throws DataBaseException
	 */
	private BatchErrorCode updateOrganizationalUnitRef(OrganizationalUnitsRefType organizationalUnitsRefType, String organizationalUnitId, String organizationalUnitType, BatchErrorCode returnCreateCode) throws DataBaseException {
		if (organizationalUnitsRefType != null && organizationalUnitsRefType.getOrganizationalUnitId()!=null && !organizationalUnitsRefType.getOrganizationalUnitId().isEmpty()) {
			if(organizationalUnitDao.existOrganizationalUnitAlreadyAssociated(organizationalUnitsRefType.getOrganizationalUnitId())) {
				logger.log(Level.WARN, "At least one OrganizationalUnitRef already associated to an other organizational unit than {}", organizationalUnitsRefType.getOrganizationalUnitId());
				return BatchErrorCode.OK_FONCTIONAL_WARNING;
			}
			for(String organizationalUnitChild : organizationalUnitsRefType.getOrganizationalUnitId()) {
				if(!organizationalUnitDao.existOrganizationalUnit(organizationalUnitChild)) {
					throw new DataBaseException("The organization unit "+organizationalUnitChild+" does not exist in DB");
				}
				organizationalUnitDao.updateOrganizationalUnitParent(organizationalUnitChild, organizationalUnitId);
			}
		} else {
			Level level = Level.INFO;
			if(Constants.NATIONAL.equals(organizationalUnitType)) {
				level = Level.WARN;
				returnCreateCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
			}
			logger.log(level, "There is no OrganizationalUnitsRef for the organizational unit {}", organizationalUnitId);
		}
		return returnCreateCode;
	}


	/**
	 * Create Users and all data associated
	 * 
	 * @param usersType
	 * @param returnCode
	 * @return
	 */
	private BatchErrorCode createUsers(UsersType usersType, BatchErrorCode returnCode) {
		BatchErrorCode returnCreateCode = returnCode;
		if (usersType != null) {
			for (UserType user : usersType.getUser()) {
				if (userDao.existUser(user.getId())) {
					logger.log(Level.WARN, "The user {} already exists", user.getId());
					returnCreateCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
				} else {
					userDao.createUser(user);
					logger.log(Level.INFO, "The user {} has been created", user.getId());
				}
			}
		} else {
			logger.log(Level.WARN, "There is no users to treat");
			returnCreateCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
		}
		return returnCreateCode;
	}

	/**
	 * Create Interviewers and all data associated
	 * 
	 * @param inerviewersType
	 * @param returnCode
	 * @return
	 */
	private BatchErrorCode createInterviewers(InerviewersType inerviewersType, BatchErrorCode returnCode) {
		BatchErrorCode returnCreateCode = returnCode;
		if (inerviewersType != null) {
			for (InterviewerType interviewer : inerviewersType.getInterviewer()) {
				if (interviewerDao.existInterviewer(interviewer.getId())) {
					logger.log(Level.WARN, "The interviewer {} already exists", interviewer.getId());
					returnCreateCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
				} else {
					interviewerDao.createInterviewer(interviewer);
					logger.log(Level.INFO, "The interviewer {} has been created", interviewer.getId());
				}
			}
		} else {
			logger.log(Level.WARN, "There is no interviewers to treat");
			returnCreateCode = BatchErrorCode.OK_FONCTIONAL_WARNING;
		}
		return returnCreateCode;
	}

}
