package fr.insee.pearljam.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.FileSystemUtils;

import fr.insee.pearljam.batch.config.ApplicationContext;
import fr.insee.pearljam.batch.dao.MessageDao;
import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.service.PilotageLauncherService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.PathUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestsEndToEndDeleteCampaign extends PearlJamBatchApplicationTests {
	
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContext.class);
	
	PilotageLauncherService pilotageLauncherService = context.getBean(PilotageLauncherService.class);
	
	private static final String OUT = "src/test/resources/out/delete/testScenarios";

	/**
	 * This method is executed before each test in this class.
	 * It setup the environment by inserting the data and copying the necessaries files.
	 * @throws Exception 
	 */
	@BeforeEach
	void setUp() throws Exception {
		initData();
		copyFiles("delete");
	}
	
	static UnitTests unitTests = new UnitTests();
	
	/**
	 * Scenario 1 : XML file is not valid (<Ide> markup instead of <Id>)
	 * @throws ValidateException
	 */
	@Test
	void testScenario1() throws Exception {
		String in = "src/test/resources/in/delete/testScenarios/deleteScenario1";
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.DELETECAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error validating campaign.to.delete.xml : "));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.error.xml"));
		}
	}
	
	/**
	 * Scenario 2 : Organizational unit is missing in XML file
	 * @throws Exception
	 */
	@Test
	void testScenario2() throws Exception {
		String in = "src/test/resources/in/delete/testScenarios/deleteScenario2";
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.DELETECAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error validating campaign.xml : "));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.error.xml"));
		}
	}
	
	/**
	 * Scenario 3 : Interviewer associated to an other Organizational unit
	 * @throws Exception
	 */
	@Test
	void testScenario3() throws Exception {
		String in = "src/test/resources/in/delete/testScenarios/deleteScenario3";
		try {
			assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING,	pilotageLauncherService.validateLoadClean(BatchOption.DELETECAMPAIGN, in, OUT));
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error validating campaign.xml : "));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.warning.xml"));
		}
	}
	
	/**
	 * Scenario 4 : Campaing in XML file not exist
	 * @throws Exception
	 */
	@Test
	void testScenario4() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario4", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.warning.xml"));
	}
	
	/**
	 * Scenario 5 : XML ok, campaign exist but no survey-units to treat in the file
	 * @throws Exception
	 */
	@Test
	void testScenario5() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario5", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.archive.xml"));
	}
	
	/**
	 * Scenario 6 : XML ok, campaign exist with 1 survey-unit in the XML file
	 * @throws Exception
	 */
	@Test
	void testScenario6() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario6", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.archive.xml"));
	}
	
	/**
	 * Scenario 7 : XML ok, campaign exist with multiple survey-units in the XML file
	 * @throws Exception
	 */
	@Test
	void testScenario7() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario7", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.archive.xml"));
	}
	
	/**
	 * Scenario 8 : XML ok, campaign exist with 1 survey-unit that doesn't exist
	 * @throws Exception
	 */
	@Test
	void testScenario8() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario8", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.warning.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.archive.xml"));
	}
	
	/**
	 * Scenario 9 : XML ok, campaign exist, multiple survey-units with 1 that doesn't exist
	 * @throws Exception
	 */
	@Test
	void testScenario9() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario9", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.warning.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.archive.xml"));
	}
	
	/**
	 * Scenario 10 : XML ok, campaign exist with multiple survey-units in the XML file
	 * Test that the notifications link to campaign are deleted
	 * @throws Exception
	 */
	@Test
	void testScenario10() throws Exception {
		MessageDao messageDao = context.getBean(MessageDao.class);
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario7", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "campaign","delete.archive.xml"));
		assertEquals(false, messageDao.isIdPresentForCampaignId("SIMPSONS2020X00"));
	}
	
	@AfterEach
	void cleanOutFolder() {
		purgeDirectory(new File(OUT));
	}
	
	void purgeDirectory(File dir) {
	    for (File file: dir.listFiles()) {
	        if (file.isFile())
	            file.delete();
	    }
	}
	
	@AfterAll
	static void deleteFiles() throws IOException {
		File deleteFolderInDeleteForTest = new File("src/test/resources/in/delete/testScenarios");
		FileSystemUtils.deleteRecursively(deleteFolderInDeleteForTest);
	}
}
