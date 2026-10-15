import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * 5.1. Prueba de Carga (Load Testing)
 * Objetivo: Validar la eficiencia de lectura masiva del catálogo público.
 */
export const options = {
    vus: 500,
    duration: '5m',
    thresholds: {
        http_req_duration: ['p(95)<200'],
        http_req_failed: ['rate==0'],
    },
};

export default function () {
    // CORRECCIÓN: Usamos 'backend' (sin guiones bajos) para que Tomcat acepte el Host Header
    const res = http.get('http://backend:8082/api/v1/catalog/rooms');

    check(res, {
        'status es 200 OK': (r) => r.status === 200,
    });

    sleep(1);
}
