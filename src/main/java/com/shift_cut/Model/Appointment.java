package com.shift_cut.Model;

import com.shift_cut.Model.Enum.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad de turno de la barberia")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID unico del turno", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Service type cannot be null")
    @Schema(description = "Tipo de servicio", example = "HAIR_CUT", allowableValues = {"HAIR_CUT", "HAIR_CUT_AND_BEARD"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private ServiceType typeShift;

    @Column(name = "date", nullable = false)
    @NotNull(message = "Date cannot be null")
    @Schema(description = "Fecha del turno", example = "2026-03-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;

    @Column(name = "time", nullable = false)
    @NotNull(message = "Time cannot be null")
    @Schema(description = "Hora del turno", example = "10:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    @NotNull(message = "Barber cannot be null")
    @Schema(description = "Barbero asignado al turno", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserEntity barber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Client cannot be null")
    @Schema(description = "Cliente que reservo el turno", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserEntity user;
}
