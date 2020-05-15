package arbina.app.template.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.List;
import java.util.Objects;

@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private TokenStore tokenStore;

    public WebSocketConfig(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(2048);
        container.setMaxBinaryMessageBufferSize(2048);
        return container;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new WebsocketJWTAuthInterceptor(tokenStore));
    }

    /**
     * Security
     */
    @Configuration
    public static class WebSocketAuthorizationSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

        @Override
        protected void configureInbound(final MessageSecurityMetadataSourceRegistry messages) {
            messages.anyMessage().authenticated();
        }

        @Override
        protected boolean sameOriginDisabled() {
            return true;
        }
    }

    private static class WebsocketJWTAuthInterceptor implements ChannelInterceptor {

        private Logger logger = LogManager.getLogger();

        private TokenStore tokenStore;

        public WebsocketJWTAuthInterceptor(TokenStore tokenStore) {
            this.tokenStore = tokenStore;
        }

        @Override
        public Message<?> preSend(Message<?> message,
                                  MessageChannel channel) {

            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor != null) {

                if (Objects.equals(StompCommand.CONNECT, accessor.getCommand())) {

                    try {

                        List<String> jwtTokenHeader = accessor.getNativeHeader("Authorization");

                        if (jwtTokenHeader == null || jwtTokenHeader.size() == 0)
                            throw new AccessDeniedException("No token header presented");

                        Authentication user = tokenStore.readAuthentication(jwtTokenHeader.get(0));
                        if (user == null || user.getPrincipal() == null)
                            throw new AccessDeniedException("Wrong auth token presented");

                        accessor.setUser(user);

                    } catch (Exception e) {

                        logger.debug("Can't authenticate websocket client", e);

                        return null;
                    }
                }
            }

            return message;
        }
    }
}