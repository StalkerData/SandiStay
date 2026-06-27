<script lang="ts">
    import Card from '$lib/components/ui/Card.svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import type { PageData } from './$types';
    import type { CatalogAreaResponse } from './+page.server';
    import { PUBLIC_API_URL } from '$env/static/public';

    // --- PROPS & ESTADO DERIVADO ---
    let { data }: { data: PageData } = $props();
    let areas: CatalogAreaResponse[] = $derived(data.areas);
</script>

<svelte:head>
    <title>Áreas Sociales y Eventos | SandiStay</title>
    <meta name="description" content="Reserva nuestras exclusivas áreas sociales y salones de eventos en SandiStay. Espacios perfectos para tus reuniones y celebraciones." />
    <meta property="og:title" content="Áreas Sociales y Eventos | SandiStay" />
    <meta property="og:description" content="Espacios exclusivos para tus reuniones y momentos de esparcimiento." />
</svelte:head>

<main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
    
    <header class="mb-10 text-center md:text-left">
        <h1 class="font-serif text-4xl md:text-5xl font-bold text-dark mb-4">Áreas Sociales y Eventos</h1>
        <p class="text-neutral text-lg">Espacios exclusivos para tus reuniones y momentos de esparcimiento.</p>
    </header>

    {#if areas.length === 0}
        <!-- EMPTY STATE -->
        <section class="bg-surface border border-neutral/10 rounded-2xl p-12 text-center shadow-sm animate-fade-in">
            <div class="w-20 h-20 bg-neutral/5 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg class="w-10 h-10 text-neutral/50" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
                </svg>
            </div>
            <h2 class="text-2xl font-bold text-dark mb-2">No hay áreas disponibles</h2>
            <p class="text-neutral mb-6 max-w-md mx-auto">
                En este momento estamos actualizando nuestro catálogo de áreas sociales. Por favor, vuelve a revisar más tarde.
            </p>
        </section>
    {:else}
        <!-- GRID DE ÁREAS -->
        <section class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8" aria-label="Lista de áreas sociales">
            {#each areas as area (area.id)}
                <Card>
                    <!-- Svelte 5 Snippet para el Header del Card -->
                    {#snippet header()}
                        <div class="relative overflow-hidden group">
                            <img 
                                src={`${PUBLIC_API_URL}${area.imagenPrincipal}`} 
                                alt={`Fotografía de ${area.nombre}`} 
                                class="w-full h-56 object-cover transition-transform duration-500 group-hover:scale-105"
                                loading="lazy"
                                onerror={(e) => e.currentTarget.src = 'https://via.placeholder.com/800x600?text=Sin+Imagen'}
                            />
                        </div>
                    {/snippet}

                    <!-- Cuerpo del Card -->
                    <div class="mb-2 flex justify-between items-start mt-4">
                        <h3 class="font-serif text-xl font-bold text-dark">{area.nombre}</h3>
                        <span class="bg-secondary/10 text-secondary text-xs px-2.5 py-1 rounded-full font-bold tracking-wide uppercase">
                            {area.tipo}
                        </span>
                    </div>
                    <p class="text-neutral text-sm mb-6 line-clamp-2" title={area.descripcion}>
                        {area.descripcion}
                    </p>
                    
                    <!-- Svelte 5 Snippet para el Footer del Card -->
                    {#snippet footer()}
                        <div class="flex justify-between items-center w-full pt-4 border-t border-neutral/10">
                            <div class="flex flex-col">
                                <span class="text-xs text-neutral uppercase font-bold tracking-wider">Por hora</span>
                                <span class="font-bold text-xl text-primary">${area.precioHora.toLocaleString('es-CO')}</span>
                            </div>
                            <a href={`/areas/${area.id}`} aria-label={`Ver detalles y reservar ${area.nombre}`}>
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