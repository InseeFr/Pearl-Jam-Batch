package fr.insee.pearljam.batch.dao;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.UUID;

import fr.insee.pearljam.batch.config.ApplicationConfig;
import fr.insee.pearljam.batch.dto.CampaignDataCollectionDto;
import fr.insee.pearljam.batch.dto.InterrogationDataCollectionDto;
import fr.insee.pearljam.batch.exception.DataCollectionApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DataCollectionRestRepository implements DataCollectionRepository {

    private final RestClient restClient;
    private final ApplicationConfig appConfig;

    public CampaignDataCollectionDto retrieveCampaign(String campaignId) throws DataCollectionApiException {
        log.info("searching campaign: {}", campaignId);
        String resourceUri = "/api/admin/campaigns/" + campaignId;
        return this.restClient.get()
                .uri(resourceUri)
                .attributes(clientRegistrationId(appConfig.getKeycloakDataCollectionRegistrationId()))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            String errorMessage = String.format("Error when retrieving campaign %s:, Code HTTP: %s", campaignId, response.getStatusCode().value());
                            throw new DataCollectionApiException(errorMessage);
                        }
                )
                .onStatus(
                        HttpStatusCode::is2xxSuccessful,
                        (request, response) -> log.info(String.format("Campaign %s found", campaignId)))
                .body(CampaignDataCollectionDto.class);
    }

    public void deleteInterrogation(String interrogationId) {
        log.info("Delete interrogation: {}", interrogationId);
        String resourceUri = "/api/interrogations/" + interrogationId;

        this.restClient.delete()
                .uri(resourceUri)
                .attributes(clientRegistrationId(appConfig.getKeycloakDataCollectionRegistrationId()))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            String errorMessage = String.format("Error when deleting interrogation %s:, Code HTTP: %s", interrogationId, response.getStatusCode().value());
                            throw new DataCollectionApiException(errorMessage);
                        }
                )
                .onStatus(
                        HttpStatusCode::is2xxSuccessful,
                        (request, response) -> log.info("interrogation {} deleted", interrogationId))
                .toBodilessEntity();
    }

    public void createOrUpdateInterrogation(String interrogationId, String surveyUnitId, String questionnaireModelId, String campaignId, ObjectNode data) {
        log.info("Create interrogation: {}, survey-unit: {}", interrogationId, surveyUnitId);
        String resourceUri = "/api/campaign/" + campaignId + "/interrogation";
        InterrogationDataCollectionDto interrogationDataCollectionDto =
                new InterrogationDataCollectionDto(interrogationId, surveyUnitId, questionnaireModelId, data);

        this.restClient.post()
                .uri(resourceUri)
                .body(interrogationDataCollectionDto)
                .attributes(clientRegistrationId(appConfig.getKeycloakDataCollectionRegistrationId()))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            String errorMessage = String.format("Error when creating/updating interrogation: %s, survey-unit: %s, Code HTTP: %s", interrogationId, surveyUnitId, response.getStatusCode().value());
                            throw new DataCollectionApiException(errorMessage);
                        }
                )
                .onStatus(
                        HttpStatusCode::is2xxSuccessful,
                        (request, response) -> log.info("interrogation {} created/updated", interrogationId))
                .toBodilessEntity();
    }
}

