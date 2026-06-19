import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import type { Room } from '../../+page.server';
import { PUBLIC_API_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ params, fetch }) => {
    // 1. Extraemos el ID de la URL (ej: si la URL es /rooms/1, params.id será "1")
    const roomId = params.id;

    try {
        // 2. Hacemos la petición a Spring Boot pidiendo esa habitación específica
        const response = await fetch(`${PUBLIC_API_URL}/catalog/rooms/${roomId}`);

        // 3. Manejo de errores HTTP
        if (!response.ok) {
            if (response.status === 404) {
                // Si Spring Boot dice que no existe, lanzamos un error 404 de SvelteKit
                // Esto mostrará automáticamente la página de error 404 de Svelte
                error(404, {
                    message: 'La habitación que buscas no existe o no está disponible.'
                });
            }
            
            // Para otros errores (500, etc.)
            console.error(`Error del Backend: ${response.status}`);
            error(500, {
                message: 'Ocurrió un error al consultar los detalles de la habitación.'
            });
        }

        // 4. Convertimos el JSON a nuestro objeto Room
        const room: Room = await response.json();

        // 5. Retornamos los datos a la página (+page.svelte)
        return {
            room
        };

    } catch (err) {
        // 6. Manejo de errores de red (Spring Boot apagado)
        console.error('Error de conexión con el Backend:', err);
        
        // Si el error ya es un HttpError de SvelteKit (lanzado arriba), lo dejamos pasar
        if (err && typeof err === 'object' && 'status' in err) {
            throw err;
        }
        
        error(500, {
            message: 'No se pudo conectar con el servidor del hotel.'
        });
    }
};