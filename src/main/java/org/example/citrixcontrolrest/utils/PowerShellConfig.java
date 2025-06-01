package org.example.citrixcontrolrest.utils;

public class PowerShellConfig {

    private static final String POWER_SHELL_COMMAND;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            POWER_SHELL_COMMAND = "powershell.exe";
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
            POWER_SHELL_COMMAND = "pwsh"; // PowerShell Core
        } else {
            throw new UnsupportedOperationException("Sistema operativo no compatible con PowerShell.");
        }
    }

    private PowerShellConfig() {} // Previene instanciación

    public static String getPowerShellCommand() {
        return POWER_SHELL_COMMAND;
    }
}
