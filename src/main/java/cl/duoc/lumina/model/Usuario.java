package cl.duoc.lumina.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String id;

    // RUN y apellidos son opcionales porque el registro de la tienda no los pide.
    @Schema(description = "RUN sin puntos ni guion", example = "199608584")
    @Pattern(regexp = "[0-9]{7,8}[0-9kK]", message = "RUN inválido")
    private String run;

    @NotBlank
    private String nombre;

    private String apellidos;

    @Schema(example = "camila.ibanez@gmail.com")
    @NotBlank
    @Email
    private String correo;

    // Solo se recibe (al crear o editar); nunca se devuelve en las respuestas.
    // En la base se guarda el hash BCrypt, no la contraseña.
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(example = "1998-01-20")
    private String fechaNacimiento;

    @Schema(allowableValues = {"administrador", "vendedor", "cliente"})
    @NotBlank
    @Pattern(regexp = "administrador|vendedor|cliente")
    private String tipoUsuario;

    private String region;
    private String comuna;
    private String direccion;

    // Id del refresh token vigente. Al renovar se reemplaza y al cerrar sesión se borra,
    // así un refresh token robado o ya usado deja de servir.
    @JsonIgnore
    private String refreshId;
}
