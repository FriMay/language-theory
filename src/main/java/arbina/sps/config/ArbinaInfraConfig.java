package arbina.sps.config;

import arbina.infra.auth.FeignRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArbinaInfraConfig {

    @Value("${arbina.infra.oauth.client-id}")
    private String oAuthClientId;

    @Value("${arbina.infra.oauth.client-secret}")
    private String oAuthClientSecret;

    @Value("${arbina.infra.oauth.server}")
    private String oAuthServer;

    @Bean
    public OAuth2FeignRequestInterceptor oAuthRequestInterceptor() {
        return FeignRequestInterceptor.create(oAuthClientId, oAuthClientSecret, oAuthServer);
    }
}