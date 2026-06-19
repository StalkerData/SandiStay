import { error } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * Endpoint Proxy (BFF) para descargar el PDF.
 * Recibe un GET del navegador, pero hace un POST a Spring Boot inyectando el Token seguro.
 */
export const GET: RequestHandler = async ({ params, locals, fetch }) => {
    const token = locals.token;
    if (!token) error(401, 'No autorizado');

    const idReserva = params.id;

    try {
        // Hacemos el POST a Spring Boot
        const response = await fetch(`${PUBLIC_API_URL}/finance/invoices/generate`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ idReserva: parseInt(idReserva) })
        });

        if (!response.ok) {
            throw new Error('Error al generar la factura en el Backend');
        }

        // Extraemos el binario (Blob) del PDF
        const pdfBlob = await response.blob();

        // Se lo devolvemos al navegador con las cabeceras correctas para que lo abra
        return new Response(pdfBlob, {
            headers: {
                'Content-Type': 'application/pdf',
                'Content-Disposition': `inline; filename="factura-reserva-${idReserva}.pdf"`
            }
        });

    } catch (err) {
        console.error('Error Proxy PDF:', err);
        error(500, 'No se pudo generar el documento PDF.');
    }
};