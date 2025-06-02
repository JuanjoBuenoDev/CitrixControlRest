package org.example.citrixcontrolrest;

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
        System.setProperty("java.awt.headless", "false");

        // Iniciar Spring Boot
        ConfigurableApplicationContext context = new SpringApplicationBuilder(CitrixControlRestApplication.class)
                .headless(false)
                .run(args);

        CitrixService citrixService = context.getBean(CitrixService.class);

        SwingUtilities.invokeLater(() -> {
            try {
                // Crear UI principal
                MainFrame mainFrame = new MainFrame(citrixService);

                // Registrar paneles
                mainFrame.addPanel("SITE", new SitePanel(citrixService));
                mainFrame.addPanel("DGs", new DGPanel());
                mainFrame.addPanel("VDAs", new VDAPanel());
                mainFrame.addPanel("APPs", new APPPanel());
                mainFrame.addPanel("Active Users", new UserPanel());
                mainFrame.addPanel("CONFIG", new ConfigPanel(citrixService, mainFrame));

                // Mostrar pantalla inicial
                mainFrame.showPanel("CONFIG");
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
