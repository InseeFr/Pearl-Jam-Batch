package fr.insee.pearljam.batch.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import fr.insee.queen.batch.config.ConditonJpa;
import fr.insee.queen.batch.config.ConditonMongo;
import fr.insee.queen.batch.service.FolderService;

@Configuration
@ComponentScan(basePackages = {"fr.insee.pearljam.*", "fr.insee.queen.batch.*"}, excludeFilters={
		@ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value= FolderService.class),@ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value= fr.insee.queen.batch.config.ApplicationContext.class)})
@PropertySource(value = {"classpath:/pearljam-bo.properties", "file:${properties.path}/pearljam-bo.properties"}, ignoreResourceNotFound = true)
public class ApplicationContext {

	@Autowired
	ApplicationConfig applicationReception;

	@Autowired
	ConfigurableEnvironment envSpring;

	@Value("${fr.insee.queen.key.paradata.id}")
	private String keyParadataIdSu;

	@Value("${fr.insee.queen.key.paradata.events}")
	private String keyParadataEvents;
	

	private String filename = "";
	
	private String campaignName = "";
	
	@PostConstruct
	private void fillReception() {
		ApplicationConfig.pilotageDbHost = envSpring.getProperty("fr.insee.pearljam.persistence.database.host");
		ApplicationConfig.pilotageDbPort = envSpring.getProperty("fr.insee.pearljam.persistence.database.port");
		ApplicationConfig.pilotageDbSchema = envSpring.getProperty("fr.insee.pearljam.persistence.database.schema");
		ApplicationConfig.pilotageDbUser = envSpring.getProperty("fr.insee.pearljam.persistence.database.user");
		ApplicationConfig.pilotageDbPassword = envSpring.getProperty("fr.insee.pearljam.persistence.database.password");
		ApplicationConfig.pilotageDbDriver = envSpring.getProperty("fr.insee.pearljam.persistence.database.driver");
		
		ApplicationConfig.dbHost = envSpring.getProperty("fr.insee.queen.persistence.database.host");
		ApplicationConfig.dbPort = envSpring.getProperty("fr.insee.queen.persistence.database.port");
		ApplicationConfig.dbSchema = envSpring.getProperty("fr.insee.queen.persistence.database.schema");
		ApplicationConfig.dbUser = envSpring.getProperty("fr.insee.queen.persistence.database.user");
		ApplicationConfig.dbPassword = envSpring.getProperty("fr.insee.queen.persistence.database.password");
		ApplicationConfig.dbDriver = envSpring.getProperty("fr.insee.queen.persistence.database.driver");
		
		ApplicationConfig.FOLDER_IN = envSpring.getProperty("fr.insee.pearljam.folder.in");
		ApplicationConfig.FOLDER_OUT = envSpring.getProperty("fr.insee.pearljam.folder.out");
		ApplicationConfig.contextReferentialScheme = envSpring.getProperty("fr.insee.pearljam.context.referential.service.url.scheme");
		ApplicationConfig.contextReferentialHost = envSpring.getProperty("fr.insee.pearljam.context.referential.service.url.host");
		ApplicationConfig.contextReferentialPort = envSpring.getProperty("fr.insee.pearljam.context.referential.service.url.port");
		ApplicationConfig.contextReferentialPath = envSpring.getProperty("fr.insee.pearljam.context.referential.service.url.path");
		ApplicationConfig.authServerURL = envSpring.getProperty("keycloak.auth-server-url");
		ApplicationConfig.realm = envSpring.getProperty("keycloak.realm");
		ApplicationConfig.FOLDER_IN_QUEEN = envSpring.getProperty("fr.insee.pearljam.folder.queen.in");
		ApplicationConfig.FOLDER_OUT_QUEEN = envSpring.getProperty("fr.insee.pearljam.folder.queen.out");

		ApplicationConfig.ldapServiceUrlScheme=envSpring.getProperty("fr.insee.pearljam.ldap.service.url.scheme");
		ApplicationConfig.ldapServiceUrlHost=envSpring.getProperty("fr.insee.pearljam.ldap.service.url.host");
		ApplicationConfig.ldapServiceUrlPort=envSpring.getProperty("fr.insee.pearljam.ldap.service.url.port");
		ApplicationConfig.ldapServiceUrlPath=envSpring.getProperty("fr.insee.pearljam.ldap.service.url.path");
		
	}

	/**
	 * Bean to get the filename
	 * @return
	 */
	@Bean(name = "filename")
	public String getFilename() {
		return filename;
	}
	

	/**
	 * Bean to get the campaign name
	 * @return
	 */
	@Bean
	public String getCampaignName() {
		return campaignName;
	}

	@Bean
	public PlatformTransactionManager txManager() {
	    return new DataSourceTransactionManager(pilotageDataSource()); // (2)
	}
	
	/***
	 * This method build the context referential base URL
	 * @return context referential base URL
	 */
	@Bean(name = "contextReferentialBaseUrl")
	public String getContextReferentialBaseUrl() {
		return ApplicationConfig.contextReferentialScheme + "://" + ApplicationConfig.contextReferentialHost + ":"
				+ ApplicationConfig.contextReferentialPort + "/"
				+ ApplicationConfig.contextReferentialPath;
	}
	
