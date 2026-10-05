package com.financerksd.api.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.financerksd.api.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret:financer-ksd-clave-de-desarrollo-cambiar-en-produccion-32}")
    private String secretoConfigurado;

    @Value("${jwt.expiracion-ms:86400000}") // 24 horas por defecto
    private long expiracionMs;

    private SecretKey clave() {
        return Keys.hmacShaKeyFor(secretoConfigurado.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(String.valueOf(usuario.getIdUsuario()))
                .claim("correo", usuario.getCorreo())
                .claim("nombre", usuario.getNombre())
                .claim("tipoUsuario", usuario.getTipoUsuario())
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(clave())
                .compact();
    }

    public Claims validarYExtraer(String token) {
        return Jwts.parser().verifyWith(clave()).build().parseSignedClaims(token).getPayload();
    }
}
