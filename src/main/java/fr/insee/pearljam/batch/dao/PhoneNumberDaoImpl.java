package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.PhoneNumberType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface for the PhoneNumber entity
 * 
 * @author scorcaud
 *
 */
@Service
public class PhoneNumberDaoImpl implements PhoneNumberDao {

	@Autowired
	@Qualifier("pilotageJdbcTemplate")
	JdbcTemplate pilotageJdbcTemplate;

	@Override
	public void createPhoneNumber(PhoneNumberType phoneNumber, Long personId) {
		String qString = "INSERT INTO phone_number (favorite, number, source, person_id) VALUES (?, ?,?, ?)";
		Integer source = switch (phoneNumber.getSource().toLowerCase()) {
            case "fiscal" -> 0;
            case "directory" -> 1;
            case "interviewer" -> 2;
            default -> null;
        };
        boolean favorite= phoneNumber.isFavorite() != null && phoneNumber.isFavorite();

        pilotageJdbcTemplate.update(qString, favorite,phoneNumber.getNumber(), source, personId);
	}

	@Override
	public void deletePhoneNumbersBySurveyUnitId(String surveyUnitId) {
		String qString = """
                DELETE FROM phone_number WHERE person_id IN 
                (SELECT id FROM person WHERE survey_unit_id=?)""";
		pilotageJdbcTemplate.update(qString, surveyUnitId);
	}
	
	private static final class PhoneNumberTypeMapper implements RowMapper<PhoneNumberType> {
        public PhoneNumberType mapRow(ResultSet rs, int rowNum) throws SQLException         {
        	PhoneNumberType phoneNumber = new PhoneNumberType();
        	phoneNumber.setNumber(rs.getString("number"));
			phoneNumber.setFavorite(rs.getBoolean("favorite"));
        	Integer source = rs.getInt("source");
        	if(!rs.wasNull()) {
        		switch(source) {
	    			case 0:
	    				phoneNumber.setSource("fiscal");
	    				break;
	    			case 1:
	    				phoneNumber.setSource("directory");
	    				break;
	    			case 2: 
	    				phoneNumber.setSource("interviewer");
	    				break;
	    			default:
	    				break;
        		}
        	}
            return phoneNumber;
        }
    }
	
	@Override
	public List<PhoneNumberType> getPhoneNumbersByPersonId(Long id) {
		String qString = "SELECT phone_number.* FROM phone_number WHERE person_id=?";
		return pilotageJdbcTemplate.query(qString, new Object[] {id}, new PhoneNumberTypeMapper());
	}
}
