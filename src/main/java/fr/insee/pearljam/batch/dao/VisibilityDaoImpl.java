package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.insee.pearljam.batch.Constants;
import fr.insee.pearljam.batch.campaign.Campaign;
import fr.insee.pearljam.batch.campaign.OrganizationalUnitType;
import fr.insee.pearljam.batch.exception.BatchException;

/**
 * Service for the State entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
public class VisibilityDaoImpl implements VisibilityDao {

	@Autowired 
	JdbcTemplate jdbcTemplate;
		
	private static final Logger logger = LogManager.getLogger(VisibilityDaoImpl.class);
	
	@Override
	public boolean existVisibility(String campaignId, String organizationalUnitId){
		String qString = "SELECT COUNT(*) FROM visibility WHERE campaign_id=? AND organization_unit_id=?";
		Long nbRes = jdbcTemplate.queryForObject(qString, new Object[]{campaignId, organizationalUnitId}, Long.class);
		return nbRes>0;	
	}
	
	public List<OrganizationalUnitType> getAllVisibilitiesByCampaignId(String campaignId) {
		String qString = "SELECT * FROM visibility WHERE campaign_id=?";
		return jdbcTemplate.query(qString, new Object[] {campaignId}, new OrganizationalUnitTypeMapper());
		
	}
	
	private static final class OrganizationalUnitTypeMapper implements RowMapper<OrganizationalUnitType> {
        public OrganizationalUnitType mapRow(ResultSet rs, int rowNum) throws SQLException         {
        	OrganizationalUnitType ou = new OrganizationalUnitType();
        	ou.setCollectionStartDate(String.valueOf(rs.getLong("collection_start_date")));
        	ou.setCollectionEndDate(String.valueOf(rs.getLong("collection_end_date")));
            return ou;
        }
    }
	
	public void createVisibility(Campaign campaign, OrganizationalUnitType organizationalUnitType) {
		String qString = new StringBuilder("INSERT INTO visibility (campaign_id, organization_unit_id, collection_end_date, ")
				.append("collection_start_date, end_date, identification_phase_start_date, ")
				.append("interviewer_start_date, management_start_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
				.toString();
		Long collectionStartDate;
		Long collectionEndDate;
		Long endDate;
		Long identificationPhaseStartDate;
		Long interviewerStartDate;
		Long managementStartDate;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
			collectionStartDate = sdf.parse(organizationalUnitType.getCollectionStartDate()).getTime();
			collectionEndDate = sdf.parse(organizationalUnitType.getCollectionEndDate()).getTime();
			endDate = sdf.parse(organizationalUnitType.getEndDate()).getTime();
			identificationPhaseStartDate = sdf.parse(organizationalUnitType.getIdentificationPhaseStartDate()).getTime();
			interviewerStartDate = sdf.parse(organizationalUnitType.getInterviewerStartDate()).getTime();
			managementStartDate = sdf.parse(organizationalUnitType.getManagementStartDate()).getTime();
		}catch (ParseException e) {
			logger.log(Level.ERROR, e.getMessage());
			collectionStartDate =null;
			collectionEndDate = null;
			endDate = null;
			identificationPhaseStartDate = null;
			interviewerStartDate = null;
			managementStartDate = null;
		}
	    jdbcTemplate.update(qString, campaign.getId().toUpperCase(), organizationalUnitType.getId(), collectionEndDate, 
	    		collectionStartDate, endDate, identificationPhaseStartDate, interviewerStartDate, managementStartDate);
	}
	
	public void updateDateVisibilityByCampaignIdAndOrganizationalUnitId(Campaign campaign, OrganizationalUnitType organizationalUnitType) throws BatchException{
		String qString =new StringBuilder("UPDATE visibility SET collection_end_date=?, collection_start_date=?, ")
				.append("end_date=?, identification_phase_start_date=?, interviewer_start_date=?, management_start_date=? ")
				.append("WHERE campaign_id=? and organization_unit_id=?")
				.toString();
		Long collectionStartDate;
		Long collectionEndDate;
		Long endDate;
		Long identificationPhaseStartDate;
		Long interviewerStartDate;
		Long managementStartDate;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
			collectionStartDate = sdf.parse(organizationalUnitType.getCollectionStartDate()).getTime();
			collectionEndDate = sdf.parse(organizationalUnitType.getCollectionEndDate()).getTime();
			endDate = sdf.parse(organizationalUnitType.getEndDate()).getTime();
			identificationPhaseStartDate = sdf.parse(organizationalUnitType.getIdentificationPhaseStartDate()).getTime();
			interviewerStartDate = sdf.parse(organizationalUnitType.getInterviewerStartDate()).getTime();
			managementStartDate = sdf.parse(organizationalUnitType.getManagementStartDate()).getTime();
		}catch (Exception e) {
			logger.log(Level.ERROR, e.getMessage());
			throw new BatchException("Error during update of the visibility for campaign "+campaign.getId()+" : "+e.getMessage());
		}
		jdbcTemplate.update(qString, collectionEndDate, collectionStartDate, endDate, identificationPhaseStartDate, 
				interviewerStartDate, managementStartDate, campaign.getId().toUpperCase(), organizationalUnitType.getId());
	}
	
	@Transactional
	public void updateVisibilityByCampaignIdAndOrganizationalUnitId(Campaign campaign, OrganizationalUnitType organizationalUnitType) throws BatchException{
		String qString = new StringBuilder("UPDATE visibility SET organization_unit_id=?, collection_end_date=?, collection_start_date=?, ")
				.append("end_date=?, identification_phase_start_date=?, interviewer_start_date=?, management_start_date=? ")
				.append("WHERE campaign_id=? ")
				.toString();
		Long collectionStartDate;
		Long collectionEndDate;
		Long endDate;
		Long identificationPhaseStartDate;
		Long interviewerStartDate;
		Long managementStartDate;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
			collectionStartDate = sdf.parse(organizationalUnitType.getCollectionStartDate()).getTime();
			collectionEndDate = sdf.parse(organizationalUnitType.getCollectionEndDate()).getTime();
			endDate = sdf.parse(organizationalUnitType.getEndDate()).getTime();
			identificationPhaseStartDate = sdf.parse(organizationalUnitType.getIdentificationPhaseStartDate()).getTime();
			interviewerStartDate = sdf.parse(organizationalUnitType.getInterviewerStartDate()).getTime();
			managementStartDate = sdf.parse(organizationalUnitType.getManagementStartDate()).getTime();
		}catch (Exception e) {
			logger.log(Level.ERROR, e.getMessage());
			throw new BatchException("Error during update of the visibility for campaign "+campaign.getId()+" : "+e.getMessage());
		}
		jdbcTemplate.update(qString, organizationalUnitType.getId(), collectionEndDate, collectionStartDate, endDate, identificationPhaseStartDate, 
				interviewerStartDate, managementStartDate, campaign.getId());
	}
	
	public void deleteVisibilityByCampaignId(String campaignId) {
		String qString = "DELETE FROM visibility WHERE campaign_id=?";
		jdbcTemplate.update(qString, campaignId);
	}
}
