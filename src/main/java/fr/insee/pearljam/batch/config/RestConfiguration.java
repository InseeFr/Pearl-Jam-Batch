package fr.insee.pearljam.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RestConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestClient dataCollectionRestClient(OAuth2AuthorizedClientManager authorizedClientManager,
                                               ApplicationConfig appConfig) {
        OAuth2ClientHttpRequestInterceptor requestInterceptor =
                new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);

        return RestClient.builder()
                .requestInterceptor(requestInterceptor)
                .baseUrl(appConfig.dataCollectionApiUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(ApplicationConfig appConfig) {
        String keycloakUrl = appConfig.keycloakDataCollectionServer() + "/realms/" + appConfig.keycloakDataCollectionRealm() + "/protocol/openid-connect/token";
        ClientRegistration registration = ClientRegistration
                .withRegistrationId(appConfig.keycloakDataCollectionRegistrationId())
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientId(appConfig.keycloakDataCollectionClientId())
                .clientSecret(appConfig.keycloakDataCollectionClientSecret())
                .tokenUri(keycloakUrl)
                .build();
        return new InMemoryClientRegistrationRepository(List.of(registration));
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    public OAuth2AuthorizedClientService clientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }
}
