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

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private final PersonaRepository personaRepository;
	private final UsuarioRepository usuarioRepository;
	private final RolRepository rolRepository;
	private final PasswordEncoder passwordEncoder;
	private final UsuarioViewRepository usuarioViewRepository;

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
		 // REGLA DE NEGOCIO: Solo puede existir un Administrador
        if ("ADMIN".equalsIgnoreCase(request.rolCodigo())) {
            throw new RuntimeException("Operación denegada: No se pueden crear más cuentas de Administrador. Solo se permite registrar personal STAFF.");
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

	/**
	 * Lista todos los usuarios para el panel de administración.
	 */
	public List<UsuarioView> getAllUsers() {
		return usuarioViewRepository.findAll();
	}

	/**
	 * Borrado lógico de un usuario (Desactivación).
	 */
	@Transactional
	public void deactivateUser(Integer idUsuario) {
		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		// Regla de negocio: Un admin no debería poder borrarse a sí mismo por error
		// (Opcional, pero buena práctica)

		usuario.setActivo(false);
		usuarioRepository.save(usuario);
	}

	/**
	 * Cambio de contraseña seguro.
	 */
	@Transactional
	public void changePassword(String username, ChangePasswordRequest request) {
		Usuario usuario = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		// 1. Verificar que la contraseña actual sea correcta
		if (!passwordEncoder.matches(request.oldPassword(), usuario.getPasswordHash())) {
			throw new RuntimeException("La contraseña actual es incorrecta.");
		}

		// 2. Encriptar y guardar la nueva
		usuario.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		usuarioRepository.save(usuario);
	}
}