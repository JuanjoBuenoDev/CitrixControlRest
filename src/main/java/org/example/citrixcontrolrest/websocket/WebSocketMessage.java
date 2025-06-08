package org.example.citrixcontrolrest.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebSocketMessage<T> {
    @JsonProperty("type")
    private String type;

    @JsonProperty("payload")
    private T payload;
}