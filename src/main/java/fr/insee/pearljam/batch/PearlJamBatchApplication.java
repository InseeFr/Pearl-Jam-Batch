package fr.insee.pearljam.batch;


import fr.insee.pearljam.batch.config.ApplicationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "fr.insee.pearljam.batch")
@EnableConfigurationProperties(ApplicationConfig.class)
public class PearlJamBatchApplication {
    public static void main(String[] args) {
        var ctx = SpringApplication.run(PearlJamBatchApplication.class, args);
        int code = SpringApplication.exit(ctx);
        System.exit(code);
    }
}
