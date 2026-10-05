package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.Title;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactHistoryDaoImplTest {

    @Mock
    private JdbcTemplate pilotageJdbcTemplate;

    @InjectMocks
    private ContactHistoryDaoImpl contactHistoryDao;

    // Tests for deletePreviousContactTypeBySurveyUnitId
    private static final String EXPECTED_DELETE_SQL = "DELETE FROM contact_history WHERE survey_unit_id=? and contact_history_type='PREVIOUS'";

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
        contactHistoryDao.deletePreviousContactTypeBySurveyUnitId(surveyUnitId);
        verify(pilotageJdbcTemplate).update(EXPECTED_DELETE_SQL, surveyUnitId);
    }

    // Tests for ContactPrecedentRowMapper
    
    private static Stream<Arguments> provideMapperTestCases() {
        // Note: timestamp 946684800000L = January 1, 2000 00:00:00 UTC
        // timestamp 946684800000L formatted as dd/MM/yyyy in default timezone = 01/01/2000
        return Stream.of(
                // title, wasNullTitle, firstName, panel, birthdate, wasNullBirthdate, expectedTitle, expectedFirstName, expectedPanel, expectedBirthdate
                Arguments.of(0, false, "John", true, 946684800000L, false, Title.MISTER, "John", true, "01/01/2000"),
                Arguments.of(1, false, "Jane", false, 946684800000L, false, Title.MISS, "Jane", false, "01/01/2000"),
                Arguments.of(2, false, "Alex", null, null, true, Title.UNDEFINED, "Alex", null, null),
                Arguments.of(null, true, "NoTitle", true, null, true, null, "NoTitle", true, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideMapperTestCases")
    void testContactPrecedentRowMapper_MapRow(Integer title, boolean wasNullTitle, String firstName, 
            Boolean panel, Long birthdate, boolean wasNullBirthdate,
            Title expectedTitle, String expectedFirstName, Boolean expectedPanel, 
            String expectedBirthdate) throws SQLException {
        
        // Arrange - mock ResultSet with proper sequencing
        ResultSet mockRs = mock(ResultSet.class);
        
        when(mockRs.getInt("title")).thenReturn(title != null ? title : 0);
        when(mockRs.wasNull()).thenReturn(wasNullTitle, wasNullBirthdate);
        when(mockRs.getString("first_name")).thenReturn(firstName);
        when(mockRs.getObject("panel", Boolean.class)).thenReturn(panel);
        when(mockRs.getLong("birthdate")).thenReturn(birthdate != null ? birthdate : 0L);
        
        // Act - call the mapper directly
        ContactHistoryDaoImpl.ContactPrecedentRowMapper mapper = new ContactHistoryDaoImpl.ContactPrecedentRowMapper();
        fr.insee.pearljam.batch.campaign.PreviousContactType result = mapper.mapRow(mockRs, 1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(expectedFirstName);
        assertThat(result.isPanel()).isEqualTo(expectedPanel);
        assertThat(result.getTitle()).isEqualTo(expectedTitle);
        assertThat(result.getDateOfBirth()).isEqualTo(expectedBirthdate);
    }

    @Test
    void testContactPrecedentRowMapper_MapRowWithAllNullValues() throws SQLException {
        // Arrange
        ResultSet mockRs = mock(ResultSet.class);
        
        when(mockRs.getInt("title")).thenReturn(0);
        when(mockRs.wasNull()).thenReturn(true, true); // title is null, birthdate is null
        when(mockRs.getString("first_name")).thenReturn(null);
        when(mockRs.getObject("panel", Boolean.class)).thenReturn(null);
        when(mockRs.getLong("birthdate")).thenReturn(0L);
        
        // Act
        ContactHistoryDaoImpl.ContactPrecedentRowMapper mapper = new ContactHistoryDaoImpl.ContactPrecedentRowMapper();
        fr.insee.pearljam.batch.campaign.PreviousContactType result = mapper.mapRow(mockRs, 1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isNull();
        assertThat(result.isPanel()).isNull();
        assertThat(result.getTitle()).isNull();
        assertThat(result.getDateOfBirth()).isNull();
    }
}
