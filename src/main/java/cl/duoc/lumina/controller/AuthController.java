package cl.duoc.lumina.controller;

import cl.duoc.lumina.model.Usuario;
import cl.duoc.lumina.service.AuthService;
import cl.duoc.lumina.service.UsuariosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login, registro y renovación de tokens JWT")
public class AuthController {
    private final AuthService servicio;
    private final UsuariosService usuarios;

    public AuthController(AuthService servicio, UsuariosService usuarios) {
        this.servicio = servicio;
        this.usuarios = usuarios;
    }

    @Operation(summary = "Iniciar sesión", description = "Devuelve un access token (15 min), un refresh token (7 días) y los datos del usuario.")
    @ApiResponse(responseCode = "200", description = "Credenciales correctas")
    @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos")
    @PostMapping("/login")
    public AuthService.Sesion login(@Valid @RequestBody AuthService.Login datos) {
        return servicio.login(datos);
    }

    @Operation(summary = "Registrar un cliente", description = "Crea una cuenta con perfil cliente.")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado")
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario registro(@Valid @RequestBody AuthService.Registro datos) {
        return servicio.registrar(datos);
    }

    @Operation(summary = "Renovar tokens", description = "Cambia un refresh token vigente por un par de tokens nuevos. El refresh usado deja de servir.")
    @ApiResponse(responseCode = "200", description = "Tokens renovados")
    @ApiResponse(responseCode = "401", description = "Refresh token vencido, alterado o ya usado")
    @PostMapping("/refresh")
    public AuthService.Sesion refresh(@Valid @RequestBody AuthService.Refresh datos) {
        return servicio.renovar(datos.refreshToken());
    }

    @Operation(summary = "Cerrar sesión", description = "Invalida el refresh token del usuario.")
    @SecurityRequirement(name = "bearer")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication auth) {
        servicio.logout(auth.getName());
    }

    @Operation(summary = "Usuario actual", description = "Datos del dueño del token.")
    @SecurityRequirement(name = "bearer")
    @GetMapping("/yo")
    public Usuario yo(Authentication auth) {
        return usuarios.buscarPorCorreo(auth.getName());
    }
}
