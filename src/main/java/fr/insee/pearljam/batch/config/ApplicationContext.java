package fr.insee.pearljam.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
@ComponentScan(basePackages = {"fr.insee.pearljam.*"})
@ConfigurationPropertiesScan
@RequiredArgsConstructor
public class ApplicationContext {

    private final ApplicationConfig appConfig;


    private String filename = "";

    private String campaignName = "";

    @Bean
    public PlatformTransactionManager txManager() {
        return new DataSourceTransactionManager(pilotageDataSource()); // (2)
    }

    /***
     * This method create a new Datasource object
     * @return new Datasource
     */
    @Bean("pilotageDataSource")
    public DataSource pilotageDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(appConfig.pilotageDbDriver());
        dataSource.setUrl(String.format("jdbc:postgresql://%s:%s/%s", appConfig.pilotageDbHost(), appConfig.pilotageDbPort(), appConfig.pilotageDbSchema()));
        dataSource.setUsername(appConfig.pilotageDbUser());
        dataSource.setPassword(appConfig.pilotageDbPassword());
        return dataSource;
    }

    /***
     * This method return datasource connection
     * @param pilotageDataSource
     * @return Connection
     * @throws SQLException
     */
    @Bean("pilotageConnection")
    public Connection pilotageConnection(@Autowired @Qualifier("pilotageDataSource") DataSource pilotageDataSource) {
        return DataSourceUtils.getConnection(pilotageDataSource);
    }

    /***
     * Create a new JdbcTemplate with a datasource passed in parameter
     * @param pilotageDataSource
     * @return JdbcTemplate
     */
    @Bean("pilotageJdbcTemplate")
    public JdbcTemplate pilotageJdbcTemplate(@Autowired @Qualifier("pilotageDataSource") DataSource pilotageDataSource) {
        JdbcTemplate pilotageJdbcTemplate = null;
        pilotageJdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(pilotageConnection(pilotageDataSource), false));
        pilotageJdbcTemplate.setResultsMapCaseInsensitive(true);
        return pilotageJdbcTemplate;
    }

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
}
