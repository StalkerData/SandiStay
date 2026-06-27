import { error, fail } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

// --- INTERFACES ---
interface AdminCatalogItem {
    idHabitacion?: number;
    idArea?: number;
    nombre: string;
    codigo: string;
    precioNoche?: number;
    precioHora?: number;
}

export const load: PageServerLoad = async ({ fetch, locals }) => {
    const token = locals.token;
    if (!token) error(401, 'No autorizado');

    try {
        // Carga concurrente de catálogos para el selector
        const [resRooms, resAreas] = await Promise.all([
            fetch(`${PUBLIC_API_URL}/catalog/admin/rooms`, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch(`${PUBLIC_API_URL}/catalog/admin/areas`, { headers: { 'Authorization': `Bearer ${token}` } })
        ]);

        if (!resRooms.ok || !resAreas.ok) {
            throw new Error('Error al cargar los catálogos internos');
        }

        const rooms: AdminCatalogItem[] = await resRooms.json();
        const areas: AdminCatalogItem[] = await resAreas.json();

        return { rooms, areas };
    } catch (err) {
        console.error('[Admin Catalog Error]', err);
        error(500, 'No se pudo cargar el catálogo para nuevas reservas.');
    }
};

export const actions: Actions = {
    
    /**
     * @action createBooking
     * @description Procesa una reserva manual (Walk-in). 
     * Se ejecuta en el servidor para poder inyectar el Token JWT del empleado y auditar la transacción.
     */
    createBooking: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const tipoReserva = data.get('tipoReserva')?.toString();
        
        // Construcción del DTO
        const payload = {
            tipoReserva: tipoReserva,
            itemId: parseInt(data.get('itemId')?.toString() || '0'),
            fechaInicio: data.get('fechaInicio'),
            fechaFin: tipoReserva === 'Habitacion' ? data.get('fechaFin') : null,
            horaInicio: tipoReserva === 'Area' ? `${data.get('horaInicio')}:00` : null,
            horaFin: tipoReserva === 'Area' ? `${data.get('horaFin')}:00` : null,
            numeroAdultos: parseInt(data.get('numeroAdultos')?.toString() || '1'),
            numeroNinos: tipoReserva === 'Habitacion' ? parseInt(data.get('numeroNinos')?.toString() || '0') : 0,
            nombresHuesped: data.get('nombresHuesped'),
            apellidosHuesped: data.get('apellidosHuesped'),
            idTipoDocumento: parseInt(data.get('idTipoDocumento')?.toString() || '1'),
            documentoHuesped: data.get('documentoHuesped'),
            emailHuesped: data.get('emailHuesped'),
            telefonoHuesped: data.get('telefonoHuesped'),
            idPais: parseInt(data.get('idPais')?.toString() || '1'),
            paymentTxId: 'EFECTIVO' // Bypass del Mock de Pagos
        };

        try {
            const res = await fetch(`${PUBLIC_API_URL}/bookings`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${locals.token}` // <--- AUDITORÍA ASEGURADA
                },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                const contentType = res.headers.get("content-type");
                let errorMessage = 'Error al procesar la reserva.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await res.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(res.status, { error: errorMessage });
            }

            const result = await res.json();
            return { 
                success: true, 
                message: `¡Reserva creada exitosamente! Código: ${result.codigoReserva}` 
            };

        } catch (err) {
            console.error('[Walk-in Booking Error]', err);
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};