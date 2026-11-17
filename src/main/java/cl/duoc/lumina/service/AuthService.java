package cl.duoc.lumina.service;

import cl.duoc.lumina.model.Usuario;
import cl.duoc.lumina.repository.RepositorioUsuarios;
import cl.duoc.lumina.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final String DOMINIOS = "(?i).+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)";

    private final RepositorioUsuarios repositorio;
    private final UsuariosService usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(RepositorioUsuarios repositorio, UsuariosService usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.repositorio = repositorio;
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public record Login(@NotBlank @Email String correo, @NotBlank String password) {
    }

    public record Registro(
            @NotBlank String nombre,
            @NotBlank @Email String correo,
            @NotBlank String password,
            @NotBlank String region,
            @NotBlank String comuna,
            @NotBlank String direccion) {
    }

    public record Refresh(@NotBlank String refreshToken) {
    }

    public record Sesion(String accessToken, String refreshToken, Usuario usuario) {
    }

    public Sesion login(Login datos) {
        // Mismo mensaje si falla el correo o la contraseña, para no revelar qué correos existen.
        Usuario usuario = repositorio.findByCorreoIgnoreCase(datos.correo().trim())
                .filter(u -> encoder.matches(datos.password(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
        return emitir(usuario);
    }

    // El registro público siempre crea clientes; los otros roles los crea un administrador.
    public Usuario registrar(Registro datos) {
        if (!datos.correo().matches(DOMINIOS)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se aceptan correos @duoc.cl, @profesor.duoc.cl o @gmail.com");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(datos.nombre());
        usuario.setCorreo(datos.correo().trim());
        usuario.setPassword(datos.password());
        usuario.setRegion(datos.region());
        usuario.setComuna(datos.comuna());
        usuario.setDireccion(datos.direccion());
        usuario.setTipoUsuario("cliente");
        return usuarios.crear(usuario);
    }

    // Renovación con rotación: el refresh token usado queda inválido y se entrega uno nuevo.
    public Sesion renovar(String refreshToken) {
        Claims claims;
        try {
            claims = jwt.leer(refreshToken, "refresh");
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión expirada");
        }
        Usuario usuario = repositorio.findByCorreoIgnoreCase(claims.getSubject())
                .filter(u -> claims.getId().equals(u.getRefreshId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión expirada"));
        return emitir(usuario);
    }

    // Al cerrar sesión se borra el refresh vigente: ya no se puede renovar con él.
    public void logout(String correo) {
        repositorio.findByCorreoIgnoreCase(correo).ifPresent(u -> {
            u.setRefreshId(null);
            repositorio.save(u);
        });
    }

    private Sesion emitir(Usuario usuario) {
        usuario.setRefreshId(UUID.randomUUID().toString());
        repositorio.save(usuario);
        return new Sesion(jwt.generarAccess(usuario), jwt.generarRefresh(usuario), usuario);
    }
}
