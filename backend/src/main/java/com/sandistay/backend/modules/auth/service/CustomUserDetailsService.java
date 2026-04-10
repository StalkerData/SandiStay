package com.sandistay.backend.modules.auth.service;

import com.sandistay.backend.modules.auth.model.Usuario;
import com.sandistay.backend.modules.auth.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// 1. Buscar el usuario en nuestra base de datos (PostgreSQL)
		Usuario usuario = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

		// 2. Validar si el usuario está activo y no bloqueado
		if (!usuario.getActivo() || usuario.getBloqueado()) {
			throw new UsernameNotFoundException("El usuario está inactivo o bloqueado.");
		}

		// 3. Extraer los roles de nuestra entidad y convertirlos al formato de Spring
		// Security
		// Spring Security requiere que los roles tengan el prefijo "ROLE_"
		List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
				.map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getCodigo())).collect(Collectors.toList());

		// 4. Construir y retornar el objeto UserDetails que Spring Security entiende
		return new User(usuario.getUsername(), usuario.getPasswordHash(), // Spring Security usará este hash para
																			// comparar con la contraseña ingresada
				authorities);
	}
}