package com.shift_cut.Model.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Roles disponibles en el sistema")
public enum Role {
    @Schema(description = "Cliente de la barberia")
    USER,
    @Schema(description = "Administrador del sistema")
    ADMIN,
    @Schema(description = "Barbero del negocio")
    BARBER
}
