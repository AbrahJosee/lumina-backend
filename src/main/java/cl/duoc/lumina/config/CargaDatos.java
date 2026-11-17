package cl.duoc.lumina.config;

import cl.duoc.lumina.model.Categoria;
import cl.duoc.lumina.model.Producto;
import cl.duoc.lumina.model.Usuario;
import cl.duoc.lumina.repository.RepositorioCategorias;
import cl.duoc.lumina.repository.RepositorioProductos;
import cl.duoc.lumina.repository.RepositorioUsuarios;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

// Al arrancar, si una colección está vacía, la llena con los JSON de resources/data
// (los mismos datos que usaba el frontend). Si ya tiene datos, no toca nada.
@Component
public class CargaDatos implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(CargaDatos.class);

    private final RepositorioCategorias categorias;
    private final RepositorioProductos productos;
    private final RepositorioUsuarios usuarios;
    private final PasswordEncoder encoder;
    private final ObjectMapper json;

    public CargaDatos(RepositorioCategorias categorias, RepositorioProductos productos,
            RepositorioUsuarios usuarios, PasswordEncoder encoder, ObjectMapper json) {
        this.categorias = categorias;
        this.productos = productos;
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.json = json;
    }

    @Override
    public void run(String... args) throws IOException {
        if (categorias.count() == 0) {
            categorias.saveAll(leer("categorias.json", new TypeReference<List<Categoria>>() {}));
            log.info("Categorías cargadas");
        }
        if (productos.count() == 0) {
            productos.saveAll(leer("productos.json", new TypeReference<List<Producto>>() {}));
            log.info("Productos cargados");
        }
        if (usuarios.count() == 0) {
            List<Usuario> lista = leer("usuarios.json", new TypeReference<List<Usuario>>() {});
            lista.forEach(u -> u.setPassword(encoder.encode(u.getPassword())));
            usuarios.saveAll(lista);
            log.info("Usuarios cargados");
        }
    }

    private <T> T leer(String archivo, TypeReference<T> tipo) throws IOException {
        try (InputStream entrada = new ClassPathResource("data/" + archivo).getInputStream()) {
            return json.readValue(entrada, tipo);
        }
    }
}
