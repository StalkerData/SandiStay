import { error, fail } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

// --- INTERFACES (DTOs) ---
export interface UserViewDTO {
    idUsuario: number;
    username: string;
    nombreCompleto: string;
    email: string;
    rol: string;
    usuarioActivo: boolean;
}

export const load: PageServerLoad = async ({ fetch, locals }) => {
    // 1. BARRERA DE SEGURIDAD (Zero Trust)
    if (locals.user?.role !== 'ADMIN') {
        error(403, 'Acceso denegado. Solo los administradores pueden gestionar usuarios.');
    }

    try {
        const response = await fetch(`${PUBLIC_API_URL}/users`, {
            headers: { 'Authorization': `Bearer ${locals.token}` }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP Error ${response.status}`);
        }
        
        const users: UserViewDTO[] = await response.json();
        return { users };
    } catch (err) {
        console.error('[Users Load Error]', err);
        error(500, 'No se pudo cargar la lista de usuarios.');
    }
};

export const actions: Actions = {
    
    /**
     * @action createUser
     * @description Crea un nuevo usuario operativo (STAFF).
     */
    createUser: async ({ request, locals, fetch }) => {
        if (locals.user?.role !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        
        const data = await request.formData();

        const payload = {
            nombres: data.get('nombres')?.toString(),
            apellidos: data.get('apellidos')?.toString(),
            idTipoDocumento: parseInt(data.get('idTipoDocumento')?.toString() || '1'),
            numeroDocumento: data.get('numeroDocumento')?.toString(),
            email: data.get('email')?.toString(),
            idPais: parseInt(data.get('idPais')?.toString() || '1'),
            username: data.get('username')?.toString(),
            password: data.get('password')?.toString(),
            rolCodigo: data.get('rolCodigo')?.toString() || 'STAFF'
        };

        try {
            const response = await fetch(`${PUBLIC_API_URL}/users`, {
                method: 'POST',
                headers: { 
                    'Authorization': `Bearer ${locals.token}`, 
                    'Content-Type': 'application/json' 
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al crear el usuario.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }
            
            const result = await response.json();
            return { success: true, message: `Usuario ${result.username} creado exitosamente.` };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    /**
     * @action deactivateUser
     * @description Desactiva lógicamente a un usuario (Soft Delete).
     */
    deactivateUser: async ({ request, locals, fetch }) => {
        if (locals.user?.role !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        
        const data = await request.formData();
        const id = data.get('id');

        try {
            const response = await fetch(`${PUBLIC_API_URL}/users/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al desactivar el usuario.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }
            
            return { success: true, message: 'Usuario desactivado correctamente.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};