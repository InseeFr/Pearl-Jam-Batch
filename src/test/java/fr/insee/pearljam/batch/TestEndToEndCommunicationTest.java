package fr.insee.pearljam.batch;

import fr.insee.pearljam.batch.campaign.CommunicationRequestType;
import fr.insee.pearljam.batch.config.ApplicationContext;
import fr.insee.pearljam.batch.dao.CommunicationRequestDao;
import fr.insee.pearljam.batch.dao.CommunicationRequestStatusDao;
import fr.insee.pearljam.batch.service.CommunicationService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class TestEndToEndCommunicationTest extends PearlJamBatchApplicationTests {

	private static final String OUT_PATH = "src/test/resources/out/unitTests";

	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContext.class);
	CommunicationService communicationService = context.getBean(CommunicationService.class);

	@BeforeEach
	void setUp() throws Exception {
		reinitData();
	}

	@Test
	void testHandleCommunications_success() throws Exception {
		BatchErrorCode result = communicationService.handleCommunications();
		assertEquals(BatchErrorCode.OK, result, "Expected success");
	}

	@AfterEach
	void cleanUp() {
		purgeDirectory(new java.io.File(OUT_PATH));
	}


}