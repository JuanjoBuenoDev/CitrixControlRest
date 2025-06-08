package org.example.citrixcontrolrest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.example.citrixcontrolrest.context.CitrixContext;
import org.example.citrixcontrolrest.model.*;
import org.example.citrixcontrolrest.powershell.PowerShellExecutor;
import org.example.citrixcontrolrest.scheduler.CitrixScheduledUpdater;
import org.example.citrixcontrolrest.websocket.WebSocketSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CitrixService {

    private final CitrixContext citrixContext;
    private final PowerShellExecutor powerShellExecutor;
    private final ObjectMapper objectMapper;
    private final CitrixScheduledUpdater updater;
    private final WebSocketSender webSocketSender;


    public CitrixService(CitrixContext citrixContext, PowerShellExecutor powerShellExecutor, ObjectMapper objectMapper, CitrixScheduledUpdater updater, WebSocketSender webSocketSender) {
        this.powerShellExecutor = powerShellExecutor;
        this.citrixContext = citrixContext;
        this.objectMapper = objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
        this.updater = updater;
        this.webSocketSender = webSocketSender;

        // Configurar las funciones de actualización
        this.updater.setRefreshDGsFunction(this::refreshDGsWrapper);
        this.updater.setRefreshVDAsFunction(this::refreshVDAsWrapper);
        this.updater.setRefreshAppsFunction(this::refreshAppsWrapper);
        this.updater.setRefreshActiveUsersFunction(this::refreshActiveUsersWrapper);
        this.updater.setRefreshCitrixSiteFunction(this::refreshCitrixSiteWrapper);
        this.updater.setRefreshDDCsFunction(this::refreshDDCsWrapper);
    }

    public void reiniciarScheduler() {
        stopScheduler();  // Siempre detener antes
        Map<String, DDCDTO> disponibles = getDdcs();

        List<DDCDTO> ddcsActivos = disponibles.values().stream()
                .filter(ddc -> ddc != null && "Active".equalsIgnoreCase(ddc.getState()))
                .collect(Collectors.toList());

        if (ddcsActivos.isEmpty()) {
            throw new IllegalStateException("No hay DDCs activos en el contexto para iniciar el scheduler.");
        }

        updater.setDdcList(ddcsActivos);
        updater.start();  // Siempre iniciar
    }

    private void iniciarScheduler() {
        // Ya no se expone externamente
        if (!updater.isRunning()) {
            updater.start();
        }
    }

    public void stopScheduler() {
        if (updater.isRunning()) {
            updater.stop();
        }
    }

    // Wrappers para las funciones de actualización
    private Void refreshDGsWrapper(String ddc) {
        try {
            refreshDGs(ddc);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error al actualizar DGs", e);
        }
        return null;
    }

    private Void refreshVDAsWrapper(String ddc) {
        try {
            refreshVDAs(ddc);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error al actualizar VDAs", e);
        }
        return null;
    }

    private Void refreshAppsWrapper(String ddc) {
        try {
            refreshApps(ddc);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error al actualizar Apps", e);
        }
        return null;
    }

    private Void refreshActiveUsersWrapper(String ddc) {
        try {
            refreshActiveUsers(ddc);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error al actualizar usuarios activos", e);
        }
        return null;
    }

    private Void refreshCitrixSiteWrapper(String ddc) {
        try {
            refreshCitrixSite(ddc);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error al actualizar el sitio Citrix", e);
        }
        return null;
    }

    private Void refreshDDCsWrapper(String ddc) {
        try {
            refreshDDCs(ddc);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error al actualizar DDCs", e);
        }
        return null;
    }

    // Métodos para comenzar con la info de los DDCs
    public boolean firstDDCs(String ddc) throws IOException, InterruptedException {
        String jsonOutput = powerShellExecutor.getDdcStatus(ddc);
        System.out.println(jsonOutput);

        // Deserialización robusta
        List<DDCDTO> ddcs;
        try {
            // Intenta como lista primero
            ddcs = objectMapper.readValue(jsonOutput, new TypeReference<List<DDCDTO>>() {});
        } catch (MismatchedInputException e) {
            // Si falla, intenta como objeto único
            DDCDTO single = objectMapper.readValue(jsonOutput, DDCDTO.class);
            ddcs = Collections.singletonList(single);
        }

        // Validación
        boolean validate = ddcs.stream().noneMatch(d -> "Error".equals(d.getState()));
        if (validate) {
            citrixContext.updateDDCs(ddcs);
            webSocketSender.sendUpdate("ddc", ddcs);
        }
        return validate;
    }

    // Métodos para actualizar la info en el contexto (se sobrescriben los datos completos)
    public void refreshDDCs(String ddc) throws IOException, InterruptedException {
        String jsonOutput = powerShellExecutor.getDdcStatus(ddc);
        System.out.println(jsonOutput);

        // Deserializa el JSON de salida
        List<DDCDTO> ddcs = objectMapper.readValue(
                jsonOutput, new TypeReference<List<DDCDTO>>() {}
        );
        citrixContext.updateDDCs(ddcs);
        webSocketSender.sendUpdate("ddc", ddcs);    }

    public void refreshDGs(String ddc) throws IOException, InterruptedException {
        String jsonOutput = powerShellExecutor.getDGStatus(ddc);
        System.out.println(jsonOutput);

        // Deserializa el JSON de salida
        List<DgDTO> dgs = objectMapper.readValue(
                jsonOutput, new TypeReference<List<DgDTO>>() {}
        );
        citrixContext.updateDeliveryGroups(dgs);
        webSocketSender.sendUpdate("dg", dgs);    }

    public void refreshActiveUsers(String ddc) throws IOException, InterruptedException {
        String jsonOutput = powerShellExecutor.getActiveUserStatus(ddc);
        System.out.println(jsonOutput);

        // Deserializa el JSON de salida
        List<UserDTO> usuarios = objectMapper.readValue(
                jsonOutput, new TypeReference<List<UserDTO>>() {}
        );
        citrixContext.updateActiveUsers(usuarios);
        webSocketSender.sendUpdate("user", usuarios);
    }

    public void refreshVDAs(String ddc) throws IOException, InterruptedException {
        String jsonOutput = powerShellExecutor.getVdaStatus(ddc);
        System.out.println(jsonOutput);

        // Deserializa el JSON de salida
        List<VdaDTO> vdas = objectMapper.readValue(
                jsonOutput, new TypeReference<List<VdaDTO>>() {}
        );
        citrixContext.updateVDAs(vdas);
        webSocketSender.sendUpdate("vda", vdas);
    }

    public void refreshApps(String ddc) throws IOException, InterruptedException {
        String jsonOutput = powerShellExecutor.getAppStatus(ddc);
        System.out.println(jsonOutput);

        // Deserializa el JSON de salida
        List<AppDTO> apps = objectMapper.readValue(
                jsonOutput, new TypeReference<List<AppDTO>>() {}
        );
        citrixContext.updateApps(apps);
        webSocketSender.sendUpdate("app", apps);
    }

    public void refreshCitrixSite(String ddc) throws IOException, InterruptedException {
        String jsonOutput = powerShellExecutor.getSite(ddc);

        CitrixSiteDTO newSite = objectMapper.readValue(jsonOutput, CitrixSiteDTO.class);
        citrixContext.updateCitrixSite(newSite);
        webSocketSender.sendUpdate("site", newSite);
    }

    // Métodos para leer la info del contexto y devolverla
    public Map<String, DDCDTO> getDdcs() {
        return citrixContext.getDdcs();
    }

    public List<String> getDDCNames() {
        return new ArrayList<>(citrixContext.getDdcs().keySet());
    }

    public Map<String, DgDTO> getDeliveryGroups() {
        return citrixContext.getDeliveryGroups();
    }

    public Map<String, UserDTO> getActiveUsers() {
        return citrixContext.getActiveUsers();
    }

    public Map<String, VdaDTO> getVdas() {
        return citrixContext.getVdas();
    }

    public Map<String, AppDTO> getApps() {
        return citrixContext.getApps();
    }

    public CitrixSiteDTO getCitrixSite() {
        return citrixContext.getCitrixSite();
    }
}