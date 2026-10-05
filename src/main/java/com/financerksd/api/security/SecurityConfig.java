package com.financerksd.api.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Value("${app.cors.origins:http://localhost:4200,http://localhost:5173}")
    private List<String> origenesPermitidos;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                    .authenticationEntryPoint(new RespuestaJsonNoAutenticado())
                    .accessDeniedHandler(new RespuestaJsonSinPermiso()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Publico: registro de clientes, login y documentacion.
                .requestMatchers(HttpMethod.POST, "/api/usuarios/clientes").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/usuarios/login").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                // Solo asesores.
                .requestMatchers(HttpMethod.POST, "/api/usuarios/asesores").hasRole("ASESOR")
                .requestMatchers(HttpMethod.GET, "/api/clientes").hasRole("ASESOR")
                .requestMatchers(HttpMethod.PATCH, "/api/clientes/*/asesor").hasRole("ASESOR")
                .requestMatchers(HttpMethod.POST, "/api/clientes/*/diagnosticos").hasRole("ASESOR")
                .requestMatchers(HttpMethod.POST, "/api/clientes/*/planes").hasRole("ASESOR")
                .requestMatchers(HttpMethod.PUT, "/api/planes/*").hasRole("ASESOR")
                .requestMatchers(HttpMethod.POST, "/api/planes/*/metas").hasRole("ASESOR")
                .requestMatchers(HttpMethod.PUT, "/api/metas/*").hasRole("ASESOR")
                .requestMatchers(HttpMethod.DELETE, "/api/metas/*").hasRole("ASESOR")

                // Resto de /api: basta con estar logueado. Cada controller valida ademas
                // que el cliente sea DUEÑO del recurso (AutorizacionService).
                .requestMatchers("/api/**").authenticated()

                .anyRequest().permitAll())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenesPermitidos);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
