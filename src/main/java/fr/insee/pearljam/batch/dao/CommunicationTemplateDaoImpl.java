package fr.insee.pearljam.batch.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.CommunicationTemplateType;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class CommunicationTemplateDaoImpl implements CommunicationTemplateDao {

    @Autowired
    JdbcTemplate pilotageJdbcTemplate;

    @Override
    public CommunicationTemplateType findById(Long id) {
        String qString = "SELECT * FROM communication_template where communication_template.id= ?";
        return pilotageJdbcTemplate.queryForObject(qString,
                new CommunicationTemplateDaoImpl.CommunicationTemplateTypeMapper(), id);
    }

    /**
     * Implements the mapping between the result of the query and the ClosingCauseType entity
     *
     * @return CommunicationTemplateTypeMapper
     */
    private static final class CommunicationTemplateTypeMapper implements RowMapper<CommunicationTemplateType> {
        public CommunicationTemplateType mapRow(ResultSet rs, int rowNum) throws SQLException {
            CommunicationTemplateType comTemp = new CommunicationTemplateType();
            comTemp.setId(rs.getLong("id"));
            comTemp.setType(rs.getString("type"));
            comTemp.setMedium(rs.getString("medium"));
            comTemp.setMeshuggahId(rs.getString("meshuggah_id"));
            comTemp.setCampaignId(rs.getString("campaign_id"));
            return comTemp;
        }
    }

}
