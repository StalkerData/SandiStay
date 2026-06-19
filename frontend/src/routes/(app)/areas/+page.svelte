<script lang="ts">
    import Card from '$lib/components/ui/Card.svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import type { PageData } from './$types';
    import { PUBLIC_API_URL } from '$env/static/public';

    let { data }: { data: PageData } = $props();
    let areas = $derived(data.areas);
</script>

<svelte:head>
    <title>Áreas Sociales | SandiStay</title>
</svelte:head>

<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
    <h1 class="font-serif text-4xl font-bold text-dark mb-4">Áreas Sociales y Eventos</h1>
    <p class="text-neutral mb-10">Espacios exclusivos para tus reuniones y momentos de esparcimiento.</p>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {#each areas as area}
            <Card>
                {#snippet header()}
                    <img 
                        src={`${PUBLIC_API_URL}${area.imagenPrincipal}`} 
                        alt={area.nombre} 
                        class="w-full h-56 object-cover"
                        onerror={(e) => e.currentTarget.src = 'https://via.placeholder.com/800x600?text=Sin+Imagen'}
                    />
                {/snippet}

                <div class="mb-2 flex justify-between items-start">
                    <h3 class="font-serif text-xl font-bold text-dark">{area.nombre}</h3>
                    <span class="bg-secondary/10 text-secondary text-xs px-2 py-1 rounded-full font-medium">{area.tipo}</span>
                </div>
                <p class="text-neutral text-sm mb-4 line-clamp-2">{area.descripcion}</p>
                
               {#snippet footer()}
                    <div class="flex justify-between items-center w-full">
                        <span class="font-bold text-lg text-primary">${area.precioHora.toLocaleString('es-CO')} <span class="text-xs text-neutral">/ hora</span></span>
                        <!-- Cambiamos el botón para que vaya a la página de detalles -->
                        <a href={`/areas/${area.id}`}><Button variant="outline">Ver Detalles</Button></a>
                    </div>
                {/snippet}
            </Card>
        {/each}
    </div>
</div>