import { error } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const GET: RequestHandler = async ({ params, locals, fetch }) => {
    const token = locals.token;
    if (!token) error(401, 'No autorizado');

    // Ahora 'id' en la URL realmente contiene el código alfanumérico (Ej: RES-A25827EA)
    const codigoReserva = params.id;

    try {
        // Hacemos el POST a Spring Boot enviando el nuevo DTO esperado
        const response = await fetch(`${PUBLIC_API_URL}/finance/invoices/generate`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            // CORRECCIÓN: Enviamos 'codigoReserva' como String, sin parseInt
            body: JSON.stringify({ codigoReserva: codigoReserva })
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => null);
            console.error("Error del backend al generar factura:", errData);
            throw new Error('Error al generar la factura en el Backend');
        }

        // Extraemos el binario (Blob) del PDF
        const pdfBlob = await response.blob();

        // Se lo devolvemos al navegador
        return new Response(pdfBlob, {
            headers: {
                'Content-Type': 'application/pdf',
                'Content-Disposition': `inline; filename="factura-${codigoReserva}.pdf"`
            }
        });

    } catch (err) {
        console.error('Error Proxy PDF Factura:', err);
        error(500, 'No se pudo generar el documento PDF.');
    }
};