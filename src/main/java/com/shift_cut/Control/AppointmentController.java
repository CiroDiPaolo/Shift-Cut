package com.shift_cut.Control;

import com.shift_cut.Model.Appointment;
import com.shift_cut.Model.DTO.AppointmentDTO;
import com.shift_cut.Service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
@Tag(name = "Turnos", description = "Operaciones para gestión de turnos de la barbería")
public class AppointmentController {

    private final AppointmentService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los turnos", description = "Solo administradores pueden ver todos los turnos registrados.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de turnos", content = @Content(schema = @Schema(implementation = AppointmentDTO.class)))
    })
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        return ResponseEntity.ok(service.getAllAppointments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener turno por ID", description = "Solo administradores pueden consultar cualquier turno por su ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Turno encontrado", content = @Content(schema = @Schema(implementation = AppointmentDTO.class))),
        @ApiResponse(responseCode = "404", description = "Turno no encontrado", content = @Content)
    })
    public ResponseEntity<AppointmentDTO> getAppointmentById(
            @Parameter(description = "ID del turno", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(service.getAppointmentById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and #userId == principal.id)")
    @Operation(summary = "Listar turnos de un usuario", description = "Permite a un usuario autenticado ver sus propios turnos, o a un administrador ver los de cualquier usuario.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de turnos del usuario", content = @Content(schema = @Schema(implementation = AppointmentDTO.class)))
    })
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByUserId(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long userId) {
        return ResponseEntity.ok(service.getAppointmentsByUserId(userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Crear un turno", description = "Permite a un usuario autenticado o administrador crear un nuevo turno.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Turno creado", content = @Content(schema = @Schema(implementation = AppointmentDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public ResponseEntity<AppointmentDTO> createAppointment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del turno a crear", required = true, content = @Content(schema = @Schema(implementation = Appointment.class)))
            @RequestBody Appointment appointment) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAppointment(appointment));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un turno", description = "Solo administradores pueden actualizar turnos.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Turno actualizado", content = @Content(schema = @Schema(implementation = AppointmentDTO.class))),
        @ApiResponse(responseCode = "404", description = "Turno no encontrado", content = @Content)
    })
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @Parameter(description = "ID del turno a actualizar", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del turno a actualizar", required = true, content = @Content(schema = @Schema(implementation = Appointment.class)))
            @RequestBody Appointment appointment) {
        return ResponseEntity.ok(service.updateAppointment(id, appointment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un turno", description = "Solo administradores pueden eliminar turnos.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Turno eliminado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Turno no encontrado", content = @Content)
    })
    public ResponseEntity<Void> deleteAppointment(
            @Parameter(description = "ID del turno a eliminar", example = "1") @PathVariable Long id) {
        service.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
