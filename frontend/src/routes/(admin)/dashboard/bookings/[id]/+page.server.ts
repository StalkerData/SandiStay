import { error, fail } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ params, fetch, locals }) => {
    const idReserva = params.id;
    const token = locals.token;

    if (!token) error(401, 'No autorizado');

    try {
        // 1. Traer el detalle completo de la reserva
        const resDetalle = await fetch(`http://localhost:8082/api/v1/bookings/${idReserva}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!resDetalle.ok) throw new Error('Error al cargar detalles');
        const detalle = await resDetalle.json();

        // 2. Traer el balance financiero
        const resBalance = await fetch(`http://localhost:8082/api/v1/finance/balance/${idReserva}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        let balance = null;
        if (resBalance.ok) {
            balance = await resBalance.json();
        }

        // 3. Traer el historial del cliente REAL
        const idCliente = detalle.idCliente; // <--- AHORA USAMOS EL ID REAL
        
        const resHistory = await fetch(`http://localhost:8082/api/v1/reports/clients/${idCliente}/history`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        let clientHistory = null;
        if (resHistory.ok) {
            clientHistory = await resHistory.json();
        }

        return {
            detalle,
            balance,
            clientHistory
        };

    } catch (err) {
        console.error('Error:', err);
        error(500, 'No se pudo cargar la información de la reserva.');
    }
};

export const actions: Actions = {
    payBalance: async ({ request, params, locals, fetch }) => {
        const data = await request.formData();
        const metodoPago = data.get('metodoPago');
        const referencia = data.get('referencia') || '';
        const idReserva = params.id;

        try {
            const response = await fetch(`http://localhost:8082/api/v1/bookings/${idReserva}/pay`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${locals.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ metodoPago, referencia })
            });

            if (!response.ok) {
                const errData = await response.json();
                return fail(response.status, { error: errData.message || 'Error al registrar el pago.' });
            }

            return { success: true, message: 'Pago registrado exitosamente. El saldo ahora es $0.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};