package com.shift_cut.Model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta de autenticacion que contiene el token JWT")
public class AuthResponse {

    @Schema(description = "Token JWT para autenticarse en los endpoints protegidos", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
}
