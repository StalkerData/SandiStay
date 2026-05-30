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

		// Logueamos el error real en el servidor
		System.err.println("Error Crítico: " + ex.getMessage());

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error", "Ocurrió un error interno en el servidor. Por favor, intenta más tarde.",
				request.getRequestURI());

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

	/**
	 * 4. Maneja errores de Integridad de Datos (Constraints de PostgreSQL,
	 * Triggers, Overbooking). Devuelve un 409 Conflict ocultando el SQL real por
	 * seguridad.
	 */
	@ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
			org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest request) {

		// Imprimimos el error real en la consola del servidor para el DevOps
		System.err.println("Data Integrity Error: " + ex.getMessage());

		// Le devolvemos al Frontend un mensaje limpio y seguro
		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.CONFLICT.value(), "Conflict",
				"Las fechas seleccionadas ya están ocupadas o no cumplen con las reglas del hotel (ej. mínimo 1 noche).",
				request.getRequestURI());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}
	
	/**
     * 5. Maneja excepciones lanzadas intencionalmente desde Procedimientos Almacenados (RAISE EXCEPTION).
     * Extrae el mensaje de negocio (ej: "Saldo pendiente") y lo devuelve como un 400 Bad Request.
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(
            org.springframework.dao.DataAccessException ex, 
            HttpServletRequest request) {
        
        String mensajeUsuario = "Error al procesar la operación en la base de datos.";
        
        // Si el error viene de un RAISE EXCEPTION de Postgres, extraemos el mensaje útil
        if (ex.getCause() != null && ex.getCause().getCause() != null) {
            String mensajeSql = ex.getCause().getCause().getMessage();
            if (mensajeSql != null && mensajeSql.contains("ERROR:")) {
                // Limpiamos el mensaje (quitamos el "ERROR: " del principio)
                mensajeUsuario = mensajeSql.split("\n")[0].replace("ERROR: ", "").trim();
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Business Rule Violation",
                mensajeUsuario,
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * 6. Maneja errores de Acceso Denegado (Falta de permisos o roles).
     * Devuelve un 403 Forbidden.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, 
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "No tienes los permisos necesarios para realizar esta acción.",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

	// Más adelante agregaremos manejadores personalizados aquí
	// Como (Ej: ReservaNotFoundException, PagoRechazadoException)
}
