package org.example.citrixcontrolrest.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CitrixWebSocketHandler citrixWebSocketHandler;

    public WebSocketConfig(CitrixWebSocketHandler citrixWebSocketHandler) {
        this.citrixWebSocketHandler = citrixWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(citrixWebSocketHandler, "/ws/citrix")
                .setAllowedOrigins("*");
    }
}
