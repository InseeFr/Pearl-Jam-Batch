package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.SurveyUnitType;

/**
 * Service for the SurveyUnit entity that implements the interface associated
 * 
 * @author scorcaud
 *
 */
@Service
public class SurveyUnitDaoImpl implements SurveyUnitDao {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	public boolean existSurveyUnit(String id) {
		String qString = "SELECT COUNT(id) FROM survey_unit WHERE id=?";
		Long nbRes = jdbcTemplate.queryForObject(qString, new Object[] { id }, Long.class);
		return nbRes > 0;
	}

	@Override
	public boolean existSurveyUnitForCampaign(String id, String campaignId) {
		String qString = "SELECT COUNT(id) FROM survey_unit WHERE id=? AND campaign_id<>?";
		Long nbRes = jdbcTemplate.queryForObject(qString, new Object[] { id, campaignId }, Long.class);
		return nbRes > 0;
	}

	@Override
	public void updateSurveyUnitById(String campaignId, SurveyUnitType surveyUnit, String interviewerId, String organizationUnitId) {
		String qString = "UPDATE survey_unit SET priority=?, campaign_id=?, interviewer_id=?, organization_unit_id=? WHERE id=?";
		jdbcTemplate.update(qString, surveyUnit.isPriority(), campaignId,
				interviewerId, organizationUnitId, surveyUnit.getId());
	}

	public void deleteSurveyUnitByCampaignId(String campaignId) {
		String qString = "DELETE FROM survey_unit WHERE campaign_id=?";
		jdbcTemplate.update(qString, campaignId);
	}

	@Override
	public void createSurveyUnit(String campaignId, SurveyUnitType surveyUnit, Long addressId,
			Long sampleIdentifierId, String interviewerId, String organizationUnitId) {
		String qString = "INSERT INTO survey_unit (id, priority, address_id, campaign_id, interviewer_id, sample_identifier_id, organization_unit_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
		
		jdbcTemplate.update(qString, surveyUnit.getId(), surveyUnit.isPriority(),
				addressId, campaignId, interviewerId, sampleIdentifierId, organizationUnitId);
	}

	@Override
	public List<String> getAllSurveyUnitByCampaignId(String campaignId) {
		String qString = "SELECT id FROM survey_unit WHERE campaign_id=?";
		return jdbcTemplate.queryForList(qString, new Object[] {campaignId}, String.class);
	}
	
	public SurveyUnitType getSurveyUnitById(String surveyUnitId) {
		String qString = "SELECT * FROM survey_unit WHERE id=?";
		return jdbcTemplate.queryForObject(qString, new Object[] {surveyUnitId}, new SurveyUnitTypeMapper());
	}
	
	@Override
	public String getSurveyUnitInterviewerAffectation(String surveyUnitId) {
		String qString = "SELECT interviewer_id FROM survey_unit WHERE id=?";
		return jdbcTemplate.queryForObject(qString, new Object[] {surveyUnitId}, String.class);
	}
	
	@Override
	public void setSurveyUnitInterviewerAffectation(String surveyUnitId, String idep) {
		String qString = "UPDATE survey_unit SET interviewer_id=?  WHERE id=?";
		jdbcTemplate.update(qString, idep, surveyUnitId);
	}
	
	@Override
	public String getSurveyUnitOrganizationUnitAffectation(String surveyUnitId) {
		String qString = "SELECT organization_unit_id FROM survey_unit WHERE id=?";
		return jdbcTemplate.queryForObject(qString, new Object[] {surveyUnitId}, String.class);
	}
	
	@Override
	public void setSurveyUnitOrganizationUnitAffectation(String surveyUnitId, String organizationUnitId) {
		String qString = "UPDATE survey_unit SET organization_unit_id=?  WHERE id=?";
		jdbcTemplate.update(qString, organizationUnitId, surveyUnitId);
	}
	
	
	
	/**
	 * Implements the mapping between the result of the query and the ReportingUnit entity
	 * @return ReportingUnitMapper
	 */
	private static final class SurveyUnitTypeMapper implements RowMapper<SurveyUnitType> {
        public SurveyUnitType mapRow(ResultSet rs, int rowNum) throws SQLException         {
        	SurveyUnitType su = new SurveyUnitType();
            su.setId(rs.getString("id"));
            su.setPriority(rs.getBoolean("priority"));
            su.setInterwieverId(rs.getString("interviewer_id"));
            su.setOrganizationalUnitId(rs.getString("organization_unit_id"));
            return su;
        }
    }
	
