import { error, fail, redirect } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

// Agregamos idReserva a la interfaz
export interface ReservaResumen {
    idReserva: number; 
    codigoReserva: string;
    tipoReserva: string;
    nombreCliente: string;
    nombreHuesped: string;
    fechaReserva: string;
    estado: string;
    estadoColor: string;
}

export const load: PageServerLoad = async ({ fetch, locals }) => {
    const token = locals.token;
    if (!token) error(401, 'No autorizado');

    try {
        const response = await fetch(`${PUBLIC_API_URL}/bookings`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                error(401, 'Tu sesión ha expirado o no tienes permisos.');
            }
            error(500, 'Error al cargar la lista de reservas.');
        }

        const reservas: Array<ReservaResumen> = await response.json();
        return { reservas };

    } catch (err) {
        error(500, 'No se pudo conectar con el servidor del hotel.');
    }
};

// --- ACCIONES DEL SERVIDOR ---
export const actions: Actions = {
    
    logout: async ({ cookies }) => {
        cookies.delete('jwt_token', { path: '/' });
        cookies.delete('user_role', { path: '/' });
        throw redirect(303, '/login');
    },

    // NUEVA ACCIÓN: Check-out
    checkout: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const id = data.get('id');

        const response = await fetch(`${PUBLIC_API_URL}/bookings/${id}/checkout`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${locals.token}` }
        });

        if (!response.ok) {
            const errData = await response.json();
            // Si el cliente debe dinero, Spring Boot devolverá un 400 y lo mostraremos aquí
            return fail(response.status, { error: errData.message || 'Error al realizar el check-out.' });
        }
        return { success: true, message: 'Check-out realizado exitosamente.' };
    },

    // NUEVA ACCIÓN: Cancelar
    cancelar: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const id = data.get('id');
        const motivo = data.get('motivo');

        if (!motivo) {
            return fail(400, { error: 'El motivo de cancelación es obligatorio.' });
        }

        const response = await fetch(`${PUBLIC_API_URL}/bookings/${id}/cancel`, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${locals.token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ motivo })
        });

        if (!response.ok) {
            const errData = await response.json();
            return fail(response.status, { error: errData.message || 'Error al cancelar la reserva.' });
        }
        
        const result = await response.json();
        return { 
            success: true, 
            message: `Reserva cancelada. Multa: $${result.montoMulta}. Reembolso: $${result.montoReembolso}.` 
        };
    }
};