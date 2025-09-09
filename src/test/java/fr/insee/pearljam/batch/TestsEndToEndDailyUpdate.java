package fr.insee.pearljam.batch;

import java.io.File;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import fr.insee.pearljam.batch.utils.DBResetHelper;
import fr.insee.pearljam.batch.utils.FileHelper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileSystemUtils;

import fr.insee.pearljam.batch.campaign.StateType;
import fr.insee.pearljam.batch.dao.MessageDao;
import fr.insee.pearljam.batch.dao.StateDao;
import fr.insee.pearljam.batch.service.TriggerService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class TestsEndToEndDailyUpdate {

	@Autowired
	private TriggerService triggerService;

	@Autowired
	private MessageDao messageDao;

	@Autowired
	private StateDao stateDao;

	@Autowired
	private DBResetHelper dbResetHelper;
	
	/**
	 * This method is executed before each test in this class.
	 * It setup the environment by inserting the data
	 * @throws Exception 
	 */
	@BeforeEach
	void setUp() throws Exception {
		dbResetHelper.reinitData();
	}
	
	//Testing update of states and delete of noifications
	@Test
	void testScenario1() throws Exception {
		TimeZone utcTimeZone =TimeZone.getTimeZone("UTC");
		Calendar c = Calendar.getInstance(utcTimeZone);
		c.add(Calendar.MONTH, -1);
		assertEquals(5, messageDao.getIdsToDelete(c.getTimeInMillis()).size());

		// set a fixed clock in triggerService to avoid exceeding test data dates and failing tests
		Instant fixedInstant = LocalDateTime.of(2020, 7, 14, 0, 0).atZone(ZoneOffset.UTC).toInstant();
		triggerService.setClock(Clock.fixed(fixedInstant, utcTimeZone.toZoneId()));
		triggerService.updateStates();
		triggerService.initDefaultClock();
		
		assertTrue(messageDao.getIdsToDelete(c.getTimeInMillis()).isEmpty());
		assertEquals(List.of("NVM"), stateDao.getStateBySurveyUnitId("24").stream().map(StateType::getType).collect(Collectors.toList()));
		assertEquals(List.of("NVM","ANV"), stateDao.getStateBySurveyUnitId("25").stream().map(StateType::getType).collect(Collectors.toList()));
		assertEquals(List.of("NVM","ANV","VIN"), stateDao.getStateBySurveyUnitId("26").stream().map(StateType::getType).collect(Collectors.toList()));
		assertEquals(List.of("NVM","ANV","VIN"), stateDao.getStateBySurveyUnitId("27").stream().map(StateType::getType).collect(Collectors.toList()));
		assertEquals(List.of("NVM","ANV","VIN","NVA"), stateDao.getStateBySurveyUnitId("28").stream().map(StateType::getType).collect(Collectors.toList()));
	}
	
	@AfterEach
	void cleanOutFolder() {
		FileHelper.purgeDirectory(new File("src/test/resources/out/context/testScenarios"));
	}
	
	@AfterAll
	static void deleteFiles() {
		File deleteFolderInContextForTest = new File("src/test/resources/in/context/testScenarios");
		FileSystemUtils.deleteRecursively(deleteFolderInContextForTest);
	}
}
