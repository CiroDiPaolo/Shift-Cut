package com.shift_cut.Integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shift_cut.Config.Auth.LoginRequest;
import com.shift_cut.Config.Auth.RegisterRequest;
import com.shift_cut.Model.Enum.Role;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DisplayName("Tests de integración - AuthController")
class AuthControllerIntegrationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(springSecurityFilterChain)
                .build();
        appointmentRepository.deleteAll();
        userEntityRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        appointmentRepository.deleteAll();
        userEntityRepository.deleteAll();
    }

    // ── POST /auth/register ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/register: registra usuario nuevo y retorna 201 con token")
    void register_withValidData_returns201WithToken() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "testuser@mail.com", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.token", not(emptyString())));
    }

    @Test
    @DisplayName("POST /auth/register: retorna 409 si el email ya está registrado")
    void register_withExistingEmail_returns409() throws Exception {
        // Crear usuario existente en la BD
        userEntityRepository.save(UserEntity.builder()
                .username("existing")
                .email("existing@mail.com")
                .password(passwordEncoder.encode("pass123"))
                .role(Role.USER)
                .status(true)
                .build());

        RegisterRequest request = new RegisterRequest("newuser", "existing@mail.com", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /auth/register: retorna 400 si el email es inválido")
    void register_withInvalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "not-an-email", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register: retorna 400 si la contraseña es muy corta")
    void register_withShortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "testuser@mail.com", "123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── POST /auth/login ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login: retorna 200 con token para credenciales correctas")
    void login_withValidCredentials_returns200WithToken() throws Exception {
        // Crear usuario en la BD
        userEntityRepository.save(UserEntity.builder()
                .username("loginuser")
                .email("loginuser@mail.com")
                .password(passwordEncoder.encode("pass123"))
                .role(Role.USER)
                .status(true)
                .build());

        LoginRequest request = new LoginRequest("loginuser@mail.com", "pass123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.token", not(emptyString())));
    }

    @Test
    @DisplayName("POST /auth/login: retorna 401 para contraseña incorrecta")
    void login_withWrongPassword_returns401() throws Exception {
        userEntityRepository.save(UserEntity.builder()
                .username("loginuser")
                .email("loginuser@mail.com")
                .password(passwordEncoder.encode("pass123"))
                .role(Role.USER)
                .status(true)
                .build());

        LoginRequest request = new LoginRequest("loginuser@mail.com", "WRONGPASSWORD");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login: retorna 401 para email inexistente")
    void login_withUnknownEmail_returns401() throws Exception {
        LoginRequest request = new LoginRequest("noexiste@mail.com", "pass123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login: retorna 400 si el email está vacío")
    void login_withEmptyEmail_returns400() throws Exception {
        LoginRequest request = new LoginRequest("", "pass123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
