import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * 5.3. Prueba de Fuga de Memoria (Soak Testing)
 * Objetivo: Validar que JasperReports y HikariCP liberan la memoria y las conexiones
 * correctamente bajo una carga continua de generación de PDFs.
 */
export const options = {
    vus: 10,          // 10 usuarios concurrentes
    duration: '5m',   // Durante 5 minutos
};

// 1. SETUP: Hacemos login para obtener el Token JWT (Requerido para facturar)
export function setup() {
    const loginRes = http.post('http://backend:8082/api/v1/auth/login', JSON.stringify({
        username: 'admin',
        password: 'admin123'
    }), { headers: { 'Content-Type': 'application/json' } });

    const token = JSON.parse(loginRes.body).token;
    return { token: token };
}

// 2. ATAQUE: Generación masiva de PDFs
export default function (data) {
    const url = 'http://backend:8082/api/v1/finance/invoices/generate';

    const payload = JSON.stringify({
        codigoReserva: 'RES-PERF-PDF' // La reserva que inyectamos en el seed 07
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${data.token}`
        },
        // CRÍTICO: Le decimos a k6 que descarte el cuerpo de la respuesta.
        // Si k6 guarda los miles de PDFs en su propia memoria, k6 colapsará antes que Java.
        responseType: 'none',
    };

    const res = http.post(url, payload, params);

    check(res, {
        '📄 Factura PDF Generada (201 Created)': (r) => r.status === 201,
    });

    // Pequeña pausa para simular el ritmo de un hotel real
    sleep(0.5);
}
