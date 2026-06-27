import { redirect, type Handle, type HandleFetch } from '@sveltejs/kit';
import { env } from '$env/dynamic/private';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * @description Decodifica el payload de un JWT sin verificar la firma.
 * (La verificación de la firma es responsabilidad exclusiva del Backend en Spring Boot).
 */
function decodeJwtPayload(token: string) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
        );
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error('[Security] Error decodificando JWT:', e);
        return null;
    }
}

/**
 * @function handle
 * @description Middleware global. Intercepta todas las peticiones entrantes al servidor de SvelteKit.
 * Implementa el patrón Zero Trust y Role-Based Access Control (RBAC) a nivel de UI.
 */
export const handle: Handle = async ({ event, resolve }) => {
    const token = event.cookies.get('jwt_token');
    
    // 1. Protegemos dinámicamente cualquier ruta dentro del grupo (admin)
    const isProtectedRoute = event.route.id?.startsWith('/(admin)');

    if (token) {
        const payload = decodeJwtPayload(token);
        
        // 2. Validación de expiración (Shift-Left)
        // Si el token expiró, limpiamos las cookies y forzamos re-login
        if (payload && payload.exp && payload.exp * 1000 < Date.now()) {
            event.cookies.delete('jwt_token', { path: '/' });
            event.cookies.delete('user_role', { path: '/' });
            if (isProtectedRoute) throw redirect(303, '/login?expired=true');
        } else if (payload) {
            // 3. Inyectamos los datos del usuario en 'locals' para que estén disponibles en los +page.server.ts
            event.locals.token = token;
            event.locals.user = {
                username: payload.sub, // 'sub' es el estándar JWT para el username
                role: payload.roles ? payload.roles[0] : 'UNKNOWN'
            };
        }
    }

    // 4. Si la ruta es protegida y no hay usuario válido en locals, bloqueamos el acceso
    if (isProtectedRoute && !event.locals.user) {
        throw redirect(303, '/login');
    }

    return await resolve(event);
};

/**
 * @function handleFetch
 * @description Interceptor de peticiones HTTP salientes desde el servidor (SSR).
 * Resuelve el problema de enrutamiento en redes Docker aislando la red interna de la pública.
 */
export const handleFetch: HandleFetch = async ({ request, fetch }) => {
    const internalHost = env.INTERNAL_API_HOST; // Ej: http://nginx:80 o http://backend:8080

    // Si estamos en SSR y la petición va hacia nuestra API pública...
    if (internalHost && request.url.includes('/api/v1')) {
        const url = new URL(request.url);
        
        // Reescribimos la URL para usar la red interna de Docker
        const internalUrl = `${internalHost}${url.pathname}${url.search}`;

        const newHeaders = new Headers(request.headers);
        
        // Limpiamos cabeceras que podrían causar conflictos de CORS/Host en el API Gateway
        newHeaders.delete('referer');
        newHeaders.delete('host');
        
        // Fijamos el Origin explícitamente para pasar los filtros de Spring Security
        newHeaders.set('origin', new URL(PUBLIC_API_URL).origin);

        // Manejo especial para peticiones con Body en Node.js (requerido por SvelteKit)
        if (request.method !== 'GET' && request.method !== 'HEAD') {
            return fetch(internalUrl, {
                method: request.method,
                headers: newHeaders,
                body: request.body,
                duplex: 'half' // Necesario para streams en Node 18+
            } as RequestInit);
        }

        return fetch(internalUrl, { method: request.method, headers: newHeaders });
    }

    // Si no es una petición a nuestra API, la dejamos pasar tal cual
    return fetch(request);
};