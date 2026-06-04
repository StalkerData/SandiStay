import { error, fail } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ fetch, locals }) => {
    const token = locals.token;
    const userRole = locals.userRole;

    if (!token) error(401, 'No autorizado');

    try {
        // AHORA USAMOS LOS ENDPOINTS DE ADMIN PARA VER TODO EL INVENTARIO
        const [resRooms, resAreas] = await Promise.all([
            fetch('http://localhost:8082/api/v1/catalog/admin/rooms', { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch('http://localhost:8082/api/v1/catalog/admin/areas', { headers: { 'Authorization': `Bearer ${token}` } })
        ]);

        if (!resRooms.ok || !resAreas.ok) throw new Error('Error al cargar el catálogo');

        return {
            rooms: await resRooms.json(),
            areas: await resAreas.json(),
            userRole
        };
    } catch (err) {
        error(500, 'No se pudo cargar el catálogo.');
    }
};

export const actions: Actions = {
    
    updateStatus: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const id = data.get('id');
        const tipo = data.get('tipo');
        const idEstado = data.get('idEstado');

        const endpoint = tipo === 'room' 
            ? `http://localhost:8082/api/v1/catalog/rooms/${id}/status?idEstadoLugar=${idEstado}`
            : `http://localhost:8082/api/v1/catalog/areas/${id}/status?idEstadoLugar=${idEstado}`;

        const response = await fetch(endpoint, {
            method: 'PATCH',
            headers: { 'Authorization': `Bearer ${locals.token}` }
        });

        if (!response.ok) return fail(response.status, { error: 'Error al actualizar el estado.' });
        return { success: true, message: 'Estado actualizado correctamente.' };
    },

    deleteItem: async ({ request, locals, fetch }) => {
        if (locals.userRole !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        const data = await request.formData();
        const id = data.get('id');
        const tipo = data.get('tipo');

        const endpoint = tipo === 'room' 
            ? `http://localhost:8082/api/v1/catalog/rooms/${id}`
            : `http://localhost:8082/api/v1/catalog/areas/${id}`;

        const response = await fetch(endpoint, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${locals.token}` }
        });

        if (!response.ok) return fail(response.status, { error: 'Error al eliminar.' });
        return { success: true, message: 'Registro eliminado correctamente.' };
    },

    saveRoom: async ({ request, locals, fetch }) => {
        if (locals.userRole !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        const data = await request.formData();
        const id = data.get('id');

        const payload = {
            codigo: data.get('codigo'),
            numero: data.get('numero'),
            nombre: data.get('nombre'),
            idTipoHabitacion: parseInt(data.get('idTipoHabitacion') as string),
            capacidadAdultos: parseInt(data.get('capacidadAdultos') as string),
            capacidadNinos: parseInt(data.get('capacidadNinos') as string),
            capacidadMaxima: parseInt(data.get('capacidadMaxima') as string),
            precioNoche: parseFloat(data.get('precioNoche') as string),
            precioDia: parseFloat(data.get('precioDia') as string),
            descripcion: data.get('descripcion')
        };

        const method = id ? 'PUT' : 'POST';
        const url = id ? `http://localhost:8082/api/v1/catalog/rooms/${id}` : `http://localhost:8082/api/v1/catalog/rooms`;

        const response = await fetch(url, {
            method: method,
            headers: { 'Authorization': `Bearer ${locals.token}`, 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) return fail(response.status, { error: 'Error al guardar la habitación.' });
        return { success: true, message: 'Habitación guardada exitosamente.' };
    },

    // NUEVA ACCIÓN: Guardar Área
    saveArea: async ({ request, locals, fetch }) => {
        if (locals.userRole !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        const data = await request.formData();
        const id = data.get('id');

        const payload = {
            codigo: data.get('codigo'),
            nombre: data.get('nombre'),
            idTipoArea: parseInt(data.get('idTipoArea') as string),
            capacidadPersonas: parseInt(data.get('capacidadPersonas') as string),
            precioHora: parseFloat(data.get('precioHora') as string),
            descripcion: data.get('descripcion')
        };

        const method = id ? 'PUT' : 'POST';
        const url = id ? `http://localhost:8082/api/v1/catalog/areas/${id}` : `http://localhost:8082/api/v1/catalog/areas`;

        const response = await fetch(url, {
            method: method,
            headers: { 'Authorization': `Bearer ${locals.token}`, 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) return fail(response.status, { error: 'Error al guardar el área.' });
        return { success: true, message: 'Área guardada exitosamente.' };
    },

    uploadImage: async ({ request, locals, fetch }) => {
        if (locals.userRole !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        const data = await request.formData();
        const id = data.get('id');
        const tipo = data.get('tipo');
        const file = data.get('file') as File;

        if (!file || file.size === 0) return fail(400, { error: 'Debe seleccionar una imagen.' });

        const formData = new FormData();
        formData.append('file', file);

        const url = tipo === 'room' 
            ? `http://localhost:8082/api/v1/catalog/rooms/${id}/images`
            : `http://localhost:8082/api/v1/catalog/areas/${id}/images`;

        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${locals.token}` },
            body: formData
        });

        if (!response.ok) return fail(response.status, { error: 'Error al subir la imagen.' });
        return { success: true, message: 'Imagen subida exitosamente.' };
    },

    // NUEVA ACCIÓN: Borrar Imagen
    deleteImage: async ({ request, locals, fetch }) => {
        if (locals.userRole !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        const data = await request.formData();
        const idImagen = data.get('idImagen');
        const tipo = data.get('tipo');

        const url = tipo === 'room' 
            ? `http://localhost:8082/api/v1/catalog/rooms/images/${idImagen}`
            : `http://localhost:8082/api/v1/catalog/areas/images/${idImagen}`;

        const response = await fetch(url, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${locals.token}` }
        });

        if (!response.ok) return fail(response.status, { error: 'Error al eliminar la imagen.' });
        return { success: true, message: 'Imagen eliminada del servidor.' };
    }
};