package com.sandistay.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// 1. Habilitar CORS con nuestra configuración personalizada
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))

				// 2. Deshabilitar CSRF (Cross-Site Request Forgery)
				// Justificación: Como usaremos JWT y no sesiones de servidor (JSESSIONID),
				// el CSRF tradicional no aplica y bloquearía nuestras peticiones POST.
				.csrf(csrf -> csrf.disable())

				// 3. Configurar el manejo de sesiones como STATELESS (Sin estado)
				// Justificación: El servidor no recordará quién está logueado.
				// Cada petición deberá traer su propio token JWT.
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// 4. Reglas de Autorización de Rutas (Endpoints)
				.authorizeHttpRequests(auth -> auth
						// Rutas públicas (Cualquiera puede entrar sin token)
						.requestMatchers("/api/v1/auth/login").permitAll().requestMatchers("/api/v1/catalog/**")
						.permitAll()

						// Cualquier otra ruta requiere estar autenticado (Tener un JWT válido)
						.anyRequest().authenticated());

		// Más adelante, aquí agregaremos nuestro filtro JWT personalizado
		// http.addFilterBefore(jwtAuthenticationFilter,
		// UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Configuración estricta de CORS para permitir que SvelteKit se comunique con
	 * Spring Boot.
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// Permitir solo el origen de nuestro Frontend en desarrollo
		configuration.setAllowedOrigins(List.of("http://localhost:5173"));

		// Permitir los métodos HTTP que usaremos
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

		// Permitir el envío de cabeceras de autorización y JSON
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

		// CRÍTICO: Permitir el envío de credenciales (Cookies HttpOnly para el JWT)
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		// Aplicar esta configuración a todas las rutas de la API
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}
