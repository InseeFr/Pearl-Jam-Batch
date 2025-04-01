package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationMetadataType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommunicationMetadataDaoImpl implements CommunicationMetadataDao {

	@Autowired
	@Qualifier("pilotageJdbcTemplate")
	JdbcTemplate pilotageJdbcTemplate;

	@Override
	public List<CommunicationMetadataType> findMetadataByCampaignIdAndMeshuggahIdAndSurveyUnitId(
			String campaignId, String meshuggahId, String surveyUnitId) {

		final String QUERY = "SELECT metadata_key, metadata_value, campaign_id, meshuggah_id "
				+ "FROM communication_metadata "
				+ "WHERE campaign_id = ? AND meshuggah_id = ? AND survey_unit_id = ?";

		return pilotageJdbcTemplate.query(QUERY, new Object[]{campaignId, meshuggahId, surveyUnitId},
				(rs, rowNum) -> {
					CommunicationMetadataType metadata = new CommunicationMetadataType();
					metadata.setKey(rs.getString("metadata_key"));
					metadata.setValue(rs.getString("metadata_value"));
					metadata.setCampaignId(rs.getString("campaign_id"));
					metadata.setMeshuggahId(rs.getString("meshuggah_id"));
					return metadata;
				});
	}


	@Override
	public void createAllMetadataForSurveyUnits(Map<String, List<CommunicationMetadataType>> metadataBySurveyUnit) {
		List<Object[]> batchArgs = new ArrayList<>();
		final String INSERT_METADATA_SQL = "INSERT INTO communication_metadata (survey_unit_id, metadata_key, metadata_value, campaign_id, meshuggah_id) VALUES (?, ?, ?, ?, ?)";

		for (Map.Entry<String, List<CommunicationMetadataType>> entry : metadataBySurveyUnit.entrySet()) {
			String surveyUnitId = entry.getKey();

			for (CommunicationMetadataType metadata : entry.getValue()) {

				batchArgs.add(new Object[]{
						surveyUnitId,
						metadata.getKey(),
						metadata.getValue(),
						metadata.getCampaignId(),
						metadata.getMeshuggahId()
				});
			}
		}

		// Exécution du batch insert
		pilotageJdbcTemplate.batchUpdate(INSERT_METADATA_SQL, batchArgs);
	}

}
