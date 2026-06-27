<script lang="ts">
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import Card from '$lib/components/ui/Card.svelte';
    import { goto } from '$app/navigation'; 
    import type { PageData } from './$types';
    import { PUBLIC_API_URL } from '$env/static/public';

    // --- PROPS & ESTADO DERIVADO ---
    let { data }: { data: PageData } = $props();
    
    // --- ESTADO DEL BUSCADOR ---
    // Calculamos la fecha de hoy en formato YYYY-MM-DD para restringir inputs pasados
    const today = new Date().toISOString().split('T')[0];
    
    let checkIn = $state('');
    let checkOut = $state('');
    let huespedes = $state(2);

    // Derivamos la fecha mínima de checkout (debe ser al menos el día de check-in)
    let minCheckOut = $derived(checkIn ? checkIn : today);

    /**
     * @description Redirige al catálogo público inyectando los parámetros de búsqueda.
     */
    function handleSearch(e: Event) {
        e.preventDefault();
        goto(`/rooms?fechaInicio=${checkIn}&fechaFin=${checkOut}&adultos=${huespedes}`);
    }
</script>

<svelte:head>
    <!-- SEO Crítico para la Landing Page -->
    <title>SandiStay | Lujo, Confort y Experiencias Inolvidables</title>
    <meta name="description" content="Reserva tu estadía en SandiStay. Habitaciones de lujo, áreas sociales exclusivas y el mejor servicio para tus vacaciones o viajes de negocios." />
    <meta property="og:title" content="SandiStay | Tu refugio perfecto" />
    <meta property="og:description" content="Experiencias inolvidables en habitaciones de lujo y áreas sociales exclusivas." />
    <meta property="og:type" content="website" />
</svelte:head>

