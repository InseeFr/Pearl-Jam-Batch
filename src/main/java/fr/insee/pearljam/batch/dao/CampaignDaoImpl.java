package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.Campaign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Service for the Campaign entity that implements the interface associated
 * 
 * @author scorcaud
 *
 */
@Service
public class CampaignDaoImpl implements CampaignDao {

	@Autowired
	@Qualifier("pilotageJdbcTemplate")
	JdbcTemplate pilotageJdbcTemplate;

	@Override
	public boolean existCampaign(String id) {
		String qString = "SELECT COUNT(id) FROM campaign WHERE id=?";
		Long nbRes = pilotageJdbcTemplate.queryForObject(qString, new Object[] { id }, Long.class);
		return nbRes > 0;
	}

	@Override
	public void deleteCampaign(Campaign campaign) {
		String qString = "DELETE FROM campaign WHERE id=?";
		pilotageJdbcTemplate.update(qString, campaign.getId());
	}

	/**
	 * Implements the mapping between the result of the query and the Campaign
	 * entity
	 * 
	 * @return CommentTypeMapper
	 */
	private static final class CampaignTypeMapper implements RowMapper<Campaign> {
		public Campaign mapRow(ResultSet rs, int rowNum) throws SQLException {
			Campaign c = new Campaign();
			c.setId(rs.getString("id"));
			c.setSensitivity(rs.getBoolean("sensitivity"));
			c.setLabel(rs.getString("label"));
			c.setIdentificationConfiguration(rs.getString("identification_configuration"));
			c.setOrganizationalUnits(null);
			c.setSurveyUnits(null);
			return c;
		}
	}

	@Override
	public Campaign findById(String campaignId) {
		String qString = "SELECT * FROM campaign where campaign.id =?";
		List<Campaign> campaigns = pilotageJdbcTemplate.query(qString, new Object[] { campaignId },
				new CampaignTypeMapper());
		if (!campaigns.isEmpty()) {
			return campaigns.getFirst();
		} else {
			return null;
		}
	}

}