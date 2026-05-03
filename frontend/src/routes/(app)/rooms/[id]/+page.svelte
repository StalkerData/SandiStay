<script lang="ts">
    import Button from '$lib/components/ui/Button.svelte';
    import type { PageData } from './$types';

    // Recibimos los datos inyectados por el servidor (SSR)
    let { data }: { data: PageData } = $props();
    
    // Usamos $derived para mantener la reactividad en Svelte 5
    let room = $derived(data.room);
</script>

<svelte:head>
    <title>{room.nombre} | SandiStay</title>
</svelte:head>

<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
    
    <!-- Botón de regreso -->
    <a href="/" class="inline-flex items-center text-secondary hover:text-primary font-medium mb-8 transition-colors">
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        Volver al catálogo
    </a>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
        
        <!-- COLUMNA IZQUIERDA: Galería e Información (Ocupa 2/3 del espacio) -->
        <div class="lg:col-span-2 space-y-8">
            
            <!-- Imagen Principal -->
            <div class="rounded-2xl overflow-hidden shadow-lg h-[400px] md:h-[500px]">
                <img 
                    src={`http://localhost:8082/api/v1${room.imagenPrincipal}`} 
                    alt={room.nombre} 
                    class="w-full h-full object-cover hover:scale-105 transition-transform duration-700"
                    onerror={(e) => e.currentTarget.src = 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?q=80&w=1200&auto=format&fit=crop'}
                />
            </div>

            <!-- Título y Etiquetas -->
            <div>
                <div class="flex items-center gap-3 mb-2">
                    <span class="bg-secondary/10 text-secondary px-3 py-1 rounded-full text-sm font-bold tracking-wide uppercase">
                        {room.tipo}
                    </span>
                    <span class="text-neutral font-medium">Habitación {room.numero}</span>
                </div>
                <h1 class="font-serif text-4xl md:text-5xl font-bold text-dark mb-4">{room.nombre}</h1>
                <p class="text-lg text-neutral leading-relaxed">
                    {room.descripcion || 'Disfruta de una estancia inolvidable en esta hermosa habitación, diseñada pensando en tu máximo confort y descanso.'}
                </p>
            </div>

            <!-- Características (Iconos) -->
            <div class="grid grid-cols-2 md:grid-cols-4 gap-6 py-8 border-y border-neutral/20">
                <div class="flex flex-col items-center text-center gap-2">
                    <span class="text-3xl">👤</span>
                    <span class="text-sm font-medium text-dark">{room.capacidadAdultos} Adultos</span>
                </div>
                <div class="flex flex-col items-center text-center gap-2">
                    <span class="text-3xl">👶</span>
                    <span class="text-sm font-medium text-dark">{room.capacidadNinos} Niños</span>
                </div>
                <div class="flex flex-col items-center text-center gap-2">
                    <span class="text-3xl">🛏️</span>
                    <span class="text-sm font-medium text-dark">Cama King</span>
                </div>
                <div class="flex flex-col items-center text-center gap-2">
                    <span class="text-3xl">📶</span>
                    <span class="text-sm font-medium text-dark">Wi-Fi Gratis</span>
                </div>
            </div>
        </div>

        <!-- COLUMNA DERECHA: Tarjeta de Reserva (Sticky) -->
        <div class="lg:col-span-1">
            <div class="bg-surface rounded-2xl shadow-xl p-8 border border-neutral/10 sticky top-24">
                
                <h3 class="font-serif text-2xl font-bold text-dark mb-6">Resumen de Tarifas</h3>
                
                <div class="space-y-4 mb-8">
                    <div class="flex justify-between items-center pb-4 border-b border-neutral/10">
                        <span class="text-neutral">Precio por Noche</span>
                        <span class="font-bold text-xl text-dark">${room.precioNoche.toLocaleString('es-CO')}</span>
                    </div>
                    <div class="flex justify-between items-center pb-4 border-b border-neutral/10">
                        <span class="text-neutral">Precio por Día (Pasadía)</span>
                        <span class="font-bold text-xl text-dark">${room.precioDia.toLocaleString('es-CO')}</span>
                    </div>
                </div>

                <div class="bg-base/50 p-4 rounded-lg mb-8 text-sm text-neutral">
                    <p>💡 <strong>Nota:</strong> El precio final se calculará en el siguiente paso según las fechas seleccionadas.</p>
                </div>

                <!-- Call to Action (CTA) -->
                <!-- Pasamos el ID de la habitación en la URL hacia la página de checkout -->
                <a href={`/checkout/${room.id}?type=Habitacion`} class="block w-full">
                    <Button variant="primary" class="w-full py-4 text-lg shadow-lg shadow-primary/30 hover:shadow-primary/50 transition-shadow">
                        Reservar Ahora
                    </Button>
                </a>
                
                <p class="text-center text-xs text-neutral mt-4">
                    No se cobrará nada en este momento.
                </p>
            </div>
        </div>

    </div>
</div>