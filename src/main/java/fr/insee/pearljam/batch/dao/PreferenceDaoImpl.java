package fr.insee.pearljam.batch.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for the Preference entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
public class PreferenceDaoImpl implements PreferenceDao {
	
	@Autowired
	@Qualifier("pilotageJdbcTemplate")
	JdbcTemplate pilotageJdbcTemplate;
	
	public void deletePreferenceByCampaignId(String campaignId) {
		String qString = "DELETE FROM preference WHERE id_campaign=?";
		pilotageJdbcTemplate.update(qString, campaignId);
	}
}
