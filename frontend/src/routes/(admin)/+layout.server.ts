import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = async ({ locals }) => {
    // Pasamos el rol y el username (que podríamos extraer del JWT o de una cookie)
    // a todas las páginas que usen este layout.
    return {
        userRole: locals.userRole,
        // TODO: En un sistema real, decodificaríamos el JWT en hooks.server.ts para sacar el username.
        // Por ahora, usamos un valor genérico o lo leemos de una cookie si la creamos en el login.
        username: locals.userRole === 'ADMIN' ? 'Admin' : 'Staff' 
    };
};