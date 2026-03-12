package com.shift_cut.Security;

import com.shift_cut.Config.Security.JwtService;
import com.shift_cut.Model.Enum.Role;
import com.shift_cut.Model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests unitarios - JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Inyectar la clave secreta usando ReflectionTestUtils (simula @Value)
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY",
                "TestSecretKey12345678901234567890123456");

        user = UserEntity.builder()
                .id(1L)
                .username("juanperez")
                .email("juan@user.com")
                .password("encodedPass")
                .role(Role.USER)
                .status(true)
                .build();
    }

    @Test
    @DisplayName("generateToken: genera un token no nulo y no vacío")
    void generateToken_returnsNonNullToken() {
        String token = jwtService.generateToken(user);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("getUsernameFromToken: extrae el username como subject del JWT")
    void getUsernameFromToken_returnsUsername() {
        String token = jwtService.generateToken(user);

        // UserEntity.getUsername() (via Lombok) retorna el campo username
        String subject = jwtService.getUsernameFromToken(token);

        assertThat(subject).isEqualTo("juanperez");
    }

    @Test
    @DisplayName("isTokenValid: retorna true para token válido y usuario correcto")
    void isTokenValid_withValidToken_returnsTrue() {
        String token = jwtService.generateToken(user);

        boolean valid = jwtService.isTokenValid(token, user);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: retorna false para token de otro usuario")
    void isTokenValid_withWrongUser_returnsFalse() {
        String token = jwtService.generateToken(user);

        UserEntity anotherUser = UserEntity.builder()
                .id(2L)
                .username("otrousuario")
                .email("otro@user.com")
                .password("encodedPass")
                .role(Role.USER)
                .status(true)
                .build();

        boolean valid = jwtService.isTokenValid(token, anotherUser);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("generateToken: el token generado tiene el subject correcto y es válido")
    void generateToken_isValidAndHasCorrectSubject() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.getUsernameFromToken(token)).isEqualTo("juanperez");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }
}

