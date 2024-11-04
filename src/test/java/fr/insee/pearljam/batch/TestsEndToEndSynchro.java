package fr.insee.pearljam.batch;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.pearljam.batch.config.ApplicationContext;
import fr.insee.pearljam.batch.dao.ClosingCauseDao;
import fr.insee.pearljam.batch.dao.InterviewerTypeDao;
import fr.insee.pearljam.batch.dao.OrganizationalUnitTypeDao;
import fr.insee.pearljam.batch.dao.SurveyUnitDao;
import fr.insee.pearljam.batch.dto.InterviewerAffectationsDto;
import fr.insee.pearljam.batch.dto.InterviewerDto;
import fr.insee.pearljam.batch.dto.InterviewersAffectationsResponseDto;
import fr.insee.pearljam.batch.dto.InterviewersResponseDto;
import fr.insee.pearljam.batch.dto.KeycloakResponseDto;
import fr.insee.pearljam.batch.dto.OrganizationUnitAffectationsDto;
import fr.insee.pearljam.batch.dto.OrganizationUnitDto;
import fr.insee.pearljam.batch.dto.OrganizationUnitsAffectationsResponseDto;
import fr.insee.pearljam.batch.dto.OrganizationUnitsResponseDto;
import fr.insee.pearljam.batch.dto.SimpleIdDto;
import fr.insee.pearljam.batch.service.TriggerService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.PathUtils;

