import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const load: PageServerLoad = async ({ params, fetch }) => {
    const areaId = params.id;
    try {
        // TODO: En el backend no hicimos el GET /catalog/areas/{id}. 
        // Por ahora, traemos todas y filtramos en memoria (Deuda técnica menor).
        const response = await fetch(`${PUBLIC_API_URL}/catalog/areas`);
        if (!response.ok) throw new Error('No encontrado');
        
        const areas = await response.json();
        const area = areas.find((a: any) => a.id.toString() === areaId);
        
        if (!area) error(404, 'Área no encontrada');
        return { area };
    } catch (err) {
        error(404, 'El área que buscas no existe.');
    }
};