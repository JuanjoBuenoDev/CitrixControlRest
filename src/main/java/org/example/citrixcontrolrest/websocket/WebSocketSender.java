package org.example.citrixcontrolrest.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketSender extends TextWebSocketHandler {

    private final CitrixWebSocketHandler handler;

    public WebSocketSender(CitrixWebSocketHandler handler) {
        this.handler = handler;
    }

    public void sendUpdateToClients(String jsonData) {
        handler.sendMessageToAll(jsonData);
    }

    private WebSocketSender webSocketSender;

    public void setWebSocketSender(WebSocketSender webSocketSender) {
        this.webSocketSender = webSocketSender;
    }
}
