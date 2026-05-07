import { fail, redirect } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';

// 1. Si el usuario ya está logueado, no debería ver la pantalla de login
export const load: PageServerLoad = async ({ cookies }) => {
    const token = cookies.get('jwt_token');
    if (token) {
        throw redirect(303, '/dashboard');
    }
    return {};
};

// 2. Definimos las acciones del servidor (Server Actions)
export const actions: Actions = {
    
    // Esta función se ejecuta cuando el formulario hace POST a "?/login"
    login: async ({ request, cookies, fetch }) => {
        
        // A. Extraemos los datos del formulario HTML
        const data = await request.formData();
        const username = data.get('username');
        const password = data.get('password');

        // Validación básica
        if (!username || !password) {
            return fail(400, { error: 'Usuario y contraseña son obligatorios.' });
        }

        try {
            // B. Hacemos la petición a nuestro Backend en Spring Boot
            const response = await fetch('http://localhost:8082/api/v1/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            // C. Manejo de errores del Backend (Ej: 401 Unauthorized)
            if (!response.ok) {
                const errorData = await response.json();
                return fail(response.status, { 
                    error: errorData.message || 'Credenciales incorrectas. Intenta de nuevo.' 
                });
            }

            // D. Si el login es exitoso, extraemos el Token JWT
            const result = await response.json();
            const token = result.token;

            // E. ¡LA MAGIA DE SEGURIDAD! Guardamos el token en una Cookie HttpOnly
            cookies.set('jwt_token', token, {
                path: '/', // Disponible en toda la aplicación
                httpOnly: true, // Inaccesible para JavaScript (Protección XSS)
                sameSite: 'strict', // Protección CSRF
                secure: process.env.NODE_ENV === 'production', // Solo HTTPS en producción
                maxAge: 60 * 60 * 24 // Expira en 24 horas (en segundos)
            });

            // F. Guardamos el rol del usuario en otra cookie (opcional, para UX rápida)
            // Esta no es HttpOnly para que el frontend pueda leerla y mostrar/ocultar botones
            if (result.roles && result.roles.length > 0) {
                cookies.set('user_role', result.roles[0], {
                    path: '/',
                    httpOnly: false, 
                    sameSite: 'strict',
                    secure: process.env.NODE_ENV === 'production',
                    maxAge: 60 * 60 * 24
                });
            }

        } catch (err) {
            console.error('Error de conexión con el Backend:', err);
            return fail(500, { error: 'Error de conexión con el servidor. Intenta más tarde.' });
        }

        // G. Redirigimos al Dashboard si todo salió bien
        throw redirect(303, '/dashboard');
    }
};