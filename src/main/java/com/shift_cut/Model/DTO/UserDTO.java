package com.shift_cut.Model.DTO;

import com.shift_cut.Model.Enum.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos publicos del usuario (sin contrasena)")
public class UserDTO {

    @Schema(description = "ID unico del usuario", example = "1")
    private Long id;

    @Schema(description = "Nombre de usuario", example = "juanperez")
    private String username;

    @Schema(description = "Email del usuario", example = "juan@example.com")
    private String email;

    @Schema(description = "Rol del usuario en el sistema", example = "USER")
    private Role role;

    @Schema(description = "Estado de la cuenta (true = activa)", example = "true")
    private boolean status;
}
