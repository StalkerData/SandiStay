import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * @interface CatalogAreaResponse
 * @description DTO que mapea la respuesta pública del catálogo de áreas sociales.
 */
export interface CatalogAreaResponse {
    id: number;
    nombre: string;
    tipo: string;
    descripcion: string;
    precioHora: number;
    imagenPrincipal: string;
    capacidadMaxima: number;
}

/**
 * @function load
 * @description Carga el catálogo de áreas sociales mediante SSR.
 * Implementa Edge Caching para optimizar el rendimiento de lectura.
 */
export const load: PageServerLoad = async ({ fetch, setHeaders }) => {
    
    // OPTIMIZACIÓN DE RENDIMIENTO (Caching)
    // Cacheamos esta respuesta pública en el CDN/Navegador por 60 segundos.
    setHeaders({
        'Cache-Control': 'public, max-age=60, s-maxage=60'
    });

    try {
        const response = await fetch(`${PUBLIC_API_URL}/catalog/areas`);
        
        if (!response.ok) {
            console.error(`[Areas Catalog Error] HTTP ${response.status}`);
            throw new Error('Error al cargar áreas desde el backend');
        }
        
        const areas: CatalogAreaResponse[] = await response.json();
        
        return { areas };

    } catch (err) {
        console.error('[SSR Error] Fallo en el catálogo de áreas:', err);
        // Lanzamos un 500 controlado que SvelteKit renderizará en la página de error genérica
        error(500, {
            message: 'En este momento no podemos cargar el catálogo de áreas. Por favor, intenta de nuevo en unos minutos.'
        });
    }
};