package fr.insee.pearljam.batch.dao;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContactHistoryDaoImplTest {

    @Mock
    private JdbcTemplate pilotageJdbcTemplate;

    @InjectMocks
    private ContactHistoryDaoImpl contactHistoryDao;

    private static final String EXPECTED_SQL = "DELETE FROM contact_history WHERE survey_unit_id=? and contact_history_type='PREVIOUS'";

    private static Stream<String> provideSurveyUnitIds() {
        return Stream.of(
                "SIM1234",
                "DIFFERENT_ID",
                "",
                "SIM-1234-TEST"
        );
    }

    @ParameterizedTest
    @MethodSource("provideSurveyUnitIds")
    void testDeletePreviousContactTypeBySurveyUnitId(String surveyUnitId) {
        // Call the method
        contactHistoryDao.deletePreviousContactTypeBySurveyUnitId(surveyUnitId);

        // Verify that JdbcTemplate.update was called with the correct SQL and parameter
        verify(pilotageJdbcTemplate).update(EXPECTED_SQL, surveyUnitId);
    }
}