<main>
    <!-- ========================================== -->
    <!-- HERO SECTION (Optimizado para LCP)         -->
    <!-- ========================================== -->
    <section class="relative h-[70vh] min-h-[500px] flex items-center justify-center" aria-label="Bienvenida a SandiStay">
        <div class="absolute inset-0 z-0 bg-dark">
            <!-- TODO: Reemplazar con la fotografía oficial de la fachada del hotel -->
            <img 
                src="https://plus.unsplash.com/premium_photo-1681422570054-9ae5b8b03e46?q=80&w=1332&auto=format&fit=crop" 
                alt="Vista panorámica del Hotel SandiStay" 
                class="w-full h-full object-cover opacity-60"
                fetchpriority="high"
                loading="eager"
            />
            <div class="absolute inset-0 bg-gradient-to-b from-dark/40 to-dark/80"></div>
        </div>

        <div class="relative z-10 text-center px-4 max-w-4xl mx-auto mt-[-50px] animate-fade-in">
            <h1 class="font-serif text-5xl md:text-6xl lg:text-7xl font-bold text-white mb-6 drop-shadow-lg tracking-tight">
                Descubre tu refugio perfecto
            </h1>
            <p class="text-lg md:text-xl text-white/90 mb-8 font-sans max-w-2xl mx-auto font-medium drop-shadow-md">
                Experiencias inolvidables en habitaciones de lujo y áreas sociales exclusivas.
            </p>
        </div>
    </section>

    <!-- ========================================== -->
    <!-- SEARCH BAR (Buscador Flotante)             -->
    <!-- ========================================== -->
    <section class="relative z-20 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mt-[-60px]" aria-label="Buscador de disponibilidad">
        <div class="bg-surface rounded-xl shadow-2xl p-6 border border-neutral/10 backdrop-blur-sm bg-white/95">
            <form onsubmit={handleSearch} class="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
                <Input 
                    id="checkin" 
                    label="Check-in" 
                    type="date" 
                    min={today}
                    bind:value={checkIn} 
                    required 
                />
                <Input 
                    id="checkout" 
                    label="Check-out" 
                    type="date" 
                    min={minCheckOut}
                    bind:value={checkOut} 
                    required 
                />
                <Input 
                    id="huespedes" 
                    label="Huéspedes" 
                    type="number" 
                    min="1" 
                    max="10"
                    bind:value={huespedes} 
                    required 
                />
                <Button type="submit" variant="primary" class="w-full h-[42px] shadow-lg shadow-primary/20 transition-transform active:scale-95">
                    Buscar Disponibilidad
                </Button>
            </form>
        </div>
    </section>

    <!-- ========================================== -->
    <!-- CATÁLOGO DESTACADO                         -->
    <!-- ========================================== -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24" aria-labelledby="featured-title">
        <header class="text-center mb-16">
            <h2 id="featured-title" class="font-serif text-3xl md:text-4xl font-bold text-secondary mb-4">Habitaciones Destacadas</h2>
            <p class="text-neutral max-w-2xl mx-auto text-lg">
                Diseñadas para tu máximo confort. Explora nuestras opciones más populares y reserva tu estadía hoy mismo.
            </p>
        </header>

        <!-- Manejo de Errores del Servidor (Soft-Fail) -->
        {#if data.error}
            <div class="bg-red-50 border border-red-200 text-red-700 px-6 py-8 rounded-xl text-center max-w-2xl mx-auto shadow-sm" role="alert">
                <svg class="w-12 h-12 mx-auto mb-4 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                <p class="font-bold text-lg mb-1">Lo sentimos</p>
                <p>{data.error}</p>
            </div>
        {:else if data.rooms.length === 0}
            <div class="text-center text-neutral py-12 bg-base/50 rounded-xl border border-neutral/10">
                No hay habitaciones disponibles en este momento.
            </div>
        {:else}
            <!-- Grid de Tarjetas -->
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                {#each data.rooms as room (room.id)}
                    <Card>
                        <!-- Snippet del Encabezado (Imagen) -->
                        {#snippet header()}
                            <div class="overflow-hidden group">
                                <img 
                                    src={`${PUBLIC_API_URL}${room.imagenPrincipal}`} 
                                    alt={`Fotografía de ${room.nombre}`} 
                                    class="w-full h-64 object-cover transition-transform duration-700 group-hover:scale-105"
                                    loading="lazy"
                                    onerror={(e) => e.currentTarget.src = 'https://via.placeholder.com/800x600?text=Sin+Imagen'}
                                />
                            </div>
                        {/snippet}

                        <!-- Cuerpo de la Tarjeta -->
                        <div class="mb-3 flex justify-between items-start mt-2">
                            <h3 class="font-serif text-xl font-bold text-dark">{room.nombre}</h3>
                            <span class="bg-secondary/10 text-secondary text-xs px-2.5 py-1 rounded-full font-bold tracking-wide">
                                {room.tipo}
                            </span>
                        </div>
                        
                        <p class="text-neutral text-sm mb-6 line-clamp-2" title={room.descripcion || ''}>
                            {room.descripcion || 'Una hermosa habitación equipada con todo lo necesario para tu descanso.'}
                        </p>
                        
                        <div class="flex gap-4 text-sm text-dark font-medium mb-2">
                            <span class="flex items-center gap-1" aria-label={`${room.capacidadAdultos} Adultos`}><span aria-hidden="true">👤</span> {room.capacidadAdultos}</span>
                            {#if room.capacidadNinos > 0}
                                <span class="flex items-center gap-1" aria-label={`${room.capacidadNinos} Niños`}><span aria-hidden="true">👶</span> {room.capacidadNinos}</span>
                            {/if}
                        </div>

                        <!-- Snippet del Pie de Página (Precio y Botón) -->
                        {#snippet footer()}
                            <div class="flex justify-between items-center w-full pt-4 border-t border-neutral/10">
                                <div class="flex flex-col">
                                    <span class="text-xs text-neutral uppercase font-bold tracking-wider">Desde</span>
                                    <span class="font-bold text-xl text-primary">
                                        ${room.precioNoche.toLocaleString('es-CO')} <span class="text-sm font-normal text-neutral">/ noche</span>
                                    </span>
                                </div>
                                <a href={`/rooms/${room.id}`} aria-label={`Ver detalles de ${room.nombre}`}>
                                    <Button variant="outline" class="transition-colors hover:bg-primary hover:text-white hover:border-primary">
                                        Ver Detalles
                                    </Button>
                                </a>
                            </div>
                        {/snippet}
                    </Card>
                {/each}
            </div>
            
            <div class="text-center mt-12">
                <a href="/rooms">
                    <Button variant="outline" class="px-8 py-3 text-lg">Ver todo el catálogo</Button>
                </a>
            </div>
        {/if}
    </section>
</main>