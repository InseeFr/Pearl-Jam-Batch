package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.IdentificationType;

/**
 * Interface for the Identification table
 * 
 * @author bclaudel
 *
 */
@Service
public class IdentificationDaoImpl implements IdentificationDao {

     @Autowired
     @Qualifier("pilotageJdbcTemplate")
     JdbcTemplate pilotageJdbcTemplate;

     @Override
     public IdentificationType getIdentificationTypeBySurveyUnitId(String surveyUnitId) {
          String qString = "SELECT * FROM identification WHERE survey_unit_id=?";
          List<IdentificationType> listRes = pilotageJdbcTemplate.query(qString, new Object[] { surveyUnitId },
                    new IdentificationTypeMapper());
          if (!listRes.isEmpty()) {
               return listRes.get(0);
          } else {
               return null;
          }
     }

     private static final class IdentificationTypeMapper implements RowMapper<IdentificationType> {
          public IdentificationType mapRow(ResultSet rs, int rowNum) throws SQLException {
               IdentificationType ident = new IdentificationType();
               ident.setIdentification(rs.getString("identification"));
               ident.setAccess(rs.getString("access"));
               ident.setSituation(rs.getString("situation"));
               ident.setCategory(rs.getString("category"));
               ident.setOccupant(rs.getString("occupant"));
               return ident;
          }
     }

     @Override
     public void deleteIdentificationBySurveyUnitId(String surveyUnitId) throws SQLException {
          String qString = "DELETE FROM identification WHERE survey_unit_id=?";
          pilotageJdbcTemplate.update(qString, surveyUnitId);

     }

}
