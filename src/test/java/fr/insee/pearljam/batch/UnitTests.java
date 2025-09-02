package fr.insee.pearljam.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import fr.insee.pearljam.batch.config.ApplicationConfig;
import fr.insee.pearljam.batch.service.CommunicationService;
import fr.insee.pearljam.batch.service.PilotageDBService;
import fr.insee.pearljam.batch.service.TriggerService;
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
import fr.insee.pearljam.batch.service.PilotageLauncherService;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UnitTests {

	@Autowired
	private PilotageLauncherService pilotageLauncherService;
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
	 * @throws Exception 
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
		assertEquals(true, PathUtils.isDirectoryExist("src/test/resources/in"));
	}
	
	@Test
	void directoryShouldntExist() {
		assertEquals(false, PathUtils.isDirectoryExist("src/test/resources/test"));
	}
	
	@Test
	void directoryShouldContainsExtension() {
		assertEquals(true, PathUtils.isDirContainsFileExtension(Path.of("src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5"), "sampleProcessing.xml"));
	}
	
	@Test
	void fileShouldExist() {
		assertEquals(true, PathUtils.isFileExist("src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5/sampleProcessing.xml"));
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
	 * @throws Exception
	 */
	@Test
	void shouldValidateCampaignWithoutError() {
		boolean error = false;
		try {
			XmlUtils.validateXMLSchema(Constants.MODEL_SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5/sampleProcessing.xml");
		} catch (Exception e) {
			error = true;
		}
		assertEquals(false, error);
	}
	
	/**
	 * This method tests the validation part of the file campaignWithErrors.xml.
	 * @throws Exception
	 */
	@Test
	void shouldValidateCampaignWithError() {
		boolean error = false;
		try {
			XmlUtils.validateXMLSchema(Constants.MODEL_SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario1/sampleProcessing.xml");
		} catch (Exception e) {
			error = true;
		}
		assertEquals(true, error);
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
			assertEquals(true, PathUtils.isDirContainsErrorFile(
			Path.of("src/test/resources/out/sampleprocessing/testScenarios"), "sampleProcessing", ".error.xml"));
		}
	}
	
	/* Clean and reset */
	
	/**
	 * This method tests the clean and reset step for the nomenclature part
	 * when there is no errors during the batch execution.
	 * @throws Exception
	 */
	@Test
	void cleandAndResetCampaignWithoutError() throws Exception {
		File deleteOutFile = new File("src/test/resources/out/sampleprocessing/testScenarios");
		FileUtils.cleanDirectory(deleteOutFile);
		pilotageLauncherService.cleanAndReset("sampleProcessing", "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5/sampleProcessing.xml", "src/test/resources/out/sampleprocessing/testScenarios", PROCESSING, BatchErrorCode.OK, BatchOption.SAMPLEPROCESSING);
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sampleprocessing/testScenarios"),"sampleProcessing", ".done.xml"));
	}
	
	/**
	 * This method tests the clean and reset step for the nomenclature part.
	 * when there is errors during the batch execution.
	 * @throws Exception
	 */
	@Test
	void cleandAndResetCampaignWithError() throws Exception {
		File deleteOutFile = new File("src/test/resources/out/sampleprocessing/testScenarios");
		FileUtils.cleanDirectory(deleteOutFile);
		pilotageLauncherService.cleanAndReset("sampleProcessing", "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario4/sampleProcessing.xml", "src/test/resources/out/sampleprocessing/testScenarios", PROCESSING, BatchErrorCode.KO_FONCTIONAL_ERROR, BatchOption.SAMPLEPROCESSING);
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/sampleprocessing/testScenarios"), "sampleProcessing",".error.xml"));
	}

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
