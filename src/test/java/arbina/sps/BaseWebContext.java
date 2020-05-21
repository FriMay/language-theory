package arbina.sps;

import arbina.infra.auth.FeignRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan("arbina.infra")
@EnableFeignClients("arbina.infra.services")
@EnableJpaRepositories(basePackages = "arbina")
@DataJpaTest
@EnableAutoConfiguration
public class BaseWebContext {

    @Value("${client-id}")
    private String oAuthClientId;

    @Value("${client-secret}")
    private String oAuthClientSecret;

    @Value("${oauth-server}")
    private String oAuthServer;

    @Bean
    private OAuth2FeignRequestInterceptor requestInterceptor() {
        return FeignRequestInterceptor.create(oAuthClientId, oAuthClientSecret, oAuthServer);
    }
}