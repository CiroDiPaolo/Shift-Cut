package com.shift_cut.Model.DTO;

import com.shift_cut.Model.Enum.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Datos del turno de barberia")
public class AppointmentDTO {

    @Schema(description = "ID unico del turno", example = "1")
    private Long id;

    @Schema(description = "Tipo de servicio solicitado", example = "HAIR_CUT")
    private ServiceType typeShift;

    @Schema(description = "Fecha del turno", example = "2026-03-15")
    private LocalDate date;

    @Schema(description = "Hora del turno", example = "10:30:00")
    private LocalTime time;

    @Schema(description = "ID del barbero asignado", example = "2")
    private Long barberId;

    @Schema(description = "Nombre de usuario del barbero", example = "carlossanchez")
    private String barberUsername;

    @Schema(description = "ID del cliente", example = "3")
    private Long userId;

    @Schema(description = "Nombre de usuario del cliente", example = "juanperez")
    private String userUsername;
}
