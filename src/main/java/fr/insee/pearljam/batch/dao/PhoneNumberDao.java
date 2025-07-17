package fr.insee.pearljam.batch.dao;

import java.util.List;

import fr.insee.pearljam.batch.campaign.PhoneNumberType;

/**
 * Interface for the PhoneNumber table
 * @author scorcaud
 *
 */
public interface PhoneNumberDao {
	/**
     * Create a phoneNumber in database
     * @param phoneNumber
     * @param personId
     */
	void createPhoneNumber(PhoneNumberType phoneNumber, Long personId);

	void deletePhoneNumbersBySurveyUnitId(String surveyUnitId);

	List<PhoneNumberType> getPhoneNumbersByPersonId(Long id);
	
}
