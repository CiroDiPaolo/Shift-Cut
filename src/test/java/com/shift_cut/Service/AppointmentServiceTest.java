package com.shift_cut.Service;

import com.shift_cut.Exceptions.AppointmentNotFound;
import com.shift_cut.Model.Appointment;
import com.shift_cut.Model.DTO.AppointmentDTO;
import com.shift_cut.Model.Enum.Role;
import com.shift_cut.Model.Enum.ServiceType;
import com.shift_cut.Model.UserEntity;
import com.shift_cut.Repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - AppointmentService")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private UserEntity barber;
    private UserEntity client;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        barber = UserEntity.builder()
                .id(1L)
                .username("carlossanchez")
                .email("carlos@barber.com")
                .password("encoded")
                .role(Role.BARBER)
                .status(true)
                .build();

        client = UserEntity.builder()
                .id(2L)
                .username("juanperez")
                .email("juan@user.com")
                .password("encoded")
                .role(Role.USER)
                .status(true)
                .build();

        appointment = Appointment.builder()
                .id(1L)
                .typeShift(ServiceType.HAIR_CUT)
                .date(LocalDate.of(2026, 4, 10))
                .time(LocalTime.of(10, 30))
                .barber(barber)
                .user(client)
                .build();
    }

    // ── getAppointmentById ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAppointmentById: retorna DTO cuando el turno existe")
    void getAppointmentById_whenExists_returnsDTO() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        AppointmentDTO result = appointmentService.getAppointmentById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTypeShift()).isEqualTo(ServiceType.HAIR_CUT);
        assertThat(result.getBarberId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getBarberUsername()).isEqualTo("carlossanchez");
        assertThat(result.getUserUsername()).isEqualTo("juanperez");
        verify(appointmentRepository).findById(1L);
    }

    @Test
    @DisplayName("getAppointmentById: lanza AppointmentNotFound cuando no existe")
    void getAppointmentById_whenNotExists_throwsException() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(99L))
                .isInstanceOf(AppointmentNotFound.class)
                .hasMessageContaining("99");
    }

    // ── getAllAppointments ────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllAppointments: retorna lista completa de DTOs")
    void getAllAppointments_returnsList() {
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment));

        List<AppointmentDTO> result = appointmentService.getAllAppointments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(appointmentRepository).findAll();
    }

    @Test
    @DisplayName("getAllAppointments: retorna lista vacía cuando no hay turnos")
    void getAllAppointments_whenEmpty_returnsEmptyList() {
        when(appointmentRepository.findAll()).thenReturn(List.of());

        List<AppointmentDTO> result = appointmentService.getAllAppointments();

        assertThat(result).isEmpty();
    }

    // ── getAppointmentsByUserId ───────────────────────────────────────────────

    @Test
    @DisplayName("getAppointmentsByUserId: retorna turnos del usuario indicado")
    void getAppointmentsByUserId_returnsList() {
        when(appointmentRepository.findByUser_Id(2L)).thenReturn(List.of(appointment));

        List<AppointmentDTO> result = appointmentService.getAppointmentsByUserId(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(2L);
        verify(appointmentRepository).findByUser_Id(2L);
    }

    @Test
    @DisplayName("getAppointmentsByUserId: retorna lista vacía cuando el usuario no tiene turnos")
    void getAppointmentsByUserId_whenNoAppointments_returnsEmpty() {
        when(appointmentRepository.findByUser_Id(99L)).thenReturn(List.of());

        List<AppointmentDTO> result = appointmentService.getAppointmentsByUserId(99L);

        assertThat(result).isEmpty();
    }

    // ── createAppointment ────────────────────────────────────────────────────

    @Test
    @DisplayName("createAppointment: guarda y retorna DTO correctamente")
    void createAppointment_savesAndReturnsDTO() {
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        AppointmentDTO result = appointmentService.createAppointment(appointment);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTypeShift()).isEqualTo(ServiceType.HAIR_CUT);
        verify(appointmentRepository).save(appointment);
    }

    // ── updateAppointment ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateAppointment: actualiza y retorna DTO cuando el turno existe")
    void updateAppointment_whenExists_returnsUpdatedDTO() {
        Appointment updated = Appointment.builder()
                .typeShift(ServiceType.HAIR_CUT_AND_BEARD)
                .date(LocalDate.of(2026, 5, 1))
                .time(LocalTime.of(14, 0))
                .barber(barber)
                .user(client)
                .build();

        Appointment savedUpdated = Appointment.builder()
                .id(1L)
                .typeShift(ServiceType.HAIR_CUT_AND_BEARD)
                .date(LocalDate.of(2026, 5, 1))
                .time(LocalTime.of(14, 0))
                .barber(barber)
                .user(client)
                .build();

        when(appointmentRepository.existsById(1L)).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedUpdated);

        AppointmentDTO result = appointmentService.updateAppointment(1L, updated);

        assertThat(result.getTypeShift()).isEqualTo(ServiceType.HAIR_CUT_AND_BEARD);
        verify(appointmentRepository).existsById(1L);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("updateAppointment: lanza AppointmentNotFound cuando el turno no existe")
    void updateAppointment_whenNotExists_throwsException() {
        when(appointmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.updateAppointment(99L, appointment))
                .isInstanceOf(AppointmentNotFound.class)
                .hasMessageContaining("99");

        verify(appointmentRepository, never()).save(any());
    }

    // ── deleteAppointment ────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteAppointment: elimina correctamente cuando el turno existe")
    void deleteAppointment_whenExists_deletesSuccessfully() {
        when(appointmentRepository.existsById(1L)).thenReturn(true);

        assertThatNoException().isThrownBy(() -> appointmentService.deleteAppointment(1L));

        verify(appointmentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteAppointment: lanza AppointmentNotFound cuando no existe")
    void deleteAppointment_whenNotExists_throwsException() {
        when(appointmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.deleteAppointment(99L))
                .isInstanceOf(AppointmentNotFound.class)
                .hasMessageContaining("99");

        verify(appointmentRepository, never()).deleteById(any());
    }
}

