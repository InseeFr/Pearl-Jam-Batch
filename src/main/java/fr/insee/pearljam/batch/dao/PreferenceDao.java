package fr.insee.pearljam.batch.dao;

/**
 * Interface for the Preference table
 * @author scorcaud
 *
 */
public interface PreferenceDao {
	/**
     * Delete preferences by campaign id in database
     * @param campaignId
     */
	void deletePreferenceByCampaignId(String campaignId);
}
