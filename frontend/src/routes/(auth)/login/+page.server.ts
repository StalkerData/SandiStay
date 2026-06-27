import { fail, redirect } from '@sveltejs/kit';
import type { Actions, PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * Interfaz que mapea la respuesta de AuthResponse.java del Backend
 */
interface AuthResponse {
    token: string;
    roles: string[];
    message?: string;
}

/**
 * @function load
 * @description Middleware de ruta. Si el usuario ya tiene un token válido, 
 * se le prohíbe ver el login y se le redirige al Dashboard (Zero Trust).
 */
export const load: PageServerLoad = async ({ cookies }) => {
    const token = cookies.get('jwt_token');
    if (token) {
        throw redirect(303, '/dashboard');
    }
    return {};
};

/**
 * @description Server Actions para manejar la mutación de datos de forma segura en el servidor.
 */
export const actions: Actions = {
    
    login: async ({ request, cookies, fetch }) => {
        
        // 1. Extracción de datos del FormData
        const data = await request.formData();
        const username = data.get('username')?.toString();
        const password = data.get('password')?.toString();
        const rememberMe = data.get('remember-me') === 'on'; // Capturamos el checkbox

        // 2. Validación de entrada (Fail-fast)
        if (!username || !password) {
            return fail(400, { error: 'Usuario y contraseña son obligatorios.' });
        }

        try {
            // 3. Petición al Backend (Spring Boot)
            const response = await fetch(`${PUBLIC_API_URL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            // 4. Manejo de Errores Resiliente
            if (!response.ok) {
                const contentType = response.headers.get("content-type");
                let errorMessage = 'Credenciales incorrectas. Intenta de nuevo.';
                
                if (contentType && contentType.includes("application/json")) {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorMessage;
                }
                
                return fail(response.status, { error: errorMessage });
            }

            // 5. Extracción del Token
            const result: AuthResponse = await response.json();
            const token = result.token;

            // 6. Configuración de Seguridad de Cookies (Security by Design)
            // Si "Recordarme" está activo, dura 7 días. Si no, es una cookie de sesión (se borra al cerrar el navegador).
            const maxAge = rememberMe ? 60 * 60 * 24 * 7 : undefined;

            cookies.set('jwt_token', token, {
                path: '/', 
                httpOnly: true, // Mitiga XSS: Inaccesible para document.cookie
                sameSite: 'strict', // Mitiga CSRF
                secure: process.env.NODE_ENV === 'production', // Solo HTTPS en Prod
                maxAge: maxAge 
            });

            // 7. Cookie auxiliar para UI (No sensible)
            if (result.roles && result.roles.length > 0) {
                cookies.set('user_role', result.roles[0], {
                    path: '/',
                    httpOnly: false, // Accesible para Svelte en el cliente
                    sameSite: 'strict',
                    secure: process.env.NODE_ENV === 'production',
                    maxAge: maxAge
                });
            }

        } catch (err) {
            console.error('[Auth Error] Fallo en la conexión con el Backend:', err);
            return fail(500, { error: 'Error de conexión con el servidor. Intenta más tarde.' });
        }

        // 8. Redirección post-login exitoso
        throw redirect(303, '/dashboard');
    }
};