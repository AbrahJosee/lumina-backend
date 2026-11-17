package cl.duoc.lumina.security;

import cl.duoc.lumina.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey clave;
    private final Duration duracionAccess;
    private final Duration duracionRefresh;

    public JwtService(
            @Value("${jwt.secret}") String secreto,
            @Value("${jwt.access-minutos}") long accessMinutos,
            @Value("${jwt.refresh-dias}") long refreshDias) {
        // HMAC-SHA exige una clave de al menos 256 bits (32 caracteres).
        if (secreto == null || secreto.length() < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionAccess = Duration.ofMinutes(accessMinutos);
        this.duracionRefresh = Duration.ofDays(refreshDias);
    }

    // Token corto que el frontend manda en cada petición (header Authorization).
    public String generarAccess(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getTipoUsuario())
                .claim("tipo", "access")
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracionAccess)))
                .signWith(clave)
                .compact();
    }

    // Token largo que solo sirve para pedir un access token nuevo en /api/auth/refresh.
    // Su id (jti) se guarda en el usuario para poder invalidarlo.
    public String generarRefresh(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .id(usuario.getRefreshId())
                .claim("tipo", "refresh")
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracionRefresh)))
                .signWith(clave)
                .compact();
    }

    // Verifica la firma y la expiración; lanza JwtException si algo no calza.
    // También revisa el tipo, para que un refresh token no sirva como access token y viceversa.
    public Claims leer(String token, String tipoEsperado) {
        Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
        if (!tipoEsperado.equals(claims.get("tipo"))) {
            throw new JwtException("Tipo de token incorrecto");
        }
        return claims;
    }
}
