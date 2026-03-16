package com.shift_cut.Model.DTO;

import com.shift_cut.Model.Enum.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos publicos del usuario (sin contrasena)")
public record UserDTO(

    @Schema(description = "ID unico del usuario", example = "1")
    Long id,

    @Schema(description = "Nombre de usuario", example = "juanperez")
    String username,

    @Schema(description = "Email del usuario", example = "juan@example.com")
    String email,

    @Schema(description = "Rol del usuario en el sistema", example = "USER")
    Role role,

    @Schema(description = "Estado de la cuenta (true = activa)", example = "true")
    boolean status
) {
}
