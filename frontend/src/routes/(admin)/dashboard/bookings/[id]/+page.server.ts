import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

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

        // 2. Traer el balance financiero (Estado de cuenta)
        const resBalance = await fetch(`http://localhost:8082/api/v1/finance/balance/${idReserva}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        let balance = null;
        if (resBalance.ok) {
            balance = await resBalance.json();
        }

        return {
            detalle,
            balance
        };

    } catch (err) {
        console.error('Error:', err);
        error(500, 'No se pudo cargar la información de la reserva.');
    }
};