import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { PUBLIC_API_URL } from '$env/static/public';

/**
 * Interfaz que representa el DTO unificado del Catálogo (Habitación o Área).
 * Mapea con CatalogRoomResponse.java y CatalogAreaResponse.java del Backend.
 */
export interface CatalogItem {
    id?: number;
    idArea?: number;
    nombre: string;
    tipo?: string;
    tipoArea?: string;
    precioNoche?: number;
    precioHora?: number;
    imagenPrincipal: string;
    capacidadMaxima: number;
}

/**
 * @function load
 * @description Ejecuta la carga de datos en el servidor (SSR) antes de renderizar el Checkout.
 * Aplica el patrón BFF (Backend For Frontend) consultando el API Gateway.
 */
export const load: PageServerLoad = async ({ params, fetch, url }) => {
    const itemId = params.id;
    
    // 1. Determinamos el Bounded Context (Habitación o Área)
    const tipoReserva = url.searchParams.get('type') || 'Habitacion';

    try {
        let item: CatalogItem | null = null;

        if (tipoReserva === 'Habitacion') {
            // 2A. Consulta directa al endpoint de Habitaciones
            const response = await fetch(`${PUBLIC_API_URL}/catalog/rooms/${itemId}`);
            
            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status} - Habitación no encontrada`);
            }
            item = await response.json();
            
        } else {
            // 2B. Consulta al catálogo de Áreas (Filtrado en memoria por falta de endpoint GET by ID)
            // TODO (Tech Debt): Crear endpoint GET /catalog/areas/{id} en CatalogController.java
            const response = await fetch(`${PUBLIC_API_URL}/catalog/areas`);
            
            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status} - Áreas no encontradas`);
            }
            
            const areas: CatalogItem[] = await response.json();
            item = areas.find(a => a.idArea?.toString() === itemId) || null;
            
            if (!item) {
                throw new Error('Área no encontrada en el catálogo');
            }
        }

        // 3. Retornamos el DTO tipado a la vista de Svelte
        return { 
            room: item 
        };

    } catch (err) {
        console.error('[SSR Error] Fallo al cargar datos para el checkout:', err);
        // Lanzamos un error 404 controlado que SvelteKit atrapará y mostrará en +error.svelte
        error(404, {
            message: 'El ítem que intentas reservar no está disponible o fue retirado del catálogo.'
        });
    }
};