package cl.duoc.lumina.repository;

import cl.duoc.lumina.model.Usuario;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RepositorioUsuarios extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByRun(String run);
}
