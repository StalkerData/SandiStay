import { test, expect } from '@playwright/test';

test.describe('E2E-01: Guest Checkout (Happy Path)', () => {

    test('Debe completar una reserva de habitación exitosamente', async ({ page }, testInfo) => {

        // 1. Navegar al catálogo público
        await page.goto('/rooms');
        await expect(page.locator('h1')).toContainText('Nuestras Habitaciones');

        // 2. Seleccionar la primera habitación disponible
        await page.locator('text=Ver Detalles').first().click();
        await expect(page.locator('text=Resumen de Tarifas')).toBeVisible();

        // 3. Clic en "Reservar Ahora"
        await page.locator('text=Reservar Ahora').click();
        await expect(page.locator('h2').filter({ hasText: '1. Fechas de Estadía' })).toBeVisible();

        // ====================================================================
        // PREVENCIÓN DE OVERBOOKING (Fechas Aleatorias)
        // Generamos un salto aleatorio entre 10 y 300 días en el futuro.
        // Esto garantiza que la habitación SIEMPRE esté libre en cada ejecución.
        // ====================================================================
        const randomOffset = Math.floor(Math.random() * 290) + 10;
        const offsetDays = randomOffset + (testInfo.workerIndex * 5);

        const checkIn = new Date();
        checkIn.setDate(checkIn.getDate() + offsetDays);

        const checkOut = new Date();
        checkOut.setDate(checkOut.getDate() + offsetDays + 2); // Reserva de 2 noches

        const checkInDate = checkIn.toISOString().split('T')[0];
        const checkOutDate = checkOut.toISOString().split('T')[0];

        // 4. Llenar formulario de Fechas
        await page.fill('#fechaInicio', checkInDate);
        await page.fill('#fechaFin', checkOutDate);
        await page.fill('#adultos', '2');

        // Llenar formulario de Datos del Huésped
        await page.fill('#nombres', `Usuario ${testInfo.project.name}`);
        await page.fill('#apellidos', 'Playwright');
        await page.selectOption('#idTipoDocumento', '1');
        await page.fill('#documento', `998877${testInfo.workerIndex}${Math.floor(Math.random() * 100)}`); // Documento único
        await page.fill('#email', `qa-${testInfo.project.name}-${Math.floor(Math.random() * 100)}@sandistay.com`);
        await page.fill('#telefono', '3001234567');
        await page.selectOption('#idPais', '1');

        // 5. Continuar al Pago
        await page.click('button[type="submit"]');
        await expect(page.locator('h2').filter({ hasText: '3. Pago Seguro' })).toBeVisible();

        // ====================================================================
        // EXTRACCIÓN DINÁMICA DEL MONTO PARA EL MOCK DE PYTHON
        // ====================================================================
        const totalLocator = page.locator('span.text-xl.text-primary');
        await expect(totalLocator).toBeVisible();

        const totalText = await totalLocator.textContent();
        const amountStr = totalText?.replace(/\D/g, '') || '0';

        console.log(`[${testInfo.project.name}] Intentando reservar por un total de: $${amountStr}`);

        // Llenamos el comprobante con el formato que espera Python
        await page.fill('#comprobante', `EXACTO-${amountStr}`);

        // 6. Enviar la reserva
        await page.click('button:has-text("Verificar Pago y Reservar")');

        // ====================================================================
        // 7. CRITERIO DE ACEPTACIÓN INTELIGENTE
        // Esperamos la redirección de éxito O atrapamos el error del Backend
        // ====================================================================
        await Promise.race([
            page.waitForURL(/\/checkout\/success\?code=RES-/),

            // Si aparece el cuadro rojo de error, abortamos la prueba y mostramos el motivo
            page.waitForSelector('.bg-red-100.text-red-700', { state: 'visible', timeout: 10000 }).then(async () => {
                const errorText = await page.locator('.bg-red-100.text-red-700').textContent();
                throw new Error(`\n❌ EL BACKEND RECHAZÓ LA RESERVA:\nMotivo: ${errorText?.trim()}\n`);
            })
        ]);

        // Si llegamos aquí, la reserva fue un éxito
        await expect(page.locator('h1')).toContainText('¡Reserva Confirmada!');

        const codigoReserva = await page.locator('p[aria-label="Código de reserva"]').textContent();
        expect(codigoReserva).toContain('RES-');

        console.log(`[${testInfo.project.name}] ✅ Reserva exitosa: ${codigoReserva}`);
    });

});