@Disabled
class TestsEndToEndSynchro {

	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContext.class);

	TriggerService triggerService = context.getBean(TriggerService.class);

	private RestTemplate restTemplate = context.getBean(RestTemplate.class);

	private String keycloakTokenUrl = (String) context.getBean("keycloakAuthUrl");
	private String contextReferentialBaseUrl = (String) context.getBean("contextReferentialBaseUrl");

	@Autowired
	@Qualifier("habilitationApiBaseUrl")
	private String habilitationApiRootUrl;

	private InterviewerTypeDao interviewerDao = context.getBean(InterviewerTypeDao.class);
	private OrganizationalUnitTypeDao ouDao = context.getBean(OrganizationalUnitTypeDao.class);
	private SurveyUnitDao suDao = context.getBean(SurveyUnitDao.class);
	private ClosingCauseDao closingCauseDao = context.getBean(ClosingCauseDao.class);

	private MockRestServiceServer mockServer;
	private ObjectMapper mapper = new ObjectMapper();

	private static final String outFolder = "src/test/resources/out/contextReferentialSynchro";

	@BeforeAll
	static void setProperties() {
		System.setProperty("fr.insee.pearljam.ldap.service.pw", "pw");
		System.setProperty("fr.insee.pearljam.ldap.service.login", "login");

	}

	/**
	 * This method is executed before each test in this class.
	 * It setup the environment by inserting the data and copying the necessaries
	 * files.
	 * 
	 * @throws Exception
	 */
	/*@BeforeEach
	void setUp() throws Exception {
		reinitData();
		copyFiles("campaign");

		MockitoAnnotations.initMocks(this);
		mockServer = MockRestServiceServer.createServer(restTemplate);
	}*/

	static UnitTests unitTests = new UnitTests();

	/**
	 * Scenario 1 : all OK
	 * 
	 * @throws Exception
	 */
	@Test
	void testAllOKAffectingToExistingIntAndOu() throws Exception {
		InterviewersResponseDto intResp = makeInterviewerRespDto();
		OrganizationUnitsResponseDto ouResp = makeOuRespDto();
		InterviewersAffectationsResponseDto intSuResp = makeIntAffRespDto();
		OrganizationUnitsAffectationsResponseDto ouSuResp = makeOuAffRespDto();

		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers", intResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers/survey-units", intSuResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units", ouResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units/survey-units", ouSuResp);

		assertEquals(BatchErrorCode.OK, triggerService.synchronizeWithOpale(outFolder));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.ITW", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.SU_ITW", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.OU", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.SU_OU", ".xml"));

		assertTrue(interviewerDao.existInterviewer("TEST"));
		assertEquals("INTW3", suDao.getSurveyUnitInterviewerAffectation("12"));

		// Delete closing causes if reaffected
		assertTrue(closingCauseDao.getClosingCausesBySuId("12").isEmpty());
		assertFalse(closingCauseDao.getClosingCausesBySuId("14").isEmpty());

		assertTrue(ouDao.existOrganizationalUnit("OU-PACA"));
		assertEquals("OU-SOUTH", suDao.getSurveyUnitOrganizationUnitAffectation("12"));
	}

	/**
	 * Scenario 2 : cannot reach context referential
	 * 
	 * @throws Exception
	 */

	@Test
	void testCannotReachContextReferential() throws Exception {

		expectExternalCallWithToken(habilitationApiRootUrl + Constants.API_LDAP_HEALTHCHECK, null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers", null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers/survey-units", null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units", null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units/survey-units", null);

		assertEquals(BatchErrorCode.KO_TECHNICAL_ERROR, triggerService.synchronizeWithOpale(outFolder));

	}

	/**
	 * Scenario 3 : cannot reach keycloak auth server
	 * 
	 * @throws Exception
	 */

	@Test
	void testCannotReachKeycloakServer() throws Exception {
		expectExternalCall(keycloakTokenUrl, null);
		expectExternalCall(keycloakTokenUrl, null);
		expectExternalCall(keycloakTokenUrl, null);
		expectExternalCall(keycloakTokenUrl, null);

		assertEquals(BatchErrorCode.KO_TECHNICAL_ERROR, triggerService.synchronizeWithOpale(outFolder));
	}

	/**
	 * Scenario 4 : trying to affect to an interviewer that does not exist
	 * 
	 * @throws Exception
	 */

	@Test
	void testInterviewerDoesntExist() throws Exception {
		InterviewersResponseDto intResp = makeInterviewerRespDto();
		OrganizationUnitsResponseDto ouResp = makeOuRespDto();
		OrganizationUnitsAffectationsResponseDto ouSuResp = makeOuAffRespDto();

		InterviewersAffectationsResponseDto intSuResp = new InterviewersAffectationsResponseDto();
		List<InterviewerAffectationsDto> interviewerAffs = new ArrayList<>();
		InterviewerAffectationsDto intAff1 = new InterviewerAffectationsDto();
		intAff1.setIdep("DOESNTEXIST");
		List<SimpleIdDto> suIdList = new ArrayList<>();
		SimpleIdDto suId = new SimpleIdDto();
		suId.setId("14");
		suIdList.add(suId);
		SimpleIdDto suId2 = new SimpleIdDto();
		suId2.setId("12");
		suIdList.add(suId2);
		intAff1.setSurveyUnits(suIdList);
		interviewerAffs.add(intAff1);
		intSuResp.setInterviewers(interviewerAffs);

		expectExternalCallWithToken(habilitationApiRootUrl + Constants.API_LDAP_HEALTHCHECK, null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers", intResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers/survey-units", intSuResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units", ouResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units/survey-units", ouSuResp);

		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, triggerService.synchronizeWithOpale(outFolder));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.ITW", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.SU_ITW", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.OU", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.SU_OU", ".xml"));

		assertTrue(interviewerDao.existInterviewer("TEST"));
		assertEquals("INTW1", suDao.getSurveyUnitInterviewerAffectation("12"));

		assertTrue(ouDao.existOrganizationalUnit("OU-PACA"));
		assertEquals("OU-SOUTH", suDao.getSurveyUnitOrganizationUnitAffectation("12"));
	}

	/**
	 * Scenario 5 : trying to affect to an OU that does not exist
	 * 
	 * @throws Exception
	 */
	@Test
	void testOUDoesntExist() throws Exception {
		InterviewersResponseDto intResp = makeInterviewerRespDto();
		OrganizationUnitsResponseDto ouResp = makeOuRespDto();
		InterviewersAffectationsResponseDto intSuResp = makeIntAffRespDto();
		OrganizationUnitsAffectationsResponseDto ouSuResp = new OrganizationUnitsAffectationsResponseDto();
		List<OrganizationUnitAffectationsDto> ouAffs = new ArrayList<>();
		OrganizationUnitAffectationsDto ouAffDto = new OrganizationUnitAffectationsDto();
		ouAffDto.setId("OU-MISSING");
		List<SimpleIdDto> suIdListOu = new ArrayList<>();
		SimpleIdDto suIdOu = new SimpleIdDto();
		suIdOu.setId("14");
		suIdListOu.add(suIdOu);
		SimpleIdDto suIdOu2 = new SimpleIdDto();
		suIdOu2.setId("12");
		suIdListOu.add(suIdOu2);
		ouAffDto.setSurveyUnits(suIdListOu);
		ouAffs.add(ouAffDto);
		ouSuResp.setOrganizationUnits(ouAffs);

		expectExternalCallWithToken(habilitationApiRootUrl + Constants.API_LDAP_HEALTHCHECK, null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers", intResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers/survey-units", intSuResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units", ouResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units/survey-units", ouSuResp);

		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, triggerService.synchronizeWithOpale(outFolder));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.ITW", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.SU_ITW", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.OU", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.SU_OU", ".xml"));

		assertTrue(interviewerDao.existInterviewer("TEST"));
		assertEquals("INTW3", suDao.getSurveyUnitInterviewerAffectation("12"));

		assertTrue(ouDao.existOrganizationalUnit("OU-PACA"));
		assertEquals("OU-NORTH", suDao.getSurveyUnitOrganizationUnitAffectation("12"));
	}

	/**
	 * Scenario 6 : Trying to create an OU with an OU parent that doesnt exist
	 * 
	 * @throws Exception
	 */
	@Test
	void testOUParentDoesntExist() throws Exception {
		InterviewersResponseDto intResp = makeInterviewerRespDto();
		InterviewersAffectationsResponseDto intSuResp = makeIntAffRespDto();
		OrganizationUnitsAffectationsResponseDto ouSuResp = makeOuAffRespDto();

		OrganizationUnitsResponseDto ouResp = new OrganizationUnitsResponseDto();
		List<OrganizationUnitDto> organizationUnits = new ArrayList<>();
		OrganizationUnitDto ou1 = new OrganizationUnitDto();
		ou1.setCodeEtab("OU-PACA");
		ou1.setIdEtab(1345L);
		ou1.setLatitudeMaximum(33f);
		ou1.setLatitudeMinimum(33f);
		ou1.setLongitudeMaximum(33f);
		ou1.setLongitudeMinimum(33f);
		ou1.setOrigineEtab("OU-MISSING");
		organizationUnits.add(ou1);
		ouResp.setOrganizationUnits(organizationUnits);

		expectExternalCallWithToken(habilitationApiRootUrl + Constants.API_LDAP_HEALTHCHECK, null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers", intResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers/survey-units", intSuResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units", ouResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units/survey-units", ouSuResp);

		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, triggerService.synchronizeWithOpale(outFolder));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.ITW", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.SU_ITW", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.OU", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.SU_OU", ".xml"));

		assertTrue(interviewerDao.existInterviewer("TEST"));
		assertEquals("INTW3", suDao.getSurveyUnitInterviewerAffectation("12"));

		assertFalse(ouDao.existOrganizationalUnit("OU-PACA"));
		assertEquals("OU-SOUTH", suDao.getSurveyUnitOrganizationUnitAffectation("12"));
	}

	/**
	 * Scenario 7 : all OK, affecting to interviewer and ou added by synchro
	 * 
	 * @throws Exception
	 */
	@Test
	void testAllOKAffectingToSyncIntAndOu() throws Exception {
		InterviewersResponseDto intResp = makeInterviewerRespDto();
		OrganizationUnitsResponseDto ouResp = makeOuRespDto();

		InterviewersAffectationsResponseDto intSuResp = new InterviewersAffectationsResponseDto();
		List<InterviewerAffectationsDto> interviewerAffs = new ArrayList<>();
		InterviewerAffectationsDto intAff1 = new InterviewerAffectationsDto();
		intAff1.setIdep("INTW1");
		List<SimpleIdDto> suIdList = new ArrayList<>();
		SimpleIdDto suId = new SimpleIdDto();
		suId.setId("11");
		suIdList.add(suId);
		SimpleIdDto suId2 = new SimpleIdDto();
		suId2.setId("12");
		suIdList.add(suId2);
		intAff1.setSurveyUnits(suIdList);
		interviewerAffs.add(intAff1);

		InterviewerAffectationsDto intAff2 = new InterviewerAffectationsDto();
		intAff2.setIdep("TEST");
		List<SimpleIdDto> suIdList2 = new ArrayList<>();
		SimpleIdDto suId3 = new SimpleIdDto();
		suId3.setId("14");
		suIdList2.add(suId3);
		intAff2.setSurveyUnits(suIdList2);
		interviewerAffs.add(intAff2);

		intSuResp.setInterviewers(interviewerAffs);

		OrganizationUnitsAffectationsResponseDto ouSuResp = new OrganizationUnitsAffectationsResponseDto();
		List<OrganizationUnitAffectationsDto> ouAffs = new ArrayList<>();
		OrganizationUnitAffectationsDto ouAffDto = new OrganizationUnitAffectationsDto();
		ouAffDto.setId("OU-NORTH");
		List<SimpleIdDto> suIdListOu = new ArrayList<>();
		SimpleIdDto suIdOu = new SimpleIdDto();
		suIdOu.setId("11");
		suIdListOu.add(suIdOu);
		SimpleIdDto suIdOu2 = new SimpleIdDto();
		suIdOu2.setId("15");
		suIdListOu.add(suIdOu2);
		ouAffDto.setSurveyUnits(suIdListOu);
		ouAffs.add(ouAffDto);

		OrganizationUnitAffectationsDto ouAffDto2 = new OrganizationUnitAffectationsDto();
		ouAffDto2.setId("OU-PACA");
		List<SimpleIdDto> suIdListOu2 = new ArrayList<>();
		SimpleIdDto suIdOu3 = new SimpleIdDto();
		suIdOu3.setId("12");
		suIdListOu2.add(suIdOu3);
		ouAffDto2.setSurveyUnits(suIdListOu2);
		ouAffs.add(ouAffDto2);

		ouSuResp.setOrganizationUnits(ouAffs);

		expectExternalCallWithToken(habilitationApiRootUrl + Constants.API_LDAP_HEALTHCHECK, null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers", intResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers/survey-units", intSuResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units", ouResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units/survey-units", ouSuResp);

		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, triggerService.synchronizeWithOpale(outFolder));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.ITW", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.SU_ITW", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.OU", ".xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(outFolder + "/synchro"), "sync.SU_OU", ".xml"));

		assertTrue(interviewerDao.existInterviewer("TEST"));
		assertEquals("TEST", suDao.getSurveyUnitInterviewerAffectation("14"));

		// Delete closing causes if reaffected
		assertTrue(closingCauseDao.getClosingCausesBySuId("14").isEmpty());

		assertTrue(ouDao.existOrganizationalUnit("OU-PACA"));
		assertEquals("OU-PACA", suDao.getSurveyUnitOrganizationUnitAffectation("12"));
	}

	/**
	 * Scenario 8 : above threshold for interviewer reaffectation absolute
	 * 
	 * @throws Exception
	 */

	@Test
	void testAboveInterviewerReaffectationAbsoluteThreshold() throws Exception {
		InterviewersResponseDto intResp = makeInterviewerRespDto();
		OrganizationUnitsResponseDto ouResp = makeOuRespDto();
		OrganizationUnitsAffectationsResponseDto ouSuResp = makeOuAffRespDto();

		InterviewersAffectationsResponseDto intSuResp = new InterviewersAffectationsResponseDto();
		List<InterviewerAffectationsDto> interviewerAffs = new ArrayList<>();
		InterviewerAffectationsDto intAff1 = new InterviewerAffectationsDto();
		intAff1.setIdep("INTW1");
		List<SimpleIdDto> suIdList = new ArrayList<>();
		SimpleIdDto suId = new SimpleIdDto();
		suId.setId("14");
		suIdList.add(suId);

		SimpleIdDto suId2 = new SimpleIdDto();
		suId2.setId("12");
		suIdList.add(suId2);

		SimpleIdDto suId3 = new SimpleIdDto();
		suId3.setId("22");
		suIdList.add(suId3);

		SimpleIdDto suId4 = new SimpleIdDto();
		suId4.setId("23");
		suIdList.add(suId4);

		SimpleIdDto suId5 = new SimpleIdDto();
		suId5.setId("24");
		suIdList.add(suId5);

		SimpleIdDto suId6 = new SimpleIdDto();
		suId6.setId("25");
		suIdList.add(suId6);

		intAff1.setSurveyUnits(suIdList);
		interviewerAffs.add(intAff1);
		intSuResp.setInterviewers(interviewerAffs);

		expectExternalCallWithToken(habilitationApiRootUrl + Constants.API_LDAP_HEALTHCHECK, null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers", intResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers/survey-units", intSuResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units", ouResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units/survey-units", ouSuResp);

		assertEquals(BatchErrorCode.KO_FONCTIONAL_ERROR, triggerService.synchronizeWithOpale(outFolder));

		assertFalse(interviewerDao.existInterviewer("TEST"));
		assertEquals("INTW3", suDao.getSurveyUnitInterviewerAffectation("14"));

		assertFalse(ouDao.existOrganizationalUnit("OU-PACA"));
		assertEquals("OU-NORTH", suDao.getSurveyUnitOrganizationUnitAffectation("12"));
	}

	/**
	 * Scenario 9 : above threshold for interviewer reaffectation relative
	 * 
	 * @throws Exception
	 */

	@Test
	void testAboveInterviewerReaffectationRelativeThreshold() throws Exception {
		InterviewersResponseDto intResp = makeInterviewerRespDto();
		OrganizationUnitsResponseDto ouResp = makeOuRespDto();
		OrganizationUnitsAffectationsResponseDto ouSuResp = makeOuAffRespDto();

		InterviewersAffectationsResponseDto intSuResp = new InterviewersAffectationsResponseDto();
		List<InterviewerAffectationsDto> interviewerAffs = new ArrayList<>();
		InterviewerAffectationsDto intAff1 = new InterviewerAffectationsDto();
		intAff1.setIdep("INTW1");
		List<SimpleIdDto> suIdList = new ArrayList<>();
		SimpleIdDto suId = new SimpleIdDto();
		suId.setId("14");
		suIdList.add(suId);

		SimpleIdDto suId4 = new SimpleIdDto();
		suId4.setId("23");
		suIdList.add(suId4);

		SimpleIdDto suId5 = new SimpleIdDto();
		suId5.setId("24");
		suIdList.add(suId5);

		intAff1.setSurveyUnits(suIdList);
		interviewerAffs.add(intAff1);
		intSuResp.setInterviewers(interviewerAffs);
		expectExternalCallWithToken(habilitationApiRootUrl + Constants.API_LDAP_HEALTHCHECK, null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers", intResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers/survey-units", intSuResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units", ouResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units/survey-units", ouSuResp);

		assertEquals(BatchErrorCode.KO_FONCTIONAL_ERROR, triggerService.synchronizeWithOpale(outFolder));

		assertFalse(interviewerDao.existInterviewer("TEST"));
		assertEquals("INTW3", suDao.getSurveyUnitInterviewerAffectation("14"));

		assertFalse(ouDao.existOrganizationalUnit("OU-PACA"));
		assertEquals("OU-NORTH", suDao.getSurveyUnitOrganizationUnitAffectation("12"));
	}

	/**
	 * Scenario 10 : above threshold for OU reaffectation absolute
	 * 
	 * @throws Exception
	 */
	@Test
	void testAboveOrganizationReaffectationAbsoluteThreshold() throws Exception {
		InterviewersResponseDto intResp = makeInterviewerRespDto();
		OrganizationUnitsResponseDto ouResp = makeOuRespDto();
		InterviewersAffectationsResponseDto intSuResp = makeIntAffRespDto();
		OrganizationUnitsAffectationsResponseDto ouSuResp = new OrganizationUnitsAffectationsResponseDto();
		List<OrganizationUnitAffectationsDto> ouAffs = new ArrayList<>();
		OrganizationUnitAffectationsDto ouAffDto = new OrganizationUnitAffectationsDto();
		ouAffDto.setId("OU-SOUTH");
		List<SimpleIdDto> suIdListOu = new ArrayList<>();
		SimpleIdDto suIdOu = new SimpleIdDto();
		suIdOu.setId("12");
		suIdListOu.add(suIdOu);
		SimpleIdDto suIdOu2 = new SimpleIdDto();
		suIdOu2.setId("13");
		suIdListOu.add(suIdOu2);

		SimpleIdDto suIdOu3 = new SimpleIdDto();
		suIdOu3.setId("14");
		suIdListOu.add(suIdOu3);

		SimpleIdDto suIdOu4 = new SimpleIdDto();
		suIdOu4.setId("20");
		suIdListOu.add(suIdOu4);

		SimpleIdDto suIdOu5 = new SimpleIdDto();
		suIdOu5.setId("21");
		suIdListOu.add(suIdOu5);

		SimpleIdDto suIdOu6 = new SimpleIdDto();
		suIdOu6.setId("22");
		suIdListOu.add(suIdOu6);

		ouAffDto.setSurveyUnits(suIdListOu);
		ouAffs.add(ouAffDto);
		ouSuResp.setOrganizationUnits(ouAffs);

		expectExternalCallWithToken(habilitationApiRootUrl + Constants.API_LDAP_HEALTHCHECK, null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers", intResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers/survey-units", intSuResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units", ouResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units/survey-units", ouSuResp);

		assertEquals(BatchErrorCode.KO_FONCTIONAL_ERROR, triggerService.synchronizeWithOpale(outFolder));

		assertFalse(interviewerDao.existInterviewer("TEST"));
		assertEquals("INTW3", suDao.getSurveyUnitInterviewerAffectation("14"));

		assertFalse(ouDao.existOrganizationalUnit("OU-PACA"));
		assertEquals("OU-NORTH", suDao.getSurveyUnitOrganizationUnitAffectation("12"));
	}

	/**
	 * Scenario 11 : above threshold for OU reaffectation relative
	 * 
	 * @throws Exception
	 */
	@Test
	void testAboveOrganizationReaffectationRelativeThreshold() throws Exception {
		InterviewersResponseDto intResp = makeInterviewerRespDto();
		OrganizationUnitsResponseDto ouResp = makeOuRespDto();
		InterviewersAffectationsResponseDto intSuResp = makeIntAffRespDto();
		OrganizationUnitsAffectationsResponseDto ouSuResp = new OrganizationUnitsAffectationsResponseDto();
		List<OrganizationUnitAffectationsDto> ouAffs = new ArrayList<>();
		OrganizationUnitAffectationsDto ouAffDto = new OrganizationUnitAffectationsDto();
		ouAffDto.setId("OU-SOUTH");
		List<SimpleIdDto> suIdListOu = new ArrayList<>();
		SimpleIdDto suIdOu = new SimpleIdDto();
		suIdOu.setId("12");
		suIdListOu.add(suIdOu);
		SimpleIdDto suIdOu2 = new SimpleIdDto();
		suIdOu2.setId("13");
		suIdListOu.add(suIdOu2);

		SimpleIdDto suIdOu4 = new SimpleIdDto();
		suIdOu4.setId("20");
		suIdListOu.add(suIdOu4);

		ouAffDto.setSurveyUnits(suIdListOu);
		ouAffs.add(ouAffDto);
		ouSuResp.setOrganizationUnits(ouAffs);

		expectExternalCallWithToken(habilitationApiRootUrl + Constants.API_LDAP_HEALTHCHECK, null);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers", intResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/interviewers/survey-units", intSuResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units", ouResp);
		expectExternalCallWithToken(contextReferentialBaseUrl + "/sabiane/organization-units/survey-units", ouSuResp);

		assertEquals(BatchErrorCode.KO_FONCTIONAL_ERROR, triggerService.synchronizeWithOpale(outFolder));

		assertFalse(interviewerDao.existInterviewer("TEST"));
		assertEquals("INTW3", suDao.getSurveyUnitInterviewerAffectation("14"));

		assertFalse(ouDao.existOrganizationalUnit("OU-PACA"));
		assertEquals("OU-NORTH", suDao.getSurveyUnitOrganizationUnitAffectation("12"));
	}

	private InterviewersResponseDto makeInterviewerRespDto() {
		InterviewersResponseDto intResp = new InterviewersResponseDto();
		List<InterviewerDto> interviewers = new ArrayList<>();
		InterviewerDto int1 = new InterviewerDto();
		int1.setIdep("TEST");
		int1.setIdSirh(1234L);
		int1.setMailInsee("example@example.com");
		int1.setNom("Fabres");
		int1.setPrenom("Thierry");
		int1.setPoleGestionCourant("82");
		int1.setSexe("1");
		interviewers.add(int1);
		intResp.setEnqueteurs(interviewers);

		return intResp;
	}

	private OrganizationUnitsResponseDto makeOuRespDto() {
		OrganizationUnitsResponseDto ouResp = new OrganizationUnitsResponseDto();
		List<OrganizationUnitDto> organizationUnits = new ArrayList<>();
		OrganizationUnitDto ou1 = new OrganizationUnitDto();
		ou1.setCodeEtab("OU-PACA");
		ou1.setIdEtab(1345L);
		ou1.setLatitudeMaximum(33f);
		ou1.setLatitudeMinimum(33f);
		ou1.setLongitudeMaximum(33f);
		ou1.setLongitudeMinimum(33f);
		ou1.setOrigineEtab("OU-NATIONAL");
		organizationUnits.add(ou1);
		ouResp.setOrganizationUnits(organizationUnits);

		return ouResp;
	}

	private InterviewersAffectationsResponseDto makeIntAffRespDto() {
		InterviewersAffectationsResponseDto intSuResp = new InterviewersAffectationsResponseDto();
		List<InterviewerAffectationsDto> interviewerAffs = new ArrayList<>();
		InterviewerAffectationsDto intAff1 = new InterviewerAffectationsDto();
		intAff1.setIdep("INTW3");
		List<SimpleIdDto> suIdList = new ArrayList<>();
		SimpleIdDto suId = new SimpleIdDto();
		suId.setId("14");
		suIdList.add(suId);
		SimpleIdDto suId2 = new SimpleIdDto();
		suId2.setId("12");
		suIdList.add(suId2);
		intAff1.setSurveyUnits(suIdList);
		interviewerAffs.add(intAff1);
		intSuResp.setInterviewers(interviewerAffs);

		return intSuResp;
	}

	private OrganizationUnitsAffectationsResponseDto makeOuAffRespDto() {
		OrganizationUnitsAffectationsResponseDto ouSuResp = new OrganizationUnitsAffectationsResponseDto();
		List<OrganizationUnitAffectationsDto> ouAffs = new ArrayList<>();
		OrganizationUnitAffectationsDto ouAffDto = new OrganizationUnitAffectationsDto();
		ouAffDto.setId("OU-SOUTH");
		List<SimpleIdDto> suIdListOu = new ArrayList<>();
		SimpleIdDto suIdOu = new SimpleIdDto();
		suIdOu.setId("14");
		suIdListOu.add(suIdOu);
		SimpleIdDto suIdOu2 = new SimpleIdDto();
		suIdOu2.setId("12");
		suIdListOu.add(suIdOu2);
		ouAffDto.setSurveyUnits(suIdListOu);
		ouAffs.add(ouAffDto);
		ouSuResp.setOrganizationUnits(ouAffs);

		return ouSuResp;
	}

	private void expectExternalCallWithToken(String url, Object resp) throws JsonProcessingException {
		KeycloakResponseDto keycloackResp = new KeycloakResponseDto();
		keycloackResp.setAccess_token("token");

		expectExternalCall(keycloakTokenUrl, keycloackResp);
		expectExternalCall(url, resp);
	}

	private void expectExternalCall(String url, Object resp) throws JsonProcessingException {
		mockServer.expect(ExpectedCount.once(),
				requestTo(url))
				.andRespond(withStatus(HttpStatus.OK)
						.contentType(MediaType.APPLICATION_JSON)
						.body(mapper.writeValueAsString(resp)));
	}
/*
	@AfterEach
	void cleanOutFolder() {
		purgeDirectory(new File(outFolder + "/synchro"));
	}
*/
	@AfterAll
	static void deleteFiles() {
		File deleteFolderInCampaignForTest = new File("src/test/resources/in/campaign/testScenarios");
		FileSystemUtils.deleteRecursively(deleteFolderInCampaignForTest);
	}
}
