import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ fetch }) => {
    try {
        const response = await fetch('http://localhost:8082/api/v1/catalog/areas');
        if (!response.ok) throw new Error('Error');
        const areas = await response.json();
        return { areas };
    } catch (err) {
        error(500, 'No se pudo cargar el catálogo de áreas.');
    }
};
