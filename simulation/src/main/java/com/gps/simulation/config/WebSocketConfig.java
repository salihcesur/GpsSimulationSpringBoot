package com.gps.simulation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // allowedOriginPatterns kullanarak joker karakterle daha esnek bir yapı sağlıyoruz
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:3000")  // Belirli origin ekliyoruz (React uygulamanızın kökeni)
                .withSockJS(); // SockJS kullanıyoruz
    }
}
