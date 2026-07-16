package com.sandistay.backend.modules.auth.service;

import com.sandistay.backend.modules.auth.dto.ChangePasswordRequest;
import com.sandistay.backend.modules.auth.dto.CreateUserRequest;
import com.sandistay.backend.modules.auth.dto.UserResponse;
import com.sandistay.backend.modules.auth.model.Persona;
import com.sandistay.backend.modules.auth.model.Rol;
import com.sandistay.backend.modules.auth.model.Usuario;
import com.sandistay.backend.modules.auth.model.UsuarioView;
import com.sandistay.backend.modules.auth.repository.PersonaRepository;
import com.sandistay.backend.modules.auth.repository.RolRepository;
import com.sandistay.backend.modules.auth.repository.UsuarioRepository;
import com.sandistay.backend.modules.auth.repository.UsuarioViewRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para la gestión (CRUD) de usuarios del sistema.
 */
@Service
public class UserService {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioViewRepository usuarioViewRepository;

    // Constructor manual explícito
    public UserService(PersonaRepository personaRepository, UsuarioRepository usuarioRepository,
                       RolRepository rolRepository, PasswordEncoder passwordEncoder,
                       UsuarioViewRepository usuarioViewRepository) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioViewRepository = usuarioViewRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // REGLA DE NEGOCIO: Solo puede existir un Administrador (Principio de Menor Privilegio)
        if ("ADMIN".equalsIgnoreCase(request.rolCodigo())) {
            throw new IllegalArgumentException("Operación denegada: No se pueden crear más cuentas de Administrador. Solo se permite registrar personal STAFF.");
        }

        // 1. Crear la Persona
        Persona persona = new Persona();
        persona.setNombres(request.nombres());
        persona.setApellidos(request.apellidos());
        persona.setIdTipoDocumento(request.idTipoDocumento());
        persona.setNumeroDocumento(request.numeroDocumento());
        persona.setEmail(request.email());
        persona.setIdPais(request.idPais());
        persona.setIdEstadoPersona(1); // 1 = ACTIVO
        persona = personaRepository.save(persona);

        // 2. Buscar el Rol
        Rol rol = rolRepository.findByCodigo(request.rolCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + request.rolCodigo()));

        // 3. Crear el Usuario
        Usuario usuario = new Usuario();
        usuario.setIdPersona(persona.getIdPersona());
        usuario.setUsername(request.username());
        usuario.setPasswordHash(passwordEncoder.encode(request.password())); // Encriptación BCrypt

        // Nota: BCrypt ya incluye el Salt en el hash generado.
        // Mantenemos este campo por compatibilidad con el esquema de BD heredado.
        usuario.setPasswordSalt("bcrypt_internal_salt");

        usuario.getRoles().add(rol);
        usuario = usuarioRepository.save(usuario);

        return new UserResponse(usuario.getIdUsuario(), usuario.getUsername(), "Usuario creado exitosamente");
    }

    @Transactional(readOnly = true)
    public List<UsuarioView> getAllUsers() {
        return usuarioViewRepository.findAll();
    }

    @Transactional
    public void deactivateUser(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // REGLA DE NEGOCIO: Un administrador no puede desactivarse a sí mismo
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (usuario.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new IllegalArgumentException("Operación denegada: No puedes desactivar tu propia cuenta de usuario.");
        }

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // 1. Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.oldPassword(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("La contraseña actual es incorrecta.");
        }

        // 2. Encriptar y guardar la nueva
        usuario.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        usuarioRepository.save(usuario);
    }
}
