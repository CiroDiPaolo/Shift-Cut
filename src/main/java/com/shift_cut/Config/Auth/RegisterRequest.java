package com.shift_cut.Config.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Peticion de registro de nuevo usuario")
public record RegisterRequest(

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Nombre de usuario unico", example = "juanperez", requiredMode = Schema.RequiredMode.REQUIRED)
    String username,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Schema(description = "Email unico del usuario", example = "juan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Contrasena (minimo 6 caracteres)", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) {
}
