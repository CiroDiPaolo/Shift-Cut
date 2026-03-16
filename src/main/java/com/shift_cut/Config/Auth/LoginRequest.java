package com.shift_cut.Config.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Peticion de inicio de sesion")
public record LoginRequest(

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Schema(description = "Email registrado del usuario", example = "juan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @NotBlank(message = "Password cannot be blank")
    @Schema(description = "Contrasena del usuario", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) {
}
