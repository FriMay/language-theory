package arbina.app.template.config;

import com.google.common.base.Predicates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${arbina.swagger.oauth.client-id}")
    private String clientId;

    @Value("${arbina.swagger.oauth.client-secret}")
    private String clientSecret;

    @Value("${arbina.swagger.oauth.server}")
    private String oAuthServer;

    public static final String oAuth2 = "oAuth2";

    @Bean
    public Docket api() {

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName(ResourceServerConfig.RESOURCE_ID)
                .select()
                .apis(Predicates.or(
                        RequestHandlerSelectors.basePackage("arbina.app.template.api")
                ))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(metaData())
                .produces(produces())
                .securitySchemes(Collections.singletonList(oAuth()));
    }

    @Bean
    public UiConfiguration uiConfiguration() {
        return UiConfigurationBuilder.builder().build();
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder()
                .title("Arbina API Service")
                .description("Arbina REST API template.")
                .version("1.0.0")
                .contact(new Contact("Alexey Grigorkin", "https://arbina.com", "dev@arbina.com"))
                .build();
    }

    private HashSet<String> produces() {

        HashSet<String> produces = new HashSet<>();

        produces.add("application/json");

        return produces;
    }

    private List<GrantType> grantTypes() {

        List<GrantType> grantTypes = new ArrayList<>();

        grantTypes.add(new AuthorizationCodeGrant(
                new TokenRequestEndpoint(oAuthServer + "/oauth/authorize", clientId, clientSecret),
                new TokenEndpoint(oAuthServer + "/oauth/token", "token"))
        );

        return grantTypes;
    }

    private SecurityScheme oAuth() {
        return new OAuthBuilder()
                .name(oAuth2)
                .scopes(scopes())
                .grantTypes(grantTypes())
                .build();
    }

    private List<AuthorizationScope> scopes() {
        List<AuthorizationScope> list = new ArrayList<>();
//        list.add(new AuthorizationScope("read_scope","Grants read access"));
//        list.add(new AuthorizationScope("write_scope","Grants write access"));
//        list.add(new AuthorizationScope("admin_scope","Grants read write and delete access"));
        return list;
    }

    @Bean
    public SecurityConfiguration securityInfo() {
        return SecurityConfigurationBuilder.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}