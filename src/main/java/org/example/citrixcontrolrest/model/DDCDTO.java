package org.example.citrixcontrolrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"dnsName", "state", "desktopsRegistered"})
public class DDCDTO {
    @JsonProperty("dnsName")
    private String dnsName;//Id

    @JsonProperty("state")
    private String state;

    @JsonProperty("desktopsRegistered")
    private int desktopsRegistered;
}
