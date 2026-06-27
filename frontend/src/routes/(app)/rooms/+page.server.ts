import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * @interface CatalogRoomResponse
 * @description DTO que mapea la respuesta pública del catálogo de habitaciones.
 */
export interface CatalogRoomResponse {
    id: number;
    nombre: string;
    tipo: string;
    descripcion: string;
    precioNoche: number;
    imagenPrincipal: string;
    capacidadMaxima: number;
}

/**
 * @function load
 * @description Carga el catálogo de habitaciones mediante SSR. 
 * Aplica filtros dinámicos si provienen de la Landing Page.
 */
export const load: PageServerLoad = async ({ fetch, url, setHeaders }) => {
    // 1. Extracción de filtros de la URL
    const fechaInicio = url.searchParams.get('fechaInicio')?.trim() || '';
    const fechaFin = url.searchParams.get('fechaFin')?.trim() || '';
    const adultos = url.searchParams.get('adultos')?.trim() || '';

    // 2. Construcción dinámica de la URL
    let apiUrl = `${PUBLIC_API_URL}/catalog/rooms`;
    const params = new URLSearchParams();
    
    if (fechaInicio) params.append('fechaInicio', fechaInicio);
    if (fechaFin) params.append('fechaFin', fechaFin);
    if (adultos) params.append('adultos', adultos);

    const hasFilters = params.toString().length > 0;
    if (hasFilters) {
        apiUrl += `?${params.toString()}`;
    } else {
        // OPTIMIZACIÓN DE RENDIMIENTO (Edge Caching)
        // Si no hay filtros dinámicos, le decimos al navegador y al CDN (Nginx) 
        // que pueden cachear esta página pública durante 60 segundos.
        setHeaders({
            'Cache-Control': 'public, max-age=60, s-maxage=60'
        });
    }

    try {
        // 3. Petición al API Gateway
        const response = await fetch(apiUrl);
        
        if (!response.ok) {
            console.error(`[Catalog Error] HTTP ${response.status}`);
            throw new Error('Error al cargar habitaciones desde el backend');
        }
        
        const rooms: CatalogRoomResponse[] = await response.json();
        
        return { 
            rooms,
            hasFilters // Pasamos este flag a la vista para adaptar el mensaje de "Empty State"
        };

    } catch (err) {
        console.error('[SSR Error] Fallo en el catálogo público:', err);
        // Lanzamos un 500 controlado que SvelteKit renderizará en la página de error genérica
        error(500, {
            message: 'En este momento no podemos cargar el catálogo. Por favor, intenta de nuevo en unos minutos.'
        });
    }
};