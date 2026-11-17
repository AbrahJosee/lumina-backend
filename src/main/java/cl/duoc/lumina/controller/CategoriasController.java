package cl.duoc.lumina.controller;

import cl.duoc.lumina.model.Categoria;
import cl.duoc.lumina.service.CategoriasService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Leer es público; modificar, solo administrador.")
public class CategoriasController {
    private final CategoriasService servicio;

    public CategoriasController(CategoriasService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Listar categorías")
    @GetMapping
    public List<Categoria> listar() {
        return servicio.listar();
    }

    @Operation(summary = "Crear una categoría")
    @ApiResponse(responseCode = "201", description = "Creada")
    @SecurityRequirement(name = "bearer")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria crear(@Valid @RequestBody Categoria categoria) {
        return servicio.crear(categoria);
    }

    @Operation(summary = "Renombrar una categoría")
    @SecurityRequirement(name = "bearer")
    @PutMapping("/{id}")
    public Categoria actualizar(@PathVariable String id, @Valid @RequestBody Categoria categoria) {
        return servicio.actualizar(id, categoria);
    }

    @Operation(summary = "Eliminar una categoría", description = "Falla con 409 si tiene productos.")
    @ApiResponse(responseCode = "204", description = "Eliminada")
    @ApiResponse(responseCode = "409", description = "Tiene productos asociados")
    @SecurityRequirement(name = "bearer")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        servicio.eliminar(id);
    }
}
