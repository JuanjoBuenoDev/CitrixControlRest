package org.example.citrixcontrolrest.powershell;

import org.example.citrixcontrolrest.utils.PowerShellConfig;
import org.example.citrixcontrolrest.utils.PowerShellScripts;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class PowerShellExecutor {

    // Ejecuta un comando de PowerShell como String

    private List<String> buildBaseCommand(String scriptPath, boolean includeDdcs, String ddc) throws IOException, InterruptedException {
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(PowerShellConfig.getPowerShellCommand());
        commandArgs.add("-NoProfile");
        commandArgs.add("-ExecutionPolicy");
        commandArgs.add("Bypass");
        commandArgs.add("-File");
        commandArgs.add(scriptPath);
        if (includeDdcs) {
            commandArgs.add("-ddc");
            commandArgs.add(ddc);
        }
        return commandArgs;
    }


    // Ejecuta un comando de PowerShell como lista de argumentos
    private static String executeCommand(List<String> commandArgs) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(commandArgs);
        processBuilder.redirectErrorStream(true); // Fusiona stdout y stderr

        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor(); // Espera sin timeout

        if (exitCode != 0) {
            throw new RuntimeException("El proceso de PowerShell terminó con código " + exitCode);
        }
        return output.toString().trim();
    }

    // Ejecuta script para obtener estado de los DDCs
    public String getDdcStatus(String ddc) throws IOException, InterruptedException {
        List<String> commandArgs = buildBaseCommand(PowerShellScripts.DDC_STATUS, true, ddc);
        System.out.println(commandArgs);
        return executeCommand(commandArgs);
    }

    public String getDGStatus(String ddc) throws IOException, InterruptedException {
        List<String> commandArgs = buildBaseCommand(PowerShellScripts.DELIVERY_GROUPS, true, ddc);
        return executeCommand(commandArgs);
    }

    public String getVdaStatus(String ddc) throws IOException, InterruptedException {
        List<String> commandArgs = buildBaseCommand(PowerShellScripts.VDA_INFO, true, ddc);
        return executeCommand(commandArgs);
    }

    public String getAppStatus(String ddc) throws IOException, InterruptedException {
        List<String> commandArgs = buildBaseCommand(PowerShellScripts.APPLICATIONS, true, ddc);
        return executeCommand(commandArgs);
    }

    public String getActiveUserStatus(String ddc) throws IOException, InterruptedException {
        List<String> commandArgs = buildBaseCommand(PowerShellScripts.ACTIVE_USERS, true, ddc);
        return executeCommand(commandArgs);
    }

    public String getSite(String ddc) throws IOException, InterruptedException {
        List<String> commandArgs = buildBaseCommand(PowerShellScripts.SITE, false, ddc);
        return executeCommand(commandArgs);
    }
}

