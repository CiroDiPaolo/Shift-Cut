package com.shift_cut.Service;

import com.shift_cut.Exceptions.UserAlreadyExistsException;
import com.shift_cut.Exceptions.UserNotFound;
import com.shift_cut.Mapper.UserMapper;
import com.shift_cut.Model.DTO.UserDTO;
import com.shift_cut.Model.Enum.Role;
import com.shift_cut.Model.UserEntity;
import com.shift_cut.Repository.UserEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - UserEntityService")
class UserEntityServiceTest {

    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserEntityService userEntityService;

    private UserEntity adminUser;
    private UserEntity regularUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        adminUser = UserEntity.builder()
                .id(1L)
                .username("admin")
                .email("admin@admin.com")
                .password("encodedAdmin")
                .role(Role.ADMIN)
                .status(true)
                .build();

        regularUser = UserEntity.builder()
                .id(2L)
                .username("juanperez")
                .email("juan@user.com")
                .password("encodedPass")
                .role(Role.USER)
                .status(true)
                .build();
    }

    // ── getUserEntityById ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserEntityById: retorna DTO cuando el usuario existe")
    void getUserEntityById_whenExists_returnsDTO() {
        when(userEntityRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userMapper.toDto(regularUser)).thenReturn(UserDTO.builder().id(2L).username("juanperez").email("juan@user.com").role(Role.USER).build());

        UserDTO result = userEntityService.getUserEntityById(2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getUsername()).isEqualTo("juanperez");
        assertThat(result.getEmail()).isEqualTo("juan@user.com");
        assertThat(result.getRole()).isEqualTo(Role.USER);
        verify(userEntityRepository).findById(2L);
    }

    @Test
    @DisplayName("getUserEntityById: lanza UserNotFound cuando no existe")
    void getUserEntityById_whenNotExists_throwsException() {
        when(userEntityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userEntityService.getUserEntityById(99L))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining("99");
    }

    // ── getAllUsers ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers: retorna todos los usuarios como DTOs")
    void getAllUsers_returnsList() {
        when(userEntityRepository.findAll()).thenReturn(List.of(adminUser, regularUser));
        when(userMapper.toDto(adminUser)).thenReturn(UserDTO.builder().email("admin@admin.com").build());
        when(userMapper.toDto(regularUser)).thenReturn(UserDTO.builder().email("juan@user.com").build());

        List<UserDTO> result = userEntityService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDTO::getEmail)
                .containsExactlyInAnyOrder("admin@admin.com", "juan@user.com");
        verify(userEntityRepository).findAll();
    }

    @Test
    @DisplayName("getAllUsers: retorna lista vacía cuando no hay usuarios")
    void getAllUsers_whenEmpty_returnsEmptyList() {
        when(userEntityRepository.findAll()).thenReturn(List.of());

        List<UserDTO> result = userEntityService.getAllUsers();

        assertThat(result).isEmpty();
    }

    // ── getUserByUsername ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserByUsername: retorna DTO cuando el usuario existe")
    void getUserByUsername_whenExists_returnsDTO() {
        when(userEntityRepository.findByUsername("juanperez")).thenReturn(Optional.of(regularUser));
        when(userMapper.toDto(regularUser)).thenReturn(UserDTO.builder().username("juanperez").build());

        UserDTO result = userEntityService.getUserByUsername("juanperez");

        assertThat(result.getUsername()).isEqualTo("juanperez");
    }

    @Test
    @DisplayName("getUserByUsername: lanza UserNotFound cuando no existe")
    void getUserByUsername_whenNotExists_throwsException() {
        when(userEntityRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userEntityService.getUserByUsername("noexiste"))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining("noexiste");
    }

    // ── getUserByEmailDTO ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserByEmailDTO: retorna DTO cuando el email existe")
    void getUserByEmailDTO_whenExists_returnsDTO() {
        when(userEntityRepository.findByEmail("juan@user.com")).thenReturn(Optional.of(regularUser));
        when(userMapper.toDto(regularUser)).thenReturn(UserDTO.builder().email("juan@user.com").build());

        UserDTO result = userEntityService.getUserByEmailDTO("juan@user.com");

        assertThat(result.getEmail()).isEqualTo("juan@user.com");
    }

    @Test
    @DisplayName("getUserByEmailDTO: lanza UserNotFound cuando el email no existe")
    void getUserByEmailDTO_whenNotExists_throwsException() {
        when(userEntityRepository.findByEmail("noexiste@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userEntityService.getUserByEmailDTO("noexiste@mail.com"))
                .isInstanceOf(UserNotFound.class);
    }

    // ── createUserEntity ──────────────────────────────────────────────────────

    @Test
    @DisplayName("createUserEntity: guarda y retorna el usuario")
    void createUserEntity_savesUser() {
        when(userEntityRepository.save(regularUser)).thenReturn(regularUser);

        UserEntity result = userEntityService.createUserEntity(regularUser);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("juan@user.com");
        verify(userEntityRepository).save(regularUser);
    }

    // ── deleteUserEntity ──────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUserEntity: elimina correctamente cuando el usuario existe")
    void deleteUserEntity_whenExists_deletesSuccessfully() {
        when(userEntityRepository.existsById(2L)).thenReturn(true);

        assertThatNoException().isThrownBy(() -> userEntityService.deleteUserEntity(2L));

        verify(userEntityRepository).deleteById(2L);
    }

    @Test
    @DisplayName("deleteUserEntity: lanza UserNotFound cuando no existe")
    void deleteUserEntity_whenNotExists_throwsException() {
        when(userEntityRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userEntityService.deleteUserEntity(99L))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining("99");

        verify(userEntityRepository, never()).deleteById(any());
    }

    // ── updateUserEntity (como ADMIN) ─────────────────────────────────────────

    @Test
    @DisplayName("updateUserEntity: ADMIN puede actualizar cualquier campo incluyendo rol")
    void updateUserEntity_asAdmin_updatesAllFields() {
        // Simular contexto de seguridad con rol ADMIN
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                adminUser, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserEntity updatedData = UserEntity.builder()
                .username("newUsername")
                .email("newemail@admin.com")
                .password(null)
                .role(Role.BARBER)
                .status(true)
                .build();

        when(userEntityRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userEntityRepository.findByEmail("newemail@admin.com")).thenReturn(Optional.empty());
        when(userEntityRepository.findByUsername("newUsername")).thenReturn(Optional.empty());
        when(userEntityRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toDto(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            return UserDTO.builder().username(u.getUsername()).email(u.getEmail()).role(u.getRole()).build();
        });

        UserDTO result = userEntityService.updateUserEntity(2L, updatedData);

        assertThat(result.getUsername()).isEqualTo("newUsername");
        assertThat(result.getEmail()).isEqualTo("newemail@admin.com");
        assertThat(result.getRole()).isEqualTo(Role.BARBER);
    }

    @Test
    @DisplayName("updateUserEntity: usuario regular no puede cambiar su rol")
    void updateUserEntity_asUser_cannotChangeRole() {
        // Simular contexto de seguridad con rol USER (mismo usuario)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                regularUser, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserEntity updatedData = UserEntity.builder()
                .username("nuevoUsername")
                .email("juan@user.com")  // mismo email, no dispara validación de duplicado
                .password(null)
                .role(Role.ADMIN) // intenta cambiar a ADMIN
                .status(true)
                .build();

        when(userEntityRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        // email igual al existente → no busca duplicado, no mockear findByEmail
        when(userEntityRepository.findByUsername("nuevoUsername")).thenReturn(Optional.empty());
        when(userEntityRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toDto(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            return UserDTO.builder().username(u.getUsername()).email(u.getEmail()).role(u.getRole()).build();
        });

        UserDTO result = userEntityService.updateUserEntity(2L, updatedData);

        // El rol NO debe haber cambiado a ADMIN
        assertThat(result.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("updateUserEntity: lanza excepción si email ya está registrado")
    void updateUserEntity_withDuplicateEmail_throwsException() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                adminUser, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserEntity updatedData = UserEntity.builder()
                .username("juanperez")
                .email("existing@mail.com")
                .role(Role.USER)
                .status(true)
                .build();

        UserEntity existingWithEmail = UserEntity.builder()
                .id(99L)
                .email("existing@mail.com")
                .build();

        when(userEntityRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userEntityRepository.findByEmail("existing@mail.com")).thenReturn(Optional.of(existingWithEmail));

        assertThatThrownBy(() -> userEntityService.updateUserEntity(2L, updatedData))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email");
    }

    // ── findByEmailOptional / findByUsernameOptional ──────────────────────────

    @Test
    @DisplayName("findByEmailOptional: retorna Optional con usuario cuando existe")
    void findByEmailOptional_whenExists_returnsOptional() {
        when(userEntityRepository.findByEmail("juan@user.com")).thenReturn(Optional.of(regularUser));

        Optional<UserEntity> result = userEntityService.findByEmailOptional("juan@user.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("juan@user.com");
    }

    @Test
    @DisplayName("findByEmailOptional: retorna Optional vacío si email es null")
    void findByEmailOptional_withNullEmail_returnsEmpty() {
        Optional<UserEntity> result = userEntityService.findByEmailOptional(null);

        assertThat(result).isEmpty();
        verifyNoInteractions(userEntityRepository);
    }

    @Test
    @DisplayName("findByUsernameOptional: retorna Optional vacío si username es null")
    void findByUsernameOptional_withNullUsername_returnsEmpty() {
        Optional<UserEntity> result = userEntityService.findByUsernameOptional(null);

        assertThat(result).isEmpty();
        verifyNoInteractions(userEntityRepository);
    }
}
