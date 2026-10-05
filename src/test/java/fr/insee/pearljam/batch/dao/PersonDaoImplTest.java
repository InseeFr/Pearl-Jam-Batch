package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.PersonType;
import fr.insee.pearljam.batch.utils.DBResetHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map.Entry;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PersonDaoImplTest {

    @Autowired
    private PersonDaoImpl personDao;

    @Autowired
    private DBResetHelper dbResetHelper;

    @BeforeEach
    void setUp() throws Exception {
        dbResetHelper.reinitData();
    }

    @Test
    void testGetPersonsBySurveyUnitId() {
        List<Entry<Long, PersonType>> persons =
                personDao.getPersonsBySurveyUnitId("11");

        assertThat(persons).hasSize(1);

        assertThat(persons)
                .extracting(entry -> entry.getValue().getLastName())
                .containsExactlyInAnyOrder("Farmer");

        assertThat(persons)
                .allSatisfy(entry ->
                        assertThat(entry.getValue().getContactHistoryType()).isNull()
                );
    }

    @Test
    void testGetPersonsIncludingHistoryBySurveyUnitId() {
        List<Entry<Long, PersonType>> persons =
                personDao.getPersonsIncludingHistoryBySurveyUnitId("11");

        assertThat(persons).hasSize(3);

        assertThat(persons)
                .extracting(entry -> entry.getValue().getLastName())
                .containsExactlyInAnyOrder("Farmer", "Next", "Previous");

        assertThat(persons)
                .allSatisfy(entry ->
                        assertThat(entry.getValue().getContactHistoryType())
                                .isIn(null, "PREVIOUS", "NEXT")
                );
    }
}