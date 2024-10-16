package fr.insee.pearljam.batch.dao;

import java.util.List;

import fr.insee.pearljam.batch.campaign.Campaign;
import fr.insee.pearljam.batch.campaign.OrganizationalUnitType;

/**
 * Interface for the Visibility table
 * @author bclaudel
 *
 */
public interface VisibilityDao {
	/**
     * check if Visibility exist in database
     * @param campaignId
     * @param organizationalUnitId
     * @return boolean
     */
	boolean existVisibility(String campaignId, String organizationalUnitId);
	
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
     * Create a Visibility in database
     * @param campaign
     * @param organizationalUnitType
     */
	void createVisibility(Campaign campaign, OrganizationalUnitType organizationalUnitType);
	/**
     * Update Visibility dates in database
     * @param campaign
     * @param organizationalUnitType
     */
	void updateDateVisibilityByCampaignIdAndOrganizationalUnitId(Campaign campaign, OrganizationalUnitType organizationalUnitType) throws Exception;
	/**
     * Update a Visibility in database
     * @param campaign
     * @param organizationalUnitType
     */
	void updateVisibilityByCampaignIdAndOrganizationalUnitId(Campaign campaign, OrganizationalUnitType organizationalUnitType) throws Exception;
	/**
     * Delete visibilities by campaign in database
     * @param campaignId
     */
	void deleteVisibilityByCampaignId(String campaignId);
}
