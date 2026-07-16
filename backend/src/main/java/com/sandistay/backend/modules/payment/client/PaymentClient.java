package com.sandistay.backend.modules.payment.client;

import com.sandistay.backend.modules.payment.dto.PaymentVerificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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

    // Logger nativo de SLF4J para registrar auditoría de red
    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final RestClient restClient;
    private final String paymentMockUrl;

    /**
     * Constructor manual.
     * Configuramos Timeouts estrictos para evitar el agotamiento de hilos
     * (Thread Starvation) si el servicio externo falla.
     */
    public PaymentClient(@Value("${integration.payment.url}") String paymentMockUrl) {
        this.paymentMockUrl = paymentMockUrl;

        // Configuración de Timeouts (Security & Resilience)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 3 segundos máximo para establecer conexión
        factory.setReadTimeout(5000);    // 5 segundos máximo esperando respuesta

        // Usamos el método estático builder() en lugar de inyectarlo como Bean
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /**
     * Consulta el extracto bancario para verificar una transferencia.
     * Implementa un patrón de Resiliencia (Retry con Backoff) para tolerar micro-cortes de red.
     *
     * @param receiptId El ID de la transacción generada por el frontend (ej. Nequi/Daviplata).
     * @return PaymentVerificationResponse con el resultado de la conciliación.
     */
    public PaymentVerificationResponse verifyPayment(String receiptId) {
        log.info("Consultando extracto bancario para el comprobante: {}", receiptId);

        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                return restClient.get()
                        .uri(paymentMockUrl + "?receiptId={id}", receiptId)
                        .retrieve()
                        .body(PaymentVerificationResponse.class);

            } catch (RestClientException e) {
                attempt++;
                log.warn("Intento {}/{} fallido al conectar con el Banco: {}", attempt, maxRetries, e.getMessage());

                if (attempt >= maxRetries) {
                    log.error("Error crítico de comunicación con el Banco tras {} intentos.", maxRetries);
                    // Fallback seguro: Devolvemos un objeto con found = false
                    return new PaymentVerificationResponse(
                            false, receiptId, null, null, null, null, null,
                            "Error de conexión con la red bancaria. Por favor, intente más tarde."
                    );
                }

                // Backoff: Esperamos un poco antes de reintentar (500ms, 1000ms...)
                try {
                    Thread.sleep(500L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Fallback en caso de interrupción del hilo
        return new PaymentVerificationResponse(
                false, receiptId, null, null, null, null, null,
                "Operación interrumpida internamente."
        );
    }
}
