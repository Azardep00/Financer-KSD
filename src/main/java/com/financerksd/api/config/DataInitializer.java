package com.financerksd.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.financerksd.api.model.Asesor;
import com.financerksd.api.repository.UsuarioRepository;

// Como solo un asesor puede crear asesores, necesitamos uno inicial.
// Se crea unicamente si la tabla de usuarios esta vacia.
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner crearAsesorInicial(
            UsuarioRepository repo,
            @Value("${app.admin.nombre:Admin}") String nombre,
            @Value("${app.admin.apellido:Financer}") String apellido,
            @Value("${app.admin.correo:admin@financerksd.com}") String correo,
            @Value("${app.admin.contrasena:Admin123!}") String contrasena) {
        return args -> {
            if (repo.count() == 0) {
                Asesor a = new Asesor();
                a.setNombre(nombre);
                a.setApellido(apellido);
                a.setCorreo(correo);
                a.setEspecialidad("General");
                a.establecerContrasena(contrasena);
                repo.save(a);
                System.out.println(">>> Asesor inicial creado: " + correo + " (cambia la contraseña en produccion)");
            }
        };
    }
}
