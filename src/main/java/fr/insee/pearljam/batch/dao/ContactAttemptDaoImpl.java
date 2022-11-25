package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.ContactAttemptType;

/**
 * Service for the ContactAttempt entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
public class ContactAttemptDaoImpl implements ContactAttemptDao {
	
	@Autowired
	@Qualifier("pilotageJdbcTemplate")
	JdbcTemplate pilotageJdbcTemplate;
	
	public List<ContactAttemptType> getContactAttemptTypeBySurveyUnitId(String surveyUnitId){
		String qString = "SELECT * FROM contact_attempt WHERE survey_unit_id=?";
		return pilotageJdbcTemplate.query(qString, new Object[] {surveyUnitId}, new ContactAttemptTypeMapper());
	}

	/**
	 * Implements the mapping between the result of the query and the ReportingUnit entity
	 * @return ReportingUnitMapper
	 */
	private static final class ContactAttemptTypeMapper implements RowMapper<ContactAttemptType> {
        public ContactAttemptType mapRow(ResultSet rs, int rowNum) throws SQLException         {
        	ContactAttemptType cont = new ContactAttemptType();
        	cont.setDate(String.valueOf(rs.getLong("date")));
			cont.setMedium(rs.getString("medium"));
        	cont.setStatus(rs.getString("status"));
            return cont;
        }
    }
	
	public void deleteContactAttemptBySurveyUnitId(String surveyUnitId) throws SQLException {
		String qString = "DELETE FROM contact_attempt WHERE survey_unit_id=?";
		pilotageJdbcTemplate.update(qString, surveyUnitId);
	}
}
