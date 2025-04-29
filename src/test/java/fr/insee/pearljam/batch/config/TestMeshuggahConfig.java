package fr.insee.pearljam.batch.config;

import fr.insee.pearljam.batch.communication.CommunicationTemplate;
import fr.insee.pearljam.batch.service.MeshuggahService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;
import java.util.List;

@Configuration
public class TestMeshuggahConfig {

	@Bean
	@Primary
	public MeshuggahService fakeMeshuggahService() {
		return new FakeMeshuggahService();
	}

	public static class FakeMeshuggahService implements MeshuggahService {
		@Override
		public CommunicationTemplate getCommunicationTemplate(String id) {
			// Return a static template valid for testing
			CommunicationTemplate template = new CommunicationTemplate();
			template.setIdOperation("OPERATION_1");
			template.setCommunicationModel("modelA");
			template.setCommunicationType("REMINDER");
			template.setPartieNomFichierLibreZip("FREE");
			template.setCommunicationId(id);
			template.setInitAccuseReception(true);
			template.setMetadatas(List.of()); // optional
			return template;
		}

		@Override
		public String getNewEditionNumber() {
			return "ED000TEST";
		}

		@Override
		public boolean postPublication(File fileToPublish, String communicationModele) {
			return true;
		}

	}
}
