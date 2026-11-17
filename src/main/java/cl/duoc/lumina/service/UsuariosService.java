package cl.duoc.lumina.service;

import cl.duoc.lumina.model.Usuario;
import cl.duoc.lumina.repository.RepositorioUsuarios;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuariosService {
    private final RepositorioUsuarios repositorio;
    private final PasswordEncoder encoder;

    public UsuariosService(RepositorioUsuarios repositorio, PasswordEncoder encoder) {
        this.repositorio = repositorio;
        this.encoder = encoder;
    }

    public List<Usuario> listar() {
        return repositorio.findAll();
    }

    public Usuario obtener(String id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el usuario"));
    }

    public Usuario buscarPorCorreo(String correo) {
        return repositorio.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el usuario"));
    }

    public Usuario crear(Usuario usuario) {
        if (repositorio.existsByCorreoIgnoreCase(usuario.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (usuario.getRun() != null && repositorio.existsByRun(usuario.getRun())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El RUN ya está registrado");
        }
        validarPassword(usuario.getPassword());
        usuario.setId(null);
        usuario.setPassword(encoder.encode(usuario.getPassword()));
        return repositorio.save(usuario);
    }

    public Usuario actualizar(String id, Usuario datos) {
        Usuario usuario = obtener(id);
        if (!usuario.getCorreo().equalsIgnoreCase(datos.getCorreo())
                && repositorio.existsByCorreoIgnoreCase(datos.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        usuario.setRun(datos.getRun());
        usuario.setNombre(datos.getNombre());
        usuario.setApellidos(datos.getApellidos());
        usuario.setCorreo(datos.getCorreo());
        usuario.setFechaNacimiento(datos.getFechaNacimiento());
        usuario.setTipoUsuario(datos.getTipoUsuario());
        usuario.setRegion(datos.getRegion());
        usuario.setComuna(datos.getComuna());
        usuario.setDireccion(datos.getDireccion());
        // La contraseña solo cambia si viene una nueva.
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            validarPassword(datos.getPassword());
            usuario.setPassword(encoder.encode(datos.getPassword()));
        }
        return repositorio.save(usuario);
    }

    public void eliminar(String id, String correoActual) {
        Usuario usuario = obtener(id);
        if (usuario.getCorreo().equalsIgnoreCase(correoActual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes eliminar tu propia cuenta");
        }
        repositorio.delete(usuario);
    }

    // Misma regla que el formulario del frontend.
    static void validarPassword(String password) {
        if (password == null || password.length() < 4 || password.length() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener entre 4 y 10 caracteres");
        }
    }
}
