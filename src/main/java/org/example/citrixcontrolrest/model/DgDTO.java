package org.example.citrixcontrolrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({
        "uid",
        "name",
        "state",
        "sessionCount",
        "vdas",
        "averageLoadIndex",
        "isMaintenanceMode",
        "rebootEnabled",
        "rebootFrequency",
        "rebootDaysOfWeek",
        "rebootStartTime",
        "rebootDuration"
})
public class DgDTO {

    @JsonProperty("uid")
    private String UID; // Id único del Delivery Group

    @JsonProperty("name")
    private String name;

    @JsonProperty("state")
    private String state;

    @JsonProperty("sessionCount")
    private int sessionCount;

    @JsonProperty("vdas")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> vdas; // DNS de los VDAs

    @JsonProperty("averageLoadIndex")
    private String averageLoadIndex;

    @JsonProperty("isMaintenanceMode")
    private boolean isMaintenanceMode;

    // Reinicio
    @JsonProperty("rebootEnabled")
    private boolean rebootEnabled;

    @JsonProperty("rebootFrequency")
    private String rebootFrequency;

    @JsonProperty("rebootDaysOfWeek")
    private String rebootDaysOfWeek;

    @JsonProperty("rebootStartTime")
    private String rebootStartTime;

    @JsonProperty("rebootDuration")
    private String rebootDuration;
}
