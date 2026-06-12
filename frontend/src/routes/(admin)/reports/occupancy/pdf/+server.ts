import { error } from '@sveltejs/kit';
import type { RequestHandler } from './$types';

export const GET: RequestHandler = async ({ url, locals, fetch }) => {
    const token = locals.token;
    if (!token) error(401, 'No autorizado');

    const start = url.searchParams.get('start');
    const end = url.searchParams.get('end');

    if (!start || !end) error(400, 'Faltan fechas');

    try {
        const response = await fetch(`http://localhost:8082/api/v1/reports/occupancy/pdf?start=${start}&end=${end}`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Error al generar el reporte en el Backend');

        const pdfBlob = await response.blob();

        return new Response(pdfBlob, {
            headers: {
                'Content-Type': 'application/pdf',
                'Content-Disposition': `inline; filename="reporte-ocupacion-${start}.pdf"`
            }
        });
    } catch (err) {
        console.error('Error Proxy PDF Ocupación:', err);
        error(500, 'No se pudo generar el documento PDF.');
    }
};