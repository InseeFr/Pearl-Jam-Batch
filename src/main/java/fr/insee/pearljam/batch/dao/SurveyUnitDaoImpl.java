package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.SurveyUnitType;

/**
 * Service for the SurveyUnit entity that implements the interface associated
 *
 * @author scorcaud
 */
@Service
public class SurveyUnitDaoImpl implements SurveyUnitDao {

    @Autowired
    @Qualifier("pilotageJdbcTemplate")
    JdbcTemplate pilotageJdbcTemplate;

    @Override
    public boolean existSurveyUnit(String id) {
        String qString = "SELECT COUNT(id) FROM survey_unit WHERE id=?";
        Long nbRes = pilotageJdbcTemplate.queryForObject(qString,  Long.class,id);
        return nbRes > 0;
    }

    @Override
    public boolean existSurveyUnitForCampaign(String id, String campaignId) {
        String qString = "SELECT COUNT(id) FROM survey_unit WHERE id=? AND campaign_id<>?";
        Long nbRes = pilotageJdbcTemplate.queryForObject(qString, Long.class,id, campaignId);
        return nbRes > 0;
    }

    @Override
    public void updateSurveyUnitById(String campaignId, SurveyUnitType surveyUnit) {
        String qString = "UPDATE survey_unit SET priority=?, campaign_id=? WHERE id=?";
        pilotageJdbcTemplate.update(qString, surveyUnit.isPriority(), campaignId, surveyUnit.getId());
    }

    public void deleteSurveyUnitByCampaignId(String campaignId) {
        String qString = "DELETE FROM survey_unit WHERE campaign_id=?";
        pilotageJdbcTemplate.update(qString, campaignId);
    }

