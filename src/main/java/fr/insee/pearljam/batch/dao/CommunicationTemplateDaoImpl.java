package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationTemplateType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CommunicationTemplateDaoImpl implements CommunicationTemplateDao {

    @Autowired
    JdbcTemplate pilotageJdbcTemplate;

    @Override
    public List<CommunicationTemplateType> findByMeshuggahIds(Set<String> meshuggahIds) {
        String qString = "SELECT * FROM communication_template WHERE meshuggah_id IN (" +
            String.join(",", Collections.nCopies(meshuggahIds.size(), "?")) + ")";

        String[] meshuggahIdsArray = meshuggahIds.toArray(new String[0]);

        return pilotageJdbcTemplate.query(
            qString,
            meshuggahIdsArray,
            new CommunicationTemplateDaoImpl.CommunicationTemplateTypeMapper()
        );
    }

    @Override
    public List<CommunicationTemplateType> findByCampaign(String campaignId) {
        String qString = "SELECT * FROM communication_template WHERE campaign_id = ?";
        return pilotageJdbcTemplate.query(
            qString,
            new CommunicationTemplateDaoImpl.CommunicationTemplateTypeMapper(),
            campaignId
        );
    }

    /**
     * Implements the mapping between the result of the query and the ClosingCauseType entity
     *
     * @return CommunicationTemplateTypeMapper
     */
    private static final class CommunicationTemplateTypeMapper implements RowMapper<CommunicationTemplateType> {
        public CommunicationTemplateType mapRow(ResultSet rs, int rowNum) throws SQLException {
            CommunicationTemplateType comTemp = new CommunicationTemplateType();
            comTemp.setType(rs.getString("type"));
            comTemp.setMedium(rs.getString("medium"));
            comTemp.setMeshuggahId(rs.getString("meshuggah_id"));
            comTemp.setCampaignId(rs.getString("campaign_id"));
            return comTemp;
        }
    }

}
