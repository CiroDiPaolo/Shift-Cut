package com.shift_cut.Auth;

import com.shift_cut.Config.Auth.AuthService;
import com.shift_cut.Config.Auth.LoginRequest;
import com.shift_cut.Config.Auth.RegisterRequest;
import com.shift_cut.Config.Security.JwtService;
import com.shift_cut.Exceptions.UserAlreadyExistsException;
import com.shift_cut.Mapper.AuthMapper;
import com.shift_cut.Model.DTO.AuthResponse;
import com.shift_cut.Model.Enum.Role;
import com.shift_cut.Model.UserEntity;
import com.shift_cut.Service.UserEntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - AuthService")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserEntityService userEntityService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private AuthService authService;

    private UserEntity existingUser;

    @BeforeEach
    void setUp() {
        existingUser = UserEntity.builder()
                .id(1L)
                .username("juanperez")
                .email("juan@user.com")
                .password("encodedPass")
                .role(Role.USER)
                .status(true)
                .build();

        // Stub general para el mapper usado en register (lenient para evitar fallos por stubbings no usados)
        org.mockito.Mockito.lenient().when(authMapper.toEntity(any(RegisterRequest.class))).thenAnswer(inv -> {
            RegisterRequest r = inv.getArgument(0);
            return UserEntity.builder().username(r.getUsername()).email(r.getEmail()).build();
        });
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: retorna token JWT cuando las credenciales son correctas")
    void login_withValidCredentials_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("juan@user.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userEntityService.getByEmailOrThrow("juan@user.com")).thenReturn(existingUser);
        when(jwtService.generateToken(existingUser)).thenReturn("mocked-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked-jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(existingUser);
    }

    @Test
    @DisplayName("login: lanza excepción cuando las credenciales son incorrectas")
    void login_withInvalidCredentials_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("juan@user.com");
        request.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: registra nuevo usuario y retorna token JWT")
    void register_withValidData_returnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("nuevousuario");
        request.setEmail("nuevo@user.com");
        request.setPassword("pass123");

        when(userEntityService.findByEmailOptional("nuevo@user.com")).thenReturn(Optional.empty());
        when(userEntityService.findByUsernameOptional("nuevousuario")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(userEntityService.createUserEntity(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn("new-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("new-jwt-token");
        verify(userEntityService).createUserEntity(any(UserEntity.class));
        verify(jwtService).generateToken(any(UserEntity.class));
    }

    @Test
    @DisplayName("register: lanza UserAlreadyExistsException si el email ya está registrado")
    void register_withExistingEmail_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("nuevousuario");
        request.setEmail("juan@user.com");
        request.setPassword("pass123");

        when(userEntityService.findByEmailOptional("juan@user.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email");

        verify(userEntityService, never()).createUserEntity(any());
    }

    @Test
    @DisplayName("register: lanza UserAlreadyExistsException si el username ya está registrado")
    void register_withExistingUsername_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("juanperez");
        request.setEmail("nuevo@user.com");
        request.setPassword("pass123");

        when(userEntityService.findByEmailOptional("nuevo@user.com")).thenReturn(Optional.empty());
        when(userEntityService.findByUsernameOptional("juanperez")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("usuario");

        verify(userEntityService, never()).createUserEntity(any());
    }

    @Test
    @DisplayName("register: el nuevo usuario siempre tiene rol USER")
    void register_newUser_hasRoleUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("nuevousuario");
        request.setEmail("nuevo@user.com");
        request.setPassword("pass123");

        when(userEntityService.findByEmailOptional("nuevo@user.com")).thenReturn(Optional.empty());
        when(userEntityService.findByUsernameOptional("nuevousuario")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(userEntityService.createUserEntity(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn("token");

        authService.register(request);

        verify(userEntityService).createUserEntity(argThat(u -> u.getRole() == Role.USER));
    }
}