	/***
	 * This method build the keycloak Auth URL
	 * @return keycloak Auth URL
	 */
	@Bean(name = "keycloakAuthUrl")
	public String getKeycloakAuthUrl() {
		return ApplicationConfig.authServerURL + "/realms/" + ApplicationConfig.realm + "/protocol/openid-connect/token";
	}

	/***
	 * This method build the context referential base URL
	 * @return context referential base URL
	 */
	@Bean(name = "habilitationApiBaseUrl")
	public String getHabilitationApiBaseUrl() {
		return String.format("%s://%s:%s%s", ApplicationConfig.ldapServiceUrlScheme,
				ApplicationConfig.ldapServiceUrlHost, ApplicationConfig.ldapServiceUrlPort,
				ApplicationConfig.ldapServiceUrlPath);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/***
	 * This method create a new Datasource object
	 * @return new Datasource
	 */
	@Bean
	public DataSource pilotageDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(ApplicationConfig.pilotageDbDriver);
		dataSource.setUrl(String.format("jdbc:postgresql://%s:%s/%s", ApplicationConfig.pilotageDbHost, ApplicationConfig.pilotageDbPort, ApplicationConfig.pilotageDbSchema));
		dataSource.setUsername(ApplicationConfig.pilotageDbUser);
		dataSource.setPassword(ApplicationConfig.pilotageDbPassword);
		return dataSource;
	}

	/***
	 * This method return datasource connection
	 * @param pilotageDataSource
	 * @return Connection
	 * @throws SQLException
	 */
	@Bean("pilotageConnection")
	public Connection pilotageConnection(@Autowired @Qualifier("pilotageDataSource") DataSource pilotageDataSource) throws SQLException {
		return DataSourceUtils.getConnection(pilotageDataSource);
	}

	/***
	 * Create a new JdbcTemplate with a datasource passed in parameter
	 * @param pilotageDataSource
	 * @return JdbcTemplate
	 */
	@Bean("pilotageJdbcTemplate")
	public JdbcTemplate pilotageJdbcTemplate(@Autowired @Qualifier("pilotageDataSource") DataSource pilotageDataSource) throws SQLException {
		JdbcTemplate pilotageJdbcTemplate = null;
		try {
			pilotageJdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(pilotageConnection(pilotageDataSource), false));
		} catch (SQLException e) {
			e.printStackTrace();

		}
		pilotageJdbcTemplate.setResultsMapCaseInsensitive(true);
		return pilotageJdbcTemplate;
	}
	
	/***
	 * This method create a new Datasource object
	 * @return new Datasource
	 */
	@Bean
	@Conditional(value= ConditonJpa.class)
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(ApplicationConfig.dbDriver);
		dataSource.setUrl(String.format("jdbc:postgresql://%s:%s/%s", ApplicationConfig.dbHost, ApplicationConfig.dbPort, ApplicationConfig.dbSchema));
		dataSource.setUsername(ApplicationConfig.dbUser);
		dataSource.setPassword(ApplicationConfig.dbPassword);
		return dataSource;
	}

	/***
	 * This method return datasource connection
	 * @param dataSource
	 * @return Connection
	 * @throws SQLException
	 */
	@Bean("connection")
	@Conditional(value= ConditonJpa.class)
	public Connection connection(@Autowired @Qualifier("dataSource") DataSource dataSource) throws SQLException {
		return DataSourceUtils.getConnection(dataSource);
	}

	/***
	 * Create a new JdbcTemplate with a datasource passed in parameter
	 * @param dataSource
	 * @return JdbcTemplate
	 */
	@Bean("jdbcTemplate")
	@Conditional(value= ConditonJpa.class)
	public JdbcTemplate jdbcTemplate(@Autowired @Qualifier("dataSource") DataSource dataSource) throws SQLException {
		JdbcTemplate jdbcTemplate = null;
		try {
			jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection(dataSource), false));
		} catch (SQLException e) {
			e.printStackTrace();

		}
		jdbcTemplate.setResultsMapCaseInsensitive(true);
		return jdbcTemplate;
	}
	
	
	@Bean
	@Conditional(value= ConditonMongo.class)
	public MongoClient mongo() {
		ConnectionString connectionString = new ConnectionString(String.format("mongodb://%s:%s/%s", ApplicationConfig.pilotageDbHost, ApplicationConfig.pilotageDbPort, ApplicationConfig.pilotageDbSchema));
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.build();
		return MongoClients.create(mongoClientSettings);
	}

	/**
	 * Method used to create the mongoTemplate
	 * @return
	 * @throws Exception
	 */
	@Conditional(value= ConditonMongo.class)
	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(mongo(), "queen_api");
	}

	@Bean
	public String getKeyParadataIdSu() {
		return this.keyParadataIdSu;
	}

	@Bean
	public String getKeyParadataEvents() {
		return this.keyParadataEvents;
	}
}
