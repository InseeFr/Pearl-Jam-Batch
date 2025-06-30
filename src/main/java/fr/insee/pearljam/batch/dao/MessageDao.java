package fr.insee.pearljam.batch.dao;

import java.util.List;

public interface MessageDao {
	void deleteByCampaign(String campaignId);
	void deleteById(Long id);
	void deleteCampaignMessageById(Long id);
	void deleteOuMessageById(Long id);
	void deleteStatusMessageById(Long id);
	List<Long> getIdsToDelete(Long passedDate);
	boolean isIdPresentForCampaignId(String campaignId);

}
