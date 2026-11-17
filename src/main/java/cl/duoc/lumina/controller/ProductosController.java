package cl.duoc.lumina.controller;

import cl.duoc.lumina.model.Producto;
import cl.duoc.lumina.service.ProductosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Catálogo. Leer es público; crear y editar requiere administrador o vendedor; eliminar, solo administrador.")
public class ProductosController {
    private final ProductosService servicio;

    public ProductosController(ProductosService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Listar productos", description = "Ordenados por código. Se puede filtrar por categoría.")
    @GetMapping
    public List<Producto> listar(
            @Parameter(description = "Id de categoría, ej: techo") @RequestParam(required = false) String categoria) {
        return servicio.listar(categoria);
    }

    @Operation(summary = "Obtener un producto")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/{codigo}")
    public Producto obtener(@PathVariable String codigo) {
        return servicio.obtener(codigo);
    }

    @Operation(summary = "Crear un producto")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "409", description = "El código ya existe")
    @SecurityRequirement(name = "bearer")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@Valid @RequestBody Producto producto) {
        return servicio.crear(producto);
    }

    @Operation(summary = "Actualizar un producto")
    @SecurityRequirement(name = "bearer")
    @PutMapping("/{codigo}")
    public Producto actualizar(@PathVariable String codigo, @Valid @RequestBody Producto producto) {
        return servicio.actualizar(codigo, producto);
    }

    @Operation(summary = "Eliminar un producto", description = "Solo administrador.")
    @ApiResponse(responseCode = "204", description = "Eliminado")
    @SecurityRequirement(name = "bearer")
    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String codigo) {
        servicio.eliminar(codigo);
    }
}
