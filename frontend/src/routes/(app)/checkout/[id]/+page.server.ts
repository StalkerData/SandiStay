import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ params, fetch }) => {
    // Extraemos el ID de la URL (ej: /checkout/1 -> id = 1)
    const itemId = params.id;

    try {
        // Hacemos la petición a Spring Boot para traer los datos de la habitación
        const response = await fetch(`http://localhost:8082/api/v1/catalog/rooms/${itemId}`);

        if (!response.ok) {
            error(404, {
                message: 'La habitación que intentas reservar no está disponible.'
            });
        }

        // Convertimos el JSON y lo retornamos a la página
        const room = await response.json();
        
        return {
            room
        };

    } catch (err) {
        console.error('Error al cargar datos para el checkout:', err);
        
        if (err && typeof err === 'object' && 'status' in err) {
            throw err;
        }
        
        error(500, {
            message: 'Error de conexión con el servidor.'
        });
    }
};