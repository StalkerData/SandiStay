import { test, expect } from '@playwright/test';

test.describe('E2E-03: Gestión Operativa', () => {

    test('Debe cancelar una reserva desde el Dashboard', async ({ page }) => {
        // 1. Login rápido
        await page.goto('/login');
        await page.fill('#username', 'admin');
        await page.fill('#password', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL(/.*\/dashboard/);

        // Esperamos a que la red se calme para asegurar que Svelte ya cargó la interactividad
        await page.waitForLoadState('networkidle');

        // 2. Buscar el primer botón de "Cancelar" en la tabla
        const btnCancelar = page.locator('button:has-text("Cancelar")').first();

        // Validamos si hay reservas para cancelar
        if (await btnCancelar.count() === 0) {
            console.log('No hay reservas pendientes para cancelar. Prueba omitida.');
            return;
        }

        // 3. Llenar el modal de cancelación
        // CORRECCIÓN PARA CHROMIUM: Hacemos clic y esperamos el modal.
        // Si no aparece (porque Svelte estaba cargando), Playwright lo reintenta automáticamente.
        await expect(async () => {
            await btnCancelar.click();
            await expect(page.locator('div[role="dialog"]')).toBeVisible({ timeout: 2000 });
        }).toPass();

        await page.fill('#motivo', 'Cancelación automatizada por QA Playwright');
        await page.click('button:has-text("Confirmar Cancelación")');

        // 4. Validar que el Backend procesó la multa/reembolso y devolvió éxito
        const alertaExito = page.locator('.bg-green-100.text-green-700');
        await expect(alertaExito).toBeVisible({ timeout: 10000 });
        await expect(alertaExito).toContainText('Reserva cancelada');

        console.log('✅ Reserva cancelada exitosamente desde el Dashboard.');
    });

});
