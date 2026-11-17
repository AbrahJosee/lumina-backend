package cl.duoc.lumina.service;

import cl.duoc.lumina.model.Categoria;
import cl.duoc.lumina.repository.RepositorioCategorias;
import cl.duoc.lumina.repository.RepositorioProductos;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoriasService {
    private final RepositorioCategorias repositorio;
    private final RepositorioProductos productos;

    public CategoriasService(RepositorioCategorias repositorio, RepositorioProductos productos) {
        this.repositorio = repositorio;
        this.productos = productos;
    }

    public List<Categoria> listar() {
        return repositorio.findAll();
    }

    public Categoria crear(Categoria categoria) {
        if (repositorio.existsById(categoria.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe la categoría " + categoria.getId());
        }
        return repositorio.save(categoria);
    }

    public Categoria actualizar(String id, Categoria datos) {
        Categoria categoria = obtener(id);
        categoria.setNombre(datos.getNombre());
        return repositorio.save(categoria);
    }

    public void eliminar(String id) {
        obtener(id);
        // No se borra una categoría con productos: quedarían apuntando a algo que no existe.
        if (productos.existsByCategoria(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría tiene productos asociados");
        }
        repositorio.deleteById(id);
    }

    private Categoria obtener(String id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la categoría " + id));
    }
}
