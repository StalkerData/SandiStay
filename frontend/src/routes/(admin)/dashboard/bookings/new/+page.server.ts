import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ fetch, locals }) => {
    const token = locals.token;
    if (!token) error(401, 'No autorizado');

    try {
        const [resRooms, resAreas] = await Promise.all([
            fetch(`${PUBLIC_API_URL}/catalog/admin/rooms`, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch(`${PUBLIC_API_URL}/catalog/admin/areas`, { headers: { 'Authorization': `Bearer ${token}` } })
        ]);

        if (!resRooms.ok || !resAreas.ok) throw new Error('Error al cargar el catálogo');

        return {
            rooms: await resRooms.json(),
            areas: await resAreas.json()
        };
    } catch (err) {
        error(500, 'No se pudo cargar el catálogo.');
    }
};