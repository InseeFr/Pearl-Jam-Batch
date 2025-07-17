package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.OrganizationalUnitType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

    public void deleteVisibilityByCampaignId(String campaignId) {
        String qString = "DELETE FROM visibility WHERE campaign_id=?";
        pilotageJdbcTemplate.update(qString, campaignId);
    }

}
