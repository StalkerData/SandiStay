import { error } from '@sveltejs/kit';
import type { RequestHandler } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * @endpoint GET /reports/invoice/[id]
 * @description Proxy BFF para la generación y descarga de facturas PDF.
 * Actúa como un puente seguro: recibe un GET del navegador (para poder abrirlo en nueva pestaña)
 * y ejecuta un POST hacia el backend inyectando el JWT de forma segura.
 */
export const GET: RequestHandler = async ({ params, locals, fetch }) => {
    const token = locals.token;
    
    // Validación de seguridad (Zero Trust)
    if (!token) error(401, 'No autorizado');

    const codigoReserva = params.id;

    try {
        // Petición al motor de JasperReports en Spring Boot
        const response = await fetch(`${PUBLIC_API_URL}/finance/invoices/generate`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ codigoReserva })
        });

        if (!response.ok) {
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                const errData = await response.json();
                console.error("[JasperReports Error]", errData);
            }
            throw new Error(`Error del backend: HTTP ${response.status}`);
        }

        // Extraemos el binario del PDF
        const pdfBlob = await response.blob();

        // Retornamos el stream al navegador con las cabeceras correctas
        return new Response(pdfBlob, {
            headers: {
                'Content-Type': 'application/pdf',
                // 'inline' permite que el navegador lo muestre en el visor integrado.
                // Si quisieras forzar la descarga, cambiarías 'inline' por 'attachment'.
                'Content-Disposition': `inline; filename="factura-${codigoReserva}.pdf"`
            }
        });

    } catch (err) {
        console.error('[Proxy Error] Generación de Factura:', err);
        error(500, 'No se pudo generar el documento PDF de la factura.');
    }
};