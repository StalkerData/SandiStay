import { error, fail, redirect } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * @interface ReservaResumen
 * @description DTO que mapea la proyección de la base de datos para la vista rápida del Dashboard.
 */
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

/**
 * @function load
 * @description Carga la lista de reservas activas. Protegida por el token JWT inyectado en locals.
 */
export const load: PageServerLoad = async ({ fetch, locals }) => {
    const token = locals.token;
    
    // Validación redundante de seguridad (Defense in Depth)
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
        console.error('[Dashboard Load Error]', err);
        error(500, 'No se pudo conectar con el servidor del hotel.');
    }
};

/**
 * @description Server Actions para la gestión operativa del hotel.
 */
export const actions: Actions = {
    
    /**
     * @action logout
     * @description Destruye la sesión eliminando las cookies de seguridad.
     */
    logout: async ({ cookies }) => {
        cookies.delete('jwt_token', { path: '/' });
        cookies.delete('user_role', { path: '/' });
        throw redirect(303, '/login');
    },

    /**
     * @action checkout
     * @description Procesa la salida de un huésped. Valida deudas pendientes en el backend.
     */
    checkout: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const id = data.get('id');

        try {
            const response = await fetch(`${PUBLIC_API_URL}/bookings/${id}/checkout`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al realizar el check-out.';
                
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }
            
            return { success: true, message: 'Check-out realizado exitosamente.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    /**
     * @action cancelar
     * @description Cancela una reserva y ejecuta la lógica de Split Payment (Multa/Reembolso).
     */
    cancelar: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const id = data.get('id');
        const motivo = data.get('motivo')?.toString().trim();

        if (!motivo) {
            return fail(400, { error: 'El motivo de cancelación es obligatorio.' });
        }

        try {
            const response = await fetch(`${PUBLIC_API_URL}/bookings/${id}/cancel`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${locals.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ motivo })
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al cancelar la reserva.';
                
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }
            
            const result = await response.json();
            return { 
                success: true, 
                message: `Reserva cancelada. Multa: $${result.montoMulta}. Reembolso: $${result.montoReembolso}.` 
            };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};