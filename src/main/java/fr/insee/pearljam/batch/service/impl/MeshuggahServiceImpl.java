package fr.insee.pearljam.batch.service.impl;

import fr.insee.pearljam.batch.communication.CommunicationTemplate;
import fr.insee.pearljam.batch.exception.MissingCommunicationException;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.service.KeycloakService;
import fr.insee.pearljam.batch.service.MeshuggahService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class MeshuggahServiceImpl implements MeshuggahService {
    private static final Logger LOGGER = LogManager.getLogger(MeshuggahServiceImpl.class);

    private final RestTemplate restTemplate;
    private final KeycloakService keycloakService;

    @Value(("${fr.insee.pearljam.communication.service.url}"))
    String meshuggahUrl;

    @Value(("${fr.insee.pearljam.publication.service.url}"))
    String editiqueUrl;

    private HttpHeaders getHabilitationHeaders() throws SynchronizationException {
        String token = keycloakService.getContextReferentialToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    @Override
    public CommunicationTemplate getCommunicationTemplate(String templateId) throws MissingCommunicationException,
            SynchronizationException {
        String findCommunicationPath = "api/communicationTemplates";
        String uri = String.join("/", meshuggahUrl, findCommunicationPath, templateId);
        LOGGER.info("Retrieving communication template {}", templateId);
        try {
            HttpHeaders headers = getHabilitationHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the API call for the current templateId
            ResponseEntity<CommunicationTemplate> response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            entity,
                            CommunicationTemplate.class
                    );
            return response.getBody();
        } catch (RestClientException e) {
            throw new MissingCommunicationException(templateId, e);
        }
    }

    @Override
    public String getNewEditionNumber() throws SynchronizationException, RestClientException {
        String getEditionNumber = "api/edition-number";
        String uri = String.join("/", meshuggahUrl, getEditionNumber);
        HttpHeaders headers = getHabilitationHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<EditionNumber> response =
                restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        entity,
                        EditionNumber.class
                );
        return response.getBody().editionNumber;
    }

    private record EditionNumber(String editionNumber) {
    }

    public boolean postPublication(File fileToPublish, String communicationModele) {
        try {
            // Step 1: Prepare the file resource
            FileSystemResource fileResource = new FileSystemResource(fileToPublish);

            // Step 2: Prepare headers
            HttpHeaders headers = getHabilitationHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));

            // Step 3: Prepare the body (Multipart data with XML file)
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("donneesXml", fileResource);

            // Step 4: Build the HttpEntity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Step 5: Send the request
            String url = String.format("%s/api/depot/papier?validationSchema=oui", editiqueUrl);
            LOGGER.info("Trying to publish {} ", communicationModele);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            // Step 6: Check the response and move file to archive sub folder

            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException | SynchronizationException e) {
            LOGGER.warn(String.format("Can't retrieve communicationModele: %s",
                    communicationModele), e);
            return false;
        }
    }

}
