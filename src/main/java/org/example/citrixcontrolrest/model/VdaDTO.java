package org.example.citrixcontrolrest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

@Data
@JsonPropertyOrder({
        "machineName",
        "catalogName",
        "registrationState",
        "powerState",
        "inMaintenanceMode",
        "loadIndex",
        "agentVersion",
        "desktopGroupName",
        "osType",
        "deliveryType",
        "ipAddress",
        "isPhysical",
        "lastRegistrationTime",
        "persistUserChanges",
        "sessionsEstablished",
        "applications"
})
public class VdaDTO {

    @JsonProperty("machineName")
    private String machineName; // Id único del VDA

    @JsonProperty("catalogName")
    private String catalogName;

    @JsonProperty("registrationState")
    private String registrationState;

    @JsonProperty("powerState")
    private String powerState;

    @JsonProperty("inMaintenanceMode")
    private boolean inMaintenanceMode;

    @JsonProperty("loadIndex")
    private int loadIndex;

    @JsonProperty("agentVersion")
    private String agentVersion;

    @JsonProperty("desktopGroupName")
    private String desktopGroupName;

    @JsonProperty("osType")
    private String osType;

    @JsonProperty("deliveryType")
    private String deliveryType;

    @JsonProperty("ipAddress")
    private String ipAddress;

    @JsonProperty("isPhysical")
    private String isPhysical;

    @JsonProperty("lastRegistrationTime")
    private String lastRegistrationTime;

    @JsonProperty("persistUserChanges")
    private String persistUserChanges;

    @JsonProperty("sessionsEstablished")
    private int sessionsEstablished;

    @JsonProperty("applications")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> applications;

}

