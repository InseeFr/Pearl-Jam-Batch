package fr.insee.pearljam.batch;

import fr.insee.pearljam.batch.service.CommunicationService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.DBResetHelper;
import fr.insee.pearljam.batch.utils.FileHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class EndToEndCommunicationIT {

	private static final String OUT_PATH = "src/test/resources/out/unitTests";

	@Autowired
	private DBResetHelper dbResetHelper;

	@Autowired
	private CommunicationService communicationService;

	@BeforeEach
	void setUp() throws Exception {
		dbResetHelper.reinitData();
		Files.createDirectories(Paths.get("src/test/resources/out/communication"));
	}

	@Test
	void testHandleCommunications_success() throws Exception {
		BatchErrorCode result = communicationService.handleCommunications();
		assertEquals(BatchErrorCode.OK, result, "Expected success");
	}

	@AfterEach
	void cleanUp() {
		FileHelper.purgeDirectory(new java.io.File(OUT_PATH));
	}


}