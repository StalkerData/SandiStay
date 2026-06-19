import { redirect, type Handle, type HandleFetch } from '@sveltejs/kit';
import { env } from '$env/dynamic/private';
import { PUBLIC_API_URL } from '$env/static/public';

export const handle: Handle = async ({ event, resolve }) => {
    const path = event.url.pathname;
    const token = event.cookies.get('jwt_token');
    const isProtectedRoute = path.startsWith('/dashboard') ||
        path.startsWith('/reports') ||
        path.startsWith('/settings');

    if (isProtectedRoute && !token) {
        throw redirect(303, '/login');
    }

    if (token) {
        event.locals.token = token;
        event.locals.userRole = event.cookies.get('user_role') || 'UNKNOWN';
    }

    return await resolve(event);
};
export const handleFetch: HandleFetch = async ({ request, fetch }) => {
    const internalHost = env.INTERNAL_API_HOST;

    if (internalHost && request.url.includes('/api/v1')) {
        const url = new URL(request.url);
        const internalUrl = `${internalHost}${url.pathname}${url.search}`;

        const newHeaders = new Headers(request.headers);
        newHeaders.delete('referer');
        newHeaders.delete('host');
        // Fijamos el Origin explícitamente, porque si lo borramos, Node le pone "null" solo.
        newHeaders.set('origin', new URL(PUBLIC_API_URL).origin);

        if (request.method === 'GET' || request.method === 'HEAD') {
            return fetch(internalUrl, { method: request.method, headers: newHeaders });
        }

        return fetch(internalUrl, {
            method: request.method,
            headers: newHeaders,
            body: request.body,
            duplex: 'half'
        } as RequestInit);
    }

    return fetch(request);
};
/*
export const handleFetch: HandleFetch = async ({ request, fetch }) => {
    const internalHost = env.INTERNAL_API_HOST; // ej: "http://backend:8082" — solo existe en Docker

    // Si NO estamos en Docker (no hay INTERNAL_API_HOST), dejamos pasar la petición tal cual
    if (internalHost && request.url.includes('/api/v1')) {
        const url = new URL(request.url);
        const internalUrl = `${internalHost}${url.pathname}${url.search}`;

        const newHeaders = new Headers(request.headers);
        newHeaders.delete('origin');
        newHeaders.delete('referer');
        newHeaders.delete('host');

        console.log('>>> Enviando a backend:', internalUrl);
        console.log('>>> Headers finales:', JSON.stringify([...newHeaders.entries()]));

        if (request.method === 'GET' || request.method === 'HEAD') {
            return fetch(internalUrl, { method: request.method, headers: newHeaders });
        }

        return fetch(internalUrl, {
            method: request.method,
            headers: newHeaders,
            body: request.body,
            duplex: 'half'
        } as RequestInit);
    }

    return fetch(request);
};*/