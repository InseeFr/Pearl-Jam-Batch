package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import fr.insee.pearljam.batch.campaign.OrganizationalUnitType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.dto.OrganizationUnitDto;

/**
 * Service for the  OrganizationalUnit entity that implements the interface associated
 *
 * @author scorcaud
 */
@Service
public class OrganizationalUnitTypeDaoImpl implements OrganizationalUnitTypeDao {

    @Autowired
    @Qualifier("pilotageJdbcTemplate")
    JdbcTemplate pilotageJdbcTemplate;

    @Override
    public boolean existOrganizationalUnit(String id) {
        String qString = "SELECT COUNT(id) FROM organization_unit WHERE id=?";
        Long nbRes = pilotageJdbcTemplate.queryForObject(qString, new Object[]{id}, Long.class);
        return nbRes > 0;
    }

    @Override
    public void createOrganizationalUnitFromDto(OrganizationUnitDto organizationalUnit) {
        String qString = "INSERT INTO public.organization_unit(id, label, type, organization_unit_parent_id) VALUES " +
                "(?, ?, ?, ?)";
        pilotageJdbcTemplate.update(qString, organizationalUnit.getCodeEtab(), organizationalUnit.getNomEtab(),
                "LOCAL", organizationalUnit.getOrigineEtab());
    }

    @Override
    public List<OrganizationalUnitType> findIdentificationStartDateByCampaignId(String campaignId) {
        String qString = """
        SELECT ou.id,
        v.identification_phase_start_date as identificationStartDate
        
        FROM visibility v
        LEFT JOIN organization_unit ou on v.organization_unit_id = ou.id AND v.campaign_id= ?
        
        WHERE v.campaign_id= ?
        """;
        return pilotageJdbcTemplate.query(qString, new OrganizationalUnitTypeDaoImpl.OrganizationalUnitTypeMapper(),campaignId, campaignId);
    }

    private static final class OrganizationalUnitTypeMapper implements RowMapper<OrganizationalUnitType> {
        public OrganizationalUnitType mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrganizationalUnitType organizationalUnit = new OrganizationalUnitType();
            organizationalUnit.setId(rs.getString("id"));
            organizationalUnit.setIdentificationPhaseStartDate(String.valueOf(rs.getLong("identificationStartDate")));
            return organizationalUnit;
        }
    }

}
