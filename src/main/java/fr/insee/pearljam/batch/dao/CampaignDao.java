package fr.insee.pearljam.batch.dao;


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
	 * Delete a Campaign in database
	 * 
	 * @param campaign
	 */
	void deleteCampaign(Campaign campaign);

	Campaign findById(String campaignId);

}