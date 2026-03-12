package com.shift_cut.Control;

import com.shift_cut.Model.DTO.UserDTO;
import com.shift_cut.Model.UserEntity;
import com.shift_cut.Service.UserEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones relacionadas a usuarios del sistema")
public class UserEntityController {

    private final UserEntityService service;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener el usuario autenticado", description = "Devuelve los datos del usuario autenticado mediante el token JWT.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario autenticado encontrado", content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(service.getCurrentUser());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por ID", description = "Solo administradores pueden consultar cualquier usuario por su ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(service.getUserEntityById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios", description = "Solo administradores pueden listar todos los usuarios.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de usuarios", content = @Content(schema = @Schema(implementation = UserDTO.class)))
    })
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar usuario por email", description = "Solo administradores pueden buscar usuarios por email.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<UserDTO> getUserByEmail(
            @Parameter(description = "Email del usuario", example = "usuario@ejemplo.com") @PathVariable String email) {
        return ResponseEntity.ok(service.getUserByEmailDTO(email));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and #id == principal.id)")
    @Operation(summary = "Actualizar usuario", description = "Permite a un administrador o al propio usuario actualizar sus datos.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario actualizado", content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "403", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "ID del usuario a actualizar", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del usuario a actualizar", required = true, content = @Content(schema = @Schema(implementation = UserEntity.class)))
            @RequestBody UserEntity userEntity) {
        return ResponseEntity.ok(service.updateUserEntity(id, userEntity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario", description = "Solo administradores pueden eliminar usuarios.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuario eliminado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID del usuario a eliminar", example = "1") @PathVariable Long id) {
        service.deleteUserEntity(id);
        return ResponseEntity.noContent().build();
    }
}
