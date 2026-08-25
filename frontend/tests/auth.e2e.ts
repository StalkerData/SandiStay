import { test, expect } from '@playwright/test';

test.describe('E2E-02: Login y Seguridad (Zero Trust)', () => {

    test('Debe bloquear el acceso a rutas protegidas y redirigir al login', async ({ page }) => {
        // Intentamos entrar a la fuerza a la gestión de usuarios
        await page.goto('/settings/users');

        // El hooks.server.ts debe interceptarnos y mandarnos al login
        await expect(page).toHaveURL(/.*\/login/);
    });

    test('Debe permitir el login al administrador', async ({ page }) => {
        await page.goto('/login');

        // Usamos las credenciales del seed_data.sql
        await page.fill('#username', 'admin');
        await page.fill('#password', 'admin123');

        await page.click('button[type="submit"]');

        // Validamos que entramos al Dashboard
        await expect(page).toHaveURL(/.*\/dashboard/);

        // CORRECCIÓN: Buscamos específicamente el h1 que tiene el texto deseado
        // Esto evita el "strict mode violation"
        await expect(page.locator('h1', { hasText: 'Reservas Recientes' })).toBeVisible();
    });

});
