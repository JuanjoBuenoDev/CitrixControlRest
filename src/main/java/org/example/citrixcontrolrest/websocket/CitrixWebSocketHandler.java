package org.example.citrixcontrolrest.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class CitrixWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(CitrixWebSocketHandler.class);

    // Conexiones activas
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WS conectado desde " + session.getRemoteAddress());
        session.sendMessage(new TextMessage("{\"message\":\"Bienvenido al WebSocket\"}"));
        super.afterConnectionEstablished(session);
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // Por ejemplo, aquí puedes recibir mensajes del cliente, si quieres
        logger.info("Mensaje recibido de {}: {}", session.getId(), message.getPayload());
        // Responder si quieres:
        session.sendMessage(new TextMessage("Mensaje recibido: " + message.getPayload()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        logger.info("Cliente desconectado: {}", session.getId());
    }

    // Método para enviar datos a todos los clientes conectados
    public void sendMessageToAll(String message) {
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                logger.error("Error enviando mensaje a cliente: {}", session.getId(), e);
            }
        });
    }
}
