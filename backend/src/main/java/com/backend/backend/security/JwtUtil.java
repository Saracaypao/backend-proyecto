package com.backend.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    // Clave secreta para firmar los tokens (codificada en base64)
    private static final String SECRET = "zLkjs7Q8zZp2+WaMiwRZhHnFKuBbKktXcQ6+LdRWD7c=";

    // La duracion del token (1 hora)
    private static final long EXPIRATION_TIME = 3600000;

    // Devuelve la contraseña usada para firmar/verificar el token
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    // Genera un token JWT con el ID y correo del usuario
    public String generateToken(UUID userId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId.toString())
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    // Genera un token JWT con el ID, correo y rol del usuario
    public String generateToken(UUID userId, String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId.toString())
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    // Extrae el correo desde el token
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // Extrae el ID del usuario desde el token
    public String extractUserId(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    // Extrae el rol del usuario desde el token
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // Verifica si el token es valido
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Lee el token y devuelve los datos
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
