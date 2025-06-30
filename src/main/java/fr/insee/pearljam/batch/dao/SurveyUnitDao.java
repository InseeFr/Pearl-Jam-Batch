package fr.insee.pearljam.batch.dao;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.SurveyUnitType;

/**
 * Interface for the SurveyUnit table
 * @author scorcaud
 *
 */
@Service
public interface SurveyUnitDao {
	/**
     * Get a SurveyUnit by id in database
     * @param id
     * @return boolean
     */
	boolean existSurveyUnit(String id);

	/**
     * Delete a SurveyUnit in database
     * @param campaignId
     */
	void deleteSurveyUnitByCampaignId(String campaignId);
	
	/**
     * Check if SurveyUnit exist for a campaign in database
     * @param id
     * @param campaignId
     * @return boolean
     */
	boolean existSurveyUnitForCampaign(String id, String campaignId);
	/**
     * Get all SurveyUnit by campaign id in database
     * @param campaignId
     * @return List of SurveyUnit id
     */
	List<String> getAllSurveyUnitByCampaignId(String campaignId);
	/**
     * Get SurveyUnit in database
     * @param surveyUnitId
     * @return SurveyUnitType
     */
	SurveyUnitType getSurveyUnitById(String surveyUnitId);/**
     * Get SurveyUnits in database
     * @param surveyUnitIds
     * @return List<SurveyUnitType>
     */
	List<SurveyUnitType> getSurveyUnitsById(List<String> surveyUnitIds);
	/**
     * Delete a SurveyUnit in database
     * @param surveyUnitId
     */
	void deleteSurveyUnitById(String surveyUnitId);
	/**
     * Get Address Id by SurveyUnitId in database
     * @param surveyUnitId
     * @return long
     */
	long getAddressIdBySurveyUnitId(String surveyUnitId);
	/**
     * Get SampleIdentifiersId by SurveyUnitId in database
     * @param surveyUnitId
     * @return long
     */
	long getSampleIdentifiersIdBySurveyUnitId(String surveyUnitId);
	
	
	List<String> getSurveyUnitNVM(long instantDate);
     List<String> getSurveyUnitANV(long instantDate);
     List<String> getSurveyUnitNNS(long instantDate);
	List<String> getSurveyUnitForNVA(long instantDate);
	String getSurveyUnitInterviewerAffectation(String surveyUnitId);
	void setSurveyUnitInterviewerAffectation(String surveyUnitId, String idep);
	String getSurveyUnitOrganizationUnitAffectation(String surveyUnitId);
	void setSurveyUnitOrganizationUnitAffectation(String surveyUnitId, String organizationUnitId);

	void createSurveyUnit(String campaignId, SurveyUnitType surveyUnit, Long addressId, Long sampleIdentifierId,
			String interviewerId, String organizationUnitId);

	void updateSurveyUnitById(String campaignId, SurveyUnitType surveyUnit);

}
