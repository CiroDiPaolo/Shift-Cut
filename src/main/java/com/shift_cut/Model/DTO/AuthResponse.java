package com.shift_cut.Model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticacion que contiene el token JWT")
public record AuthResponse(

    @Schema(description = "Token JWT para autenticarse en los endpoints protegidos", example = "eyJhbGciOiJIUzI1NiJ9...")
    String token
) {
}
