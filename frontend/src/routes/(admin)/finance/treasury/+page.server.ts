import { error, fail } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ fetch, locals }) => {
    // 1. BARRERA DE SEGURIDAD: Solo el ADMIN puede entrar a Tesorería
    if (locals.userRole !== 'ADMIN') {
        error(403, 'Acceso denegado. Solo los administradores pueden acceder a Tesorería.');
    }

    try {
        // 2. Traemos las Cuentas por Cobrar y los Reembolsos Pendientes en paralelo
        const [resReceivables, resRefunds] = await Promise.all([
            fetch(`${PUBLIC_API_URL}/finance/treasury/receivables`, {
                headers: { 'Authorization': `Bearer ${locals.token}` }
            }),
            fetch(`${PUBLIC_API_URL}/finance/treasury/refunds`, {
                headers: { 'Authorization': `Bearer ${locals.token}` }
            })
        ]);

        if (!resReceivables.ok || !resRefunds.ok) {
            throw new Error('Error al cargar los datos de tesorería');
        }

        return {
            receivables: await resReceivables.json(),
            refunds: await resRefunds.json()
        };

    } catch (err) {
        console.error('Error:', err);
        error(500, 'No se pudo cargar el módulo de tesorería.');
    }
};

export const actions: Actions = {
    
    // Acción para registrar un nuevo pago/abono
    registerPayment: async ({ request, locals, fetch }) => {
        if (locals.userRole !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        
        const data = await request.formData();
        const payload = {
            idReserva: parseInt(data.get('idReserva') as string),
            monto: parseFloat(data.get('monto') as string),
            idMetodoPago: parseInt(data.get('idMetodoPago') as string),
            referencia: data.get('referencia'),
            observaciones: data.get('observaciones')
        };

        try {
            const response = await fetch(`${PUBLIC_API_URL}/finance/treasury/payments`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${locals.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errData = await response.json();
                return fail(response.status, { error: errData.message || 'Error al registrar el pago.' });
            }

            return { success: true, message: 'Pago registrado exitosamente. Los saldos se han actualizado.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    // Acción para marcar un reembolso como completado
    processRefund: async ({ request, locals, fetch }) => {
        if (locals.userRole !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        
        const data = await request.formData();
        const idPago = data.get('idPago');

        try {
            const response = await fetch(`${PUBLIC_API_URL}/finance/treasury/refunds/${idPago}/process`, {
                method: 'PATCH',
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!response.ok) {
                const errData = await response.json();
                return fail(response.status, { error: errData.message || 'Error al procesar el reembolso.' });
            }

            return { success: true, message: 'Reembolso marcado como DEVUELTO exitosamente.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};