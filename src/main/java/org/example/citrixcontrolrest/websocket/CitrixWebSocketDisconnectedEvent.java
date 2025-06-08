package org.example.citrixcontrolrest.websocket;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

public class CitrixWebSocketDisconnectedEvent extends ApplicationEvent {
    private final WebSocketSession session;

    public CitrixWebSocketDisconnectedEvent(Object source, WebSocketSession session) {
        super(source);
        this.session = session;
    }

    public WebSocketSession getSession() {
        return session;
    }
}
