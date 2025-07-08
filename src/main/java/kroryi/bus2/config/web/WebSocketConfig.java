package kroryi.bus2.config.web;

import kroryi.bus2.components.RedirectProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final RedirectProperties redirect;

    public WebSocketConfig(RedirectProperties redirect) {
        this.redirect = redirect;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        redirect.getBaseUrl(),
                        "https://docs.yi.or.kr:8094",
                        "https://docs.yi.or.kr:8096",
                        "https://docs.yi.or.kr:15173",
                        "https://docs.yi.or.kr:5173",
                        "https://docs.yi.or.kr:8097",
                        "https://docs.yi.or.kr:8098",
                        "https://docs.yi.or.kr:8099",
                        "https://localhost:8094",
                        "https://localhost:8096",
                        "https://localhost:15173",
                        "https://localhost:5173",
                        "https://localhost:8097",
                        "https://localhost:8098",
                        "https://localhost:8099"
                )
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(8192) // 메시지 크기 제한
                .setSendBufferSizeLimit(8192) // 버퍼 크기 제한
                .setSendTimeLimit(10000); // 전송 시간 제한 (10초)
    }
} 