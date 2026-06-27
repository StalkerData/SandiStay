<script lang="ts">
    import Card from '$lib/components/ui/Card.svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import type { PageData } from './$types';
    import type { CatalogRoomResponse } from './+page.server';
    import { PUBLIC_API_URL } from '$env/static/public';

    // --- PROPS & ESTADO DERIVADO ---
    let { data }: { data: PageData } = $props();
    let rooms: CatalogRoomResponse[] = $derived(data.rooms);
    let hasFilters = $derived(data.hasFilters);
</script>

<svelte:head>
    <title>Habitaciones y Suites | SandiStay</title>
    <meta name="description" content="Descubre nuestras habitaciones y suites de lujo. Reserva tu estadía ideal en SandiStay con los mejores precios garantizados." />
    <meta property="og:title" content="Habitaciones y Suites | SandiStay" />
    <meta property="og:description" content="Encuentra el espacio perfecto para tu descanso en SandiStay." />
</svelte:head>

<main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
    
    <header class="mb-10 text-center md:text-left">
        <h1 class="font-serif text-4xl md:text-5xl font-bold text-dark mb-4">Nuestras Habitaciones</h1>
        <p class="text-neutral text-lg">Encuentra el espacio perfecto para tu descanso y confort.</p>
    </header>

    {#if rooms.length === 0}
        <!-- EMPTY STATE (Manejo de catálogo vacío o sin disponibilidad) -->
        <section class="bg-surface border border-neutral/10 rounded-2xl p-12 text-center shadow-sm animate-fade-in">
            <div class="w-20 h-20 bg-neutral/5 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg class="w-10 h-10 text-neutral/50" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
            </div>
            <h2 class="text-2xl font-bold text-dark mb-2">No hay habitaciones disponibles</h2>
            <p class="text-neutral mb-6 max-w-md mx-auto">
                {#if hasFilters}
                    Lo sentimos, no tenemos habitaciones que coincidan con las fechas o la cantidad de personas seleccionadas. Intenta cambiar tus filtros.
                {:else}
                    En este momento estamos actualizando nuestro catálogo. Por favor, vuelve a revisar más tarde.
                {/if}
            </p>
            {#if hasFilters}
                <a href="/rooms" aria-label="Ver todas las habitaciones sin filtros">
                    <Button variant="primary">Ver todas las habitaciones</Button>
                </a>
            {/if}
        </section>
    {:else}
        <!-- GRID DE HABITACIONES -->
        <section class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8" aria-label="Lista de habitaciones">
            {#each rooms as room (room.id)}
                <Card>
                    <!-- Svelte 5 Snippet para el Header del Card -->
                    {#snippet header()}
                        <div class="relative overflow-hidden group">
                            <img 
                                src={`${PUBLIC_API_URL}${room.imagenPrincipal}`} 
                                alt={`Fotografía de ${room.nombre}`} 
                                class="w-full h-56 object-cover transition-transform duration-500 group-hover:scale-105"
                                loading="lazy"
                                onerror={(e) => e.currentTarget.src = 'https://via.placeholder.com/800x600?text=Sin+Imagen'}
                            />
                            <div class="absolute top-4 right-4">
                                <span class="bg-surface/90 backdrop-blur-sm text-dark text-xs px-3 py-1.5 rounded-full font-bold shadow-sm">
                                    Máx. {room.capacidadMaxima} pers.
                                </span>
                            </div>
                        </div>
                    {/snippet}

                    <!-- Cuerpo del Card -->
                    <div class="mb-2 flex justify-between items-start mt-4">
                        <h3 class="font-serif text-xl font-bold text-dark">{room.nombre}</h3>
                        <span class="bg-secondary/10 text-secondary text-xs px-2.5 py-1 rounded-full font-bold tracking-wide">
                            {room.tipo}
                        </span>
                    </div>
                    <p class="text-neutral text-sm mb-6 line-clamp-2" title={room.descripcion}>
                        {room.descripcion}
                    </p>
                    
                    <!-- Svelte 5 Snippet para el Footer del Card -->
                    {#snippet footer()}
                        <div class="flex justify-between items-center w-full pt-4 border-t border-neutral/10">
                            <div class="flex flex-col">
                                <span class="text-xs text-neutral uppercase font-bold tracking-wider">Por noche</span>
                                <span class="font-bold text-xl text-primary">${room.precioNoche.toLocaleString('es-CO')}</span>
                            </div>
                            <a href={`/rooms/${room.id}`} aria-label={`Ver detalles y reservar ${room.nombre}`}>
                                <Button variant="outline" class="transition-colors hover:bg-primary hover:text-white hover:border-primary">
                                    Ver Detalles
                                </Button>
                            </a>
                        </div>
                    {/snippet}
                </Card>
            {/each}
        </section>
    {/if}
</main>