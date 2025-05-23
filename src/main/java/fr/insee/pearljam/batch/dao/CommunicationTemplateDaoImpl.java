package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationTemplateType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CommunicationTemplateDaoImpl implements CommunicationTemplateDao {

	@Autowired
	JdbcTemplate pilotageJdbcTemplate;

	@Override
	public List<CommunicationTemplateType> findByCampaign(String campaignId) {
		String qString = "SELECT * FROM communication_template WHERE campaign_id = ?";
		return pilotageJdbcTemplate.query(
				qString,
				new CommunicationTemplateDaoImpl.CommunicationTemplateTypeMapper(),
				campaignId
		);
	}

	@Override
	public void deleteByCampaignId(String campaignId) {
		String qString = "DELETE FROM communication_template WHERE campaign_id = ?";
		pilotageJdbcTemplate.update(qString, campaignId);
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
