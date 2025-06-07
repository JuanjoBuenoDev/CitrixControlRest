package org.example.citrixcontrolrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({
        "username",
        "lastConnectionFailureReason",
        "lastFailureEndTime",
        "lastMachineUsed",
        "aplicacionesEnUso",
        "maquinas",
        "desktopGroups"
})
public class UserDTO {

    @JsonProperty("username")
    private String username; // ID del usuario

    @JsonProperty("lastConnectionFailureReason")
    private String lastConnectionFailureReason;

    @JsonProperty("lastFailureEndTime")
    private String lastFailureEndTime;

    @JsonProperty("lastMachineUsed")
    private String lastMachineUsed;

    @JsonProperty("aplicacionesEnUso")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> aplicacionesEnUso;

    @JsonProperty("maquinas")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> maquinas;

    @JsonProperty("desktopGroups")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> desktopGroups;
}