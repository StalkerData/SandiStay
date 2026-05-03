import type { PageServerLoad } from './$types';

// Definimos la interfaz (molde) de lo que esperamos recibir de Spring Boot.
// Esto debe coincidir exactamente con el DTO 'CatalogRoomResponse' de Java.
export interface Room {
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

export const load: PageServerLoad = async ({ fetch }) => {
    try {
        // 1. Hacemos la petición HTTP a nuestro Backend en Spring Boot
        // Usamos el fetch especial de SvelteKit que está optimizado para SSR
        const response = await fetch('http://localhost:8082/api/v1/catalog/rooms');

        // 2. Verificamos si la respuesta fue exitosa (Código 200-299)
        if (!response.ok) {
            console.error(`Error del Backend: ${response.status} ${response.statusText}`);
            return {
                rooms: [] as Room[],
                error: 'No se pudieron cargar las habitaciones en este momento.'
            };
        }

        // 3. Convertimos el JSON de respuesta a nuestro arreglo de objetos Room
        const rooms: Room[] = await response.json();

        // 4. Retornamos los datos. 
        // SvelteKit inyectará este objeto automáticamente en la variable 'data' de +page.svelte
        return {
            rooms,
            error: null
        };

    } catch (err) {
        // 5. Manejo de errores de red (Ej: Spring Boot está apagado)
        console.error('Error de conexión con el Backend:', err);
        return {
            rooms:[] as Room[],
            error: 'Error de conexión con el servidor.'
        };
    }
};