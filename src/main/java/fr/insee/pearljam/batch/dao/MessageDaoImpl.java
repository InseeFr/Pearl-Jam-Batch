package fr.insee.pearljam.batch.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageDaoImpl implements MessageDao{
	
	@Autowired
	@Qualifier("pilotageJdbcTemplate")
	JdbcTemplate pilotageJdbcTemplate;
	
	@Override
	public void deleteByCampaign(String campaignId) {
		String qString = "DELETE FROM campaign_message_recipient WHERE campaign_id=?";
		pilotageJdbcTemplate.update(qString, campaignId);
	}
	
	@Override
	public void deleteById(Long id) {
		String qString = "DELETE FROM message WHERE id=?";
		pilotageJdbcTemplate.update(qString, id);
	}
	
	@Override
	public void deleteCampaignMessageById(Long id) {
		String qString = "DELETE FROM campaign_message_recipient WHERE message_id=?";
		pilotageJdbcTemplate.update(qString, id);
	}
	
	
	@Override
	public void deleteOuMessageById(Long id) {
		String qString = "DELETE FROM oumessage_recipient WHERE message_id=?";
		pilotageJdbcTemplate.update(qString, id);
	}
	
	@Override
	public void deleteStatusMessageById(Long id) {
		String qString = "DELETE FROM message_status WHERE message_id=?";
		pilotageJdbcTemplate.update(qString, id);
	}
	
	@Override
	public List<Long> getIdsToDelete(Long passedDate){
		String qString = "SELECT mes.id FROM message mes WHERE mes.date < ?";
		return pilotageJdbcTemplate.queryForList(qString, Long.class, passedDate);
	}
	
	@Override
	public boolean isIdPresentForCampaignId(String id) {
		String qString = "SELECT COUNT(*) FROM campaign_message_recipient WHERE campaign_id=?";
		Long nbRes = pilotageJdbcTemplate.queryForObject(qString, Long.class, id);
		return nbRes>0;	
	}

}
