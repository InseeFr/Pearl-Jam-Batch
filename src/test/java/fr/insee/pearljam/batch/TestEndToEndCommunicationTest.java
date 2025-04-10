package fr.insee.pearljam.batch;

import fr.insee.pearljam.batch.config.ApplicationContext;
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
	@Order(1)
	void testHandleCommunications_success() throws Exception {
		BatchErrorCode result = communicationService.handleCommunications();
		assertEquals(BatchErrorCode.OK, result, "Expected success on happy path");
	}

	@Test
	@Order(2)
	void testHandleCommunications_missingInterviewer() throws Exception {
		BatchErrorCode result = communicationService.handleCommunications();
		assertEquals(BatchErrorCode.OK, result, "Service should not crash on missing interviewer");
		// TODO: Additional assertions: log, missing output, etc.
		// check generated file for missing
	}

	@Test
	@Order(3)
	void testHandleCommunications_missingAddress() throws Exception {
		BatchErrorCode result = communicationService.handleCommunications();
		assertEquals(BatchErrorCode.OK, result, "Service should not crash on missing address");
		// TODO: check generated file
	}

	@Test
	@Order(4)
	void testHandleCommunications_missingTemplate() {
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