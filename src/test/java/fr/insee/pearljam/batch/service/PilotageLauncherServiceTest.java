package fr.insee.pearljam.batch.service;

import fr.insee.pearljam.batch.config.ApplicationConfig;
import fr.insee.pearljam.batch.dao.CommunicationTemplateDaoImpl;
import fr.insee.pearljam.batch.dao.OrganizationalUnitTypeDao;
import fr.insee.pearljam.batch.dao.SurveyUnitDao;
import fr.insee.pearljam.batch.sampleprocessing.Campagne.Questionnaires.Questionnaire;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class PilotageLauncherServiceTest {

    private final SurveyUnitDao surveyUnitDao = mock(SurveyUnitDao.class);
    private final OrganizationalUnitTypeDao organizationalUnitTypeDao = mock(OrganizationalUnitTypeDao.class);

    @Test
    void shouldNotFetchOrganizationalUnitsForEmptyInterrogationIds() {
        PilotageLauncherService service = serviceWithIdentificationPhaseCheck(true);

        Map<String, String> result = service.fetchInterrogationOuIds(Set.of());

        assertEquals(Map.of(), result);
        verifyNoInteractions(surveyUnitDao);
    }

    @Test
    void shouldNotFilterWhenIdentificationPhaseCheckedNotEnabled() {
        PilotageLauncherService service = serviceWithIdentificationPhaseCheck(false);
        Questionnaire quest1 = new Questionnaire();

        List<Questionnaire> result = service.filterEligibleQuestionnaires(List.of(quest1), "campaign-id");

        assertEquals(List.of(quest1), result);
        verifyNoInteractions(surveyUnitDao);
        verifyNoInteractions(organizationalUnitTypeDao);
    }

    private PilotageLauncherService serviceWithIdentificationPhaseCheck(boolean enabled) {
        return new PilotageLauncherService(
                mock(PilotageFolderService.class),
                mock(CommunicationTemplateDaoImpl.class),
                mock(ApplicationConfig.class),
                mock(DataCollectionService.class),
                mock(CampaignService.class),
                surveyUnitDao,
                organizationalUnitTypeDao,
                100,
                enabled
        );
    }
}
