import { error, fail } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ fetch, locals }) => {
    if (locals.userRole !== 'ADMIN') error(403, 'Acceso denegado.');

    try {
        const response = await fetch('http://localhost:8082/api/v1/users', {
            headers: { 'Authorization': `Bearer ${locals.token}` }
        });
        if (!response.ok) throw new Error('Error al cargar usuarios');
        
        const users = await response.json();
        return { users };
    } catch (err) {
        error(500, 'No se pudo cargar la lista de usuarios.');
    }
};

export const actions: Actions = {
    createUser: async ({ request, locals, fetch }) => {
        if (locals.userRole !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        const data = await request.formData();

        const payload = {
            nombres: data.get('nombres'),
            apellidos: data.get('apellidos'),
            idTipoDocumento: parseInt(data.get('idTipoDocumento') as string),
            numeroDocumento: data.get('numeroDocumento'),
            email: data.get('email'),
            idPais: parseInt(data.get('idPais') as string),
            username: data.get('username'),
            password: data.get('password'),
            rolCodigo: data.get('rolCodigo')
        };

        try {
            const response = await fetch('http://localhost:8082/api/v1/users', {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${locals.token}`, 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errData = await response.json();
                return fail(response.status, { error: errData.message || 'Error al crear el usuario.' });
            }
            const result = await response.json();
            return { success: true, message: `Usuario ${result.username} creado exitosamente.` };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    deactivateUser: async ({ request, locals, fetch }) => {
        if (locals.userRole !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        const data = await request.formData();
        const id = data.get('id');

        const response = await fetch(`http://localhost:8082/api/v1/users/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${locals.token}` }
        });

        if (!response.ok) return fail(response.status, { error: 'Error al desactivar el usuario.' });
        return { success: true, message: 'Usuario desactivado correctamente.' };
    }
};