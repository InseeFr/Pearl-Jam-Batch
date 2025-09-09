package fr.insee.pearljam.batch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record ApplicationConfig(

		@Value("${fr.insee.pearljam.persistence.database.host}")
		String pilotageDbHost,

		@Value("${fr.insee.pearljam.persistence.database.port}")
		String pilotageDbPort,

		@Value("${fr.insee.pearljam.persistence.database.schema}")
		String pilotageDbSchema,

		@Value("${fr.insee.pearljam.persistence.database.user}")
		String pilotageDbUser,

		@Value("${fr.insee.pearljam.persistence.database.password}")
		String pilotageDbPassword,

		@Value("${fr.insee.pearljam.persistence.database.driver}")
		String pilotageDbDriver,

		@Value("${fr.insee.pearljam.folder.in}")
		String folderIn,

		@Value("${fr.insee.pearljam.folder.out}")
		String folderOut,

		@Value("${fr.insee.pearljam.context.referential.service.url}")
		String contextReferentialUrl,

		@Value("${keycloak.auth-server-url}")
		String authServerURL,

		@Value("${keycloak.realm}")
		String realm,

		@Value("${fr.insee.pearljam.ldap.service.url}")
		String ldapServiceUrl,

		@Value("${keycloak.datacollection.server}")
		String keycloakDataCollectionServer,

		@Value("${keycloak.datacollection.registration-id}")
		String keycloakDataCollectionRegistrationId,

		@Value("${keycloak.datacollection.client-id}")
		String keycloakDataCollectionClientId,

		@Value("${keycloak.datacollection.realm}")
		String keycloakDataCollectionRealm,

		@Value("${keycloak.datacollection.client-secret}")
		String keycloakDataCollectionClientSecret,

		@Value("${api.datacollection.url}")
		String dataCollectionApiUrl
) {}
