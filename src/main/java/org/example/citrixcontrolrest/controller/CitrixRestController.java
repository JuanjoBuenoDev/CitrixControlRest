package org.example.citrixcontrolrest.controller;

// CitrixRestController.java
import org.example.citrixcontrolrest.model.*;
import org.example.citrixcontrolrest.service.CitrixService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/citrix")
public class CitrixRestController {

    private final CitrixService citrixService;

    public CitrixRestController(CitrixService citrixService) {
        this.citrixService = citrixService;
    }

    @GetMapping("/ddcs")
    public Map<String, DDCDTO> getDdcs() {
        return citrixService.getDdcs();
    }

    @GetMapping("/delivery-groups")
    public Map<String, DgDTO> getDeliveryGroups() {
        return citrixService.getDeliveryGroups();
    }

    @GetMapping("/active-users")
    public Map<String, UserDTO> getActiveUsers() {
        return citrixService.getActiveUsers();
    }

    @GetMapping("/vdas")
    public Map<String, VdaDTO> getVdas() {
        return citrixService.getVdas();
    }

    @GetMapping("/apps")
    public Map<String, AppDTO> getApps() {
        return citrixService.getApps();
    }

    @GetMapping("/site")
    public CitrixSiteDTO getSite() {
        return citrixService.getCitrixSite();
    }
}

