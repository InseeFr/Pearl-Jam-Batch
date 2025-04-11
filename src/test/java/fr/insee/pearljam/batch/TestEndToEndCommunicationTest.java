package fr.insee.pearljam.batch;

import fr.insee.pearljam.batch.campaign.CommunicationRequestType;
import fr.insee.pearljam.batch.config.ApplicationContext;
import fr.insee.pearljam.batch.dao.CommunicationRequestDao;
import fr.insee.pearljam.batch.dao.CommunicationRequestStatusDao;
import fr.insee.pearljam.batch.service.CommunicationService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
class TestEndToEndCommunicationTest extends PearlJamBatchApplicationTests {

	private static final String OUT_PATH = "src/test/resources/out/unitTests";

	AnnotationConfigApplicationContext context;
	CommunicationService communicationService;

	@BeforeEach
	void setUp() throws Exception {
		reinitData();
		context = new AnnotationConfigApplicationContext(ApplicationContext.class);
		communicationService = context.getBean(CommunicationService.class);
	}

	@Test
	void testHandleCommunications_success() throws Exception {
		BatchErrorCode result = communicationService.handleCommunications();
		assertEquals(BatchErrorCode.OK, result, "Expected success on happy path");
	}

	@Test
	void testHandleCommunications_missingInterviewer() throws Exception {
		BatchErrorCode result = communicationService.handleCommunications();
		assertEquals(BatchErrorCode.OK, result, "Service should not crash on missing interviewer");
		// TODO: Additional assertions: log, missing output, etc.
		// check generated file for missing
	}

	@Test
	void testHandleCommunications_missingAddress() throws Exception {
		BatchErrorCode result = communicationService.handleCommunications();
		assertEquals(BatchErrorCode.OK, result, "Service should not crash on missing address");
		// TODO: check generated file
	}

	@Test
	void testHandleCommunications_missingTemplate() {
		CommunicationRequestDao commReqDao = context.getBean(CommunicationRequestDao.class);
		CommunicationRequestStatusDao statusDao = context.getBean(CommunicationRequestStatusDao.class);

		// insert a communication request pointing to an unknown meshuggahId
		CommunicationRequestType req = new CommunicationRequestType();
		req.setId("REQ_FAIL_TEMPLATE");
		req.setSurveyUnitId("SIM_SUCCESS");
		req.setEmitter("INTERVIEWER");
		req.setReason("REFUSAL");
		req.setCampaignId("SIMPSONS2020X00");
		req.setMeshuggahId("unknown_template");

		commReqDao.save(req);
		statusDao.addStatus("REQ_FAIL_TEMPLATE", "READY", 1590969600000L); // 2020-06-01

		Exception thrown = assertThrows(RuntimeException.class, () -> {
			communicationService.handleCommunications();
		});

		assertTrue(thrown.getMessage().contains("Error fetching template"));
	}

	@AfterEach
	void cleanUp() {
		purgeDirectory(new java.io.File(OUT_PATH));
	}


}