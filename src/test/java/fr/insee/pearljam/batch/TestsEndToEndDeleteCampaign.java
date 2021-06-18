package fr.insee.pearljam.batch;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.FileSystemUtils;

import fr.insee.pearljam.batch.config.ApplicationContext;
import fr.insee.pearljam.batch.dao.MessageDao;
import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.service.LauncherService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.PathUtils;

public class TestsEndToEndDeleteCampaign {
	
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContext.class);
	
	LauncherService launcherService = context.getBean(LauncherService.class);
	
	private static final String OUT = "src/test/resources/out/delete/testScenarios";

	/**
	 * This method is executed before each test in this class.
	 * It setup the environment by inserting the data and copying the necessaries files.
	 * @throws Exception 
	 */
	@Before
	public void setUp() throws Exception {
		PearlJamBatchApplicationTests.initData();
		PearlJamBatchApplicationTests.copyFiles("delete");
	}
	
	public static UnitTests unitTests = new UnitTests();
	
	/**
	 * Scenario 1 : XML file is not valid
	 * @throws ValidateException
	 */
	@Test
	public void testScenario1() throws Exception {
		String in = "src/test/resources/in/delete/testScenarios/deleteScenario1";
		try {
			launcherService.validateLoadClean(BatchOption.DELETECAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error validating campaign.xml : "));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.error.xml"));
		}
	}
	
	/**
	 * Scenario 2 : Organizational unit is missing in XML file
	 * @throws Exception
	 */
	@Test
	public void testScenario2() throws Exception {
		String in = "src/test/resources/in/delete/testScenarios/deleteScenario2";
		try {
			launcherService.validateLoadClean(BatchOption.DELETECAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error validating campaign.xml : "));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.error.xml"));
		}
	}
	
	/**
	 * Scenario 3 : Interviewer associated to an other Organizational unit
	 * @throws Exception
	 */
	@Test
	public void testScenario3() throws Exception {
		String in = "src/test/resources/in/delete/testScenarios/deleteScenario3";
		try {
			launcherService.validateLoadClean(BatchOption.DELETECAMPAIGN, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error validating campaign.xml : "));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.error.xml"));
		}
	}
	
	/**
	 * Scenario 4 : Campaing in XML file not exist
	 * @throws Exception
	 */
	@Test
	public void testScenario4() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, launcherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario4", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.error.xml"));
	}
	
	/**
	 * Scenario 5 : XML ok, campaign exist but no survey-units to treat in the file
	 * @throws Exception
	 */
	@Test
	public void testScenario5() throws Exception {
		assertEquals(BatchErrorCode.OK, launcherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario5", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.archive.xml"));
	}
	
	/**
	 * Scenario 6 : XML ok, campaign exist with 1 survey-unit in the XML file
	 * @throws Exception
	 */
	@Test
	public void testScenario6() throws Exception {
		assertEquals(BatchErrorCode.OK, launcherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario6", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.archive.xml"));
	}
	
	/**
	 * Scenario 7 : XML ok, campaign exist with multiple survey-units in the XML file
	 * @throws Exception
	 */
	@Test
	public void testScenario7() throws Exception {
		assertEquals(BatchErrorCode.OK, launcherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario7", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.archive.xml"));
	}
	
	/**
	 * Scenario 8 : XML ok, campaign exist with 1 survey-unit that doesn't exist
	 * @throws Exception
	 */
	@Test
	public void testScenario8() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, launcherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario8", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.error.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.archive.xml"));
	}
	
	/**
	 * Scenario 9 : XML ok, campaign exist, multiple survey-units with 1 that doesn't exist
	 * @throws Exception
	 */
	@Test
	public void testScenario9() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, launcherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario9", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.error.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.archive.xml"));
	}
	
	/**
	 * Scenario 10 : XML ok, campaign exist with multiple survey-units in the XML file
	 * Test that the notifications link to campaign are deleted
	 * @throws Exception
	 */
	@Test
	public void testScenario10() throws Exception {
		MessageDao messageDao = context.getBean(MessageDao.class);
		assertEquals(BatchErrorCode.OK, launcherService.validateLoadClean(BatchOption.DELETECAMPAIGN, "src/test/resources/in/delete/testScenarios/deleteScenario7", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of("src/test/resources/out/delete/testScenarios"), "campaign","delete.archive.xml"));
		assertEquals(false, messageDao.isIdPresentForCampaignId("SIMPSONS2020X00"));
	}
	
	@After
	public void cleanOutFolder() {
		purgeDirectory(new File("src/test/resources/out/delete/testScenarios"));
	}
	
	void purgeDirectory(File dir) {
	    for (File file: dir.listFiles()) {
	        if (file.isFile())
	            file.delete();
	    }
	}
	
	@AfterClass
	public static void deleteFiles() throws IOException {
		File deleteFolderInDeleteForTest = new File("src/test/resources/in/delete/testScenarios");
		FileSystemUtils.deleteRecursively(deleteFolderInDeleteForTest);
	}
}
