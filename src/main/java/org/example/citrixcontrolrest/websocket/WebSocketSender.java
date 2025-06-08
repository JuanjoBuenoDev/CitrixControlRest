package org.example.citrixcontrolrest.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketSender extends TextWebSocketHandler {

    private final CitrixWebSocketHandler handler;
    private final ObjectMapper mapper = new ObjectMapper(); // Usado para convertir a JSON

    public WebSocketSender(CitrixWebSocketHandler handler) {
        this.handler = handler;
    }

    // Método genérico para enviar datos estructurados como mensaje WebSocket
    public <T> void sendUpdate(String type, T payload) {
        try {
            WebSocketMessage<T> message = new WebSocketMessage<>(type, payload);
            String json = mapper.writeValueAsString(message);
            handler.sendMessageToAll(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // Manejo básico, puedes integrar logs
        }
    }

    // Método anterior (útil si necesitas enviar JSON plano)
    public void sendUpdateToClients(String jsonData) {
        handler.sendMessageToAll(jsonData);
    }
}
