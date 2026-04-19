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
    public PaymentClient(RestClient.Builder restClientBuilder,
                         @Value("${integration.payment.url}") String paymentMockUrl) {
        this.restClient = restClientBuilder.build();
        this.paymentMockUrl = paymentMockUrl;
    }

    /**
     * Verifica la validez de una transacción comunicándose con el Mock de Pagos externo.
     * 
     * @param txId El ID de la transacción generada por el frontend (ej. Nequi/Daviplata).
     * @return true si el pago fue aprobado, false en caso de rechazo o error de red.
     */
    public boolean verifyPayment(String txId) {
        log.info("Iniciando verificación de pago en pasarela externa para txId: {}", txId);

        try {
            // 1. Ejecutamos la petición HTTP GET hacia Mocki.io
            // Agregamos el txId como Query Parameter (?txId=...) por buenas prácticas, 
            // aunque Mocki devolverá la respuesta estática que configuraste.
            PaymentVerificationResponse response = restClient.get()
                    .uri(paymentMockUrl + "?txId={txId}", txId)
                    .retrieve()
                    .body(PaymentVerificationResponse.class); // 2. Mapeo automático de JSON a Record Java

            // 3. Evaluamos la respuesta usando la lógica encapsulada en el DTO
            if (response != null && response.isApproved()) {
                log.info("Pago APROBADO por la pasarela para txId: {}", txId);
                return true;
            } else {
                log.warn("Pago RECHAZADO por la pasarela para txId: {}", txId);
                return false;
            }

        } catch (RestClientException e) {
            // 4. Manejo de Fallos (Resiliencia)
            // Si Mocki.io se cae, no hay internet, o devuelve un Error 500, atrapamos la excepción
            // para que nuestra aplicación no explote, y simplemente rechazamos el pago.
            log.error("Error crítico de comunicación con el Mock de Pagos: {}", e.getMessage());
            return false;
        }
    }
}