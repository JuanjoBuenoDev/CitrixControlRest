package org.example.citrixcontrolrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DDCDTO {
    @JsonProperty("dnsName")
    private String dnsName;//Id

    @JsonProperty("state")
    private String state;

    @JsonProperty("desktopsRegistered")
    private int desktopsRegistered;
}
