package fr.insee.pearljam.batch.config;

import fr.insee.pearljam.batch.service.ContextReferentialService;
import fr.insee.pearljam.batch.service.HabilitationService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;

@TestConfiguration
public class TestDataCollectionDBConfiguration {

    @Value("${fr.insee.pearljam.persistence.datacollection.driver}")
    private String dbDriver;
    @Value("${fr.insee.pearljam.persistence.datacollection.url}")
    private String dbUrl;
    @Value("${fr.insee.pearljam.persistence.datacollection.user}")
    private String dbUser;
    @Value("${fr.insee.pearljam.persistence.datacollection.password}")
    private String dbPassword;

    @Bean("dataCollectionDataSource")
    public DataSource dataCollectionDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dbDriver);
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }

    @Bean("dataCollectionConnection")
    public Connection dataCollectionConnection(@Autowired @Qualifier("dataCollectionDataSource") DataSource pilotageDataSource) {
        return DataSourceUtils.getConnection(pilotageDataSource);
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
