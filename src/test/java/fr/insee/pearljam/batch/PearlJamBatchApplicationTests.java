package fr.insee.pearljam.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import fr.insee.pearljam.batch.config.ApplicationContext;
import fr.insee.pearljam.batch.service.TriggerService;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import fr.insee.pearljam.batch.service.ContextReferentialService;
import fr.insee.pearljam.batch.service.HabilitationService;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ResourceAccessor;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class implements the two classes of test It also initialize the testing
 * part.
 * 
 * @author scorcaud
 *
 */

abstract class PearlJamBatchApplicationTests {

	private static final Logger logger = LogManager.getLogger(PearlJamBatchApplicationTests.class);

	static final PostgreSQLContainer<?> postgreSQLContainerPilotage;

	static final PostgreSQLContainer<?> postgreSQLContainerDataCollection;

	static {
		postgreSQLContainerPilotage = new PostgreSQLContainer<>("postgres")
				.withExtraHost("localhost", "127.0.0.1")
				.withExposedPorts(PostgreSQLContainer.POSTGRESQL_PORT)
				.withDatabaseName("XXXXXXXX")
				.withUsername("XXXXXXXX")
				.withPassword("XXXXXXXX")
				.withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
						new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(5433), new ExposedPort(PostgreSQLContainer.POSTGRESQL_PORT)))
				));
		postgreSQLContainerPilotage.start();

		postgreSQLContainerDataCollection = new PostgreSQLContainer<>("postgres")
				.withExtraHost("localhost", "127.0.0.1")
				.withExposedPorts(PostgreSQLContainer.POSTGRESQL_PORT)
				.withDatabaseName("YYYYYYY")
				.withUsername("YYYYYYY")
				.withPassword("YYYYYYY")
				.withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
						new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(5434), new ExposedPort(PostgreSQLContainer.POSTGRESQL_PORT)))
				));
		postgreSQLContainerDataCollection.start();
	}

	@BeforeAll
	public static void datasourceProperties() throws IOException {
		logger.warn("Test TestsEndToEndExtractCampaign is ignored");
		logger.info("Tests starts");

		System.setProperty("fr.insee.pearljam.persistence.database.host", postgreSQLContainerPilotage.getContainerIpAddress());
		System.setProperty("fr.insee.pearljam.persistence.database.port",
				Integer.toString(postgreSQLContainerPilotage.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)));
		System.setProperty("fr.insee.pearljam.persistence.database.schema", postgreSQLContainerPilotage.getDatabaseName());
		System.setProperty("fr.insee.pearljam.persistence.database.user", postgreSQLContainerPilotage.getUsername());
		System.setProperty("fr.insee.pearljam.persistence.database.password", postgreSQLContainerPilotage.getPassword());
		System.setProperty("fr.insee.pearljam.persistence.database.driver", "org.postgresql.Driver");
		System.setProperty("fr.insee.pearljam.folder.in", "src/test/resources/in");
		System.setProperty("fr.insee.pearljam.folder.out", "src/test/resources/out");

		System.setProperty("fr.insee.queen.persistence.database.host", postgreSQLContainerDataCollection.getContainerIpAddress());
		System.setProperty("fr.insee.queen.persistence.database.port",
				Integer.toString(postgreSQLContainerDataCollection.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)));
		System.setProperty("fr.insee.queen.persistence.database.schema", postgreSQLContainerDataCollection.getDatabaseName());
		System.setProperty("fr.insee.queen.persistence.database.user", postgreSQLContainerDataCollection.getUsername());
		System.setProperty("fr.insee.queen.persistence.database.password", postgreSQLContainerDataCollection.getPassword());
		System.setProperty("fr.insee.queen.persistence.database.driver", "org.postgresql.Driver");
		System.setProperty("fr.insee.queen.folder.in", "src/test/resources/in");
		System.setProperty("fr.insee.queen.folder.out", "src/test/resources/out");
		System.setProperty("fr.insee.pearljam.folder.queen.in", "src/test/resources/in");
		System.setProperty("fr.insee.pearljam.folder.queen.out", "src/test/resources/out");

		System.setProperty(
				"fr.insee.pearljam.context.synchronization.interviewers.reaffectation.threshold.absolute",
				"2");
		System.setProperty(
				"fr.insee.pearljam.context.synchronization.interviewers.reaffectation.threshold.relative",
				"50");
		System.setProperty(
				"fr.insee.pearljam.context.synchronization.organization.reaffectation.threshold.absolute",
				"2");
		System.setProperty(
				"fr.insee.pearljam.context.synchronization.organization.reaffectation.threshold.relative",
				"50");

		Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.WARN);
		System.setProperty("fr.insee.pearljam.defaultSchema", "public");
	}

	/**
	 * This method initialize the data for testing
	 * 
	 * @throws Exception
	 */
	static void initData() throws Exception {
		PGSimpleDataSource dsPilotage = new PGSimpleDataSource();
		// Datasource initialization
		dsPilotage.setUrl(postgreSQLContainerPilotage.getJdbcUrl());
		dsPilotage.setUser(postgreSQLContainerPilotage.getUsername());
		dsPilotage.setPassword(postgreSQLContainerPilotage.getPassword());
		DatabaseConnection dbconnPilotage = new JdbcConnection(dsPilotage.getConnection());
		ResourceAccessor raPilotage = new DirectoryResourceAccessor(Paths.get("src/test/resources/sql"));
		Liquibase liquibasePilotage = new Liquibase("masterPilotage.xml", raPilotage, dbconnPilotage);
		liquibasePilotage.dropAll();
		liquibasePilotage.update(new Contexts());
		liquibasePilotage.close();
		logger.info("DB Pilotage created");

		
		PGSimpleDataSource dsDataCollection = new PGSimpleDataSource();
		// Datasource initialization
		dsDataCollection.setUrl(postgreSQLContainerDataCollection.getJdbcUrl());
		dsDataCollection.setUser(postgreSQLContainerDataCollection.getUsername());
		dsDataCollection.setPassword(postgreSQLContainerDataCollection.getPassword());
		DatabaseConnection dbconnDataCollection = new JdbcConnection(dsDataCollection.getConnection());
		ResourceAccessor raDataCollection = new DirectoryResourceAccessor(Paths.get("src/test/resources/sql"));
		Liquibase liquibaseDataCollection = new Liquibase("masterDataCollection.xml", raDataCollection, dbconnDataCollection);
		liquibaseDataCollection.dropAll();
		liquibaseDataCollection.update(new Contexts());
		liquibaseDataCollection.close();
		logger.info("DB DataCollection created");
	}

	/**
	 * This method copy the necessary files that are used in the tests. This method
	 * is useful because of the clean and reset method which remove these two files.
	 * 
	 * @throws IOException
	 */
	static void copyFiles(String name) throws IOException {
		File initInDir = new File("src/test/resources/in/" + name + "/init");
		File testScenarioInDir = new File("src/test/resources/in/" + name + "/testScenarios");
		if (!testScenarioInDir.exists()) {
			testScenarioInDir.mkdir();
		}
		
		File testScenarioProcessingDir = new File("src/test/resources/in/" + name + "/testScenarios/processing");
		if (!testScenarioProcessingDir.exists()) {
			testScenarioProcessingDir.mkdir();
		}
		
		FileUtils.copyDirectory(initInDir, testScenarioInDir);
		List<Path> subfolder = Files.walk(new File("src/test/resources/in/" + name + "/testScenarios").toPath(), 1)
	            .filter(Files::isDirectory)
	            .collect(Collectors.toList());
		subfolder.remove(0);
		subfolder.parallelStream().forEach(path -> 
			new File("src/test/resources/in/" + name + "/testScenarios/" + path.getFileName().toString() + "/processing").mkdir());
		
		File outDir = new File("src/test/resources/out");
		if (!outDir.exists()) {
			outDir.mkdir();
		}
		File campaignOutDir = new File("src/test/resources/out/" + name);
		if (!campaignOutDir.exists()) {
			campaignOutDir.mkdir();
		}
		File testScenarioOutDir = new File("src/test/resources/out/" + name + "/testScenarios");
		if (!testScenarioOutDir.exists()) {
			testScenarioOutDir.mkdir();
		}
		File unitTestsOutDir = new File("src/test/resources/out/unitTests");
		if (!unitTestsOutDir.exists()) {
			unitTestsOutDir.mkdir();
		}
		
		File synchroTestsOutDir = new File("src/test/resources/out/contextReferentialSynchro");
		if (!synchroTestsOutDir.exists()) {
			synchroTestsOutDir.mkdir();
		}
		File synchroTestsOutDirSynchroFolder = new File("src/test/resources/out/contextReferentialSynchro/synchro");
		if (!synchroTestsOutDirSynchroFolder.exists()) {
			synchroTestsOutDirSynchroFolder.mkdir();
		}
		File processingFolder = new File("src/test/resources/processing");
		if (!processingFolder.exists()) {
			processingFolder.mkdir();
		}
		File dirInSample = new File("src/test/resources/in/sample");
		if (!dirInSample.exists()) {
			dirInSample.mkdir();
		}
		File dirOutSample = new File("src/test/resources/out/sample");
		if (!dirOutSample.exists()) {
			dirOutSample.mkdir();
		}
		File dirInCampaign = new File("src/test/resources/in/campaign");
		if (!dirInCampaign.exists()) {
			dirInCampaign.mkdir();
		}
		File dirOutCampaign = new File("src/test/resources/out/campaign");
		if (!dirOutCampaign.exists()) {
			dirOutCampaign.mkdir();
		}
	}

	@Bean
    @Primary
    ContextReferentialService contextReferentialService() {
        return Mockito.mock(ContextReferentialService.class);
    }

	@Bean
	@Primary
	HabilitationService habilitationService() {
		return Mockito.mock(HabilitationService.class);
	}
}