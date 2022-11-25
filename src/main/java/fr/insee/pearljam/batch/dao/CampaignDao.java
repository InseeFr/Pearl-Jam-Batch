package fr.insee.pearljam.batch.dao;

import java.util.List;

import fr.insee.pearljam.batch.campaign.Campaign;

/**
 * Interface for the campaign table
 * 
 * @author bclaudel
 *
 */
public interface CampaignDao {
	/**
	 * Get a Campaign by id in database
	 * 
	 * @param id
	 * @return boolean object
	 */
	boolean existCampaign(String id);

	/**
	 * Create a Campaign in database
	 * 
	 * @param campaign
	 */
	void createCampaign(Campaign campaign);

	/**
	 * Delete a Campaign in database
	 * 
	 * @param campaign
	 */
	void deleteCampaign(Campaign campaign);

	/**
	 * Update a Campaign in database
	 * 
	 * @param campaign
	 */
	void updateCampaignById(Campaign campaign);

	List<Campaign> findAll();

	Campaign findById(String campaignId);

}