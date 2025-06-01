package org.example.citrixcontrolrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Config implements Serializable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("limitFail")
    private int limitFail;

    @JsonProperty("ddcs")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> ddcs;
}
