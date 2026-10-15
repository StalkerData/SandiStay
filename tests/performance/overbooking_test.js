import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 50,
    iterations: 50,
};

export function setup() {
    const res = http.get('http://backend:8082/api/v1/catalog/rooms');
    const rooms = JSON.parse(res.body);

    const targetRoom = rooms.find(r => r.codigo === 'H-PERF-1');

    if (!targetRoom) {
        console.error("❌ ERROR: No se encontró la habitación H-PERF-1 en el catálogo.");
        return { roomId: 0 };
    }

    console.log(`🎯 Habitación encontrada. ID Real: ${targetRoom.id}`);
    return { roomId: targetRoom.id };
}

export default function (data) {
    const url = 'http://backend:8082/api/v1/bookings';

    const payload = JSON.stringify({
        tipoReserva: 'Habitacion',
        itemId: data.roomId,
        fechaInicio: '2026-12-01',
        fechaFin: '2026-12-05',
        numeroAdultos: 1,
        numeroNinos: 0,
        // CORRECCIÓN: Usamos los datos exactos del cliente que inyectamos en el seed 07
        // para pasar la validación de robo de identidad de Spring Boot.
        nombresHuesped: 'QA',
        apellidosHuesped: 'Performance',
        idTipoDocumento: 1,
        documentoHuesped: '999888777',
        emailHuesped: 'qa@performance.com',
        telefonoHuesped: '3000000000',
        idPais: 1,
        paymentTxId: 'EFECTIVO'
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    if (res.status !== 201 && res.status !== 409) {
        console.log(`⚠️ Error Inesperado: HTTP ${res.status} - ${res.body}`);
    }

    check(res, {
        '✅ Reserva Exitosa (201 Created)': (r) => r.status === 201,
        '🛡️ Bloqueado por BD (409 Conflict)': (r) => r.status === 409,
    });
}
