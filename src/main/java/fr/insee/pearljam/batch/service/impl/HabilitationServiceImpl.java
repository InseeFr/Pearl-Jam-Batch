package fr.insee.pearljam.batch.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import fr.insee.pearljam.batch.Constants;
import fr.insee.pearljam.batch.dto.HabilitationGroup;
import fr.insee.pearljam.batch.dto.HabilitationActionResponseDto;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.service.HabilitationService;
import fr.insee.pearljam.batch.service.KeycloakService;

@Service
public class HabilitationServiceImpl implements HabilitationService {
    private static final Logger LOGGER = LogManager.getLogger(HabilitationServiceImpl.class);

    private static final void logUri(String uriToLog) {
        LOGGER.info("Calling {}", uriToLog);
    }

    private static final void logResponse(HttpStatus statusCode) {
        LOGGER.info("Response {}", statusCode);
    }

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    @Qualifier("habilitationApiBaseUrl")
    private String habilitationApiRootUrl;

    @Value("${fr.insee.pearljam.ldap.service.realm:#{null}}")
    private String realm;

    @Value("${fr.insee.pearljam.ldap.service.app.name:#{null}}")
    private String appName;

    @Value("${fr.insee.pearljam.ldap.service.group.interviewer:#{null}}")
    private String interviewerGroup;

    String realmAppGroupUserIdFormat = Constants.API_LDAP_REALM_APP_GROUP_USERID;
    String realmAppGroupFormat = Constants.API_LDAP_REALM_APP_GROUPID;

    private static final String NO_RESPONSE_MSG = "Could not get response from habilitation API";

    private HttpHeaders getHabilitationHeaders() throws SynchronizationException {
        String token = keycloakService.getContextReferentialToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(token);

        return headers;
    }

    @Override
    public void addInterviewerHabilitation(String interviewerIdep) throws SynchronizationException {
        LOGGER.info("Add interviewer {}", interviewerIdep);
        String parametrizedUrl = String.format(realmAppGroupUserIdFormat, realm, appName, interviewerGroup,
                interviewerIdep);
        String uri = habilitationApiRootUrl + parametrizedUrl;
        logUri(uri);

        HttpHeaders headers = getHabilitationHeaders();

        HttpEntity<?> entity = new HttpEntity<>(null, headers);
        ResponseEntity<HabilitationActionResponseDto> response = null;
        try {
            response = restTemplate.exchange(
                    uri,
                    HttpMethod.PUT,
                    entity, HabilitationActionResponseDto.class);

            LOGGER.info("response : {}", response.getStatusCode());
        } catch (Exception e) {
            LOGGER.warn("Can't add interviewer habilitation due to Exception : {}", e.getMessage());
        }

        HabilitationActionResponseDto body = response != null ? response.getBody() : null;
        if ((response != null && !response.hasBody()) || (body != null && body.getMessage() != null)) {
            throw new SynchronizationException("Can't add interviewer habilitation : " + body.getMessage());
        }
        logResponse(response.getStatusCode());
    }

    @Override
    public void isAvailable() throws SynchronizationException {
        String uri = String.join("", habilitationApiRootUrl, Constants.API_LDAP_HEALTHCHECK);

        HttpHeaders headers = getHabilitationHeaders();

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        HttpStatus returnedCode = response.getStatusCode();
        logUri(uri);
        logResponse(response.getStatusCode());

        if (!returnedCode.is2xxSuccessful()) {
            throw new SynchronizationException(NO_RESPONSE_MSG);
        }

    }

    @Override
    public List<String> getHabilitatedInterviewers() throws SynchronizationException {
        String parametrizedUrl = String.format(realmAppGroupFormat, realm, appName, interviewerGroup);
        String uri = habilitationApiRootUrl + parametrizedUrl;
        HttpHeaders headers = getHabilitationHeaders();

        HttpEntity<?> entity = new HttpEntity<>(null, headers);

        ResponseEntity<HabilitationGroup> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity, HabilitationGroup.class);

        logUri(uri);
        logResponse(response.getStatusCode());
        if (!response.hasBody())
            throw new SynchronizationException("Can't get habilitated interviewers.");

        HabilitationGroup habilitatedUsers = Optional.ofNullable(response.getBody())
                .orElse(new HabilitationGroup(Collections.emptyList()));
        return habilitatedUsers.getUsers().stream().map(user -> user.getUsername()).collect(Collectors.toList());
    }
}
