package com.shift_cut.Integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shift_cut.Config.Security.JwtService;
import com.shift_cut.Model.Appointment;
import com.shift_cut.Model.Enum.Role;
import com.shift_cut.Model.Enum.ServiceType;
import com.shift_cut.Model.UserEntity;
import com.shift_cut.Repository.AppointmentRepository;
import com.shift_cut.Repository.UserEntityRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DisplayName("Tests de integración - AppointmentController")
class AppointmentControllerIntegrationTest {
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;


    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private UserEntity adminUser;
    private UserEntity regularUser;
    private UserEntity barberUser;
    private Appointment existingAppointment;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(springSecurityFilterChain)
                .build();
        appointmentRepository.deleteAll();
        userEntityRepository.deleteAll();

        adminUser = userEntityRepository.save(UserEntity.builder()
                .username("admin")
                .email("admin@admin.com")
                .password(passwordEncoder.encode("Admin123"))
                .role(Role.ADMIN)
                .status(true)
                .build());

        regularUser = userEntityRepository.save(UserEntity.builder()
                .username("juanperez")
                .email("juan@user.com")
                .password(passwordEncoder.encode("pass123"))
                .role(Role.USER)
                .status(true)
                .build());

        barberUser = userEntityRepository.save(UserEntity.builder()
                .username("carlossanchez")
                .email("carlos@barber.com")
                .password(passwordEncoder.encode("pass123"))
                .role(Role.BARBER)
                .status(true)
                .build());

        existingAppointment = appointmentRepository.save(Appointment.builder()
                .typeShift(ServiceType.HAIR_CUT)
                .date(LocalDate.of(2026, 4, 10))
                .time(LocalTime.of(10, 30))
                .barber(barberUser)
                .user(regularUser)
                .build());

        adminToken = jwtService.generateToken(adminUser);
        userToken = jwtService.generateToken(regularUser);
    }

    @AfterEach
    void tearDown() {
        appointmentRepository.deleteAll();
        userEntityRepository.deleteAll();
    }

    // ── GET /api/appointment ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/appointment: ADMIN puede listar todos los turnos")
    void getAllAppointments_asAdmin_returnsList() throws Exception {
        mockMvc.perform(get("/api/appointment")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/appointment: USER recibe 403")
    void getAllAppointments_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/appointment")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/appointment: sin token retorna 401")
    void getAllAppointments_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/appointment"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/appointment/{id} ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/appointment/{id}: ADMIN puede obtener turno por ID")
    void getAppointmentById_asAdmin_returnsAppointment() throws Exception {
        mockMvc.perform(get("/api/appointment/" + existingAppointment.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingAppointment.getId().intValue())))
                .andExpect(jsonPath("$.typeShift", is("HAIR_CUT")));
    }

    @Test
    @DisplayName("GET /api/appointment/{id}: retorna 404 para ID inexistente")
    void getAppointmentById_withNonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/api/appointment/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/appointment/user/{userId} ────────────────────────────────────

    @Test
    @DisplayName("GET /api/appointment/user/{userId}: usuario puede ver sus propios turnos")
    void getAppointmentsByUserId_asSelf_returnsList() throws Exception {
        mockMvc.perform(get("/api/appointment/user/" + regularUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(regularUser.getId().intValue())));
    }

    @Test
    @DisplayName("GET /api/appointment/user/{userId}: ADMIN puede ver turnos de cualquier usuario")
    void getAppointmentsByUserId_asAdmin_returnsList() throws Exception {
        mockMvc.perform(get("/api/appointment/user/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ── POST /api/appointment ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/appointment: USER puede crear un turno y retorna 201")
    void createAppointment_asUser_returns201() throws Exception {
        Appointment newAppointment = Appointment.builder()
                .typeShift(ServiceType.HAIR_CUT_AND_BEARD)
                .date(LocalDate.of(2026, 5, 20))
                .time(LocalTime.of(14, 0))
                .barber(barberUser)
                .user(regularUser)
                .build();

        mockMvc.perform(post("/api/appointment")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAppointment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeShift", is("HAIR_CUT_AND_BEARD")))
                .andExpect(jsonPath("$.userId", is(regularUser.getId().intValue())));
    }

    @Test
    @DisplayName("POST /api/appointment: sin token retorna 401")
    void createAppointment_withoutToken_returns401() throws Exception {
        Appointment newAppointment = Appointment.builder()
                .typeShift(ServiceType.HAIR_CUT)
                .date(LocalDate.of(2026, 5, 20))
                .time(LocalTime.of(14, 0))
                .barber(barberUser)
                .user(regularUser)
                .build();

        mockMvc.perform(post("/api/appointment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAppointment)))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /api/appointment/{id} ─────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/appointment/{id}: ADMIN puede actualizar un turno")
    void updateAppointment_asAdmin_returnsUpdated() throws Exception {
        Appointment updateData = Appointment.builder()
                .typeShift(ServiceType.HAIR_CUT_AND_BEARD)
                .date(LocalDate.of(2026, 6, 1))
                .time(LocalTime.of(11, 0))
                .barber(barberUser)
                .user(regularUser)
                .build();

        mockMvc.perform(put("/api/appointment/" + existingAppointment.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeShift", is("HAIR_CUT_AND_BEARD")));
    }

    @Test
    @DisplayName("PUT /api/appointment/{id}: USER recibe 403")
    void updateAppointment_asUser_returns403() throws Exception {
        Appointment updateData = Appointment.builder()
                .typeShift(ServiceType.HAIR_CUT_AND_BEARD)
                .date(LocalDate.of(2026, 6, 1))
                .time(LocalTime.of(11, 0))
                .barber(barberUser)
                .user(regularUser)
                .build();

        mockMvc.perform(put("/api/appointment/" + existingAppointment.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/appointment/{id} ──────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/appointment/{id}: ADMIN puede eliminar un turno")
    void deleteAppointment_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/api/appointment/" + existingAppointment.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/appointment/{id}: USER recibe 403")
    void deleteAppointment_asUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/appointment/" + existingAppointment.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/appointment/{id}: retorna 404 para ID inexistente")
    void deleteAppointment_withNonExistentId_returns404() throws Exception {
        mockMvc.perform(delete("/api/appointment/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}

