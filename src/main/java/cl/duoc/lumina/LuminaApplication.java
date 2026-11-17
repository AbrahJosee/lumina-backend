package cl.duoc.lumina;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "API de Lumina Home",
        description = "Tienda de iluminación: catálogo, usuarios, órdenes y autenticación con JWT.",
        version = "1.0"))
// Agrega el botón "Authorize" en Swagger para pegar el access token.
@SecurityScheme(name = "bearer", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class LuminaApplication {
    public static void main(String[] args) {
        SpringApplication.run(LuminaApplication.class, args);
    }
}
