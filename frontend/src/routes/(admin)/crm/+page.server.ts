import { fail } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

// --- INTERFACES (DTOs) ---
interface ClientData {
    idCliente: number;
    codigoCliente: string;
    nombreCompleto: string;
    idTipoCliente: number;
}

interface ClientHistory {
    totalReservas: number;
    reservasCompletadas: number;
    reservasCanceladas: number;
    totalGastado: number;
    ultimaReserva: string | null;
    habitacionFavorita: string | null;
}

/**
 * @function load
 * @description Inyecta el rol del usuario en la vista para aplicar RBAC en la UI (ocultar/mostrar controles).
 */
export const load: PageServerLoad = async ({ locals }) => {
    return { userRole: locals.user?.role || 'UNKNOWN' };
};

export const actions: Actions = {
    
    /**
     * @action searchClient
     * @description Busca un cliente por documento y recupera su historial y reservas concurrentemente.
     */
    searchClient: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const documento = data.get('documento')?.toString().trim();

        if (!documento) return fail(400, { error: 'El documento es obligatorio.' });

        try {
            // 1. Buscar datos básicos del cliente
            const resSearch = await fetch(`${PUBLIC_API_URL}/reports/clients/search?documento=${documento}`, {
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!resSearch.ok) {
                return fail(404, { error: 'Cliente no encontrado en la base de datos.' });
            }
            
            const clientData: ClientData = await resSearch.json();

            // 2. Optimización de Latencia: Ejecutar peticiones independientes en paralelo
            const [resHistory, resBookings] = await Promise.all([
                fetch(`${PUBLIC_API_URL}/reports/clients/${clientData.idCliente}/history`, {
                    headers: { 'Authorization': `Bearer ${locals.token}` }
                }),
                fetch(`${PUBLIC_API_URL}/bookings/client/${clientData.codigoCliente}`, {
                    headers: { 'Authorization': `Bearer ${locals.token}` }
                })
            ]);

            if (!resHistory.ok) {
                return fail(500, { error: 'Error al cargar el historial analítico del cliente.' });
            }

            const history: ClientHistory = await resHistory.json();
            const clientBookings = resBookings.ok ? await resBookings.json() : [];

            return { 
                success: true, 
                clientName: clientData.nombreCompleto,
                documento: documento,
                idCliente: clientData.idCliente,
                idTipoCliente: clientData.idTipoCliente,
                history,
                clientBookings
            };

        } catch (err) {
            console.error('[CRM Error]', err);
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    },

    /**
     * @action updateCategory
     * @description Actualiza el nivel de fidelización del cliente. Protegido por RBAC (Solo ADMIN).
     */
    updateCategory: async ({ request, locals, fetch }) => {
        // Validación de Seguridad (Defense in Depth)
        if (locals.user?.role !== 'ADMIN') {
            return fail(403, { error: 'No tienes permisos para modificar la categoría de un cliente.' });
        }
        
        const data = await request.formData();
        const idCliente = data.get('idCliente');
        const idTipoCliente = data.get('idTipoCliente');

        try {
            const response = await fetch(`${PUBLIC_API_URL}/reports/clients/${idCliente}/category?idTipoCliente=${idTipoCliente}`, {
                method: 'PATCH',
                headers: { 'Authorization': `Bearer ${locals.token}` }
            });

            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al actualizar la categoría.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }
            
            return { 
                success: true, 
                message: 'Categoría actualizada exitosamente. Vuelve a buscar para ver los cambios reflejados.' 
            };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};