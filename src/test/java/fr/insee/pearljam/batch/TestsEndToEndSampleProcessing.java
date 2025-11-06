package fr.insee.pearljam.batch;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fr.insee.pearljam.batch.campaign.CommunicationMetadataType;
import fr.insee.pearljam.batch.config.ApplicationConfig;
import fr.insee.pearljam.batch.dao.CommunicationMetadataDao;
import fr.insee.pearljam.batch.utils.DBResetHelper;
import fr.insee.pearljam.batch.utils.FileHelper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileSystemUtils;

import fr.insee.pearljam.batch.campaign.PersonType;
import fr.insee.pearljam.batch.dao.PersonDao;
import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.service.PilotageLauncherService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.PathUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class TestsEndToEndSampleProcessing {

	@Autowired
	private PilotageLauncherService pilotageLauncherService;
	@Autowired
	private PersonDao  personDao;
	@Autowired
	private CommunicationMetadataDao  communicationMetadataDao;
	@Autowired
	private DBResetHelper dbResetHelper;
	@Autowired
	private ApplicationConfig applicationConfig;

	private final String outDirectory = "src/test/resources/out/sampleprocessing/testScenarios";
	private final String outCampaignDirectory = "src/test/resources/out/campaign";

	/**
	 * This method is executed before each test in this class.
	 * It set up the environment by inserting the data and copying the necessaries files.
	 * @throws Exception e
	 */
	@BeforeEach
	void setUp() throws Exception {
		dbResetHelper.reinitData();
		FileHelper.copyFiles("sampleprocessing");

		File dir = new File(outDirectory +"/campaign");
		if (!dir.exists()) {
			dir.mkdir();
		}

		dir = new File(outCampaignDirectory);
		if (!dir.exists()) {
			dir.mkdir();
		}

	}

	/**
	 * Scenario 1 : XML file is not valid
	 * @throws ValidateException ve
	 */
	@Test
	void testScenario1() throws Exception {
		String in = "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario1";
		try {
			pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, in, outDirectory);
		} catch(ValidateException ve) {
			assertTrue(ve.getMessage().contains("Error validating sampleProcessing.xml"));
			assertTrue(PathUtils.isDirContainsFile(Path.of(outDirectory), "sampleProcessing", ".error.xml"));
		}
	}


	/**
	 * Scenario 2 : Campaign in XML file not exist in Datacollection DB
	 * @throws Exception e
	 */
	@Test
	void testScenario2() throws Exception{
		try {
			assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario2", outDirectory));
		} catch(ValidateException ve) {
			assertTrue(ve.getMessage().contains("Code HTTP: 404"));
			assertTrue(PathUtils.isDirContainsFile(Path.of(outDirectory), "sampleProcessing", ".error.xml"));
		}
	}

	/**
	 * Scenario 3 : Campaign in XML file not exist in Pilotage DB
	 * @throws Exception e
	 */
	@Test
	void testScenario3() throws Exception {
		try {
			assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario3", outDirectory));
		} catch(ValidateException ve) {
			assertTrue(ve.getMessage().contains("does not exist in Pilotage DB"));
			assertTrue(PathUtils.isDirContainsFile(Path.of(outDirectory), "sampleProcessing", ".error.xml"));

		}
	}

	/**
	 * Scenario 4 : XML ok and campaign exist in both DB interviewer not exist in pilotage DB
	 * @throws Exception e
	 */
	@Test
	void testScenario4() throws Exception {
		assertEquals(BatchErrorCode.OK_FONCTIONAL_WARNING, pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario4", outDirectory));
		assertTrue(PathUtils.isDirContainsFile(Path.of(outDirectory), "sampleProcessing", ".warning.xml"));
		assertTrue(PathUtils.isDirContainsFile(Path.of(outCampaignDirectory), "campaign", ".warning.xml"));

	}

	/**
	 * Scenario 5 : XML ok and campaign exist in both DB
	 * @throws Exception e
	 */
	@Test
	void testScenario5() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5", outDirectory));
		assertTrue(PathUtils.isDirContainsFile(Path.of(outDirectory), "sampleProcessing", ".done.xml"));

		// check creation of metadata
		List<CommunicationMetadataType> metadata= communicationMetadataDao.findMetadataByCampaignIdAndMeshuggahIdAndSurveyUnitId("SIMPSONS2020X00","meshuggahId1","SIM1234");

		Map<String, String> actualMetadata = metadata.stream()
				.collect(Collectors.toMap(CommunicationMetadataType::getKey, CommunicationMetadataType::getValue));
		Map<String, String> expectedMetadata = Map.of(
				"key_one", "one",
				"key_two", "two",
				"key_three", "three"
		);
		assertEquals(expectedMetadata, actualMetadata, "Metadata key-value pairs do not match");

	}

	/**
	 * Scenario 6 : Check if favorite_email is populated
	 * @throws Exception e
	 */
	@Test
	void testScenario6() throws Exception {
		assertEquals(BatchErrorCode.OK, pilotageLauncherService.validateLoadClean(BatchOption.SAMPLEPROCESSING, "src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario6", outDirectory));
		assertTrue(PathUtils.isDirContainsFile(Path.of(outDirectory), "sampleProcessing", ".done.xml"));
		assertTrue(PathUtils.isDirContainsFile(Path.of(outCampaignDirectory), "campaign", ".done.xml"));

		List<Entry<Long, PersonType>> personsMap = personDao.getPersonsBySurveyUnitId("SIM1234");
		List<PersonType> persons = personsMap.stream().map(Entry::getValue).toList();
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
		FileHelper.purgeDirectory(new File(outDirectory));
		FileHelper.purgeDirectory(new File(outCampaignDirectory));
		File sample = new File("src/test/resources/in");
		if(sample.exists()) {
			sample.delete();
		}
	}

	@AfterAll
	static void deleteFiles() {
		File deleteFolderInDeleteForTest = new File("src/test/resources/in/sampleprocessing/testScenarios");
		FileSystemUtils.deleteRecursively(deleteFolderInDeleteForTest);
	}
}
