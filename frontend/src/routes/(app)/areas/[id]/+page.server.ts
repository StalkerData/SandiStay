import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * @interface AreaDetail
 * @description DTO para el detalle del área social.
 */
export interface AreaDetail {
    id: number;
    nombre: string;
    tipo: string;
    descripcion: string;
    precioHora: number;
    capacidadPersonas?: number;
    capacidadMaxima?: number;
    imagenPrincipal: string;
}

export const load: PageServerLoad = async ({ params, fetch, setHeaders }) => {
    const areaId = params.id;

    // OPTIMIZACIÓN CRÍTICA (Mitigación de Deuda Técnica)
    // Como estamos trayendo TODAS las áreas para filtrar una sola, 
    // cachear esta respuesta es vital para no saturar la base de datos.
    setHeaders({
        'Cache-Control': 'public, max-age=300, s-maxage=300' // 5 minutos
    });

    try {
        // TODO (Tech Debt): Crear endpoint GET /catalog/areas/{id} en Spring Boot
        const response = await fetch(`${PUBLIC_API_URL}/catalog/areas`);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        
        const areas: AreaDetail[] = await response.json();
        
        // Filtrado en memoria
        const area = areas.find(a => a.id.toString() === areaId);
        
        if (!area) {
            error(404, { message: 'El área social que buscas no existe o fue retirada.' });
        }
        
        return { area };

    } catch (err) {
        console.error(`[Area Detail Error] ID ${areaId}:`, err);
        
        if (err && typeof err === 'object' && 'status' in err) {
            throw err; // Propaga el 404
        }
        
        error(500, { message: 'No se pudo conectar con el servidor del hotel.' });
    }
};