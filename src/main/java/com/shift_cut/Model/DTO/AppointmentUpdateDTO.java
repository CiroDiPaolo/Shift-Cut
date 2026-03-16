package com.shift_cut.Model.DTO;

import com.shift_cut.Model.Enum.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos para actualizar un turno")
public class AppointmentUpdateDTO {

    @NotNull
    @Schema(description = "Tipo de servicio solicitado", example = "HAIR_CUT")
    private ServiceType typeShift;

    @NotNull
    @Schema(description = "Fecha del turno", example = "2026-03-15")
    private LocalDate date;

    @NotNull
    @Schema(description = "Hora del turno", example = "10:30:00")
    private LocalTime time;

    @NotNull
    @Schema(description = "ID del barbero asignado", example = "2")
    private Long barberId;

    @NotNull
    @Schema(description = "ID del cliente", example = "3")
    private Long userId;
}

