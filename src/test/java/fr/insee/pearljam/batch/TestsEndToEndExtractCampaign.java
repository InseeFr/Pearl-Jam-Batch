package fr.insee.pearljam.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.FileSystemUtils;

import fr.insee.pearljam.batch.config.ApplicationContext;
import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.service.PilotageLauncherService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.PathUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
class TestsEndToEndExtractCampaign extends PearlJamBatchApplicationTests {
	
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContext.class);
	
	PilotageLauncherService pilotageLauncherService = context.getBean(PilotageLauncherService.class);
	
	private static final String OUT = "src/test/resources/out/extract/testScenarios";

	/**
	 * This method is executed before each test in this class.
	 * It setup the environment by inserting the data and copying the necessaries files.
	 * @throws Exception 
	 */
	@BeforeEach
	void setUp() throws Exception {
		initData();
		copyFiles("extract");
	}
	
	static UnitTests unitTests = new UnitTests();
	
	/**
	 * Scenario 1 : XML file is not valid
	 * @throws ValidateException
	 */
	@Test
	void testScenario1() throws Exception {
		String in = "src/test/resources/in/extract/testScenarios/extractScenario1";
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.EXTRACT, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error validating campaign.to.extract.xml : "));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","extract.error.xml"));
		}
	}
	
	
	/**
	 * Scenario 2 : Campaing in XML file not exist
	 * @throws Exception
	 */
	@Test
	void testScenario2() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.EXTRACT, "src/test/resources/in/extract/testScenarios/extractScenario2", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","extract.warning.xml"));
	}
	
	/**
	 * Scenario 3 : XML ok, campaign exist but no survey-units to treat in the file
	 * @throws Exception
	 */
	@Test
	void testScenario3() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.EXTRACT, "src/test/resources/in/extract/testScenarios/extractScenario3", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","extract.done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","extract.xml"));
	}
	
	
	@AfterEach
	void cleanOutFolder() {
		purgeDirectory(new File(OUT));
	}
	
	@AfterAll
	static void deleteFiles() throws IOException {
		File deleteFolderInDeleteForTest = new File("src/test/resources/in/extract/testScenarios");
		FileSystemUtils.deleteRecursively(deleteFolderInDeleteForTest);
	}
}
