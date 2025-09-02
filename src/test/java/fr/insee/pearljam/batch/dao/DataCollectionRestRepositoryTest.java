package fr.insee.pearljam.batch.dao;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;

import fr.insee.pearljam.batch.config.ApplicationConfig;
import fr.insee.pearljam.batch.dto.CampaignDataCollectionDto;
import fr.insee.pearljam.batch.exception.DataCollectionApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

class DataCollectionRestRepositoryTest {

    private DataCollectionRestRepository dataCollectionRestRepository;
    private WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();

        RestClient restClient = RestClient.builder()
                .baseUrl(wireMockServer.baseUrl())
                .build();

        ApplicationConfig applicationConfig = new ApplicationConfig(null, null, null,
                null, null, null,
                null, null, null,
                null, null, null,
                null, "registration-id", null,
                null, null, null);
        dataCollectionRestRepository = new DataCollectionRestRepository(restClient, applicationConfig);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Should return campaign")
    void testRetrieveCampaignSuccess() {
        // given
        String campaignId = "123";

        String jsonResponse = """
                {
                  "id": "123",
                  "questionnaireIds": ["q1", "q2", "q3"]
                }
                """;
        wireMockServer.stubFor(get("/api/admin/campaigns/" + campaignId)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)));

        // when
        CampaignDataCollectionDto campaignDto = dataCollectionRestRepository.retrieveCampaign(campaignId);

        // then
        assertThat(campaignDto).isNotNull();
        assertThat(campaignDto.id()).isEqualTo("123");
        assertThat(campaignDto.questionnaireIds()).contains("q1", "q2", "q3");
    }

    @Test
    @DisplayName("Should throw exception when response is en error response")
    void testRetrieveCampaignServerError() {
        // given
        String campaignId = "789";

        wireMockServer.stubFor(get("/api/admin/campaigns/" + campaignId)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        // when & then
        assertThatThrownBy(() -> dataCollectionRestRepository.retrieveCampaign(campaignId))
                .isInstanceOf(DataCollectionApiException.class)
                .hasMessageContaining("Error when retrieving campaign 789:, Code HTTP: 500");
    }

    @Test
    @DisplayName("Should delete interrogation successfully")
    void testDeleteInterrogationSuccess() {
        // given
        String interrogationId = "018f63af-09e3-7e6d-8492-f26e32a6cd19";

        wireMockServer.stubFor(delete(urlEqualTo("/api/interrogations/" + interrogationId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())));

        // when
        dataCollectionRestRepository.deleteInterrogation(interrogationId);

        // then
        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/api/interrogations/" + interrogationId)));
    }

    @Test
    @DisplayName("Should throw exception when error occurs during interrogation deletion")
    void testDeleteInterrogationServerError() {
        // given
        String interrogationId = "018f63af-09e3-7e6d-8492-f26e32a6cd19";

        wireMockServer.stubFor(delete(urlEqualTo("/api/interrogations/" + interrogationId))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        // when & then
        assertThatThrownBy(() -> dataCollectionRestRepository.deleteInterrogation(interrogationId))
                .isInstanceOf(DataCollectionApiException.class)
                .hasMessageContaining("Error when deleting interrogation " + interrogationId + ":, Code HTTP: 500");
    }


    @Test
    @DisplayName("Should create interrogation successfully")
    void testCreateInterrogationSuccess() {
        // given
        String surveyUnitId = "SU101";
        String campaignId = "CAMP1";
        String interrogationId = "018f63af-09e3-7e6d-8492-f26e32a6cd19";
        String questionnaireModelId = "questionnaire-id";
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put("key", "value");
        String expectedJson = """
                {
                  "id" : "018f63af-09e3-7e6d-8492-f26e32a6cd19",
                  "surveyUnitId" : "SU101",
                  "questionnaireId" : "questionnaire-id",
                  "personalization" : [],
                  "comment" : {},
                  "data" : {
                    "key" : "value"
                  }
                }
                """;
        String resourceUri = "/api/campaign/" + campaignId + "/interrogation";
        wireMockServer.stubFor(post(urlEqualTo(resourceUri))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())));

        // when
        dataCollectionRestRepository.createOrUpdateInterrogation(interrogationId, surveyUnitId, questionnaireModelId, campaignId, data);

        // then
        wireMockServer
                .verify(postRequestedFor(urlEqualTo(resourceUri))
                .withRequestBody(equalToJson(expectedJson)));
    }

    @Test
    @DisplayName("Should throw exception when error occurs during interrogation creation")
    void testCreateInterrogationServerError() {
        // given
        String surveyUnitId = "SU101";
        String campaignId = "CAMP1";
        String interrogationId = "018f63af-09e3-7e6d-8492-f26e32a6cd19";
        String questionnaireModelId = "questionnaire-id";
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        data.put("key", "value");

        String resourceUri = "/api/campaign/" + campaignId + "/interrogation";
        wireMockServer.stubFor(post(urlEqualTo(resourceUri))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        // when & then
        assertThatThrownBy(() -> dataCollectionRestRepository.createOrUpdateInterrogation(interrogationId, surveyUnitId, questionnaireModelId, campaignId, data))
                .isInstanceOf(DataCollectionApiException.class)
                .hasMessageContaining("Error when creating/updating interrogation: " + interrogationId + ", survey-unit: "+ surveyUnitId + ", Code HTTP: 500");
    }
}
