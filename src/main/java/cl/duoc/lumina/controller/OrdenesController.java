package cl.duoc.lumina.controller;

import cl.duoc.lumina.model.Orden;
import cl.duoc.lumina.service.OrdenesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordenes")
@SecurityRequirement(name = "bearer")
@Tag(name = "Órdenes", description = "Compras. Cualquier usuario con sesión compra; administrador y vendedor las gestionan.")
public class OrdenesController {
    private final OrdenesService servicio;

    public OrdenesController(OrdenesService servicio) {
        this.servicio = servicio;
    }

    public record CambioEstado(String estado) {
    }

    @Operation(summary = "Comprar el carrito",
            description = "Valida el stock, lo descuenta y calcula el total en el servidor. Recibe solo código y cantidad.")
    @ApiResponse(responseCode = "201", description = "Orden creada")
    @ApiResponse(responseCode = "409", description = "Stock insuficiente")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Orden crear(@RequestBody List<OrdenesService.ItemPedido> items, Authentication auth) {
        return servicio.crear(auth.getName(), items);
    }

    @Operation(summary = "Mis órdenes", description = "Las órdenes del usuario del token.")
    @GetMapping("/mias")
    public List<Orden> mias(Authentication auth) {
        return servicio.deCliente(auth.getName());
    }

    @Operation(summary = "Listar todas las órdenes", description = "Administrador o vendedor.")
    @GetMapping
    public List<Orden> listar() {
        return servicio.listar();
    }

    @Operation(summary = "Cambiar el estado", description = "pendiente, enviada, entregada o cancelada. Cancelar devuelve el stock.")
    @PutMapping("/{id}/estado")
    public Orden cambiarEstado(@PathVariable String id, @RequestBody CambioEstado cambio) {
        return servicio.cambiarEstado(id, cambio.estado());
    }
}
