package cl.duoc.lumina.repository;

import cl.duoc.lumina.model.Orden;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RepositorioOrdenes extends MongoRepository<Orden, String> {
    List<Orden> findByCorreoOrderByFechaDesc(String correo);
}
