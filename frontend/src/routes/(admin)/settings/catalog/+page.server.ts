import { error, fail } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ fetch, locals }) => {
    const token = locals.token;
    const userRole = locals.user?.role || 'UNKNOWN';

    // Validación Zero Trust
    if (!token) error(401, 'No autorizado');

    try {
        // AHORA USAMOS LOS ENDPOINTS DE ADMIN PARA VER TODO EL INVENTARIO (Incluso inactivos)
        const [resRooms, resAreas] = await Promise.all([
            fetch(`${PUBLIC_API_URL}/catalog/admin/rooms`, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch(`${PUBLIC_API_URL}/catalog/admin/areas`, { headers: { 'Authorization': `Bearer ${token}` } })
        ]);

        if (!resRooms.ok || !resAreas.ok) {
            throw new Error('Error al cargar el catálogo administrativo');
        }

        return {
            rooms: await resRooms.json(),
            areas: await resAreas.json(),
            userRole
        };
    } catch (err) {
        console.error('[Admin Catalog Load Error]', err);
        error(500, 'No se pudo cargar el catálogo.');
    }
};

export const actions: Actions = {
    
    /**
     * @action updateStatus
     * @description Cambia el estado de un activo (Ej: De 'Disponible' a 'Mantenimiento').
     */
    updateStatus: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const id = data.get('id');
        const tipo = data.get('tipo');
        const idEstado = data.get('idEstado');

        const endpoint = tipo === 'room' 
            ? `${PUBLIC_API_URL}/catalog/rooms/${id}/status?idEstadoLugar=${idEstado}`
            : `${PUBLIC_API_URL}/catalog/areas/${id}/status?idEstadoLugar=${idEstado}`;

        try {
            const response = await fetch(endpoint, {
                method: 'PATCH',
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al actualizar el estado.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }
            
            return { success: true, message: 'Estado actualizado correctamente.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    /**
     * @action deleteItem
     * @description Elimina lógicamente o físicamente un activo. Protegido por RBAC.
     */
    deleteItem: async ({ request, locals, fetch }) => {
        if (locals.user?.role !== 'ADMIN') return fail(403, { error: 'No autorizado. Solo ADMIN puede eliminar.' });
        
        const data = await request.formData();
        const id = data.get('id');
        const tipo = data.get('tipo');

        const endpoint = tipo === 'room' 
            ? `${PUBLIC_API_URL}/catalog/rooms/${id}`
            : `${PUBLIC_API_URL}/catalog/areas/${id}`;

        try {
            const response = await fetch(endpoint, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al eliminar el registro.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }
            
            return { success: true, message: 'Registro eliminado correctamente.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    /**
     * @action saveRoom
     * @description Crea o actualiza una habitación.
     */
    saveRoom: async ({ request, locals, fetch }) => {
        if (locals.user?.role !== 'ADMIN') return fail(403, { error: 'No autorizado. Solo ADMIN puede editar.' });
        
        const data = await request.formData();
        const id = data.get('id');

        // Parseo seguro con fallbacks para evitar NaN
        const payload = {
            codigo: data.get('codigo')?.toString(),
            numero: data.get('numero')?.toString(),
            nombre: data.get('nombre')?.toString(),
            idTipoHabitacion: parseInt(data.get('idTipoHabitacion')?.toString() || '1'),
            capacidadAdultos: parseInt(data.get('capacidadAdultos')?.toString() || '1'),
            capacidadNinos: parseInt(data.get('capacidadNinos')?.toString() || '0'),
            capacidadMaxima: parseInt(data.get('capacidadMaxima')?.toString() || '1'),
            precioNoche: parseFloat(data.get('precioNoche')?.toString() || '0'),
            precioDia: parseFloat(data.get('precioDia')?.toString() || '0'),
            descripcion: data.get('descripcion')?.toString()
        };

        const method = id ? 'PUT' : 'POST';
        const url = id ? `${PUBLIC_API_URL}/catalog/rooms/${id}` : `${PUBLIC_API_URL}/catalog/rooms`;

        try {
            const response = await fetch(url, {
                method: method,
                headers: { 
                    'Authorization': `Bearer ${locals.token}`, 
                    'Content-Type': 'application/json' 
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al guardar la habitación.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }
            
            return { success: true, message: 'Habitación guardada exitosamente.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    /**
     * @action saveArea
     * @description Crea o actualiza un área social.
     */
    saveArea: async ({ request, locals, fetch }) => {
        if (locals.user?.role !== 'ADMIN') return fail(403, { error: 'No autorizado. Solo ADMIN puede editar.' });
        
        const data = await request.formData();
        const id = data.get('id');

        const payload = {
            codigo: data.get('codigo')?.toString(),
            nombre: data.get('nombre')?.toString(),
            idTipoArea: parseInt(data.get('idTipoArea')?.toString() || '1'),
            capacidadPersonas: parseInt(data.get('capacidadPersonas')?.toString() || '1'),
            precioHora: parseFloat(data.get('precioHora')?.toString() || '0'),
            descripcion: data.get('descripcion')?.toString()
        };

        const method = id ? 'PUT' : 'POST';
        const url = id ? `${PUBLIC_API_URL}/catalog/areas/${id}` : `${PUBLIC_API_URL}/catalog/areas`;

        try {
            const response = await fetch(url, {
                method: method,
                headers: { 
                    'Authorization': `Bearer ${locals.token}`, 
                    'Content-Type': 'application/json' 
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al guardar el área.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }
            
            return { success: true, message: 'Área guardada exitosamente.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    /**
     * @action uploadImage
     * @description Sube una imagen al servidor (Multipart/form-data).
     */
    uploadImage: async ({ request, locals, fetch }) => {
        if (locals.user?.role !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        
        const data = await request.formData();
        const id = data.get('id');
        const tipo = data.get('tipo');
        const file = data.get('file') as File;

        if (!file || file.size === 0) {
            return fail(400, { error: 'Debe seleccionar una imagen válida.' });
        }

        const formData = new FormData();
        formData.append('file', file);

        const url = tipo === 'room' 
            ? `${PUBLIC_API_URL}/catalog/rooms/${id}/images`
            : `${PUBLIC_API_URL}/catalog/areas/${id}/images`;

        try {
            // NOTA: No seteamos 'Content-Type' para que fetch genere el boundary automáticamente
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${locals.token}` },
                body: formData
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al subir la imagen.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }
            
            return { success: true, message: 'Imagen subida exitosamente.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    /**
     * @action deleteImage
     * @description Elimina una imagen del servidor.
     */
    deleteImage: async ({ request, locals, fetch }) => {
        if (locals.user?.role !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        
        const data = await request.formData();
        const idImagen = data.get('idImagen');
        const tipo = data.get('tipo');

        const url = tipo === 'room' 
            ? `${PUBLIC_API_URL}/catalog/rooms/images/${idImagen}`
            : `${PUBLIC_API_URL}/catalog/areas/images/${idImagen}`;

        try {
            const response = await fetch(url, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al eliminar la imagen.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }
            
            return { success: true, message: 'Imagen eliminada del servidor.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};