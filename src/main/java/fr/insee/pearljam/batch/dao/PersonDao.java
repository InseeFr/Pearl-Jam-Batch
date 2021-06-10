package fr.insee.pearljam.batch.dao;

import java.util.List;
import java.util.Map.Entry;

import fr.insee.pearljam.batch.campaign.PersonType;

/**
 * Interface for the person table
 * @author pguillemet
 *
 */
public interface PersonDao {

	Long createPerson(PersonType person, String surveyUnitId);

	void deletePersonBySurveyUnitId(String surveyUnitId);

	List<Entry<Long, PersonType>> getPersonsBySurveyUnitId(String id);
    
}