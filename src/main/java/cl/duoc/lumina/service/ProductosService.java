package cl.duoc.lumina.service;

import cl.duoc.lumina.model.Producto;
import cl.duoc.lumina.repository.RepositorioCategorias;
import cl.duoc.lumina.repository.RepositorioProductos;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductosService {
    private final RepositorioProductos repositorio;
    private final RepositorioCategorias categorias;

    public ProductosService(RepositorioProductos repositorio, RepositorioCategorias categorias) {
        this.repositorio = repositorio;
        this.categorias = categorias;
    }

    public List<Producto> listar(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return repositorio.findAll(Sort.by("codigo"));
        }
        return repositorio.findByCategoriaOrderByCodigo(categoria);
    }

    public Producto obtener(String codigo) {
        return repositorio.findById(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el producto " + codigo));
    }

    public Producto crear(Producto producto) {
        if (repositorio.existsById(producto.getCodigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe el producto " + producto.getCodigo());
        }
        validarCategoria(producto.getCategoria());
        return repositorio.save(producto);
    }

    public Producto actualizar(String codigo, Producto datos) {
        obtener(codigo);
        validarCategoria(datos.getCategoria());
        // El código de la URL manda: así no se puede "mover" un producto a otro código.
        datos.setCodigo(codigo);
        return repositorio.save(datos);
    }

    public void eliminar(String codigo) {
        obtener(codigo);
        repositorio.deleteById(codigo);
    }

    private void validarCategoria(String categoria) {
        if (!categorias.existsById(categoria)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la categoría " + categoria);
        }
    }
}
