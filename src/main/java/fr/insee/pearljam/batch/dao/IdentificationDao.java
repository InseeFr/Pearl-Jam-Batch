package fr.insee.pearljam.batch.dao;

import java.sql.SQLException;

import fr.insee.pearljam.batch.campaign.IdentificationType;

/**
 * Interface for the Identification table
 * 
 * @author bclaudel
 *
 */
public interface IdentificationDao {
     /**
      * Get Identification by Survey unit id in database
      * 
      * @param surveyUnitId
      * @return IdentificationType can be null
      */
     IdentificationType getIdentificationTypeBySurveyUnitId(String surveyUnitId);

     /**
      * Delete Identification by Survey unit id in database
      * 
      * @param surveyUnitId
      */
     void deleteIdentificationBySurveyUnitId(String surveyUnitId) throws SQLException;
}
