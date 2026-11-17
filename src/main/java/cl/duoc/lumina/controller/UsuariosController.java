package cl.duoc.lumina.controller;

import cl.duoc.lumina.model.Usuario;
import cl.duoc.lumina.service.UsuariosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@SecurityRequirement(name = "bearer")
@Tag(name = "Usuarios", description = "Gestión de usuarios. Solo administrador.")
public class UsuariosController {
    private final UsuariosService servicio;

    public UsuariosController(UsuariosService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Listar usuarios")
    @GetMapping
    public List<Usuario> listar() {
        return servicio.listar();
    }

    @Operation(summary = "Obtener un usuario")
    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Crear un usuario", description = "Permite elegir el perfil (administrador, vendedor o cliente).")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "409", description = "Correo o RUN repetido")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@Valid @RequestBody Usuario usuario) {
        return servicio.crear(usuario);
    }

    @Operation(summary = "Actualizar un usuario", description = "Si no se envía password, se mantiene la actual.")
    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable String id, @Valid @RequestBody Usuario usuario) {
        return servicio.actualizar(id, usuario);
    }

    @Operation(summary = "Eliminar un usuario", description = "Un administrador no puede eliminarse a sí mismo.")
    @ApiResponse(responseCode = "204", description = "Eliminado")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id, Authentication auth) {
        servicio.eliminar(id, auth.getName());
    }
}
