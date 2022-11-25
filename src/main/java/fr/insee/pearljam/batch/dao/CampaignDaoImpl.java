package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.Campaign;

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

	/**
	 * Implements the creation of a Campaign in database
	 * 
	 * @param nomenclature
	 * @throws ParseException
	 * @throws SQLException
	 */
	@Override
	public void createCampaign(Campaign campaign) {
		String qString = "INSERT INTO campaign (id, label) VALUES (?, ?)";

		pilotageJdbcTemplate.update(qString, campaign.getId().toUpperCase(), campaign.getLabel());
	}

	@Override
	public void deleteCampaign(Campaign campaign) {
		String qString = "DELETE FROM campaign WHERE id=?";
		pilotageJdbcTemplate.update(qString, campaign.getId());
	}

	public void updateCampaignById(Campaign campaign) {
		String qString = "UPDATE campaign SET label=? , identification_configuration=? , contact_attempt_configuration=? , contact_outcome_configuration=? WHERE id=?";

		pilotageJdbcTemplate.update(qString, campaign.getLabel(), campaign.getId().toUpperCase());
	}

	@Override
	public List<Campaign> findAll() {
		String qString = "SELECT * FROM campaign";
		return pilotageJdbcTemplate.query(qString, new CampaignTypeMapper());
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
			c.setLabel(rs.getString("label"));
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
			return campaigns.get(0);
		} else {
			return null;
		}
	}

}