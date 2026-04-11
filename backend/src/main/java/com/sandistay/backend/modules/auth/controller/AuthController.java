package com.sandistay.backend.modules.auth.controller;

import com.sandistay.backend.modules.auth.dto.AuthResponse;
import com.sandistay.backend.modules.auth.dto.LoginRequest;
import com.sandistay.backend.modules.auth.dto.UserProfileResponse;
import com.sandistay.backend.modules.auth.model.Usuario;
import com.sandistay.backend.modules.auth.repository.UsuarioRepository;
import com.sandistay.backend.modules.auth.service.AuthService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@SecurityRequirement(name = "Bearer Authentication")
public class AuthController {

	private final AuthService authService;
	private final UsuarioRepository usuarioRepository;

	public AuthController(AuthService authService, UsuarioRepository usuarioRepository) {
		this.authService = authService;
		this.usuarioRepository = usuarioRepository;
	}

	/**
	 * Endpoint público para iniciar sesión.
	 * 
	 * @Valid asegura que el DTO cumpla con las restricciones (@NotBlank) antes de
	 *        entrar al método.
	 */
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	/**
	 * Endpoint protegido para obtener el perfil del usuario actual. Spring Security
	 * inyecta automáticamente el objeto 'Authentication' si el JWT es válido.
	 */
	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
		// El nombre de usuario se extrae del token JWT validado
		String username = authentication.getName();

		// Buscamos los datos frescos en la base de datos (como el último login)
		Usuario usuario = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		List<String> roles = usuario.getRoles().stream().map(rol -> rol.getCodigo()).collect(Collectors.toList());

		UserProfileResponse profileResponse = new UserProfileResponse(usuario.getUsername(), roles,
				usuario.getUltimoLogin());

		return ResponseEntity.ok(profileResponse);
	}
}