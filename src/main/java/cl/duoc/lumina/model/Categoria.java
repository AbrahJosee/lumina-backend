package cl.duoc.lumina.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "categorias")
public class Categoria {
    // El id es legible (ej: "techo") porque los productos lo guardan como referencia.
    @Id
    @Schema(description = "Identificador de la categoría", example = "techo")
    @NotBlank
    @Pattern(regexp = "[a-z0-9-]+", message = "solo minúsculas, números y guiones")
    private String id;

    @Schema(description = "Nombre visible", example = "Lámparas de Techo")
    @NotBlank
    private String nombre;
}
