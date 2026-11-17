package cl.duoc.lumina.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "productos")
public class Producto {
    // El código del catálogo (ej: LMP-001) es la clave del documento.
    @Id
    @Schema(description = "Código único del producto", example = "LMP-001")
    @NotBlank
    private String codigo;

    @Schema(example = "Lámpara Colgante Nordic Oak")
    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Schema(description = "Id de la categoría", example = "techo")
    @NotBlank
    private String categoria;

    @Schema(description = "Precio en pesos chilenos (CLP)", example = "45990")
    @Min(0)
    private int precio;

    @Schema(example = "14")
    @Min(0)
    private int stock;

    @Schema(description = "Bajo este stock el producto aparece en las alertas", example = "3")
    @Min(0)
    private int stockCritico;

    @Size(max = 500)
    private String descripcion;

    @Schema(example = "/img/techo 1.jpg")
    private String imagen;
}
