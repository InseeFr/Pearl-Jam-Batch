package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationRequestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class CommunicationRequestDaoImpl implements CommunicationRequestDao{

    @Autowired
    @Qualifier("pilotageJdbcTemplate")
    JdbcTemplate pilotageJdbcTemplate;

    @Override
    public List<CommunicationRequestType> findAll() {
        String qString = """
                SELECT DISTINCT ON (cr.id)
                cr.id, cr.emitter, cr.reason, cr.communication_template_id, cr.survey_unit_id,
                crs.status, crs.date
                FROM communication_request cr
                LEFT JOIN communication_request_status crs
                ON cr.id = crs.communication_request_id
                ORDER BY cr.id, crs.date DESC""";
        return pilotageJdbcTemplate.query(qString, new CommunicationRequestDaoImpl.CommunicationRequestTypeMapper());
    }

    @Override
    public void delete(String communicationRequestId) {

    }


    private static final class CommunicationRequestTypeMapper implements RowMapper<CommunicationRequestType> {
        public CommunicationRequestType mapRow(ResultSet rs, int rowNum) throws SQLException {
            CommunicationRequestType commRequest = new CommunicationRequestType();
            commRequest.setId(rs.getString("id"));
            commRequest.setReason(rs.getString("reason"));
            commRequest.setCommunicationTemplateId(rs.getLong("communication_template_id"));
            commRequest.setStatus(rs.getString("status"));
            commRequest.setEmitter(rs.getString("emitter"));
            commRequest.setDate(rs.getLong("date"));
            commRequest.setSurveyUnitId(rs.getString("survey_unit_id"));
            return commRequest;
        }
    }

}