	public SurveyUnitType getSurveyUnitByIdTest(String surveyUnitId) {
		String qString = "SELECT su.* FROM survey_unit su INNER JOIN comment com on com.survey_unit_id = su.id WHERE id=?";
		return jdbcTemplate.queryForObject(qString, new Object[] {surveyUnitId}, new SurveyUnitTypeMapper());
	}
	
	
	public void deleteSurveyUnitById(String surveyUnitId) {
		String qString = "DELETE FROM survey_unit WHERE id=?";
		jdbcTemplate.update(qString, surveyUnitId);
	}
	
	public long getAddressIdBySurveyUnitId(String surveyUnitId) {
		String qString = "SELECT address_id FROM survey_unit WHERE id=?";
		return jdbcTemplate.queryForObject(qString, new Object[] { surveyUnitId }, Long.class);
	}
	
	public long getSampleIdentifiersIdBySurveyUnitId(String surveyUnitId) {
		String qString = "SELECT sample_identifier_id FROM survey_unit WHERE id=?";
		return jdbcTemplate.queryForObject(qString, new Object[] { surveyUnitId }, Long.class);
	}
	

	@Override
	public List<String> getSurveyUnitNVM() {
		String qString = new StringBuilder("SELECT t.id FROM ")
				.append("(SELECT su.id as id, v.management_start_date, ")
				.append("(SELECT s.type FROM state s WHERE s.survey_unit_id=su.id ORDER BY s.date DESC LIMIT 1) as lastState ")
				.append("FROM survey_unit su ")
				.append("JOIN campaign c ON su.campaign_id=c.id ")
				.append("JOIN visibility v ON v.campaign_id=c.id AND su.organization_unit_id=v.organization_unit_id) t ")
				.append("WHERE t.lastState='NVM' ")
				.append("AND t.management_start_date<=?")
				.toString();
		return jdbcTemplate.queryForList(qString, new Object[] {System.currentTimeMillis()}, String.class);
	}

	@Override
	public List<String> getSurveyUnitAnvOrNnsToVIN() {
		String qString = new StringBuilder("SELECT t.id FROM ")
				.append("(SELECT su.id as id, v.interviewer_start_date, ")
				.append("(SELECT s.type FROM state s WHERE s.survey_unit_id=su.id ORDER BY s.date DESC LIMIT 1) as lastState ")
				.append("FROM survey_unit su ")
				.append("JOIN campaign c ON su.campaign_id=c.id ")
				.append("JOIN visibility v ON v.campaign_id=c.id AND su.organization_unit_id=v.organization_unit_id) t ")
				.append("WHERE t.lastState IN ('ANV', 'NNS') ")
				.append("AND t.interviewer_start_date<?")
				.toString();
		return jdbcTemplate.queryForList(qString, new Object[] {System.currentTimeMillis()}, String.class);
	}
	
	@Override
	public List<String> getSurveyUnitForQNA() {
		String qString = new StringBuilder("SELECT t.id FROM ")
				.append("(SELECT su.id as id, v.collection_end_date, ")
				.append("(SELECT s.type FROM state s WHERE s.survey_unit_id=su.id ORDER BY s.date DESC LIMIT 1) as lastState ")
				.append("FROM survey_unit su ")
				.append("JOIN campaign c ON su.campaign_id=c.id ")
				.append("JOIN visibility v ON v.campaign_id=c.id AND su.organization_unit_id=v.organization_unit_id) t ")
				.append("WHERE t.collection_end_date<?")
				.toString();
		return jdbcTemplate.queryForList(qString, new Object[] {System.currentTimeMillis()}, String.class);
	}
	
	@Override
	public List<String> getSurveyUnitForNVA() {
		String qString = new StringBuilder("SELECT t.id FROM ")
				.append("(SELECT su.id as id, v.end_date, ")
				.append("(SELECT s.type FROM state s WHERE s.survey_unit_id=su.id ORDER BY s.date DESC LIMIT 1) as lastState ")
				.append("FROM survey_unit su ")
				.append("JOIN campaign c ON su.campaign_id=c.id ")
				.append("JOIN visibility v ON v.campaign_id=c.id AND su.organization_unit_id=v.organization_unit_id) t ")
				.append("WHERE t.lastState <> 'NVA' ")
				.append("AND t.end_date<?")
				.toString();
		return jdbcTemplate.queryForList(qString, new Object[] {System.currentTimeMillis()}, String.class);
	}
	

}
