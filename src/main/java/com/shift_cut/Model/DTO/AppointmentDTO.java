package com.shift_cut.Model.DTO;

import com.shift_cut.Model.Enum.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Datos del turno de barberia")
public record AppointmentDTO(

    @Schema(description = "ID unico del turno", example = "1")
    Long id,

    @Schema(description = "Tipo de servicio solicitado", example = "HAIR_CUT")
    ServiceType typeShift,

    @Schema(description = "Fecha del turno", example = "2026-03-15")
    LocalDate date,

    @Schema(description = "Hora del turno", example = "10:30:00")
    LocalTime time,

    @Schema(description = "ID del barbero asignado", example = "2")
    Long barberId,

    @Schema(description = "Nombre de usuario del barbero", example = "carlossanchez")
    String barberUsername,

    @Schema(description = "ID del cliente", example = "3")
    Long userId,

    @Schema(description = "Nombre de usuario del cliente", example = "juanperez")
    String userUsername
) {
}
