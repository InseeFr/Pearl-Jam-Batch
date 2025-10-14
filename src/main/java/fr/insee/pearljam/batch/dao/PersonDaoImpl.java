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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
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
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Override
	public Long createPerson(PersonType person, String surveyUnitId) {
		String qString = """
				INSERT INTO person
				(birthdate, email, first_name, last_name, title, survey_unit_id, privileged, panel, contact_history_type)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";
		Long parsedDate = null;
		String dob = person.getDateOfBirth();

		if (dob != null && !dob.isBlank()) {
			try {
				LocalDate date = LocalDate.parse(dob, DATE_FORMATTER);
				parsedDate = date.atStartOfDay(ZoneId.systemDefault())
						.toInstant()
						.toEpochMilli();
			} catch (DateTimeParseException e) {
				logger.error("Invalid date format for dob: {}", dob, e);
			}
		}
		Integer parsedTitle = null;
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
			Integer title = rs.getObject("title", Integer.class);
			person.setTitle(title == null ? "MISTER" : (title == 0 ? "MISTER" : "MISS"));

            person.setFirstName(rs.getString("first_name"));
            person.setLastName(rs.getString("last_name"));
            person.setEmail(rs.getString("email"));

			Long birthMillis = rs.getObject("birthdate", Long.class);
			if (birthMillis != null) {
				LocalDate date = Instant.ofEpochMilli(birthMillis).atZone(ZoneId.systemDefault()).toLocalDate();
				person.setDateOfBirth(DATE_FORMATTER.format(date)); // "dd/MM/yyyy"
			}
			Boolean privileged = rs.getObject("privileged", Boolean.class);
			person.setPrivileged(Boolean.TRUE.equals(privileged));

			Boolean panel = rs.getObject("panel", Boolean.class);
			person.setPanel(Boolean.TRUE.equals(panel));

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