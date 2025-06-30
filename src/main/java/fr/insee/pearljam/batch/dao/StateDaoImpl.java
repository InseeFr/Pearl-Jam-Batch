package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.StateType;
import fr.insee.pearljam.batch.campaign.SurveyUnitType;
/**
 * Service for the State entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
public class StateDaoImpl implements StateDao{

	@Autowired
	@Qualifier("pilotageJdbcTemplate")
	JdbcTemplate pilotageJdbcTemplate;
		
	@Override
	public void createState(Long date, String type, String surveyUnitId) {
		String qString = "INSERT INTO state (date, type, survey_unit_id) VALUES (?,?,?)";
		pilotageJdbcTemplate.update(qString, date, type, surveyUnitId);
	}
	
	public List<StateType> getStateBySurveyUnitId(String surveyUnitId) {
		String qString ="SELECT * FROM state WHERE survey_unit_id=?";
		return pilotageJdbcTemplate.query(qString, new Object[] {surveyUnitId}, new StateTypeMapper());
	}
	
	private static final class StateTypeMapper implements RowMapper<StateType> {
        public StateType mapRow(ResultSet rs, int rowNum) throws SQLException         {
        	StateType state = new StateType();
        	state.setDate(String.valueOf(rs.getLong("date")));
            state.setType(rs.getString("type"));
            return state;
        }
    }
	
	public void deleteStateBySurveyUnitId(String surveyUnitId) {
		String qString = "DELETE FROM state WHERE survey_unit_id=?";
		pilotageJdbcTemplate.update(qString, surveyUnitId);
	}
	
}
