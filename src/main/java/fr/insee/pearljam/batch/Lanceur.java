package fr.insee.pearljam.batch;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "fr.insee.pearljam.batch")
public class Lanceur {
    public static void main(String[] args) {
        var ctx = SpringApplication.run(Lanceur.class, args);
        int code = SpringApplication.exit(ctx);
        System.exit(code);
    }
}
