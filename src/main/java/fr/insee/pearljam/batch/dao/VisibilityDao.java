package fr.insee.pearljam.batch.dao;

import java.util.List;

import fr.insee.pearljam.batch.campaign.OrganizationalUnitType;

/**
 * Interface for the Visibility table
 * @author bclaudel
 *
 */
public interface VisibilityDao {
	/**
     * Get all Visibilities by CampaignId in database
     * @param campaignId
     * @return List of visibility
     */
	List<OrganizationalUnitType> getAllVisibilitiesByCampaignId(String campaignId);

	/**
	 * Get a Visibility by CampaignId and OrganizationUnitId in database
	 * @param campaignId campaign id
	 * @param organizationUnitId campaign id
	 * @return A visibility
	 */
	OrganizationalUnitType getVisibilityByCampaignIdAndOrganizationUnitId(String campaignId,String organizationUnitId);

	/**
     * Delete visibilities by campaign in database
     * @param campaignId
     */
	void deleteVisibilityByCampaignId(String campaignId);
}
