package fr.insee.pearljam.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.FileSystemUtils;

import fr.insee.pearljam.batch.campaign.PersonType;
import fr.insee.pearljam.batch.config.ApplicationContext;
import fr.insee.pearljam.batch.dao.PersonDao;
import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.service.PilotageLauncherService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.PathUtils;
import fr.insee.queen.batch.service.DatasetService;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestsEndToEndSampleProcessing extends PearlJamBatchApplicationTests {
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContext.class);
	
	PilotageLauncherService pilotageLauncherService = context.getBean(PilotageLauncherService.class);

	DatasetService datasetService = context.getBean(DatasetService.class);

	PersonDao  personDao = context.getBean(PersonDao.class);
	
	
	
	private static final String OUT = "src/test/resources/out/sampleprocessing/testScenarios";
	private static final String OUT_SAMPLE = "src/test/resources/out/sample";
	private static final String OUT_CAMPAIGN = "src/test/resources/out/campaign";

	/**
	 * This method is executed before each test in this class.
	 * It setup the environment by inserting the data and copying the necessaries files.
	 * @throws Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		initData();
		copyFiles("sampleprocessing");
		File dir = new File("src/test/resources/in/sample");
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(OUT+"/campaign");
		if (!dir.exists()) {
			dir.mkdir();
		}

		dir = new File(OUT_SAMPLE);
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(OUT_CAMPAIGN);
		if (!dir.exists()) {
			dir.mkdir();
		}

	}

	static UnitTests unitTests = new UnitTests();

	/**
	 * Scenario 1 : XML file is not valid
	 * @throws ValidateException
	 */
	@Test
	void testScenario1() throws Exception {
		String in = "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario1";
		datasetService.createDataSet();
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, in, OUT);
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("Error validating sampleProcessing.xml"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "sampleProcessing",".error.xml"));
		}
	}


	/**
	 * Scenario 2 : Campaign in XML file not exist in Datacollection DB
	 * @throws Exception
	 */
	@Test
	void testScenario2() throws Exception{
		datasetService.createDataSet();
		try {
			assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario2", OUT));
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("does not exist in Data-collection DB"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "sampleProcessing",".error.xml"));
		}
	}

	/**
	 * Scenario 3 : Campaign in XML file not exist in Pilotage DB
	 * @throws Exception
	 */
	@Test
	void testScenario3() throws Exception {
		datasetService.createDataSet();
		try {
			assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario3", OUT));
		} catch(ValidateException ve) {
			assertEquals(true, ve.getMessage().contains("does not exist in Pilotage DB"));
			assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "sampleProcessing",".error.xml"));

		}
	}

	/**
	 * Scenario 4 : XML ok and campaign exist in both DB interviewer not exist in pilotage DB
	 * @throws Exception
	 */
	@Test
	void testScenario4() throws Exception {
		datasetService.createDataSet();
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario4", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "sampleProcessing",".warning.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT_CAMPAIGN), "campaign",".warning.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT_SAMPLE), "sample",".warning.xml"));

	}

	/**
	 * Scenario 5 : XML ok and campaign exist in both DB
	 * @throws Exception
	 */
	@Test
	void testScenario5() throws Exception {
		datasetService.createDataSet();
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "sampleProcessing",".done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT_CAMPAIGN), "campaign",".done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT_SAMPLE), "sample",".done.xml"));

	}

	/**
	 * Scenario 6 : Check if favorite_email is populated
	 * @throws Exception
	 */
	@Test
	void testScenario6() throws Exception {
		datasetService.createDataSet();
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario6", OUT));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT), "sampleProcessing",".done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT_CAMPAIGN), "campaign",".done.xml"));
		assertEquals(true, PathUtils.isDirContainsErrorFile(Path.of(OUT_SAMPLE), "sample", ".done.xml"));

		List<Entry<Long, PersonType>> personsMap = personDao.getPersonsBySurveyUnitId("SIM1234");
		List<PersonType> persons = personsMap.stream().map(entry -> entry.getValue()).collect(Collectors.toList());
		PersonType truePreferedEmailPerson = persons.stream().filter(p->p.getFirstName().equals("John")).findFirst().get();
		PersonType falsePreferedEmailPerson = persons.stream().filter(p->p.getFirstName().equals("Jane")).findFirst().get();
		PersonType missingPreferedEmailPerson = persons.stream().filter(p->p.getFirstName().equals("Pat")).findFirst().get();
		// XML with true
		assertEquals(true,truePreferedEmailPerson.isFavoriteEmail());
		// XML with false
		assertEquals(false,falsePreferedEmailPerson.isFavoriteEmail());
		// missing XML
		assertEquals(false,missingPreferedEmailPerson.isFavoriteEmail());
	}


	@AfterEach
	void cleanOutFolder() {
		purgeDirectory(new File(OUT));
		purgeDirectory(new File(OUT_CAMPAIGN));
		purgeDirectory(new File(OUT_SAMPLE));
		File sample = new File("src/test/resources/in");
		if(sample.exists()) {
			sample.delete();
		}
	}

	@AfterAll
	static void deleteFiles() throws IOException {
		File deleteFolderInDeleteForTest = new File("src/test/resources/in/sampleprocessing/testScenarios");
		FileSystemUtils.deleteRecursively(deleteFolderInDeleteForTest);
	}
}
