package fr.insee.pearljam.batch.dao;

import java.util.List;

import fr.insee.pearljam.batch.context.OrganizationalUnitType;
import fr.insee.pearljam.batch.dto.OrganizationUnitDto;

/**
 * Interface for the OrganizationalUnit table
 * @author scorcaud
 *
 */
public interface OrganizationalUnitTypeDao {
	
	/**
     * Get a Organizational Unit by id in database
     * @param id
     * @return boolean
     */
	boolean existOrganizationalUnit(String id);
	/**
     * Create an Organizational Unit in database
     * @param organizationalUnit
     */
	void createOrganizationalUnit(OrganizationalUnitType organizationalUnit);
	/**
     * Update an Organizational Unit in database
     * @param organizationalUnitChild
     * @param organizationalUnitId
     */
	void updateOrganizationalUnitParent(String organizationalUnitChild, String organizationalUnitId);
	/**
     * check if Organizational Unit is already associated in database
     * @param organizationalUnitRefs List of organizationalUnitRefs
     * @return boolean
     */
	boolean existOrganizationalUnitAlreadyAssociated(List<String> organizationalUnitRefs);

	List<String> findChildren(String currentOu);
	
	void createOrganizationalUnitFromDto(OrganizationUnitDto organizationalUnit);
}