    @Override
    public void createSurveyUnit(String campaignId, SurveyUnitType surveyUnit, Long addressId,
                                 Long sampleIdentifierId, String interviewerId, String organizationUnitId) {
        String qString = "INSERT INTO survey_unit (id, priority, address_id, campaign_id, interviewer_id, sample_identifier_id, organization_unit_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        pilotageJdbcTemplate.update(qString, surveyUnit.getId(), surveyUnit.isPriority(),
                addressId, campaignId, interviewerId, sampleIdentifierId, organizationUnitId);
    }

    @Override
    public List<String> getAllSurveyUnitByCampaignId(String campaignId) {
        String qString = "SELECT id FROM survey_unit WHERE campaign_id=?";
        return pilotageJdbcTemplate.queryForList(qString, String.class, campaignId);
    }

    public SurveyUnitType getSurveyUnitById(String surveyUnitId) {
        String qString = "SELECT * FROM survey_unit WHERE id=?";
        return pilotageJdbcTemplate.queryForObject(qString, new SurveyUnitTypeMapper(), surveyUnitId);
    }

    public List<SurveyUnitType> getSurveyUnitsById(List<String> surveyUnitIds) {
        // prevent SQL syntax error if empty input
        if (surveyUnitIds.isEmpty()) return List.of();
        String placeholders = String.join(",", Collections.nCopies(surveyUnitIds.size(), "?"));
        String qString = String.format("SELECT * FROM survey_unit WHERE survey_unit.id in ( %s )", placeholders);
        return pilotageJdbcTemplate.query(qString, new SurveyUnitTypeMapper(), surveyUnitIds.toArray());
    }

    @Override
    public String getSurveyUnitInterviewerAffectation(String surveyUnitId) {
        String qString = "SELECT interviewer_id FROM survey_unit WHERE id=?";
        return pilotageJdbcTemplate.queryForObject(qString, String.class, surveyUnitId);
    }

    @Override
    public void setSurveyUnitInterviewerAffectation(String surveyUnitId, String idep) {
        String qString = "UPDATE survey_unit SET interviewer_id=?  WHERE id=?";
        pilotageJdbcTemplate.update(qString, idep, surveyUnitId);
    }

    @Override
    public String getSurveyUnitOrganizationUnitAffectation(String surveyUnitId) {
        String qString = "SELECT organization_unit_id FROM survey_unit WHERE id=?";
        return pilotageJdbcTemplate.queryForObject(qString, String.class, surveyUnitId);
    }

    @Override
    public void setSurveyUnitOrganizationUnitAffectation(String surveyUnitId, String organizationUnitId) {
        String qString = "UPDATE survey_unit SET organization_unit_id=?  WHERE id=?";
        pilotageJdbcTemplate.update(qString, organizationUnitId, surveyUnitId);
    }


    /**
     * Implements the mapping between the result of the query and the ReportingUnit entity
     *
     */
    private static final class SurveyUnitTypeMapper implements RowMapper<SurveyUnitType> {
        public SurveyUnitType mapRow(ResultSet rs, int rowNum) throws SQLException {
            SurveyUnitType su = new SurveyUnitType();
            su.setId(rs.getString("id"));
            su.setPriority(rs.getBoolean("priority"));
            su.setInterviewerId(rs.getString("interviewer_id"));
            su.setOrganizationalUnitId(rs.getString("organization_unit_id"));
            su.setCampaignId(rs.getString("campaign_id"));
            return su;
        }
    }

    public SurveyUnitType getSurveyUnitByIdTest(String surveyUnitId) {
        String qString = "SELECT su.* FROM survey_unit su INNER JOIN comment com on com.survey_unit_id = su.id WHERE id=?";
        return pilotageJdbcTemplate.queryForObject(qString, new SurveyUnitTypeMapper(), surveyUnitId);
    }


    public void deleteSurveyUnitById(String surveyUnitId) {
        String qString = "DELETE FROM survey_unit WHERE id=?";
        pilotageJdbcTemplate.update(qString, surveyUnitId);
    }

    public long getAddressIdBySurveyUnitId(String surveyUnitId) {
        String qString = "SELECT address_id FROM survey_unit WHERE id=?";
        return pilotageJdbcTemplate.queryForObject(qString, Long.class, surveyUnitId);
    }

    public long getSampleIdentifiersIdBySurveyUnitId(String surveyUnitId) {
        String qString = "SELECT sample_identifier_id FROM survey_unit WHERE id=?";
        return pilotageJdbcTemplate.queryForObject(qString, Long.class, surveyUnitId);
    }


    @Override
    public List<String> getSurveyUnitNVM(long instantDate) {
        String qString = """
                SELECT t.id FROM
                "(SELECT su.id as id, v.management_start_date,
                "(SELECT s.type FROM state s WHERE s.survey_unit_id=su.id ORDER BY s.date DESC LIMIT 1) as lastState
                "FROM survey_unit su
                "JOIN campaign c ON su.campaign_id=c.id
                "JOIN visibility v ON v.campaign_id=c.id AND su.organization_unit_id=v.organization_unit_id) t
                "WHERE t.lastState='NVM'
                "AND t.management_start_date<=?
                """;
        return pilotageJdbcTemplate.queryForList(qString, String.class, instantDate);
    }

    @Override
    public List<String> getSurveyUnitNNS(long instantDate) {
        String qString = """
                SELECT t.id FROM
                (SELECT su.id as id, v.management_start_date,
                (SELECT s.type FROM state s WHERE s.survey_unit_id=su.id ORDER BY s.date DESC LIMIT 1) as lastState
                FROM survey_unit su
                JOIN campaign c ON su.campaign_id=c.id
                JOIN visibility v ON v.campaign_id=c.id AND su.organization_unit_id=v.organization_unit_id) t
                WHERE t.lastState = 'NNS'
                AND t.management_start_date<?
                """;
        return pilotageJdbcTemplate.queryForList(qString, String.class, instantDate);
    }

    @Override
    public List<String> getSurveyUnitANV(long instantDate) {
        String qString = """
                SELECT t.id FROM
                (SELECT su.id as id, v.interviewer_start_date,
                (SELECT s.type FROM state s WHERE s.survey_unit_id=su.id ORDER BY s.date DESC LIMIT 1) as lastState
                FROM survey_unit su
                JOIN campaign c ON su.campaign_id=c.id
                JOIN visibility v ON v.campaign_id=c.id AND su.organization_unit_id=v.organization_unit_id) t
                WHERE t.lastState = 'ANV'
                AND t.interviewer_start_date<?
                """;
        return pilotageJdbcTemplate.queryForList(qString, String.class, instantDate);
    }

    @Override
    public List<String> getSurveyUnitForQNA(long instantDate) {
        String qString = """
                SELECT t.id FROM
                (SELECT su.id as id, v.collection_end_date,
                (SELECT s.type FROM state s WHERE s.survey_unit_id=su.id ORDER BY s.date DESC LIMIT 1) as lastState
                FROM survey_unit su
                JOIN campaign c ON su.campaign_id=c.id
                JOIN visibility v ON v.campaign_id=c.id AND su.organization_unit_id=v.organization_unit_id) t
                WHERE t.collection_end_date<?
                """;
        return pilotageJdbcTemplate.queryForList(qString, String.class, instantDate);
    }

    @Override
    public List<String> getSurveyUnitForNVA(long instantDate) {
        String qString = """
                SELECT t.id FROM
                (SELECT su.id as id, v.end_date,
                (SELECT s.type FROM state s WHERE s.survey_unit_id=su.id ORDER BY s.date DESC LIMIT 1) as lastState
                FROM survey_unit su
                JOIN campaign c ON su.campaign_id=c.id
                JOIN visibility v ON v.campaign_id=c.id AND su.organization_unit_id=v.organization_unit_id) t
                WHERE t.lastState <> 'NVA'
                AND t.end_date<?
                """;
        return pilotageJdbcTemplate.queryForList(qString, String.class, instantDate);
    }


}
