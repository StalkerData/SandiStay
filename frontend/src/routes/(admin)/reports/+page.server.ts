import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ fetch, locals, url }) => {
    const token = locals.token;
    if (!token) error(401, 'No autorizado');

    // Extraemos las fechas de la URL si existen (ej: ?start=2026-05-01&end=2026-05-31)
    const start = url.searchParams.get('start') || '';
    const end = url.searchParams.get('end') || '';

    let apiUrl = `${PUBLIC_API_URL}/reports/occupancy`;
    if (start && end) {
        apiUrl += `?start=${start}&end=${end}`;
    }

    try {
        const response = await fetch(apiUrl, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Error al cargar el reporte');
        
        const occupancy = await response.json();

        return { 
            occupancy, 
            start, 
            end 
        };
    } catch (err) {
        console.error('Error:', err);
        error(500, 'No se pudo cargar el reporte de ocupación.');
    }
};