package com.restaurant.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/** WebSocket configuration for real-time events. Implements ADR-006: STOMP over SockJS. */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker (memory-based, good for single-server setup)
        config.enableSimpleBroker("/topic");

        // Set application destination prefix for client messages
        config.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint with SockJS fallback
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
