package cl.duoc.lumina.service;

import cl.duoc.lumina.model.Orden;
import cl.duoc.lumina.model.Producto;
import cl.duoc.lumina.repository.RepositorioOrdenes;
import cl.duoc.lumina.repository.RepositorioProductos;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrdenesService {
    private static final Set<String> ESTADOS = Set.of("pendiente", "enviada", "entregada", "cancelada");

    private final RepositorioOrdenes repositorio;
    private final RepositorioProductos productos;

    public OrdenesService(RepositorioOrdenes repositorio, RepositorioProductos productos) {
        this.repositorio = repositorio;
        this.productos = productos;
    }

    // Lo único que manda el frontend es qué producto y cuántos. El precio y el total
    // los pone el servidor, así nadie puede comprar más barato editando la petición.
    public record ItemPedido(String codigo, int cantidad) {
    }

    public Orden crear(String correo, List<ItemPedido> pedido) {
        if (pedido == null || pedido.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El carrito está vacío");
        }
        // Si el mismo producto viene dos veces, se suman las cantidades.
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (ItemPedido item : pedido) {
            if (item.cantidad() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser al menos 1");
            }
            cantidades.merge(item.codigo(), item.cantidad(), Integer::sum);
        }

        // Primero se valida todo; solo si todo el carrito tiene stock se descuenta.
        // ponytail: validar y descontar no es atómico; dos compras simultáneas del último stock
        // podrían pasar ambas. Si importa, usar una transacción de MongoDB (Atlas las soporta).
        List<Producto> comprados = new ArrayList<>();
        List<Orden.Item> items = new ArrayList<>();
        int total = 0;
        for (var entrada : cantidades.entrySet()) {
            Producto producto = productos.findById(entrada.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el producto " + entrada.getKey()));
            int cantidad = entrada.getValue();
            if (producto.getStock() < cantidad) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente de " + producto.getNombre() + " (quedan " + producto.getStock() + ")");
            }
            producto.setStock(producto.getStock() - cantidad);
            comprados.add(producto);

            Orden.Item item = new Orden.Item();
            item.setCodigo(producto.getCodigo());
            item.setNombre(producto.getNombre());
            item.setPrecio(producto.getPrecio());
            item.setCantidad(cantidad);
            items.add(item);
            total += producto.getPrecio() * cantidad;
        }
        productos.saveAll(comprados);

        Orden orden = new Orden();
        orden.setCorreo(correo);
        orden.setItems(items);
        orden.setTotal(total);
        orden.setEstado("pendiente");
        orden.setFecha(Instant.now());
        return repositorio.save(orden);
    }

    public List<Orden> listar() {
        return repositorio.findAll(Sort.by(Sort.Direction.DESC, "fecha"));
    }

    public List<Orden> deCliente(String correo) {
        return repositorio.findByCorreoOrderByFechaDesc(correo);
    }

    public Orden cambiarEstado(String id, String estado) {
        if (!ESTADOS.contains(estado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido: " + estado);
        }
        Orden orden = repositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la orden"));
        if ("cancelada".equals(orden.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La orden ya está cancelada");
        }
        // Al cancelar, el stock vuelve al inventario.
        if ("cancelada".equals(estado)) {
            for (Orden.Item item : orden.getItems()) {
                productos.findById(item.getCodigo()).ifPresent(p -> {
                    p.setStock(p.getStock() + item.getCantidad());
                    productos.save(p);
                });
            }
        }
        orden.setEstado(estado);
        return repositorio.save(orden);
    }
}
