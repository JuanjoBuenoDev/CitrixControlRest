package org.example.citrixcontrolrest.websocket;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

public class CitrixWebSocketConnectedEvent extends ApplicationEvent {
    public CitrixWebSocketConnectedEvent(WebSocketSession session) {
        super(session);
    }

    public WebSocketSession getSession() {
        return (WebSocketSession) getSource();
    }
}
