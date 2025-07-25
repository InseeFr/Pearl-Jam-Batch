package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.context.InterviewerType;
import fr.insee.pearljam.batch.dto.InterviewerDto;

/**
 * Service for the Interviewer entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
public class InterviewerTypeDaoImpl implements InterviewerTypeDao {

	@Autowired
	@Qualifier("pilotageJdbcTemplate")
	JdbcTemplate pilotageJdbcTemplate;
	
	@Override
	public boolean existInterviewer(String id) {
		String qString = "SELECT COUNT(id) FROM interviewer WHERE id=?";
		Long nbRes = pilotageJdbcTemplate.queryForObject(qString, Long.class, id);
		return nbRes>0;	
	}
	

	@Override
	public void createInterviewer(InterviewerType interviewer) {
		String qString = "INSERT INTO public.interviewer(id, email, first_name, last_name, phone_number) VALUES (?, ?, ?, ?, ?)";
		pilotageJdbcTemplate.update(qString, interviewer.getId(), interviewer.getEmail(), interviewer.getFirstName(), interviewer.getLastName(), interviewer.getPhoneNumber());
	}
	
	@Override
	public void createInterviewerFromDto(InterviewerDto interviewer) {
		String qString = "INSERT INTO public.interviewer(id, email, first_name, last_name, phone_number) VALUES (?, ?, ?, ?, ?)";
		pilotageJdbcTemplate.update(qString, interviewer.getIdep(), interviewer.getMailInsee(), interviewer.getPrenom(), interviewer.getNom(), interviewer.getTelInsee());
	}
	
	@Override
	public void updateInterviewerFromDto(InterviewerDto interviewer) {
		String titleValue = convertSexeToTitle(interviewer.getSexe());
		String qString = "UPDATE public.interviewer SET email=?, first_name=?, last_name=?, phone_number=?, title=?  WHERE id=?";
		pilotageJdbcTemplate.update(qString, interviewer.getMailInsee(), interviewer.getPrenom(), interviewer.getNom(), interviewer.getTelInsee(), titleValue, interviewer.getIdep());
	}
	
	@Override
	public boolean isDifferentFromDto(InterviewerDto interviewer) {
		String titleValue = convertSexeToTitle(interviewer.getSexe());
		String qString = "SELECT count(1) FROM public.interviewer WHERE  id=? AND email=? AND first_name=? AND last_name=? AND phone_number=? AND title=? ";
		Long nbRes = pilotageJdbcTemplate.queryForObject(qString,  Long.class, interviewer.getIdep(), interviewer.getMailInsee(), interviewer.getPrenom(), interviewer.getNom(), interviewer.getTelInsee(), titleValue );
		return nbRes<1;	
	}

	@Override
	public InterviewerType findById(String interviewerId) {
		String qString = "SELECT * FROM interviewer where interviewer.id =?";
		return pilotageJdbcTemplate.queryForObject(qString, new InterviewerTypeDaoImpl.InterviewerTypeMapper(), interviewerId);
	}

	private static final class InterviewerTypeMapper implements RowMapper<InterviewerType> {
		public InterviewerType mapRow(ResultSet rs, int rowNum) throws SQLException {
			InterviewerType interviewer = new InterviewerType();
			interviewer.setId(rs.getString("id"));
			interviewer.setEmail(rs.getString("email"));
			interviewer.setPhoneNumber(rs.getString("phone_number"));
			interviewer.setFirstName(rs.getString("first_name"));
			interviewer.setLastName(rs.getString("last_name"));
			interviewer.setTitle(rs.getString("title"));
			return interviewer;
		}
	}

	private String convertSexeToTitle(String sexe){
		return sexe.equals("1")?"MISTER":"MISS";
	}

}
