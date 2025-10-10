package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.BilanDeContactType;
import fr.insee.pearljam.batch.campaign.ContactPrecedentType;
import fr.insee.pearljam.batch.campaign.ContactsPrecedentsType;
import fr.insee.pearljam.batch.campaign.InformationCollectePrecedenteType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class ContactHistoryDaoImpl implements ContactHistoryDao {

    @Autowired
    @Qualifier("pilotageJdbcTemplate")
    JdbcTemplate pilotageJdbcTemplate;

    public void createContactHistory(InformationCollectePrecedenteType informationCollectePrecedente, String surveyUnitId) {
        String qString = """
                INSERT INTO contact_history (survey_unit_id, contact_history_type,contact_outcome_value, comment)
                VALUES (?, 'PREVIOUS',?,?)
                """;
        pilotageJdbcTemplate.update(qString, surveyUnitId, informationCollectePrecedente.getBilanDeContact().value(), informationCollectePrecedente.getCommentairePrecedent());

    }

    @Override
    public InformationCollectePrecedenteType findBySurveyUnitId(String surveyUnitId) {
        final String historySql = """
                SELECT contact_outcome_value, comment
                FROM contact_history
                WHERE survey_unit_id = ? AND contact_history_type = 'PREVIOUS'
                """;

        InformationCollectePrecedenteType info = pilotageJdbcTemplate.query(historySql,
                ps -> ps.setString(1, surveyUnitId),
                rs -> {
                    if (!rs.next()) return null;
                    InformationCollectePrecedenteType icp = new InformationCollectePrecedenteType();
                    icp.setBilanDeContact(BilanDeContactType.fromValue(rs.getString("contact_outcome_value")));
                    icp.setCommentairePrecedent(rs.getString("comment"));
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

        List<ContactPrecedentType> contacts = pilotageJdbcTemplate.query(
                peopleSql,
                ps -> ps.setString(1, surveyUnitId),
                new ContactPrecedentRowMapper()
        );

        if (!contacts.isEmpty()) {
            ContactsPrecedentsType wrapper = new ContactsPrecedentsType();
            wrapper.getContact().addAll(contacts);
            info.setContacts(wrapper);
        }

        return info;
    }

    private static final class ContactPrecedentRowMapper implements RowMapper<ContactPrecedentType> {
        @Override
        public ContactPrecedentType mapRow(ResultSet rs, int rowNum) throws SQLException {
            ContactPrecedentType contactPrecedent = new ContactPrecedentType();

            contactPrecedent.setCivilite(rs.getInt("title") == 0 ? "MISTER" : "MISS");
            contactPrecedent.setPrenom(rs.getString("first_name"));
            contactPrecedent.setPanel(rs.getBoolean("panel"));
            // getLong récupère un null il renvoie 0 => vérifier avec wasNull
            long birthDate = rs.getLong("birthdate");
            if (!rs.wasNull()) {
                contactPrecedent.setDateDeNaissance(new SimpleDateFormat("dd/MM/yyyy").format(Date.from(Instant.ofEpochMilli(birthDate))));
            }
            return contactPrecedent;
        }
    }

}
