import { fail } from '@sveltejs/kit';
import type { Actions } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

export const actions: Actions = {
    
    /**
     * @action changePassword
     * @description Permite al usuario autenticado cambiar su propia contraseña.
     */
    changePassword: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const oldPassword = data.get('oldPassword')?.toString();
        const newPassword = data.get('newPassword')?.toString();
        const confirmPassword = data.get('confirmPassword')?.toString();

        // 1. Validación Fail-Fast
        if (!oldPassword || !newPassword || !confirmPassword) {
            return fail(400, { error: 'Todos los campos son obligatorios.' });
        }

        if (newPassword !== confirmPassword) {
            return fail(400, { error: 'Las contraseñas nuevas no coinciden.' });
        }

        try {
            // 2. Petición al Backend
            const response = await fetch(`${PUBLIC_API_URL}/users/me/password`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${locals.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ oldPassword, newPassword })
            });

            // 3. Manejo Resiliente de Errores
            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Error al cambiar la contraseña.';
                if (contentType && contentType.includes("application/json")) {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                }
                return fail(response.status, { error: errorMessage });
            }

            return { 
                success: true, 
                message: 'Contraseña actualizada exitosamente. Usa tu nueva clave en el próximo inicio de sesión.' 
            };
        } catch (err) {
            console.error('[Profile Error]', err);
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};