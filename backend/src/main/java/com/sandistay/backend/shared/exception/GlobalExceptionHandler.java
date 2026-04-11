package com.sandistay.backend.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
	/**
	 * 1. Maneja los errores de validación de los DTOs (Ej: @NotNull, @Email).
	 * Devuelve un 400 Bad Request con el detalle de qué campo falló.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		Map<String, String> errors = new HashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.put(error.getField(), error.getDefaultMessage());
		}

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				"Bad Request - Error de Validación", errors.toString(), request.getRequestURI());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	/**
	 * 2. Maneja cualquier error inesperado (Error 500). Evita que Spring Boot
	 * devuelva una página HTML o el StackTrace completo.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {

		// NOTA DE SEGURIDAD: En producción estricta, no se envía ex.getMessage()
		// al cliente para no revelar detalles internos de la BD o el código.
		// Por ahora lo dejamos para facilitar la depuración en desarrollo.
		// Hay que esperar a produccion para eliminar el ex.getMessage()
		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error", ex.getMessage(), request.getRequestURI());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}

	/**
	 * 3. Maneja errores de credenciales incorrectas (Login fallido). Devuelve un
	 * 401 Unauthorized en lugar de un 500.
	 */
	@ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(
			org.springframework.security.authentication.BadCredentialsException ex, HttpServletRequest request) {

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(),
				"Unauthorized", "Usuario o contraseña incorrectos", request.getRequestURI());

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	// Más adelante agregaremos manejadores personalizados aquí
	// Como (Ej: ReservaNotFoundException, PagoRechazadoException)
}
