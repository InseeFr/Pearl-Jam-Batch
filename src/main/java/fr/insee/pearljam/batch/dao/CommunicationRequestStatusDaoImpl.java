package fr.insee.pearljam.batch.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommunicationRequestStatusDaoImpl implements CommunicationRequestStatusDao {

    @Autowired
    @Qualifier("pilotageJdbcTemplate")
    JdbcTemplate pilotageJdbcTemplate;

    @Override
    public void addStatus(String communicationRequestId, String status, long timestamp) {
        String qString = """
                INSERT INTO communication_request_status
                (communication_request_id, status, "date")
                VALUES( ?, ?, ?)
                """;
        pilotageJdbcTemplate.update(qString, Long.parseLong(communicationRequestId), status, timestamp);
    }

    @Override
    public void deleteBySurveyUnitId(String surveyUnitId) {
        String deleteStatusSql = """
        DELETE FROM communication_request_status
        WHERE communication_request_id IN (
            SELECT id FROM communication_request WHERE survey_unit_id = ?
        )
    """;
        pilotageJdbcTemplate.update(deleteStatusSql, surveyUnitId);

    }


}
