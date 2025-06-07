package org.example.citrixcontrolrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({
        "licenseEdition",
        "licenseServerName",
        "licenseServerPort",
        "licensingGraceHoursLeft",
        "licensingGracePeriodActive",
        "localHostCacheEnabled",
        "peakConcurrentLicenseUsers",
        "peakConcurrentLicensedDevices",
        "dataStoreSite",
        "dataStoreMonitor",
        "dataStoreLog"
})
public class CitrixSiteDTO {

    // Licencias
    @JsonProperty("licenseEdition")
    private String licenseEdition;

    @JsonProperty("licenseServerName")
    private String licenseServerName;

    @JsonProperty("licenseServerPort")
    private int licenseServerPort;

    @JsonProperty("licensingGraceHoursLeft")
    private int licensingGraceHoursLeft;

    @JsonProperty("licensingGracePeriodActive")
    private boolean licensingGracePeriodActive;

    @JsonProperty("localHostCacheEnabled")
    private boolean localHostCacheEnabled;

    @JsonProperty("peakConcurrentLicenseUsers")
    private int peakConcurrentLicenseUsers;

    @JsonProperty("peakConcurrentLicensedDevices")
    private int peakConcurrentLicensedDevices;

    // Data store
    @JsonProperty("dataStoreSite")
    private String dataStoreSite;

    @JsonProperty("dataStoreMonitor")
    private String dataStoreMonitor;

    @JsonProperty("dataStoreLog")
    private String dataStoreLog;
}

