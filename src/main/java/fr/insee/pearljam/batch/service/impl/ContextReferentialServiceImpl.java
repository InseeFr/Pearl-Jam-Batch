package fr.insee.pearljam.batch.service.impl;

import fr.insee.pearljam.batch.Constants;
import fr.insee.pearljam.batch.dto.*;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.service.ContextReferentialService;
import fr.insee.pearljam.batch.service.KeycloakService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

// Class to call Context referential endpoints

@Service
public class ContextReferentialServiceImpl implements ContextReferentialService {
    private static final Logger logger = LogManager.getLogger(ContextReferentialServiceImpl.class);

    @Autowired
    @Qualifier("contextReferentialBaseUrl")
    String getContextReferentialBaseUrl;

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    RestTemplate restTemplate;

    private static final String NO_RESPONSE_MSG = "Could not get response from contextReferential";

    private void printUri(String uri) {
        logger.info("Calling {}", uri);
    }

    private void printResponse(String response) {
        logger.info("Response {}", response);
    }

    public List<InterviewerDto> getInterviewersFromOpale() throws SynchronizationException {
        String uri = getContextReferentialBaseUrl + Constants.API_OPALE_INTERVIEWERS;
        printUri(uri);

        HttpHeaders headers = getHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<InterviewersResponseDto> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
                InterviewersResponseDto.class);
        printResponse(response.getStatusCode().toString());

        InterviewersResponseDto body = response.getBody();

        if (body == null) {
            throw new SynchronizationException(NO_RESPONSE_MSG);
        }
        return body.getEnqueteurs();
    }

    public List<OrganizationUnitDto> getOrganizationUnitsFromOpale() throws SynchronizationException {
        String uri = getContextReferentialBaseUrl + Constants.API_OPALE_ORGANIZATION_UNITS;
        printUri(uri);

        HttpHeaders headers = getHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<OrganizationUnitsResponseDto> response = restTemplate.exchange(uri, HttpMethod.GET, entity,
                OrganizationUnitsResponseDto.class);
        printResponse(response.getStatusCode().toString());

        OrganizationUnitsResponseDto body = response.getBody();
        if (body == null) {
            throw new SynchronizationException(NO_RESPONSE_MSG);
        }

        return body.getOrganizationUnits();

    }

    public List<InterviewerAffectationsDto> getInterviewersAffectationsFromOpale() throws SynchronizationException {
        String uri = getContextReferentialBaseUrl + Constants.API_OPALE_INTERVIEWERS_AFFECTATIONS;

        HttpHeaders headers = getHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<InterviewersAffectationsResponseDto> response = restTemplate.exchange(uri, HttpMethod.GET,
                entity, InterviewersAffectationsResponseDto.class);
        printUri(uri);
        printResponse(response.getStatusCode().toString());

        InterviewersAffectationsResponseDto body = response.getBody();
        if (body == null) {
            throw new SynchronizationException(NO_RESPONSE_MSG);
        }
        logger.info("{} attributions returned", body.getInterviewers().size());
        return body.getInterviewers();
    }

    public List<OrganizationUnitAffectationsDto> getOrganizationUnitsAffectationsFromOpale()
            throws SynchronizationException {
        String uri = getContextReferentialBaseUrl + Constants.API_OPALE_ORGANIZATION_UNITS_AFFECTATIONS;

        HttpHeaders headers = getHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<OrganizationUnitsAffectationsResponseDto> response = restTemplate.exchange(uri, HttpMethod.GET,
                entity, OrganizationUnitsAffectationsResponseDto.class);
        printUri(uri);
        printResponse(response.getStatusCode().toString());

        OrganizationUnitsAffectationsResponseDto body = response.getBody();
        if (body == null) {
            throw new SynchronizationException(NO_RESPONSE_MSG);
        }

        return body.getOrganizationUnits();
    }

    @Override
    public void contextReferentialServiceIsAvailable() throws SynchronizationException {

        String uri = getContextReferentialBaseUrl + Constants.API_OPALE_HEALTHCHECK;
        printUri(uri);

        HttpHeaders headers = getHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        printResponse(response.getStatusCode().toString());

        HttpStatusCode returnedCode = response.getStatusCode();
        if (!returnedCode.is2xxSuccessful()) {
            throw new SynchronizationException(NO_RESPONSE_MSG);
        }

    }

    private HttpHeaders getHeaders() throws SynchronizationException {
        String token = keycloakService.getContextReferentialToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(token);

        return headers;
    }
}
