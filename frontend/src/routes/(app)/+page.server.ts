import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * @interface FeaturedRoom
 * @description DTO para las habitaciones destacadas en la Landing Page.
 */
export interface FeaturedRoom {
    id: number;
    codigo: string;
    numero: string;
    nombre: string;
    tipo: string;
    capacidadAdultos: number;
    capacidadNinos: number;
    precioNoche: number;
    precioDia: number;
    descripcion: string | null;
    imagenPrincipal: string;
}

export const load: PageServerLoad = async ({ fetch, setHeaders }) => {
    
    // OPTIMIZACIÓN SEO Y RENDIMIENTO (Edge Caching)
    // La Landing Page debe ser ultrarrápida. Cacheamos por 60 segundos.
    setHeaders({
        'Cache-Control': 'public, max-age=60, s-maxage=60'
    });

    try {
        const response = await fetch(`${PUBLIC_API_URL}/catalog/rooms`);

        if (!response.ok) {
            console.error(`[Landing Error] HTTP ${response.status}`);
            // Soft-Fail: Retornamos un error controlado para no romper el Hero Section
            return {
                rooms: [] as FeaturedRoom[],
                error: 'En este momento estamos actualizando nuestro catálogo.'
            };
        }

        const allRooms: FeaturedRoom[] = await response.json();
        
        // Regla de UI: Solo mostramos un máximo de 3 habitaciones en la sección "Destacadas"
        const featuredRooms = allRooms.slice(0, 3);

        return {
            rooms: featuredRooms,
            error: null
        };

    } catch (err) {
        console.error('[Landing Network Error]', err);
        // Soft-Fail: El backend está inaccesible, pero la web pública sigue viva
        return {
            rooms: [] as FeaturedRoom[],
            error: 'No se pudo conectar con el servidor del hotel.'
        };
    }
};