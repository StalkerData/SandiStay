import { error, fail } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

// --- INTERFACES (DTOs) ---
export interface ReceivableDTO {
    idReserva: number;
    codigoReserva: string;
    tipoReserva: string;
    nombreCliente: string;
    saldoPendiente: number;
}

export interface RefundDTO {
    idPago: number;
    codigoPago: string;
    idReserva: number;
    monto: number;
    observaciones: string;
}

export const load: PageServerLoad = async ({ fetch, locals }) => {
    // 1. BARRERA DE SEGURIDAD (Zero Trust): Solo el ADMIN puede entrar a Tesorería
    if (locals.user?.role !== 'ADMIN') {
        error(403, 'Acceso denegado. Solo los administradores pueden acceder a Tesorería.');
    }

    try {
        // 2. Optimización de Latencia: Peticiones en paralelo
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

        const receivables: ReceivableDTO[] = await resReceivables.json();
        const refunds: RefundDTO[] = await resRefunds.json();

        return { receivables, refunds };

    } catch (err) {
        console.error('[Treasury Load Error]', err);
        error(500, 'No se pudo cargar el módulo de tesorería.');
    }
};

export const actions: Actions = {
    
    /**
     * @action registerPayment
     * @description Registra un abono o pago total a una cuenta por cobrar.
     */
    registerPayment: async ({ request, locals, fetch }) => {
        if (locals.user?.role !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        
        const data = await request.formData();
        const payload = {
            idReserva: parseInt(data.get('idReserva')?.toString() || '0'),
            monto: parseFloat(data.get('monto')?.toString() || '0'),
            idMetodoPago: parseInt(data.get('idMetodoPago')?.toString() || '1'),
            referencia: data.get('referencia')?.toString(),
            observaciones: data.get('observaciones')?.toString()
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
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al registrar el pago.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }

            return { success: true, message: 'Pago registrado exitosamente. Los saldos se han actualizado.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    /**
     * @action processRefund
     * @description Marca un reembolso pendiente como "DEVUELTO" tras realizar la transferencia manual.
     */
    processRefund: async ({ request, locals, fetch }) => {
        if (locals.user?.role !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        
        const data = await request.formData();
        const idPago = data.get('idPago');

        try {
            const response = await fetch(`${PUBLIC_API_URL}/finance/treasury/refunds/${idPago}/process`, {
                method: 'PATCH',
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al procesar el reembolso.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }

            return { success: true, message: 'Reembolso marcado como DEVUELTO exitosamente.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};