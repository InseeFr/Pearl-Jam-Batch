package fr.insee.pearljam.batch.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.Constants;
import fr.insee.pearljam.batch.campaign.PersonType;

/**
 * Service for the Person entity that implements the interface associated
 * @author pguillemet
 *
 */
@Service
public class PersonDaoImpl implements PersonDao{
	
	@Autowired
	@Qualifier("pilotageJdbcTemplate")
	JdbcTemplate pilotageJdbcTemplate;
	
	private static final Logger logger = LogManager.getLogger(PersonDaoImpl.class);
	

	@Override
	public Long createPerson(PersonType person, String surveyUnitId) {
		String qString = new StringBuilder("INSERT INTO person (birthdate, email, favorite_email, first_name, last_name, title, survey_unit_id, privileged) ")
				.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
				.toString();
		Long parsedDate = null;
		Integer parsedTitle = null;
		try{
			parsedDate = new SimpleDateFormat(Constants.DATE_FORMAT_2).parse(person.getDateOfBirth()).getTime();
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
		boolean favoriteEmail= person.isFavoriteEmail()!=null?person.isFavoriteEmail():false;
		KeyHolder keyHolder = new GeneratedKeyHolder();
		final Long tempDate = parsedDate;
		final Integer tempTitle = parsedTitle;
		pilotageJdbcTemplate.update(
		    new PreparedStatementCreator() {
		        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
		            PreparedStatement ps = connection.prepareStatement(qString, Statement.RETURN_GENERATED_KEYS);
		            ps.setLong(1, tempDate);
		            ps.setString(2, person.getEmail());
		            ps.setBoolean(3, favoriteEmail);
		            ps.setString(4, person.getFirstName());
		            ps.setString(5, person.getLastName());
		            ps.setLong(6, tempTitle);
		            ps.setString(7, surveyUnitId);
		            ps.setBoolean(8, person.isPrivileged());
		            return ps;
		        }
		    },
		    keyHolder);
		return (Long) keyHolder.getKeyList().get(0).get("id");
    }
	
	@Override
	public void deletePersonBySurveyUnitId(String surveyUnitId) {
		String qString = "DELETE FROM person WHERE survey_unit_id=?";
		pilotageJdbcTemplate.update(qString, surveyUnitId);
	}
	

	private static final class PersonTypeTypeMapper implements RowMapper<Entry<Long,PersonType>> {
        public Entry<Long,PersonType> mapRow(ResultSet rs, int rowNum) throws SQLException         {
        	PersonType person = new PersonType();
        	int title = rs.getInt("title");
        	if(!rs.wasNull()) {
                person.setTitle(title == 0 ? "MISTER" : "MISS");
        	}
            person.setFirstName(rs.getString("first_name"));
            person.setLastName(rs.getString("last_name"));
            person.setEmail(rs.getString("email"));
            long dateTime = rs.getLong("birthdate");
            if(!rs.wasNull()) {
            	DateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_2);
                person.setDateOfBirth(df.format(new Date(dateTime)));
        	}
            person.setPrivileged(rs.getBoolean("privileged"));
            person.setFavoriteEmail(rs.getBoolean("favorite_email"));
            
            Long id = rs.getLong("id");
            
            return new AbstractMap.SimpleEntry<>(id, person);
        }
    }
	

	@Override
	public List<Entry<Long, PersonType>> getPersonsBySurveyUnitId(String id) {
		String qString = "SELECT person.* FROM person WHERE survey_unit_id=?";
		return pilotageJdbcTemplate.query(qString, new Object[] {id}, new PersonTypeTypeMapper());
	}
	
	
}