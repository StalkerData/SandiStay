package com.sandistay.backend.modules.auth.service;

import com.sandistay.backend.modules.auth.dto.AuthResponse;
import com.sandistay.backend.modules.auth.dto.LoginRequest;
import com.sandistay.backend.modules.auth.dto.UserProfileResponse;
import com.sandistay.backend.modules.auth.model.Rol;
import com.sandistay.backend.modules.auth.model.Usuario;
import com.sandistay.backend.modules.auth.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orquestador del proceso de inicio de sesión y perfil de usuario.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Constructor manual explícito (Sin Lombok)
    public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository,
                       JwtService jwtService, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Autenticar al usuario (Spring Security verifica el hash BCrypt automáticamente)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // 2. Obtener el usuario de la BD
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // 3. Actualizar métricas de seguridad
        usuario.setUltimoLogin(LocalDateTime.now());
        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);

        // 4. Extraer roles para el Token
        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getCodigo)
                .toList();

        // 5. Inyectar claims adicionales en el JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", roles);

        // 6. Generar el Token JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        String jwtToken = jwtService.generateToken(extraClaims, userDetails);

        return new AuthResponse(jwtToken, usuario.getUsername(), roles);
    }

    /**
     * Obtiene los datos del perfil del usuario autenticado.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getCodigo)
                .toList();

        return new UserProfileResponse(usuario.getUsername(), roles, usuario.getUltimoLogin());
    }
}
