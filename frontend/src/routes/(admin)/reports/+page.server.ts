import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

// --- INTERFACES (DTOs) ---
/**
 * @interface OccupancyRow
 * @description Mapea la respuesta del reporte de ocupación (probablemente proveniente de una Vista o SP en PostgreSQL).
 */
export interface OccupancyRow {
    out_fecha: string;
    out_tipo: string;
    out_ocupadas: number;
    out_ingreso: number;
}

export const load: PageServerLoad = async ({ fetch, locals, url }) => {
    // Validación de seguridad (Zero Trust)
    if (!locals.token || !locals.user) {
        error(401, 'No autorizado');
    }

    // Extraemos y sanitizamos las fechas de la URL
    const start = url.searchParams.get('start')?.trim() || '';
    const end = url.searchParams.get('end')?.trim() || '';

    let apiUrl = `${PUBLIC_API_URL}/reports/occupancy`;
    if (start && end) {
        apiUrl += `?start=${start}&end=${end}`;
    }

    try {
        const response = await fetch(apiUrl, {
            headers: { 'Authorization': `Bearer ${locals.token}` }
        });

        if (!response.ok) {
            throw new Error(`HTTP Error ${response.status}`);
        }
        
        const occupancy: OccupancyRow[] = await response.json();

        return { 
            occupancy, 
            start, 
            end 
        };
    } catch (err) {
        console.error('[Reports Load Error]', err);
        error(500, 'No se pudo cargar el reporte de ocupación y financiero.');
    }
};