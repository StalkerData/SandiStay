import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ fetch }) => {
    try {
        const response = await fetch(`${PUBLIC_API_URL}/catalog/areas`);
        if (!response.ok) throw new Error('Error');
        const areas = await response.json();
        return { areas };
    } catch (err) {
        error(500, 'No se pudo cargar el catálogo de áreas.');
    }
};
