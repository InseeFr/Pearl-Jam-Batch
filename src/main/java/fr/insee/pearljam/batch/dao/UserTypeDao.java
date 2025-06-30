package fr.insee.pearljam.batch.dao;

import java.util.List;

import fr.insee.pearljam.batch.context.UserType;

/**
 * Interface for the SurveyUnit table
 * @author bclaudel
 *
 */
public interface UserTypeDao {
	/**
     * check if user exist in database
     * @param id
     * @return boolean
     */
	boolean existUser(String id);
	/**
     * Create user in database
     * @param user
     */
	void createUser(UserType user);
	
	/**
     * Update OrganizationalUnit By UserId
     * @param userId
     * @param id
     */
	void updateOrganizationalUnitByUserId(String userId, String id);
	/**
     * check if user is already associated in database
     * @param userId List of userId
     * @param organizationUnitId
     * @return boolean
     */
	boolean userAlreadyAssociated(List<String> userId, String organizationUnitId);
	
	/**
     * check if user is already associated to a specific organization unit in database
     * @param userId
     * @param organizationalUnitId
     * @return boolean
     */
	boolean userAlreadyAssociatedToOrganizationUnitId(String userId, String organizationalUnitId);
	
	List<String> findAllUsersWithoutOrganizationUnit();

}
