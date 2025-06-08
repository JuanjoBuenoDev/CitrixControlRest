package org.example.citrixcontrolrest.utils;

import org.example.citrixcontrolrest.service.CitrixService;
import org.example.citrixcontrolrest.websocket.CitrixWebSocketConnectedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSocketListener {

    private final CitrixService citrixService;

    public WebSocketListener(CitrixService citrixService) {
        this.citrixService = citrixService;
    }

    @EventListener
    public void onWebSocketConnected(CitrixWebSocketConnectedEvent event) {
        WebSocketSession session = event.getSession();
        System.out.println("Nuevo cliente WebSocket conectado desde: " + session.getRemoteAddress());

        citrixService.reiniciarScheduler();
    }
}

