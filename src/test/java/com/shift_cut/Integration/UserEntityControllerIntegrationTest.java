package com.shift_cut.Integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shift_cut.Config.Security.JwtService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DisplayName("Tests de integración - UserEntityController")
class UserEntityControllerIntegrationTest {

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

    @Autowired
    private JwtService jwtService;

    private UserEntity adminUser;
    private UserEntity regularUser;
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

        adminToken = jwtService.generateToken(adminUser);
        userToken = jwtService.generateToken(regularUser);
    }

    @AfterEach
    void tearDown() {
        appointmentRepository.deleteAll();
        userEntityRepository.deleteAll();
    }

    // ── GET /api/user/me ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/user/me: retorna datos del usuario autenticado")
    void getMe_withValidToken_returnsCurrentUser() throws Exception {
        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("juan@user.com")))
                .andExpect(jsonPath("$.username", is("juanperez")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("GET /api/user/me: retorna 401 sin token")
    void getMe_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/user ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/user: ADMIN puede listar todos los usuarios")
    void getAllUsers_asAdmin_returnsUserList() throws Exception {
        mockMvc.perform(get("/api/user")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("GET /api/user: USER recibe 403 al intentar listar usuarios")
    void getAllUsers_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/user")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/user/{id} ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/user/{id}: ADMIN puede obtener cualquier usuario por ID")
    void getUserById_asAdmin_returnsUser() throws Exception {
        mockMvc.perform(get("/api/user/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("juan@user.com")));
    }

    @Test
    @DisplayName("GET /api/user/{id}: retorna 404 para ID inexistente")
    void getUserById_withNonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/api/user/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/user/{id}: USER recibe 403 al intentar obtener otro usuario")
    void getUserById_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/user/" + adminUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/user/email/{email} ───────────────────────────────────────────

    @Test
    @DisplayName("GET /api/user/email/{email}: ADMIN puede buscar usuario por email")
    void getUserByEmail_asAdmin_returnsUser() throws Exception {
        mockMvc.perform(get("/api/user/email/juan@user.com")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("juanperez")));
    }

    // ── PUT /api/user/{id} ────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/user/{id}: usuario puede actualizar sus propios datos")
    void updateUser_asSelf_updatesSuccessfully() throws Exception {
        UserEntity updateData = UserEntity.builder()
                .username("juanactualizado")
                .email("juanactualizado@user.com")
                .password("newpass123")
                .role(Role.USER)
                .status(true)
                .build();

        mockMvc.perform(put("/api/user/" + regularUser.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("juanactualizado")))
                .andExpect(jsonPath("$.email", is("juanactualizado@user.com")));
    }

    @Test
    @DisplayName("PUT /api/user/{id}: ADMIN puede actualizar cualquier usuario")
    void updateUser_asAdmin_updatesAnyUser() throws Exception {
        UserEntity updateData = UserEntity.builder()
                .username("juanmod")
                .email("juanmod@user.com")
                .password("newpass123")
                .role(Role.BARBER)
                .status(true)
                .build();

        mockMvc.perform(put("/api/user/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("BARBER")));
    }

    @Test
    @DisplayName("PUT /api/user/{id}: retorna 404 para ID inexistente")
    void updateUser_withNonExistentId_returns404() throws Exception {
        UserEntity updateData = UserEntity.builder()
                .username("x")
                .email("x@x.com")
                .role(Role.USER)
                .status(true)
                .build();

        mockMvc.perform(put("/api/user/9999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/user/{id} ─────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/user/{id}: ADMIN puede eliminar un usuario")
    void deleteUser_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/api/user/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/user/{id}: USER recibe 403 al intentar eliminar")
    void deleteUser_asUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/user/" + regularUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/user/{id}: retorna 404 para ID inexistente")
    void deleteUser_withNonExistentId_returns404() throws Exception {
        mockMvc.perform(delete("/api/user/9999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}

