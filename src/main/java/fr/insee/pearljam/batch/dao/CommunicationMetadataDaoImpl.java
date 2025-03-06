package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationMetadataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommunicationMetadataDaoImpl implements CommunicationMetadataDao{

  @Autowired
  @Qualifier("pilotageJdbcTemplate")
  JdbcTemplate pilotageJdbcTemplate;

  @Override
  public void createMetadata(CommunicationMetadataType metadata) {
    String qString = "INSERT INTO communication_metadata (id, survey_unit_id, metadata_key, metadata_value) VALUES (?, ?, ?, ?)";

    pilotageJdbcTemplate.update(qString, metadata.getId(), metadata.getSurveyUnitId(), metadata.getKey(), metadata.getValue());
  }


  @Override
  public void updateMetadata(CommunicationMetadataType metadata) {
    // TODO document why this method is empty
  }
}
