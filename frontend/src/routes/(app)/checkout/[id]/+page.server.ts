import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ params, fetch, url }) => {
    const itemId = params.id;
    
    // 1. Leemos el tipo de reserva desde la URL (?type=Area o ?type=Habitacion)
    const tipoReserva = url.searchParams.get('type') || 'Habitacion';

    try {
        let item = null;

        if (tipoReserva === 'Habitacion') {
            // 2A. Si es Habitación, llamamos al endpoint por ID que hicimos en Java
            const response = await fetch(`${PUBLIC_API_URL}/catalog/rooms/${itemId}`);
            if (!response.ok) throw new Error('Habitación no encontrada');
            item = await response.json();
            
        } else {
            // 2B. Si es Área, llamamos al catálogo de áreas.
            // (Como en Java no hicimos el endpoint GET /areas/{id}, usamos el truco 
            // de traer todas y filtrar en memoria, igual que en la página de detalles).
            const response = await fetch(`${PUBLIC_API_URL}/catalog/areas`);
            if (!response.ok) throw new Error('Áreas no encontradas');
            
            const areas = await response.json();
            item = areas.find((a: any) => a.id.toString() === itemId);
            
            if (!item) throw new Error('Área no encontrada');
        }

        // 3. Retornamos el ítem. 
        // Lo llamamos 'room' en el JSON de retorno para no tener que modificar 
        return { 
            room: item 
        };

    } catch (err) {
        console.error('Error al cargar datos para el checkout:', err);
        error(404, 'El ítem que intentas reservar no está disponible.');
    }
};