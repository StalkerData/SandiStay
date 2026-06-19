<script lang="ts">
    import Button from '$lib/components/ui/Button.svelte';
    import type { PageData } from './$types';
    import { PUBLIC_API_URL } from '$env/static/public';

    let { data }: { data: PageData } = $props();
    let area = $derived(data.area);
</script>

<svelte:head>
    <title>{area.nombre} | SandiStay</title>
</svelte:head>

<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
    <a href="/areas" class="inline-flex items-center text-secondary hover:text-primary font-medium mb-8 transition-colors">
        ← Volver al catálogo
    </a>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
        <div class="lg:col-span-2 space-y-8">
            <div class="rounded-2xl overflow-hidden shadow-lg h-[400px] md:h-[500px]">
                <img src={`${PUBLIC_API_URL}${area.imagenPrincipal}`} alt={area.nombre} class="w-full h-full object-cover" onerror={(e) => e.currentTarget.src = 'https://via.placeholder.com/800x600?text=Sin+Imagen'} />
            </div>
            <div>
                <div class="flex items-center gap-3 mb-2">
                    <span class="bg-secondary/10 text-secondary px-3 py-1 rounded-full text-sm font-bold uppercase">{area.tipo}</span>
                </div>
                <h1 class="font-serif text-4xl md:text-5xl font-bold text-dark mb-4">{area.nombre}</h1>
                <p class="text-lg text-neutral leading-relaxed">{area.descripcion}</p>
            </div>
            <div class="grid grid-cols-2 gap-6 py-8 border-y border-neutral/20">
                <div class="flex flex-col items-center text-center gap-2">
                    <span class="text-3xl">👥</span>
                    <span class="text-sm font-medium text-dark">Capacidad: {area.capacidadPersonas} Personas</span>
                </div>
            </div>
        </div>

        <div class="lg:col-span-1">
            <div class="bg-surface rounded-2xl shadow-xl p-8 border border-neutral/10 sticky top-24">
                <h3 class="font-serif text-2xl font-bold text-dark mb-6">Resumen de Tarifas</h3>
                <div class="space-y-4 mb-8">
                    <div class="flex justify-between items-center pb-4 border-b border-neutral/10">
                        <span class="text-neutral">Precio por Hora</span>
                        <span class="font-bold text-xl text-dark">${area.precioHora.toLocaleString('es-CO')}</span>
                    </div>
                </div>
                <div class="bg-base/50 p-4 rounded-lg mb-8 text-sm text-neutral">
                    <p>💡 <strong>Nota:</strong> Para reservar áreas sociales solo se requiere un anticipo del 30%.</p>
                </div>
                <a href={`/checkout/${area.id}?type=Area`} class="block w-full">
                    <Button variant="primary" class="w-full py-4 text-lg">Reservar Ahora</Button>
                </a>
            </div>
        </div>
    </div>
</div>