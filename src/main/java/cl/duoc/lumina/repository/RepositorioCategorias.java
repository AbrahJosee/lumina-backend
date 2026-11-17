package cl.duoc.lumina.repository;

import cl.duoc.lumina.model.Categoria;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RepositorioCategorias extends MongoRepository<Categoria, String> {
}
