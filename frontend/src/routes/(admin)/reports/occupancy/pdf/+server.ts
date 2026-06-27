import { error } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * @endpoint GET /reports/occupancy/pdf
 * @description Proxy BFF para la descarga del reporte de ocupación en formato PDF.
 */
export const GET: RequestHandler = async ({ url, locals, fetch }) => {
    const token = locals.token;
    
    // Validación de seguridad (Zero Trust)
    if (!token) error(401, 'No autorizado');

    const start = url.searchParams.get('start')?.trim();
    const end = url.searchParams.get('end')?.trim();

    if (!start || !end) {
        error(400, 'Las fechas de inicio y fin son obligatorias para generar el reporte.');
    }

    try {
        const response = await fetch(`${PUBLIC_API_URL}/reports/occupancy/pdf?start=${start}&end=${end}`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                const errData = await response.json();
                console.error("[JasperReports Error]", errData);
            }
            throw new Error(`Error del backend: HTTP ${response.status}`);
        }

        const pdfBlob = await response.blob();

        return new Response(pdfBlob, {
            headers: {
                'Content-Type': 'application/pdf',
                'Content-Disposition': `inline; filename="reporte-ocupacion-${start}-al-${end}.pdf"`
            }
        });
    } catch (err) {
        console.error('[Proxy Error] Generación de Reporte de Ocupación:', err);
        error(500, 'No se pudo generar el documento PDF del reporte.');
    }
};