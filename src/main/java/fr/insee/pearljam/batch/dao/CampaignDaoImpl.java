package fr.insee.pearljam.batch.dao;

import java.sql.SQLException;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.Campaign;

/**
 * Service for the Campaign entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
public class CampaignDaoImpl implements CampaignDao {
	
	@Autowired
	JdbcTemplate jdbcTemplate;
		
	@Override
	public boolean existCampaign(String id){
		String qString = "SELECT COUNT(id) FROM campaign WHERE id=?";
		Long nbRes = jdbcTemplate.queryForObject(qString, new Object[]{id}, Long.class);
		return nbRes>0;	
	}
	
	/**
	 * Implements the creation of a Campaign in database
	 * @param nomenclature
	 * @throws ParseException 
	 * @throws SQLException
	 */
	@Override
	public void createCampaign(Campaign campaign) {
		String qString = "INSERT INTO campaign (id, label) VALUES (?, ?)";
		
		jdbcTemplate.update(qString, campaign.getId().toUpperCase(), campaign.getLabel());
    }
	
	@Override
	public void deleteCampaign(Campaign campaign) {
		String qString ="DELETE FROM campaign WHERE id=?";
		jdbcTemplate.update(qString, campaign.getId());
	}
	
	public void updateCampaignById(Campaign campaign) {
		String qString ="UPDATE campaign SET label=? WHERE id=?";

		jdbcTemplate.update(qString, campaign.getLabel(), campaign.getId().toUpperCase());
	}
	
}