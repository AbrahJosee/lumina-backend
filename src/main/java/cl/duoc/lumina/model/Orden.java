package cl.duoc.lumina.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "ordenes")
public class Orden {
    @Id
    private String id;

    @Schema(description = "Correo del cliente que compró")
    private String correo;

    // Los ítems van dentro de la orden (documento embebido) con el nombre y precio del
    // momento de la compra: si después cambia el precio del producto, la orden no cambia.
    private List<Item> items;

    @Schema(description = "Total calculado por el servidor, en CLP")
    private int total;

    @Schema(allowableValues = {"pendiente", "enviada", "entregada", "cancelada"})
    private String estado;

    private Instant fecha;

    @Getter
    @Setter
    public static class Item {
        private String codigo;
        private String nombre;
        private int precio;
        private int cantidad;
    }
}
