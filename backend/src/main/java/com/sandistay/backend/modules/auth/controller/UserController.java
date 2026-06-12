package com.sandistay.backend.modules.auth.controller;

import com.sandistay.backend.modules.auth.dto.CreateUserRequest;
import com.sandistay.backend.modules.auth.dto.UserResponse;
import com.sandistay.backend.modules.auth.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sandistay.backend.modules.auth.dto.ChangePasswordRequest;
import com.sandistay.backend.modules.auth.model.UsuarioView;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Endpoint para crear un nuevo usuario (Staff/Admin). SOLO los usuarios con rol
	 * ADMIN pueden ejecutar esto.
	 */
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		UserResponse response = userService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UsuarioView>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> deactivateUser(@PathVariable Integer id) {
		userService.deactivateUser(id);
		return ResponseEntity.ok("Usuario desactivado exitosamente.");
	}

	@PatchMapping("/me/password")
	// Cualquier usuario logueado (ADMIN o STAFF) puede cambiar su propia contraseña
	@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
	public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request,
			Authentication authentication) {

		userService.changePassword(authentication.getName(), request);
		return ResponseEntity.ok("Contraseña actualizada exitosamente.");
	}
}