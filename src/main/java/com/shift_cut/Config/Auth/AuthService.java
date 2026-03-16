package com.shift_cut.Config.Auth;

import com.shift_cut.Config.Security.JwtService;
import com.shift_cut.Mapper.AuthMapper;
import com.shift_cut.Model.DTO.AuthResponse;
import com.shift_cut.Model.Enum.Role;
import com.shift_cut.Model.UserEntity;
import com.shift_cut.Service.UserEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import com.shift_cut.Exceptions.UserAlreadyExistsException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserEntityService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails user = userService.getByEmailOrThrow(request.email());
        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse register(RegisterRequest request) {
        // Validación previa para evitar duplicados
        if (userService.findByEmailOptional(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("El email ya está registrado");
        }
        if (userService.findByUsernameOptional(request.username()).isPresent()) {
            throw new UserAlreadyExistsException("El nombre de usuario ya está registrado");
        }

        UserEntity user = authMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setStatus(true);

        try {
            userService.createUserEntity(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("El email o nombre de usuario ya está registrado");
        }
        return new AuthResponse(jwtService.generateToken(user));
    }

}