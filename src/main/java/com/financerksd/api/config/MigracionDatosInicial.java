package com.financerksd.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

// Hibernate (ddl-auto=update) agrega las columnas nuevas, pero las filas que ya existian quedan en NULL.
// Este runner las rellena. Es idempotente: solo toca filas con NULL, asi que puede correr en cada arranque.
@Configuration
public class MigracionDatosInicial {

    private static final Logger log = LoggerFactory.getLogger(MigracionDatosInicial.class);

    @Bean
    CommandLineRunner migrarDatosViejos(JdbcTemplate jdbc) {
        return args -> {
            try {
                // Mes de cada plan viejo = el mes en que se creo.
                int p = jdbc.update("""
                        UPDATE planes_mejora
                           SET periodo_inicio = date_trunc('month', fecha_creacion)::date,
                               periodo_fin = (date_trunc('month', fecha_creacion) + interval '1 month - 1 day')::date
                         WHERE periodo_inicio IS NULL OR periodo_fin IS NULL""");

                // Antes "plan activo" = el mas reciente del cliente. Se conserva: ese queda ACTIVO y los demas CERRADO.
                int e = jdbc.update("""
                        UPDATE planes_mejora p
                           SET estado = CASE WHEN p.id_plan = (
                                   SELECT p2.id_plan FROM planes_mejora p2
                                    WHERE p2.cliente_id_usuario = p.cliente_id_usuario
                                    ORDER BY p2.fecha_creacion DESC, p2.id_plan DESC LIMIT 1)
                               THEN 'ACTIVO' ELSE 'CERRADO' END
                         WHERE p.estado IS NULL""");

                // Clientes que ya tenian plan arrancan como ACTIVO; los demas como PROSPECTO.
                int c = jdbc.update("""
                        UPDATE clientes c
                           SET estado_cliente = CASE WHEN EXISTS (
                                   SELECT 1 FROM planes_mejora p WHERE p.cliente_id_usuario = c.id_usuario)
                               THEN 'ACTIVO' ELSE 'PROSPECTO' END
                         WHERE c.estado_cliente IS NULL""");

                // Metas viejas no tienen monto: son de tipo TEXTO.
                int m = jdbc.update("UPDATE metas_financieras SET tipo = 'TEXTO' WHERE tipo IS NULL");

                if (p + e + c + m > 0) {
                    log.info("Migracion de datos viejos: {} planes con periodo, {} con estado, {} clientes con estado, {} metas con tipo.", p, e, c, m);
                }
            } catch (Exception ex) {
                // No tumbar el arranque: la app funciona igual (los getters tienen valores por defecto).
                log.warn("No se pudo completar la migracion de datos viejos: {}", ex.getMessage());
            }
        };
    }
}
