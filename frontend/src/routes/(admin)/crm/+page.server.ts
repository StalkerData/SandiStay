import { fail } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

// NUEVO: Pasamos el rol del usuario a la vista para ocultar/mostrar el selector
export const load: PageServerLoad = async ({ locals }) => {
    return { userRole: locals.userRole };
};

export const actions: Actions = {
    searchClient: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const documento = data.get('documento');

        if (!documento) return fail(400, { error: 'El documento es obligatorio.' });

        try {
            const resSearch = await fetch(`${PUBLIC_API_URL}/reports/clients/search?documento=${documento}`, {
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!resSearch.ok) return fail(404, { error: 'Cliente no encontrado en la base de datos.' });
            const clientData = await resSearch.json();

            const resHistory = await fetch(`${PUBLIC_API_URL}/reports/clients/${clientData.idCliente}/history`, {
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!resHistory.ok) return fail(500, { error: 'Error al cargar el historial del cliente.' });
            const history = await resHistory.json();

            const resBookings = await fetch(`${PUBLIC_API_URL}/bookings/client/${clientData.codigoCliente}`, {
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });
            const clientBookings = resBookings.ok ? await resBookings.json() : [];

            return { 
                success: true, 
                clientName: clientData.nombreCompleto,
                documento: documento,
                idCliente: clientData.idCliente,
                idTipoCliente: clientData.idTipoCliente, // Pasamos la categoría actual
                history,
                clientBookings
            };

        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    // NUEVA ACCIÓN: Cambiar categoría (Solo Admin)
    updateCategory: async ({ request, locals, fetch }) => {
        if (locals.userRole !== 'ADMIN') return fail(403, { error: 'No autorizado' });
        
        const data = await request.formData();
        const idCliente = data.get('idCliente');
        const idTipoCliente = data.get('idTipoCliente');

        try {
            const response = await fetch(`${PUBLIC_API_URL}/reports/clients/${idCliente}/category?idTipoCliente=${idTipoCliente}`, {
                method: 'PATCH',
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!response.ok) return fail(response.status, { error: 'Error al actualizar la categoría.' });
            
            return { success: true, message: 'Categoría de cliente actualizada exitosamente. Vuelve a buscar para ver los cambios.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};