package cl.duoc.lumina.repository;

import cl.duoc.lumina.model.Producto;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RepositorioProductos extends MongoRepository<Producto, String> {
    // Spring Data arma la consulta a partir del nombre del método.
    List<Producto> findByCategoriaOrderByCodigo(String categoria);

    boolean existsByCategoria(String categoria);
}
