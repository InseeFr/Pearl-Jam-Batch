package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.CommentType;

/**
 * Service for the Comment entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
public class CommentDaoImpl implements CommentDao{
	
	@Autowired
	@Qualifier("pilotageJdbcTemplate")
	JdbcTemplate pilotageJdbcTemplate;
	
	public List<CommentType> getCommentBySurveyUnitId(String surveyUnitId) {
		String qString = "SELECT * FROM comment WHERE survey_unit_id=?";
		return pilotageJdbcTemplate.query(qString, new Object[] {surveyUnitId}, new CommentTypeMapper());
		
	}
	/**
	 * Implements the mapping between the result of the query and the CommentType entity
	 * @return CommentTypeMapper
	 */
	private static final class CommentTypeMapper implements RowMapper<CommentType> {
        public CommentType mapRow(ResultSet rs, int rowNum) throws SQLException         {
        	CommentType com = new CommentType();
            com.setType(rs.getString("type"));
            com.setValue(rs.getString("value"));
            return com;
        }
    }
	
	public void deleteCommentBySurveyUnitId(String surveyUnitId) throws SQLException {
		String qString = "DELETE FROM comment WHERE survey_unit_id=?";
		pilotageJdbcTemplate.update(qString, surveyUnitId);
	}

	@Override
	public void createComment(CommentType comment, String surveyUnitId) {
		String qString = "INSERT INTO comment (type, value, survey_unit_id) VALUES (?, ?, ?)";
		pilotageJdbcTemplate.update(qString, comment.getType(), comment.getValue(),surveyUnitId);
	}
}
