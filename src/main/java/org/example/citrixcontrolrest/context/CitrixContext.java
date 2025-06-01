package org.example.citrixcontrolrest.context;

import lombok.Getter;
import org.example.citrixcontrolrest.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class CitrixContext {

    private final AtomicReference<Map<String, DDCDTO>> ddcs = new AtomicReference<>(Collections.emptyMap());
    private final AtomicReference<Map<String, DgDTO>> deliveryGroups = new AtomicReference<>(Collections.emptyMap());
    private final AtomicReference<Map<String, UserDTO>> activeUsers = new AtomicReference<>(Collections.emptyMap());
    private final AtomicReference<Map<String, VdaDTO>> vdas = new AtomicReference<>(Collections.emptyMap());
    private final AtomicReference<Map<String, AppDTO>> apps = new AtomicReference<>(Collections.emptyMap());
    @Getter
    private volatile CitrixSiteDTO citrixSite; // Solo uno, volatile para visibilidad

    // Actualización: construyes un nuevo mapa y lo reemplazas atómicamente
    public void updateDDCs(List<DDCDTO> nuevosDdcs) {
        Map<String, DDCDTO> nuevoMapa = new HashMap<>();
        for (DDCDTO ddc : nuevosDdcs) {
            nuevoMapa.put(ddc.getDnsName(), ddc);
        }
        ddcs.set(Collections.unmodifiableMap(nuevoMapa));
    }

    public void updateDeliveryGroups(List<DgDTO> nuevosDgs) {
        Map<String, DgDTO> nuevoMapa = new HashMap<>();
        for (DgDTO dg : nuevosDgs) {
            nuevoMapa.put(dg.getUID(), dg);
        }
        deliveryGroups.set(Collections.unmodifiableMap(nuevoMapa));
    }

    public void updateActiveUsers(List<UserDTO> nuevosUsers) {
        Map<String, UserDTO> nuevoMapa = new HashMap<>();
        for (UserDTO user : nuevosUsers) {
            nuevoMapa.put(user.getUsername(), user);
        }
        activeUsers.set(Collections.unmodifiableMap(nuevoMapa));
    }

    public void updateVDAs(List<VdaDTO> nuevosVdas) {
        Map<String, VdaDTO> nuevoMapa = new HashMap<>();
        for (VdaDTO vda : nuevosVdas) {
            nuevoMapa.put(vda.getMachineName(), vda);
        }
        vdas.set(Collections.unmodifiableMap(nuevoMapa));
    }

    public void updateApps(List<AppDTO> nuevasApps) {
        Map<String, AppDTO> nuevoMapa = new HashMap<>();
        for (AppDTO app : nuevasApps) {
            nuevoMapa.put(app.getUid(), app);
        }
        apps.set(Collections.unmodifiableMap(nuevoMapa));
    }

    public void updateCitrixSite(CitrixSiteDTO nuevoSite) {
        this.citrixSite = nuevoSite;
    }

    // Getters retornan las referencias atómicas a mapas inmutables, sin bloqueo
    public Map<String, DDCDTO> getDdcs() {
        return ddcs.get();
    }

    public Map<String, DgDTO> getDeliveryGroups() {
        return deliveryGroups.get();
    }

    public Map<String, UserDTO> getActiveUsers() {
        return activeUsers.get();
    }

    public Map<String, VdaDTO> getVdas() {
        return vdas.get();
    }

    public Map<String, AppDTO> getApps() {
        return apps.get();
    }
}


