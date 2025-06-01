package org.example.citrixcontrolrest.utils;

public class PowerShellScripts {

    //usar "src/main/resources/powershell/" para produccion
    private static final String BASE_PATH = "src/main/resources/powershell/MockPowershell/";

    public static final String DDC_STATUS = BASE_PATH + "infoDDC.ps1";
    public static final String DELIVERY_GROUPS = BASE_PATH + "infoDG.ps1";
    public static final String VDA_INFO = BASE_PATH + "infoVDA.ps1";
    public static final String APPLICATIONS = BASE_PATH + "infoAPP.ps1";
    public static final String ACTIVE_USERS = BASE_PATH + "infoUSER.ps1";
    public static final String SITE = BASE_PATH + "infoSITE.ps1";
}