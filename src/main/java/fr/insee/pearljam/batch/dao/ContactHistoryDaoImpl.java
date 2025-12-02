package fr.insee.pearljam.batch.dao;


import fr.insee.pearljam.batch.campaign.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactHistoryDaoImpl implements ContactHistoryDao {

    private final JdbcTemplate pilotageJdbcTemplate;

    public void createContactHistory(PreviousCollectionInformationType informationCollectePrecedente, String surveyUnitId) {
        String qString = """
                INSERT INTO contact_history (survey_unit_id, contact_history_type,contact_outcome_value, comment)
                VALUES (?, 'PREVIOUS',?,?)
                """;
        pilotageJdbcTemplate.update(qString, surveyUnitId, informationCollectePrecedente.getContactOutcome().value(), informationCollectePrecedente.getPreviousComment());

    }

    @Override
    public PreviousCollectionInformationType findBySurveyUnitId(String surveyUnitId) {
        final String historySql = """
                SELECT contact_outcome_value, comment
                FROM contact_history
                WHERE survey_unit_id = ? AND contact_history_type = 'PREVIOUS'
                """;

        PreviousCollectionInformationType info = pilotageJdbcTemplate.query(historySql,
                ps -> ps.setString(1, surveyUnitId),
                rs -> {
                    if (!rs.next()) return null;
                    PreviousCollectionInformationType icp = new PreviousCollectionInformationType();
                    icp.setContactOutcome(PreviousContactOutcomeType.fromValue(rs.getString("contact_outcome_value")));
                    icp.setPreviousComment(rs.getString("comment"));
                    return icp;
                }
        );
        if (info == null) {
            // Nothing recorded for PREVIOUS
            return null;
        }

        final String peopleSql = """
                SELECT first_name, panel, title, birthdate
                FROM person
                WHERE survey_unit_id = ? AND contact_history_type = 'PREVIOUS'
                """;

        List<PreviousContactType> contacts = pilotageJdbcTemplate.query(
                peopleSql,
                ps -> ps.setString(1, surveyUnitId),
                new ContactPrecedentRowMapper()
        );

        if (!contacts.isEmpty()) {
            PreviousContactsType wrapper = new PreviousContactsType();
            wrapper.getContact().addAll(contacts);
            info.setContacts(wrapper);
        }

        return info;
    }

    @Override
    public void deleteBySurveyUnitId(String surveyUnitId) {
        String qString = "DELETE FROM contact_history WHERE survey_unit_id=?";
        pilotageJdbcTemplate.update(qString, surveyUnitId);
    }

    @Override
    public void deletePreviousContactTypeBySurveyUnitId(String surveyUnitId) {
        String qString = "DELETE FROM contact_history WHERE survey_unit_id=? and contact_history_type='PREVIOUS'";
        pilotageJdbcTemplate.update(qString, surveyUnitId);
    }

    private static final class ContactPrecedentRowMapper implements RowMapper<PreviousContactType> {
        @Override
        public PreviousContactType mapRow(ResultSet rs, int rowNum) throws SQLException {
            PreviousContactType contactPrecedent = new PreviousContactType();

            int title = rs.getInt("title");
            if (!rs.wasNull()) {
                contactPrecedent.setTitle(title == 0 ? Title.MISTER : Title.MISS);
            }
            contactPrecedent.setFirstName(rs.getString("first_name"));
            contactPrecedent.setPanel(rs.getObject("panel", Boolean.class));
            // getLong on null returns 0 => handle this case with wasNull
            long birthDate = rs.getLong("birthdate");
            if (!rs.wasNull()) {
                contactPrecedent.setDateOfBirth(new SimpleDateFormat("dd/MM/yyyy").format(Date.from(Instant.ofEpochMilli(birthDate))));
            }
            return contactPrecedent;
        }
    }

}
