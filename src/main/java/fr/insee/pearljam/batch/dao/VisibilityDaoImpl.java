package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.insee.pearljam.batch.Constants;
import fr.insee.pearljam.batch.campaign.Campaign;
import fr.insee.pearljam.batch.campaign.OrganizationalUnitType;
import fr.insee.pearljam.batch.exception.BatchException;

/**
 * Service for the State entity that implements the interface associated
 *
 * @author scorcaud
 */
@Service
public class VisibilityDaoImpl implements VisibilityDao {

    @Autowired
    @Qualifier("pilotageJdbcTemplate")
    JdbcTemplate pilotageJdbcTemplate;

    private static final Logger logger = LogManager.getLogger(VisibilityDaoImpl.class);

    @Override
    public boolean existVisibility(String campaignId, String organizationalUnitId) {
        String qString = "SELECT COUNT(*) FROM visibility WHERE campaign_id=? AND organization_unit_id=?";
        Long nbRes = pilotageJdbcTemplate.queryForObject(qString,Long.class,campaignId, organizationalUnitId );
        return nbRes > 0;
    }

    public List<OrganizationalUnitType> getAllVisibilitiesByCampaignId(String campaignId) {
        String qString = "SELECT * FROM visibility WHERE campaign_id=?";
        return pilotageJdbcTemplate.query(qString, new OrganizationalUnitTypeMapper(), campaignId);

    }

    @Override
    public OrganizationalUnitType getVisibilityByCampaignIdAndOrganizationUnitId(String campaignId, String organizationUnitId) {

        String qString = "SELECT * FROM visibility WHERE campaign_id=? AND organization_unit_id=?";
        return pilotageJdbcTemplate.queryForObject(qString, new OrganizationalUnitTypeMapper(), campaignId, organizationUnitId);
    }

    private static final class OrganizationalUnitTypeMapper implements RowMapper<OrganizationalUnitType> {
        public OrganizationalUnitType mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrganizationalUnitType ou = new OrganizationalUnitType();
            ou.setCollectionStartDate(String.valueOf(rs.getLong("collection_start_date")));
            ou.setCollectionEndDate(String.valueOf(rs.getLong("collection_end_date")));
            ou.setMailCourrier(rs.getString("mail"));
            ou.setTelephoneCourrier(rs.getString("tel"));
            ou.setMailCourrier(rs.getString("mail"));
            ou.setTelephoneCourrier(rs.getString("tel"));
            return ou;
        }
    }

    public void createVisibility(Campaign campaign, OrganizationalUnitType organizationalUnitType) {
        String qString = """
                INSERT INTO visibility (campaign_id, organization_unit_id, collection_end_date,
                collection_start_date, end_date, identification_phase_start_date,
                interviewer_start_date, management_start_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Long collectionStartDate;
        Long collectionEndDate;
        Long endDate;
        Long identificationPhaseStartDate;
        Long interviewerStartDate;
        Long managementStartDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            collectionStartDate = sdf.parse(organizationalUnitType.getCollectionStartDate()).getTime();
            collectionEndDate = sdf.parse(organizationalUnitType.getCollectionEndDate()).getTime();
            endDate = sdf.parse(organizationalUnitType.getEndDate()).getTime();
            identificationPhaseStartDate = sdf.parse(organizationalUnitType.getIdentificationPhaseStartDate()).getTime();
            interviewerStartDate = sdf.parse(organizationalUnitType.getInterviewerStartDate()).getTime();
            managementStartDate = sdf.parse(organizationalUnitType.getManagementStartDate()).getTime();
        } catch (ParseException e) {
            logger.log(Level.ERROR, e.getMessage());
            collectionStartDate = null;
            collectionEndDate = null;
            endDate = null;
            identificationPhaseStartDate = null;
            interviewerStartDate = null;
            managementStartDate = null;
        }
        pilotageJdbcTemplate.update(qString, campaign.getId().toUpperCase(), organizationalUnitType.getId(), collectionEndDate,
                collectionStartDate, endDate, identificationPhaseStartDate, interviewerStartDate, managementStartDate);
    }

    public void updateDateVisibilityByCampaignIdAndOrganizationalUnitId(Campaign campaign, OrganizationalUnitType organizationalUnitType) throws BatchException {
        String qString = """
                UPDATE visibility SET collection_end_date=?, collection_start_date=?,
                end_date=?, identification_phase_start_date=?, interviewer_start_date=?, management_start_date=?
                WHERE campaign_id=? and organization_unit_id=?
                """;
        long collectionStartDate;
        long collectionEndDate;
        long endDate;
        long identificationPhaseStartDate;
        long interviewerStartDate;
        long managementStartDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            collectionStartDate = sdf.parse(organizationalUnitType.getCollectionStartDate()).getTime();
            collectionEndDate = sdf.parse(organizationalUnitType.getCollectionEndDate()).getTime();
            endDate = sdf.parse(organizationalUnitType.getEndDate()).getTime();
            identificationPhaseStartDate = sdf.parse(organizationalUnitType.getIdentificationPhaseStartDate()).getTime();
            interviewerStartDate = sdf.parse(organizationalUnitType.getInterviewerStartDate()).getTime();
            managementStartDate = sdf.parse(organizationalUnitType.getManagementStartDate()).getTime();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
            throw new BatchException("Error during update of the visibility for campaign " + campaign.getId() + " : " + e.getMessage());
        }
        pilotageJdbcTemplate.update(qString, collectionEndDate, collectionStartDate, endDate, identificationPhaseStartDate,
                interviewerStartDate, managementStartDate, campaign.getId().toUpperCase(), organizationalUnitType.getId());
    }

    @Transactional
    public void updateVisibilityByCampaignIdAndOrganizationalUnitId(Campaign campaign, OrganizationalUnitType organizationalUnitType) throws BatchException {
        String qString = """
                UPDATE visibility SET organization_unit_id=?, collection_end_date=?, collection_start_date=?,
                end_date=?, identification_phase_start_date=?, interviewer_start_date=?, management_start_date=?
                WHERE campaign_id=?
                """;
        long collectionStartDate;
        long collectionEndDate;
        long endDate;
        long identificationPhaseStartDate;
        long interviewerStartDate;
        long managementStartDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            collectionStartDate = sdf.parse(organizationalUnitType.getCollectionStartDate()).getTime();
            collectionEndDate = sdf.parse(organizationalUnitType.getCollectionEndDate()).getTime();
            endDate = sdf.parse(organizationalUnitType.getEndDate()).getTime();
            identificationPhaseStartDate = sdf.parse(organizationalUnitType.getIdentificationPhaseStartDate()).getTime();
            interviewerStartDate = sdf.parse(organizationalUnitType.getInterviewerStartDate()).getTime();
            managementStartDate = sdf.parse(organizationalUnitType.getManagementStartDate()).getTime();
        } catch (Exception e) {
            logger.error( e.getMessage(),e);
            throw new BatchException("Error during update of the visibility for campaign " + campaign.getId() + " : " + e.getMessage());
        }
        pilotageJdbcTemplate.update(qString, organizationalUnitType.getId(), collectionEndDate, collectionStartDate, endDate, identificationPhaseStartDate,
                interviewerStartDate, managementStartDate, campaign.getId());
    }

    public void deleteVisibilityByCampaignId(String campaignId) {
        String qString = "DELETE FROM visibility WHERE campaign_id=?";
        pilotageJdbcTemplate.update(qString, campaignId);
    }


}
