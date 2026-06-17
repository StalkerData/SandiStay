package com.sandistay.backend.modules.payment.client;

import com.sandistay.backend.modules.payment.dto.PaymentVerificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente HTTP encargado de la comunicación con pasarelas de pago externas.
 * Actúa como una Capa Anticorrupción (Anti-Corruption Layer) aislando al dominio
 * de las fallas de red o cambios en la API de terceros.
 */
@Component
public class PaymentClient {

    // Logger nativo de SLF4J (Sin Lombok) para registrar auditoría de red
    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final RestClient restClient;
    private final String paymentMockUrl;

    /**
     * Inyectamos el Builder de RestClient (provisto por Spring Boot) y la URL desde application.yml.
     */
    public PaymentClient(@Value("${integration.payment.url}") String paymentMockUrl) {
        // Instanciamos el RestClient directamente sin pedirle el Builder a Spring
        this.restClient = RestClient.create(); 
        this.paymentMockUrl = paymentMockUrl;
    }

    /**
     * Verifica la validez de una transacción comunicándose con el Mock de Pagos externo.
     * 
     * @param txId El ID de la transacción generada por el frontend (ej. Nequi/Daviplata).
     * @return true si el pago fue aprobado, false en caso de rechazo o error de red.
     */
    /**
     * Consulta el extracto bancario para verificar una transferencia.
     */
    public PaymentVerificationResponse verifyPayment(String receiptId) {
        log.info("Consultando extracto bancario para el comprobante: {}", receiptId);

        try {
            return restClient.get()
                    .uri(paymentMockUrl + "?receiptId={id}", receiptId)
                    .retrieve()
                    .body(PaymentVerificationResponse.class);

        } catch (RestClientException e) {
            log.error("Error crítico de comunicación con el Banco: {}", e.getMessage());
            // Fallback seguro: Devolvemos un objeto con found = false
            return new PaymentVerificationResponse(
                    false, receiptId, null, null, null, null, null, 
                    "Error de conexión con la red bancaria."
            );
        }
    }
}