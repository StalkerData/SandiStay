import { fail } from '@sveltejs/kit';
import type { Actions } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const actions: Actions = {
    searchClient: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const documento = data.get('documento');

        if (!documento) return fail(400, { error: 'El documento es obligatorio.' });

        try {
            // 1. Buscamos el ID del cliente por su documento
            const resSearch = await fetch(`${PUBLIC_API_URL}/reports/clients/search?documento=${documento}`, {
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!resSearch.ok) {
                return fail(404, { error: 'Cliente no encontrado en la base de datos.' });
            }

            const clientData = await resSearch.json();

            // 2. Con el ID, buscamos su historial CRM
            const resHistory = await fetch(`${PUBLIC_API_URL}/reports/clients/${clientData.idCliente}/history`, {
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!resHistory.ok) {
                return fail(500, { error: 'Error al cargar el historial del cliente.' });
            }

            const history = await resHistory.json();

            // 3. Devolvemos todo a la vista
            return { 
                success: true, 
                clientName: clientData.nombreCompleto,
                documento: documento,
                history 
            };

        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};