package fr.insee.pearljam.batch.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@RequiredArgsConstructor
@Getter
public class ApplicationConfig {
	@Value("${fr.insee.pearljam.folder.in}")
	public final String folderIn;
	@Value("${fr.insee.pearljam.folder.out}")
	public final String folderOut;
	@Value("${fr.insee.pearljam.context.referential.service.url}")
	public final String contextReferentialUrl;
	@Value("${keycloak.auth-server-url}")
	public final String authServerURL;
	@Value("${keycloak.realm}")
	public final String realm;
	@Value("${fr.insee.pearljam.ldap.service.url}")
	public final String ldapServiceUrl;
	@Value("${keycloak.datacollection.server}")
	public final String keycloakDataCollectionServer;
	@Value("${keycloak.datacollection.registration-id}")
	public final String keycloakDataCollectionRegistrationId;
	@Value("${keycloak.datacollection.client-id}")
	public final String keycloakDataCollectionClientId;
	@Value("${keycloak.datacollection.realm}")
	public final String keycloakDataCollectionRealm;
	@Value("${keycloak.datacollection.client-secret}")
	public final String keycloakDataCollectionClientSecret;
	@Value("${api.datacollection.url}")
	String dataCollectionApiUrl;
}
