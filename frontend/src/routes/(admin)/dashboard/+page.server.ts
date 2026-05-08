import type { PageServerLoad } from './$types';
import { error, redirect } from '@sveltejs/kit';

// Definimos la interfaz basada en la vista 'v_reservas_completo' de nuestra BD
export interface ReservaResumen {
    codigoReserva: string;
    tipoReserva: string;
    nombreCliente: string;
    nombreHuesped: string;
    fechaReserva: string;
    estado: string;
    estadoColor: string;
}

export const load: PageServerLoad = async ({ fetch, locals }) => {
    
    // 1. Extraemos el token que nuestro Guardián (hooks.server.ts) guardó en locals
    const token = locals.token;

    if (!token) {
        // Por seguridad extra, si no hay token, lanzamos error (aunque el hook ya debería haberlo evitado)
        error(401, 'No autorizado');
    }

    try {
        // 2. Hacemos la petición a Spring Boot ENVIANDO EL TOKEN en la cabecera
        const response = await fetch('http://localhost:8082/api/v1/bookings', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        // 3. Manejo de errores (Ej: Token expirado)
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                // Si Spring Boot rechaza el token, lanzamos un 401.
                // (En un sistema más avanzado, aquí borraríamos la cookie y redirigiríamos al login)
                error(401, 'Tu sesión ha expirado o no tienes permisos.');
            }
            
            console.error(`Error del Backend: ${response.status}`);
            error(500, 'Error al cargar la lista de reservas.');
        }

        // 4. Convertimos el JSON a nuestro arreglo de reservas
        const reservas: ReservaResumen[] = await response.json();
        // 5. Retornamos los datos a la página (+page.svelte)
        return {
            reservas
        };

    } catch (err) {
        console.error('Error de conexión con el Backend:', err);
        error(500, 'No se pudo conectar con el servidor del hotel.');
    }

};

export const actions = {
    logout: async ({ cookies }) => {
        // 1. Borramos la cookie segura del JWT
        cookies.delete('jwt_token', { path: '/' });
        
        // 2. Borramos la cookie del rol
        cookies.delete('user_role', { path: '/' });

        // 3. Redirigimos al login
        throw redirect(303, '/login');
    }
};