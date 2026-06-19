import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ fetch, url }) => {
    // 1. Extraemos los filtros de la URL (si el usuario viene de la Landing Page)
    const fechaInicio = url.searchParams.get('fechaInicio') || '';
    const fechaFin = url.searchParams.get('fechaFin') || '';
    const adultos = url.searchParams.get('adultos') || '';

    // 2. Construimos la URL para Spring Boot
    let apiUrl = `${PUBLIC_API_URL}/catalog/rooms`;
    const params = new URLSearchParams();
    
    if (fechaInicio) params.append('fechaInicio', fechaInicio);
    if (fechaFin) params.append('fechaFin', fechaFin);
    if (adultos) params.append('adultos', adultos);

    if (params.toString()) {
        apiUrl += `?${params.toString()}`;
    }

    try {
        // 3. Hacemos la petición con los filtros aplicados
        const response = await fetch(apiUrl);
        if (!response.ok) throw new Error('Error al cargar habitaciones');
        
        const rooms = await response.json();
        return { rooms };
    } catch (err) {
        error(500, 'No se pudo cargar el catálogo de habitaciones.');
    }
};