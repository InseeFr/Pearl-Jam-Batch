package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.PersonType;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

/**
 * Service for the Person entity that implements the interface associated
 * @author pguillemet
 *
 */
@Service
@RequiredArgsConstructor
public class PersonDaoImpl implements PersonDao{

	private final JdbcTemplate pilotageJdbcTemplate;
	
	private static final Logger logger = LogManager.getLogger(PersonDaoImpl.class);
	public static final String DATE_FORMAT = "dd/MM/yyyy";

	@Override
	public Long createPerson(PersonType person, String surveyUnitId) {
		String qString = """
				INSERT INTO person
				(birthdate, email, first_name, last_name, title, survey_unit_id, privileged, panel, contact_history_type)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";
		Long parsedDate = null;
		Integer parsedTitle = null;
		try{
			parsedDate = new SimpleDateFormat(DATE_FORMAT).parse(person.getDateOfBirth()).getTime();
		} catch (ParseException e) {
			logger.log(Level.ERROR, e.getMessage());
		}
		String lowercaseTitle = person.getTitle().toLowerCase();
		if(lowercaseTitle.contains("miss") || lowercaseTitle.contains("mme")) {
			parsedTitle = 1;
		}
		else if(lowercaseTitle.equals("mister") || lowercaseTitle.equals("m.")) {
			parsedTitle = 0;
		}
		else {
			logger.log(Level.ERROR,"Could not parse title of person '{} {}'", person.getFirstName(), person.getLastName());
		}
		KeyHolder keyHolder = new GeneratedKeyHolder();
		final Long tempDate = parsedDate;
		final Integer tempTitle = parsedTitle;
		pilotageJdbcTemplate.update(
				connection -> {
					PreparedStatement ps = connection.prepareStatement(qString, Statement.RETURN_GENERATED_KEYS);
					if (tempDate != null) {
						ps.setLong(1, tempDate);
					} else {
						ps.setNull(1, Types.BIGINT);
					}
					ps.setString(2, person.getEmail());
					ps.setString(3, person.getFirstName());
					ps.setString(4, person.getLastName());
					ps.setLong(5, tempTitle);
					ps.setString(6, surveyUnitId);
					ps.setBoolean(7, person.isPrivileged());
					ps.setBoolean(8, person.isPanel());
					ps.setString(9, person.getContactHistoryType());
					return ps;
				},
		    keyHolder);
		return (Long) keyHolder.getKeyList().getFirst().get("id");
    }
	
	@Override
	public void deletePersonBySurveyUnitId(String surveyUnitId) {
		String qString = "DELETE FROM person WHERE survey_unit_id=?";
		pilotageJdbcTemplate.update(qString, surveyUnitId);
	}

	private static final class PersonTypeMapper implements RowMapper<Entry<Long,PersonType>> {
        public Entry<Long,PersonType> mapRow(ResultSet rs, int rowNum) throws SQLException         {
        	PersonType person = new PersonType();
        	int title = rs.getInt("title");
			if (!rs.wasNull()) {
				person.setTitle(title == 0 ? "MISTER" : "MISS");
			} else {
				person.setTitle("MISTER");
			}
            person.setFirstName(rs.getString("first_name"));
            person.setLastName(rs.getString("last_name"));
            person.setEmail(rs.getString("email"));
            long dateTime = rs.getLong("birthdate");
            if(!rs.wasNull()) {
				DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                person.setDateOfBirth(df.format(new Date(dateTime)));
        	}
            person.setPrivileged(rs.getBoolean("privileged"));
			person.setPanel(rs.getBoolean("panel"));
			person.setContactHistoryType(rs.getString("contact_history_type"));

			Long id = rs.getLong("id");
            
            return new AbstractMap.SimpleEntry<>(id, person);
        }
    }
	

	@Override
	public List<Entry<Long, PersonType>> getPersonsBySurveyUnitId(String id) {
		String qString = "SELECT person.* FROM person WHERE survey_unit_id=?";
		return pilotageJdbcTemplate.query(qString, new PersonTypeMapper(), id);
	}


}