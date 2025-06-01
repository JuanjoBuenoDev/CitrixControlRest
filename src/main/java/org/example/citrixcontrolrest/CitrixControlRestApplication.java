package org.example.citrixcontrolrest;

import org.example.citrixcontrolrest.controller.NavigationController;
import org.example.citrixcontrolrest.service.CitrixService;
import org.example.citrixcontrolrest.ui.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.swing.*;

@SpringBootApplication
@EnableScheduling
public class CitrixControlRestApplication {

    public static void main(String[] args) {
        // Configurar para permitir GUI y desactivar headless
        System.setProperty("java.awt.headless", "false");

        // Iniciar Spring Boot con configuración para GUI
        ConfigurableApplicationContext context = new SpringApplicationBuilder(CitrixControlRestApplication.class)
                .headless(false)
                .run(args);

        // Obtener el servicio de Citrix del contexto de Spring
        CitrixService citrixService = context.getBean(CitrixService.class);

        // Lanzar la interfaz gráfica en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mainFrame = new MainFrame(citrixService);
                NavigationController navController = new NavigationController(mainFrame);

                // Configurar los paneles
                navController.addPanel("SITE", new SitePanel(citrixService));
                navController.addPanel("DGs", new DGPanel());
                navController.addPanel("VDAs", new VDAPanel());
                navController.addPanel("APPs", new APPPanel());
                navController.addPanel("Active Users", new UserPanel());
                navController.addPanel("CONFIG", new ConfigPanel(citrixService, mainFrame, navController));

                // Mostrar el panel inicial
                navController.showPanel("CONFIG");
                mainFrame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar la interfaz gráfica: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}

