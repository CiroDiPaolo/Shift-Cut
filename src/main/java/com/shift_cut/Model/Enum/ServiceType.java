package com.shift_cut.Model.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipos de servicio disponibles en la barberia")
public enum ServiceType {
    @Schema(description = "Solo corte de cabello")
    HAIR_CUT,
    @Schema(description = "Corte de cabello y arreglo de barba")
    HAIR_CUT_AND_BEARD
}
