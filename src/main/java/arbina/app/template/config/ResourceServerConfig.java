package arbina.app.template.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 20)
@EnableResourceServer
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    public static final String RESOURCE_ID = "arbina:app-template";
    public static final String OBSERVER = "OBSERVER";

    /**
     * Use @WithMockUser (with @SpringBootTest) inside an oAuth2 Resource Server Application.
     *
     * https://stackoverflow.com/a/44502136
     */
    @Value("${arbina.security.stateless:true}")
    private Boolean stateless;

    @Override
    public void configure(ResourceServerSecurityConfigurer security) {
        security.resourceId(RESOURCE_ID);
        security.stateless(stateless);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/api/**")
                .authorizeRequests()
                .anyRequest().authenticated()
        .and()
                .csrf().disable()
                .cors()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Value("${spring.security.oauth2.resource.jwk.key-set-uri}")
    private String keySetUri;

    @Bean
    public TokenStore tokenStore() {
        return new JwkTokenStore(keySetUri, accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        return new JwtAccessTokenConverter();
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
    }
}