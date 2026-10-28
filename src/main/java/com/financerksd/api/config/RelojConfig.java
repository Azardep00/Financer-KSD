package com.financerksd.api.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// El servidor (Render) corre en UTC: sin esto, "hoy" cambia a las 7pm hora Colombia y el
// cierre de mes / las fechas de los planes quedarian corridas. Configurable con APP_ZONA_HORARIA.
@Configuration
public class RelojConfig {

    @Bean
    Clock reloj(@Value("${app.zona-horaria:America/Bogota}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
