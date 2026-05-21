package com.sandistay.backend.modules.auth.service;

import com.sandistay.backend.modules.auth.dto.CreateUserRequest;
import com.sandistay.backend.modules.auth.dto.UserResponse;
import com.sandistay.backend.modules.auth.model.Persona;
import com.sandistay.backend.modules.auth.model.Rol;
import com.sandistay.backend.modules.auth.model.Usuario;
import com.sandistay.backend.modules.auth.repository.PersonaRepository;
import com.sandistay.backend.modules.auth.repository.RolRepository;
import com.sandistay.backend.modules.auth.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(PersonaRepository personaRepository, UsuarioRepository usuarioRepository, 
                       RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
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
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + request.rolCodigo()));

        // 3. Crear el Usuario
        Usuario usuario = new Usuario();
        usuario.setIdPersona(persona.getIdPersona());
        usuario.setUsername(request.username());
        usuario.setPasswordHash(passwordEncoder.encode(request.password())); // Encriptación BCrypt
        usuario.setPasswordSalt("salt_dummy"); // Requerido por la BD
        usuario.getRoles().add(rol);
        
        usuario = usuarioRepository.save(usuario);

        return new UserResponse(usuario.getIdUsuario(), usuario.getUsername(), "Usuario creado exitosamente");
    }
}