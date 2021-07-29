package fr.insee.pearljam.batch;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
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
import fr.insee.pearljam.batch.dto.InterviewerDto;
import fr.insee.pearljam.batch.dto.KeycloakResponseDto;
import fr.insee.pearljam.batch.dto.SimpleIdDto;
import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.service.PilotageLauncherService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.PathUtils;

public class TestsEndToEndCampaign {
	
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContext.class);
	
	PilotageLauncherService pilotageLauncherService = context.getBean(PilotageLauncherService.class);
	
	private RestTemplate restTemplate = context.getBean(RestTemplate.class);
	
	private MockRestServiceServer mockServer;
    private ObjectMapper mapper = new ObjectMapper();
    
	private String keycloakTokenUrl = (String) context.getBean("keycloakAuthUrl");
	private String contextReferentialBaseUrl = (String) context.getBean("contextReferentialBaseUrl");
	private static final String OUT = "src/test/resources/out/campaign/testScenarios";
	
	/**
	 * This method is executed before each test in this class.
	 * It setup the environment by inserting the data and copying the necessaries files.
	 * @throws Exception 
	 */
	@Before
	public void setUp() throws Exception {
		PearlJamBatchApplicationTests.initData();
		PearlJamBatchApplicationTests.copyFiles("campaign");

		MockitoAnnotations.initMocks(this);
		mockServer = MockRestServiceServer.createServer(restTemplate);
	}
	
	public static UnitTests unitTests = new UnitTests();
	
	/**
	 * Scenario 1 : Campaign.xml is not valid
	 * @throws Exception
	 */
	@Test
	public void testScenario1() throws Exception {
		String in = "src/test/resources/in/campaign/testScenarios/campaignScenario1";
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error validating campaign.xml : "));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.xml"));
		}
	}

	/**
	 * Scenario 2 : Organizational Unit is missing in Campaign.xml file
	 * @throws Exception
	 */
	@Test
	public void testScenario2() throws Exception {
		String in = "src/test/resources/in/campaign/testScenarios/campaignScenario2";
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error during load campaign"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.xml"));
		}
	}
	
	/**
	 * Scenario 3 : Interviewer associated to an other Organizational Unit in Campaign.xml file
	 * @throws Exception
	 */
	@Test
	public void testScenario3() throws Exception {
		String in = "src/test/resources/in/campaign/testScenarios/campaignScenario3";
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error during process, error loading campaign"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.xml"));
		}
	}
	
	/**
	 * Scenario 4 : Campaign not exist and survey units already exists in Campaign.xml file
	 * @throws Exception
	 */
	@Test
	public void testScenario4() throws Exception {
		String in = "src/test/resources/in/campaign/testScenarios/campaignScenario4";
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error during creation of campaign"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.xml"));
		}
	}
	
	/**
	 * Scenario 5 : XML files, campaign not exist and survey-units ok
	 * @throws Exception
	 */
	@Test
	public void testScenario5() throws Exception {
		
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario5", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","done.xml"));
	}
	
	/**
	 * Scenario 6 : XML files ok, campaign not exist and 1 survey-unit in error (geographical location not exist)
	 * @throws Exception
	 */
	@Test
	public void testScenario6() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario6", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 7 : XML ok, Campaign exist, and survey-units exist but associated
	 * to an other campaign
	 * @throws Exception
	 */
	@Test
	public void testScenario7() throws Exception {
		try {
			assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario7", OUT));
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error during creation of campaign"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
		}
	}
	
	/**
	 * Scenario 8 : XML ok, campaign exist, survey-unit exist and ok
	 * @throws Exception
	 */
	@Test
	public void testScenario8() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario8", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","done.xml"));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 9 : XML ok, campaign exist, survey-units not exist with 1 in error (geographical location not exist)
	 * @throws Exception
	 */
	@Test
	public void testScenario9() throws Exception {
		try {
			assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario9", OUT));
		} catch (ValidateException ve){
			assertEquals(true, ve.getMessage().contains("Error during creation of campaign"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
		}
	}
	
	/**
	 * Scenario 10 : XML ok, campaign exist, survey-units exists adn ok
	 * @throws Exception
	 */
	@Test
	public void testScenario10() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario10", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","done.xml"));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 11 : XML ok, campaign exist, survey-units not exist with 1 in error (geographical location null)
	 * @throws Exception
	 */
	@Test
	public void testScenario11() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario11", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 12 : XML ok, campaign exist but no survey-units to treats in the file
	 * @throws Exception
	 */
	@Test
	public void testScenario12() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario12", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","done.xml"));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 13 : XML ok, campaign not exist with 1 survey-unit in error (geographical location null)
	 * @throws Exception
	 */
	@Test
	public void testScenario13() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario13", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 14 : XML ok, campaign not exist with 1 survey-unit in error (interviewer null)
	 * @throws Exception
	 */
	@Test
	public void testScenario14() throws Exception {
		KeycloakResponseDto resp = new KeycloakResponseDto();
		resp.setAccess_token("token");
		SimpleIdDto idDto = new SimpleIdDto();
		idDto.setId("OU-NORTH");
		InterviewerDto intDto = new InterviewerDto();
		intDto.setIdep("INTW1");
		
		expectExternalCall(keycloakTokenUrl, resp);
		expectExternalCall(contextReferentialBaseUrl + "/sabiane/survey-unit/su1234/interviewer", intDto);
		expectExternalCall(keycloakTokenUrl, resp);
		expectExternalCall(contextReferentialBaseUrl + "/sabiane/organization-units/survey-unit/su1234", idDto);

		expectExternalCall(keycloakTokenUrl, resp);
		expectExternalCall(contextReferentialBaseUrl + "/sabiane/survey-unit/su5678/interviewer", intDto);
		expectExternalCall(keycloakTokenUrl, resp);
		expectExternalCall(contextReferentialBaseUrl + "/sabiane/organization-units/survey-unit/su5678", idDto);

		
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario14", OUT));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 15 : XML ok, campaign not exist with 1 survey-unit in 
	 * error (survey-unit already associated to an other campaign)
	 * @throws Exception
	 */
	@Test
	public void testScenario15() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario15", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 16 : XML ok, campaign exist, survey-units not exist with 1 in error (geographical location null)
	 * @throws Exception
	 */
	@Test
	public void testScenario16() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario16", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 17 : XML ok, campaign exist, survey-unit not exist with 1 in error (interviewer null)
	 * @throws Exception
	 */
	@Test
	public void testScenario17() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario17", OUT));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 18 : XML ok, survey-units not exist with 1 in 
	 * error (survey-unit already associated to an other campaign)
	 * @throws Exception
	 */
	@Test
	public void testScenario18() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario18", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 19 : XML ok, campaign exist with 1 survey-unit in error (geographical location null)
	 * @throws Exception
	 */
	@Test
	public void testScenario19() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario19", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 20 : XML ok, campaign exist with 1 survey-unit in error (interviewer null)
	 * @throws Exception
	 */
	@Test
	public void testScenario20() throws Exception {
		KeycloakResponseDto resp = new KeycloakResponseDto();
		resp.setAccess_token("token");
		SimpleIdDto idDto = new SimpleIdDto();
		idDto.setId("OU-NORTH");
		InterviewerDto intDto = new InterviewerDto();
		intDto.setIdep("INTW1");
		
		expectExternalCall(keycloakTokenUrl, resp);
		expectExternalCall(contextReferentialBaseUrl + "/sabiane/survey-unit/11/interviewer", intDto);
		expectExternalCall(keycloakTokenUrl, resp);
		expectExternalCall(contextReferentialBaseUrl + "/sabiane/organization-units/survey-unit/11", idDto);

		expectExternalCall(keycloakTokenUrl, resp);
		expectExternalCall(contextReferentialBaseUrl + "/sabiane/survey-unit/12/interviewer", intDto);
		expectExternalCall(keycloakTokenUrl, resp);
		expectExternalCall(contextReferentialBaseUrl + "/sabiane/organization-units/survey-unit/12", idDto);

		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario20", OUT));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(false, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 21 : XML ok, campaign exist with 1 survey-unit in
	 * error (survey-unit already associated to an other campaign)
	 * @throws Exception
	 */
	@Test
	public void testScenario21() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario21", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.list"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","warning.xml"));
	}
	
	/**
	 * Scenario 22 : xml with wrong encoding
	 * @throws Exception
	 */
	@Test
	public void testScenario22() throws Exception {
		String in = "src/test/resources/in/campaign/testScenarios/campaignScenario22";
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error during validation"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.xml"));
		}
	}
	
	/**
	 * Scenario 23 : xml with wrong dates formats
	 * @throws Exception
	 */
	@Test
	public void testScenario23() throws Exception {
		String in = "src/test/resources/in/campaign/testScenarios/campaignScenario23";
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error validating campaign.xml : "));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.xml"));
		}
	}
	
	/**
	 * Scenario 24 : xml with no coherency between dates
	 * @throws Exception
	 */
	@Test
	public void testScenario24() throws Exception {
		String in = "src/test/resources/in/campaign/testScenarios/campaignScenario24";
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error during validation"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.xml"));
		}
	}
	
	/**
	 * Scenario 25 : No interviewerId, can't reach context referential
	 * @throws Exception
	 */
	@Test
	public void testScenario25() throws Exception {
		KeycloakResponseDto resp = new KeycloakResponseDto();
		resp.setAccess_token("token");
		
		expectExternalCall(keycloakTokenUrl, resp);
		expectExternalCall(contextReferentialBaseUrl + "/sabiane/survey-unit/su1234/interviewer", null);

		try {
			pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario14", OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error during process, error loading campaign : Could not get response from contextReferential"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","error.xml"));
		}
	}
	
	/**
	 * Scenario 26 : XML OK , Campaign not exist, SU OK no <OrganizationalUnits>
	 * @throws Exception
	 */
	@Test
	public void testScenario26() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.LOADCAMPAIGN, "src/test/resources/in/campaign/testScenarios/campaignScenario26", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/campaign/testScenarios"), "campaign","done.xml"));
	}
	
	void purgeDirectory(File dir) {
	    for (File file: dir.listFiles()) {
	        if (file.isFile())
	            file.delete();
	    }
	}
	
	private void expectExternalCall(String url, Object resp) throws JsonProcessingException {
		mockServer.expect(ExpectedCount.once(), 
		          requestTo(url))
		          .andRespond(withStatus(HttpStatus.OK)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(mapper.writeValueAsString(resp))
		        ); 
	}
	
	@After
	public void cleanOutFolder() {
		purgeDirectory(new File("src/test/resources/out/campaign/testScenarios"));
	}
	
	@AfterClass
	public static void deleteFiles() throws IOException {
		File deleteFolderInCampaignForTest = new File("src/test/resources/in/campaign/testScenarios");
		FileSystemUtils.deleteRecursively(deleteFolderInCampaignForTest);
	}
}
