import type { LayoutServerLoad } from './$types';

/**
 * @function load
 * @description Carga los datos base para todo el portal administrativo.
 * Al estar en el layout raíz de (admin), estos datos estarán disponibles 
 * en la store $page.data para cualquier componente hijo (Navbar, Sidebar, etc.).
 */
export const load: LayoutServerLoad = async ({ locals }) => {
    
    // locals.user ya fue validado y decodificado por hooks.server.ts
    // Si llegamos aquí, es 100% seguro que locals.user existe gracias al redirect del hook.
    
    return {
        user: locals.user
    };
};