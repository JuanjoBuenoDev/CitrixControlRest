package org.example.citrixcontrolrest.utils;

import org.example.citrixcontrolrest.scheduler.CitrixScheduledUpdater;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ToastNotifier toastNotifier() {
        return new ToastNotifier(); // Asegúrate de que tenga un constructor sin parámetros
    }

    @Bean
    public CitrixScheduledUpdater citrixScheduledUpdater(ToastNotifier toastNotifier) {
        return new CitrixScheduledUpdater(6, toastNotifier);
    }
}

