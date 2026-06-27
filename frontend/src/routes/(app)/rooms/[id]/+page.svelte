<script lang="ts">
    import Button from '$lib/components/ui/Button.svelte';
    import type { PageData } from './$types';
    import type { RoomDetail } from './+page.server';
    import { PUBLIC_API_URL } from '$env/static/public';

    // --- PROPS & ESTADO DERIVADO ---
    let { data }: { data: PageData } = $props();
    let room: RoomDetail = $derived(data.room);
</script>

<svelte:head>
    <!-- SEO Dinámico y Open Graph para Redes Sociales -->
    <title>{room.nombre} | SandiStay</title>
    <meta name="description" content={room.descripcion || `Reserva ${room.nombre} en SandiStay. Capacidad para ${room.capacidadMaxima} personas.`} />
    <meta property="og:title" content={`${room.nombre} | SandiStay`} />
    <meta property="og:description" content={`Precio por noche: $${room.precioNoche.toLocaleString('es-CO')}. ¡Reserva ahora!`} />
    <meta property="og:image" content={`${PUBLIC_API_URL}${room.imagenPrincipal}`} />
    <meta property="og:type" content="website" />
</svelte:head>

<main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
    
    <!-- Navegación de regreso (Breadcrumb visual) -->
    <nav aria-label="Breadcrumb" class="mb-8">
        <a href="/rooms" class="inline-flex items-center text-secondary hover:text-primary font-medium transition-colors">
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Volver al catálogo
        </a>
    </nav>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
        
        <!-- COLUMNA IZQUIERDA: Galería e Información -->
        <article class="lg:col-span-2 space-y-8">
            
            <!-- Imagen Principal (Optimizada para LCP) -->
            <figure class="rounded-2xl overflow-hidden shadow-lg h-[400px] md:h-[500px] bg-neutral/10">
                <img 
                    src={`${PUBLIC_API_URL}${room.imagenPrincipal}`} 
                    alt={`Fotografía de ${room.nombre}`} 
                    class="w-full h-full object-cover hover:scale-105 transition-transform duration-700"
                    fetchpriority="high" 
                    loading="eager"
                    onerror={(e) => e.currentTarget.src = 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?q=80&w=1200&auto=format&fit=crop'}
                />
            </figure>

            <!-- Título y Etiquetas -->
            <header>
                <div class="flex items-center gap-3 mb-2">
                    <span class="bg-secondary/10 text-secondary px-3 py-1 rounded-full text-sm font-bold tracking-wide uppercase shadow-sm">
                        {room.tipo}
                    </span>
                    <span class="text-neutral font-medium">Habitación {room.numero}</span>
                </div>
                <h1 class="font-serif text-4xl md:text-5xl font-bold text-dark mb-4">{room.nombre}</h1>
                <p class="text-lg text-neutral leading-relaxed">
                    {room.descripcion || 'Disfruta de una estancia inolvidable en esta hermosa habitación, diseñada pensando en tu máximo confort y descanso.'}
                </p>
            </header>

            <!-- Características (Iconos) -->
            <section class="grid grid-cols-2 md:grid-cols-4 gap-6 py-8 border-y border-neutral/20" aria-label="Características de la habitación">
                <div class="flex flex-col items-center text-center gap-2">
                    <span class="text-3xl" aria-hidden="true">👤</span>
                    <span class="text-sm font-medium text-dark">{room.capacidadAdultos} Adultos</span>
                </div>
                <div class="flex flex-col items-center text-center gap-2">
                    <span class="text-3xl" aria-hidden="true">👶</span>
                    <span class="text-sm font-medium text-dark">{room.capacidadNinos} Niños</span>
                </div>
                <div class="flex flex-col items-center text-center gap-2">
                    <span class="text-3xl" aria-hidden="true">🛏️</span>
                    <span class="text-sm font-medium text-dark">Cama King</span>
                </div>
                <div class="flex flex-col items-center text-center gap-2">
                    <span class="text-3xl" aria-hidden="true">📶</span>
                    <span class="text-sm font-medium text-dark">Wi-Fi Gratis</span>
                </div>
            </section>
        </article>

        <!-- COLUMNA DERECHA: Tarjeta de Reserva (Sticky) -->
        <aside class="lg:col-span-1">
            <div class="bg-surface rounded-2xl shadow-xl p-8 border border-neutral/10 sticky top-24">
                
                <h2 class="font-serif text-2xl font-bold text-dark mb-6">Resumen de Tarifas</h2>
                
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

                <div class="bg-base/50 p-4 rounded-lg mb-8 text-sm text-neutral border border-neutral/10">
                    <p>💡 <strong class="text-dark">Nota:</strong> El precio final se calculará en el siguiente paso según las fechas seleccionadas.</p>
                </div>

                <!-- Call to Action (CTA) -->
                <a href={`/checkout/${room.id}?type=Habitacion`} class="block w-full" aria-label={`Reservar ${room.nombre}`}>
                    <Button variant="primary" class="w-full py-4 text-lg shadow-lg shadow-primary/30 hover:shadow-primary/50 transition-all active:scale-95">
                        Reservar Ahora
                    </Button>
                </a>
                
                <p class="text-center text-xs text-neutral mt-4 flex items-center justify-center gap-1">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                    No se cobrará nada en este paso.
                </p>
            </div>
        </aside>

    </div>
</main>