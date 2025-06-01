package org.example.citrixcontrolrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
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

