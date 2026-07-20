package fr.insee.pearljam.batch.dao;

import java.util.List;

import fr.insee.pearljam.batch.campaign.OrganizationalUnitType;
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

	void createOrganizationalUnitFromDto(OrganizationUnitDto organizationalUnit);

	List<OrganizationalUnitType> findIdentificationStartDateByCampaignId(String campaignId);
}