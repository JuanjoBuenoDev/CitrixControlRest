package org.example.citrixcontrolrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AppDTO {

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("name")
    private String name;

    @JsonProperty("applicationName")
    private String applicationName;

    @JsonProperty("browserName")
    private String browserName;

    @JsonProperty("publishedName")
    private String publishedName;

    @JsonProperty("maxTotalInstances")
    private Integer maxTotalInstances; // Cambiado a Integer

    @JsonProperty("maxPerUserInstances")
    private Integer maxPerUserInstances; // Cambiado a Integer

    @JsonProperty("commandLineExecutable")
    private String commandLineExecutable;

    @JsonProperty("commandLineArguments")
    private String commandLineArguments;

    @JsonProperty("directory")
    private String directory;

    @JsonProperty("userFolder")
    private String userFolder;

    @JsonProperty("desktopGroups")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> desktopGroups = new ArrayList<>(); // Inicializado

    @JsonProperty("vdas")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> vdas = new ArrayList<>(); // Inicializado

    @JsonProperty("activeUsers")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> activeUsers = new ArrayList<>(); // Inicializado

    @JsonProperty("enabled")
    private Boolean enabled; // Cambiado a Boolean

    @JsonProperty("executablePath")
    private String executablePath;
}

