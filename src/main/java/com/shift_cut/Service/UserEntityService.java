package com.shift_cut.Service;

import com.shift_cut.Exceptions.UserNotFound;
import com.shift_cut.Exceptions.UserAlreadyExistsException;
import com.shift_cut.Model.DTO.UserDTO;
import com.shift_cut.Model.UserEntity;
import com.shift_cut.Repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserEntityService {

    private final UserEntityRepository repo;
    private final PasswordEncoder passwordEncoder;

    private UserDTO toDTO(UserEntity user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.isStatus())
                .build();
    }

    public UserDTO getUserEntityById(Long id) {
        UserEntity user = repo.findById(id)
                .orElseThrow(() -> new UserNotFound("User not found with id: " + id));
        return toDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UserNotFound("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserEntity userEntity) {
            return toDTO(userEntity);
        }

        if (principal instanceof UserDetails userDetails) {
            return toDTO(getByEmailOrThrow(userDetails.getUsername()));
        }

        throw new UserNotFound("Authenticated principal not recognized");
    }

    public UserDTO getUserByUsername(String username) {
        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> new UserNotFound("User not found with username: " + username));
        return toDTO(user);
    }

    public UserDTO getUserByEmailDTO(String email) {
        return toDTO(getByEmailOrThrow(email));
    }

    public Optional<UserEntity> findByEmailOptional(String email) {
        if (email == null) return Optional.empty();
        return repo.findByEmail(email);
    }

    public Optional<UserEntity> findByUsernameOptional(String username) {
        if (username == null) return Optional.empty();
        return repo.findByUsername(username);
    }

    public UserEntity getByEmailOrThrow(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found with email: " + email));
    }

    public UserEntity getByUsernameOrThrow(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new UserNotFound("User not found with username: " + username));
    }

    public UserEntity createUserEntity(UserEntity userEntity) {
        return repo.save(userEntity);
    }

    public UserDTO updateUserEntity(Long id, UserEntity userEntity) {
        UserEntity existing = repo.findById(id)
                .orElseThrow(() -> new UserNotFound("User not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isSelf = false;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserEntity u) {
            isSelf = u.getId().equals(id);
        } else if (principal instanceof UserDetails u) {
            isSelf = existing.getEmail().equals(u.getUsername()) || existing.getUsername().equals(u.getUsername());
        }

        // Validar email único
        if (userEntity.getEmail() != null && !userEntity.getEmail().equals(existing.getEmail())) {
            repo.findByEmail(userEntity.getEmail()).ifPresent(u -> {
                throw new UserAlreadyExistsException("El email ya está registrado");
            });
        }
        // Validar username único
        if (userEntity.getUsername() != null && !userEntity.getUsername().equals(existing.getUsername())) {
            repo.findByUsername(userEntity.getUsername()).ifPresent(u -> {
                throw new UserAlreadyExistsException("El nombre de usuario ya está registrado");
            });
        }

        if (isAdmin) {
            // El admin puede cambiar cualquier campo
            existing.setUsername(userEntity.getUsername());
            existing.setEmail(userEntity.getEmail());
            if (userEntity.getPassword() != null && !userEntity.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(userEntity.getPassword()));
            }
            existing.setRole(userEntity.getRole());
            existing.setStatus(userEntity.isStatus());
        } else if (isSelf) {
            // El usuario solo puede cambiar username, email y password
            existing.setUsername(userEntity.getUsername());
            existing.setEmail(userEntity.getEmail());
            if (userEntity.getPassword() != null && !userEntity.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(userEntity.getPassword()));
            }
            // Ignorar cambios en el role y status
        } else {
            throw new SecurityException("No autorizado para modificar este usuario");
        }
        return toDTO(repo.save(existing));
    }

    public void deleteUserEntity(Long id) {
        if (!repo.existsById(id)) {
            throw new UserNotFound("User not found with id: " + id);
        }
        repo.deleteById(id);
    }
}
