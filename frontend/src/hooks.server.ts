import { redirect, type Handle } from '@sveltejs/kit';

/**
 * El Guardián de Rutas (Route Guard).
 * Esta función se ejecuta en CADA petición que llega al servidor de SvelteKit.
 */
export const handle: Handle = async ({ event, resolve }) => {
    
    // 1. Extraemos la ruta a la que el usuario intenta acceder
    const path = event.url.pathname;

    // 2. Extraemos el Token JWT de las cookies (si existe)
    const token = event.cookies.get('jwt_token');

    // 3. Definimos qué rutas están protegidas
    // En nuestra arquitectura, todo lo que empiece con /dashboard, /reports o /settings es privado
    const isProtectedRoute = path.startsWith('/dashboard') || 
                             path.startsWith('/reports') || 
                             path.startsWith('/settings');

    // 4. LÓGICA DE PROTECCIÓN
    if (isProtectedRoute) {
        // Si intenta entrar a una ruta protegida y NO tiene token, lo pateamos al login
        if (!token) {
            console.log(`[Guard] Acceso denegado a ${path}. Redirigiendo a /login`);
            throw redirect(303, '/login');
        }

        // Opcional: Aquí podríamos decodificar el JWT para verificar si expiró,
        // pero por rendimiento, dejaremos que Spring Boot lo valide cuando hagamos fetch.
        // Si Spring Boot devuelve 401, cerraremos la sesión en ese momento.
    }

    // 5. INYECCIÓN DE DATOS GLOBALES (Locals)
    // Si el usuario tiene token, lo guardamos en 'event.locals' para que cualquier 
    // +page.server.ts pueda usarlo fácilmente al hacer fetch a Spring Boot.
    if (token) {
        event.locals.token = token;
        event.locals.userRole = event.cookies.get('user_role') || 'UNKNOWN';
    }

    // 6. Dejamos que la petición continúe su camino normal
    const response = await resolve(event);
    return response;
};