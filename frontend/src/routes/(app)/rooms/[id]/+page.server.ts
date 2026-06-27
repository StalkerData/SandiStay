import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * @interface RoomDetail
 * @description DTO específico para la vista de detalle. Contiene más información 
 * que el DTO del catálogo general (ej. capacidades específicas, precio de pasadía).
 */
export interface RoomDetail {
    id: number;
    numero: string;
    nombre: string;
    tipo: string;
    descripcion: string;
    precioNoche: number;
    precioDia: number;
    capacidadAdultos: number;
    capacidadNinos: number;
    capacidadMaxima: number;
    imagenPrincipal: string;
}

/**
 * @function load
 * @description Carga los detalles de una habitación específica.
 * Implementa Edge Caching para optimizar el rendimiento de lectura.
 */
export const load: PageServerLoad = async ({ params, fetch, setHeaders }) => {
    const roomId = params.id;

    // OPTIMIZACIÓN DE RENDIMIENTO (Caching)
    // Cacheamos esta respuesta en el CDN/Navegador por 5 minutos.
    setHeaders({
        'Cache-Control': 'public, max-age=300, s-maxage=300'
    });

    try {
        const response = await fetch(`${PUBLIC_API_URL}/catalog/rooms/${roomId}`);

        if (!response.ok) {
            if (response.status === 404) {
                // Error controlado: El ID no existe en la base de datos
                error(404, {
                    message: 'La habitación que buscas no existe o fue retirada del catálogo.'
                });
            }
            throw new Error(`HTTP ${response.status}`);
        }

        const room: RoomDetail = await response.json();

        return {
            room
        };

    } catch (err) {
        console.error(`[Room Detail Error] ID ${roomId}:`, err);
        
        // Si el error es un HttpError de SvelteKit (como el 404 de arriba), lo dejamos propagar
        if (err && typeof err === 'object' && 'status' in err) {
            throw err;
        }
        
        // Error genérico de infraestructura (ej. Spring Boot apagado)
        error(500, {
            message: 'No se pudo conectar con el servidor del hotel. Intenta nuevamente.'
        });
    }
};