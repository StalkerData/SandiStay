package com.sandistay.backend.modules.payment.dto;

/**
 * DTO inmutable que mapea la respuesta del Mock de Pagos externo (Mocki.io / Contenedor Node).
 * Los nombres de los atributos deben coincidir exactamente con las claves del JSON devuelto por la API.
 */
public record PaymentVerificationResponse(
        String status,
        String message
) {
    /**
     * Método de conveniencia para saber si el pago fue aprobado.
     * Encapsula la lógica de comparación de strings para no repetirla en los servicios.
     */
    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(this.status);
    }
}