package com.sandistay.backend.modules.auth.service;

import com.sandistay.backend.modules.auth.model.Usuario;
import com.sandistay.backend.modules.auth.repository.UsuarioRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación personalizada para conectar la base de datos de SandiStay con Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    // Constructor manual explícito
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Buscar el usuario en PostgreSQL
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Validaciones de estado (Spring Security maneja estas excepciones nativamente)
        if (!usuario.getActivo()) {
            throw new DisabledException("La cuenta de usuario está inactiva.");
        }
        if (usuario.getBloqueado()) {
            throw new LockedException("La cuenta de usuario ha sido bloqueada por seguridad.");
        }

        // 3. Extraer roles y añadir el prefijo "ROLE_" requerido por Spring Security
        List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getCodigo()))
                .toList();

        // 4. Retornar el objeto UserDetails
        return new User(
                usuario.getUsername(),
                usuario.getPasswordHash(),
                authorities
        );
    }
}
