package fr.insee.pearljam.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import fr.insee.pearljam.batch.config.ApplicationConfig;
import fr.insee.pearljam.batch.service.*;
import fr.insee.pearljam.batch.utils.*;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.exception.ArgumentException;
import fr.insee.pearljam.batch.exception.ValidateException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UnitTests {

	@Autowired
	private PilotageLauncherService pilotageLauncherService;
	@Autowired
	private PilotageFolderService pilotageFolderService;
	@Autowired
	private PilotageDBService pilotageDBService;
	@Autowired
	private TriggerService triggerService;
	@Autowired
	private CommunicationService communicationService;
	@Autowired
	private ApplicationConfig applicationConfig;
	private Launcher launcher;
	@Autowired
	private DBResetHelper dbResetHelper;

	private static final String PROCESSING = "src/test/resources/in/sampleprocessing/testScenarios/processing";

	/**
	 * This method is executed before each test in this class.
	 * It setup the environment by inserting the data and copying the necessaries files.
	 * @throws Exception e
	 */
	@BeforeEach
	void setUp() throws Exception {
		dbResetHelper.reinitData();
		FileHelper.copyFiles("sampleprocessing");
		launcher = new Launcher(pilotageDBService, pilotageLauncherService, triggerService, communicationService, applicationConfig);
	}
		
	/* Tests for PathUtils.java */
	
	@Test
	void directoryShouldExist() {
		assertTrue(PathUtils.isDirectoryExist("src/test/resources/in"));
	}
	
	@Test
	void directoryShouldntExist() {
		assertFalse(PathUtils.isDirectoryExist("src/test/resources/test"));
	}
	
	@Test
	void directoryShouldContainsExtension() {
		assertTrue(PathUtils.isDirContainsFileExtension(Path.of("src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5"), "sampleProcessing.xml"));
	}
	
	@Test
	void fileShouldExist() {
		assertTrue(PathUtils.isFileExist("src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5/sampleProcessing.xml"));
	}
	
	/* Run Batch */
	
	@SuppressWarnings("static-access")
	@Test
	void noOptionDefine() {
		String[] options= {};
		assertThrows(ArgumentException.class, () -> launcher.runBatch(options));
	}
 			
	/* Validation */
	
	/**
	 * This method tests the validation part of the file campaign.xml.
	 */
	@Test
	void shouldValidateCampaignWithoutError() {
		assertDoesNotThrow(() ->
				XmlUtils.validateXMLSchema(Constants.MODEL_SAMPLEPROCESSING,
						"src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5/sampleProcessing.xml"));

	}
	
	/**
	 * This method tests the validation part of the file campaignWithErrors.xml.
	 */
	@Test
	void shouldValidateCampaignWithError() {
		assertThrows(
				Exception.class, // or a more specific type e.g. SAXException
				() -> XmlUtils.validateXMLSchema(
						Constants.MODEL_SAMPLEPROCESSING,
						"src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario1/sampleProcessing.xml")
		);
	}
	
	/* Load */
	
	@Test
	void loadSampleProcessingWithoutError() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.load(BatchOption.SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5/sampleProcessing.xml", "src/test/resources/out/sampleprocessing/testScenarios", PROCESSING));
	}
	
	@Test
	void loadSampleProcessingWithError() throws Exception {
		File deleteOutFile = new File("src/test/resources/out/sampleprocessing/testScenarios");
		FileUtils.cleanDirectory(deleteOutFile);

		try {
			pilotageLauncherService.load(BatchOption.SAMPLEPROCESSING,
			"src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario4/sampleProcessing.xml",
			"src/test/resources/out/sampleprocessing/testScenarios/", PROCESSING);
		} catch(ValidateException ve) {
			assertTrue(PathUtils.isDirContainsFile(
					Path.of("src/test/resources/out/sampleprocessing/testScenarios"), "sampleProcessing", ".error.xml"));
		}
	}
	
	/* Clean and reset */


	@AfterEach
	void cleanOutFolder() {
		FileHelper.purgeDirectory(new File("src/test/resources/out/sampleprocessing/testScenarios"));
		FileHelper.purgeDirectory(new File(PROCESSING));
	}
	
	@AfterAll
	static void deleteFiles() throws IOException {
		File deleteUnitTestsOutDir = new File("src/test/resources/out/sampleprocessing/testScenarios");
		FileUtils.deleteDirectory(deleteUnitTestsOutDir);
	}
}
