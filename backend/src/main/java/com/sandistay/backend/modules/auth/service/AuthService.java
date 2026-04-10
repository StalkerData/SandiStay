package com.sandistay.backend.modules.auth.service;

import com.sandistay.backend.modules.auth.dto.AuthResponse;
import com.sandistay.backend.modules.auth.dto.LoginRequest;
import com.sandistay.backend.modules.auth.model.Usuario;
import com.sandistay.backend.modules.auth.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UsuarioRepository usuarioRepository;
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository,
			JwtService jwtService, UserDetailsService userDetailsService) {
		this.authenticationManager = authenticationManager;
		this.usuarioRepository = usuarioRepository;
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Orquesta el proceso de inicio de sesión.
	 */
	public AuthResponse login(LoginRequest request) {
		// 1. Autenticar al usuario (Spring Security verifica el hash de la contraseña
		// automáticamente)
		// Si falla, lanza una excepción (BadCredentialsException) que será atrapada por
		// el GlobalExceptionHandler
		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

		// 2. Si la autenticación es exitosa, obtenemos el usuario de la BD
		Usuario usuario = usuarioRepository.findByUsername(request.username())
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado tras autenticación"));

		// 3. Actualizar la fecha de último login y reiniciar intentos fallidos (Detalle
		// Senior)
		usuario.setUltimoLogin(LocalDateTime.now());
		usuario.setIntentosFallidos(0);
		usuarioRepository.save(usuario);

		// 4. Extraer los roles en formato String para el Token y la respuesta
		List<String> roles = usuario.getRoles().stream().map(rol -> rol.getCodigo()).collect(Collectors.toList());

		// 5. Preparar claims adicionales para inyectar en el JWT (útil para que
		// SvelteKit lea los roles sin consultar la BD)
		Map<String, Object> extraClaims = new HashMap<>();
		extraClaims.put("roles", roles);

		// 6. Generar el Token JWT
		UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
		String jwtToken = jwtService.generateToken(extraClaims, userDetails);

		// 7. Retornar la respuesta al Frontend
		return new AuthResponse(jwtToken, usuario.getUsername(), roles);
	}
}