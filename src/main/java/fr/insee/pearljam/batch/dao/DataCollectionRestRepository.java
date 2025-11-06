package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.config.ApplicationConfig;
import fr.insee.pearljam.batch.dto.CampaignDataCollectionDto;
import fr.insee.pearljam.batch.dto.InterrogationDataCollectionDto;
import fr.insee.pearljam.batch.exception.DataCollectionApiException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Repository
@RequiredArgsConstructor
public class DataCollectionRestRepository implements DataCollectionRepository {

    private final RestClient restClient;
    private final ApplicationConfig appConfig;
    private static final Logger log = LogManager.getLogger(DataCollectionRestRepository.class);

    @Override
    public CampaignDataCollectionDto retrieveCampaign(String campaignId) throws DataCollectionApiException {
        log.info("searching campaign: {}", campaignId);
        String resourceUri = "/api/admin/campaigns/" + campaignId;
        return this.restClient.get()
                .uri(resourceUri)
                .attributes(clientRegistrationId(appConfig.keycloakDataCollectionRegistrationId()))
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
                        (request, response) -> log.info("Campaign {} found", campaignId))
                .body(CampaignDataCollectionDto.class);
    }

    @Override
    public void saveInterrogations(List<InterrogationDataCollectionDto> interrogations, String campaignId) {
        List<String> surveyUnitIds = interrogations.stream().map(InterrogationDataCollectionDto::surveyUnitId).toList();
        if(log.isInfoEnabled()) {
            log.info("Create interrogations for survey-units: [{}]", String.join(", ", surveyUnitIds));
        }
        String resourceUri = "/api/campaigns/" + campaignId + "/interrogations";

        this.restClient.post()
                .uri(resourceUri)
                .body(interrogations)
                .attributes(clientRegistrationId(appConfig.keycloakDataCollectionRegistrationId()))
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            String errorMessage = String.format("Error when creating interrogations for survey-units [%s]: Code HTTP: %s", String.join(", ", surveyUnitIds), response.getStatusCode().value());
                            throw new DataCollectionApiException(errorMessage);
                        }
                )
                .onStatus(
                        HttpStatusCode::is2xxSuccessful,
                        (request, response) -> log.info("interrogations created/updated"))
                .toBodilessEntity();
    }
}

