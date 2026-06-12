import { fail } from '@sveltejs/kit';
import type { Actions } from './$types';

export const actions: Actions = {
    changePassword: async ({ request, locals, fetch }) => {
        const data = await request.formData();
        const oldPassword = data.get('oldPassword');
        const newPassword = data.get('newPassword');
        const confirmPassword = data.get('confirmPassword');

        if (newPassword !== confirmPassword) {
            return fail(400, { error: 'Las contraseñas nuevas no coinciden.' });
        }

        try {
            const response = await fetch('http://localhost:8082/api/v1/users/me/password', {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${locals.token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ oldPassword, newPassword })
            });

            if (!response.ok) {
                const errData = await response.json();
                return fail(response.status, { error: errData.message || 'Error al cambiar la contraseña.' });
            }

            return { success: true, message: 'Contraseña actualizada exitosamente. Usa tu nueva clave en el próximo login.' };
        } catch (err) {
            return fail(500, { error: 'Error de conexión con el servidor.' });
        }
    }
};