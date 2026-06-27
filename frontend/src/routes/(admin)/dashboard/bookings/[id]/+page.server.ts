import { error, fail } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ params, fetch, locals }) => {
    const idReserva = params.id;
    const token = locals.token;

    // Validación de seguridad (Zero Trust)
    if (!token) error(401, 'No autorizado');

    try {
        // 1. Traer el detalle completo de la reserva (Bloqueante, necesitamos el idCliente)
        const resDetalle = await fetch(`${PUBLIC_API_URL}/bookings/${idReserva}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!resDetalle.ok) {
            if (resDetalle.status === 404) error(404, 'Reserva no encontrada');
            throw new Error('Error al cargar detalles de la reserva');
        }
        
        const detalle = await resDetalle.json();
        const idCliente = detalle.idCliente;

        // 2. Optimización de Latencia: Traer Balance e Historial en paralelo
        const [resBalance, resHistory] = await Promise.all([
            fetch(`${PUBLIC_API_URL}/finance/balance/${idReserva}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            }),
            fetch(`${PUBLIC_API_URL}/reports/clients/${idCliente}/history`, {
                headers: { 'Authorization': `Bearer ${token}` }
            })
        ]);

        // 3. Manejo resiliente: Si fallan los servicios secundarios, no rompemos la vista principal
        const balance = resBalance.ok ? await resBalance.json() : null;
        const clientHistory = resHistory.ok ? await resHistory.json() : null;

        if (!resBalance.ok) console.warn(`[Finance] No se pudo cargar el balance para la reserva ${idReserva}`);
        if (!resHistory.ok) console.warn(`[CRM] No se pudo cargar el historial para el cliente ${idCliente}`);

        return {
            detalle,
            balance,
            clientHistory
        };

    } catch (err) {
        console.error('[Booking Detail Error]', err);
        // Si ya es un error de SvelteKit (ej. 404), lo dejamos pasar
        if (err && typeof err === 'object' && 'status' in err) throw err;
        
        error(500, 'No se pudo cargar la información de la reserva.');
    }
};

export const actions: Actions = {
    
    /**
     * @action payBalance
     * @description Registra un pago manual para saldar la deuda de una reserva.
     */
    payBalance: async ({ request, params, locals, fetch }) => {
        const data = await request.formData();
        const metodoPago = data.get('metodoPago')?.toString();
        const referencia = data.get('referencia')?.toString() || '';
        const idReserva = params.id;

        if (!metodoPago) {
            return fail(400, { error: 'El método de pago es obligatorio.' });
        }

        try {
            const response = await fetch(`${PUBLIC_API_URL}/bookings/${idReserva}/pay`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${locals.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ metodoPago, referencia })
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al registrar el pago.';
                
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }

            return { success: true, message: 'Pago registrado exitosamente. El saldo ha sido actualizado.' };
        } catch (err) {
            console.error('[Payment Error]', err);
            return fail(500, { error: 'Error de conexión con el servidor financiero.' });
        }
    }
};