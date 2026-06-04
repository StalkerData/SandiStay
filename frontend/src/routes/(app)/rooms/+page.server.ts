import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ fetch }) => {
    try {
        const response = await fetch('http://localhost:8082/api/v1/catalog/rooms');
        if (!response.ok) throw new Error('Error');
        const rooms = await response.json();
        return { rooms };
    } catch (err) {
        error(500, 'No se pudo cargar el catálogo de habitaciones.');
    }
};