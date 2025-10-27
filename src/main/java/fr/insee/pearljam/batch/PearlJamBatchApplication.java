package fr.insee.pearljam.batch;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "fr.insee.pearljam.batch")
public class PearlJamBatchApplication {
    public static void main(String[] args) {
        var ctx = SpringApplication.run(PearlJamBatchApplication.class, args);
        int code = SpringApplication.exit(ctx);
        System.exit(code);
    }
}
