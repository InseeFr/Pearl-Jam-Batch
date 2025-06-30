package fr.insee.pearljam.batch.dao;

import java.util.List;

import fr.insee.pearljam.batch.campaign.StateType;

/**
 * Interface for the State table
 * @author scorcaud
 *
 */
public interface StateDao {
	/**
     * Create a State in database
     * @param date state date
     * @param type state type
     * @param surveyUnitId linked SU id
     */
	void createState(Long date, String type, String surveyUnitId);
	/**
     * Get States by SurveyUnit id in database
     * @param surveyUnitId linked SU id
     * @return list of SurveyUnit
     */
	List<StateType> getStateBySurveyUnitId(String surveyUnitId);
	/**
     * Delete States by surveyUnitId in database
     * @param surveyUnitId linked SU id
     */
	void deleteStateBySurveyUnitId(String surveyUnitId);
}
