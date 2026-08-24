package fr.insee.pearljam.batch;


import fr.insee.pearljam.batch.config.PropertyLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "fr.insee.pearljam.batch")
@Slf4j
public class PearlJamBatchApplication {
    static void main(String[] args) {
        var ctx = configure(new SpringApplicationBuilder()).build().run(args);
        int code = SpringApplication.exit(ctx);
        System.exit(code);
    }

    protected static SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PearlJamBatchApplication.class).listeners(new PropertyLogger());
    }
}
