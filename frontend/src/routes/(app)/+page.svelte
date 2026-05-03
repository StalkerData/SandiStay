<script lang="ts">
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import Card from '$lib/components/ui/Card.svelte';
    import type { PageData } from './$types';

    // 1. Recibimos los datos inyectados por +page.server.ts (SSR)
    let { data }: { data: PageData } = $props();
    
    // 2. Variables de estado reactivas (Runes) para el buscador
    let checkIn = $state('');
    let checkOut = $state('');
    let huespedes = $state(2);

    // Función para manejar la búsqueda (Por ahora solo visual)
    function handleSearch(e: Event) {
        e.preventDefault();
        console.log(`Buscando para ${huespedes} personas del ${checkIn} al ${checkOut}`);
        // TODO: Redirigir a la página de resultados con los filtros en la URL
    }
</script>

<svelte:head>
    <title>SandiStay | Lujo y Comodidad</title>
</svelte:head>

<!-- ========================================== -->
<!-- HERO SECTION (Imagen de fondo y Título)    -->
<!-- ========================================== -->
<section class="relative h-[70vh] min-h-[500px] flex items-center justify-center">
    <!-- Imagen de fondo (Usamos una de Unsplash como placeholder) -->
    <div class="absolute inset-0 z-0">
        <img 
        //! TODO: Cambiar por imagen de hotel
            src="https://plus.unsplash.com/premium_photo-1681422570054-9ae5b8b03e46?q=80&w=1332&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D" 
            alt="Hotel SandiStay" 
            class="w-full h-full object-cover"
        />
        <!-- Overlay oscuro para que el texto blanco resalte -->
        <div class="absolute inset-0 bg-dark/60"></div>
    </div>

    <!-- Contenido del Hero -->
    <div class="relative z-10 text-center px-4 max-w-4xl mx-auto mt-[-50px]">
        <h1 class="font-serif text-5xl md:text-6xl font-bold text-white mb-6 drop-shadow-lg">
            Descubre tu refugio perfecto
        </h1>
        <p class="text-lg md:text-xl text-base/90 mb-8 font-sans">
            Experiencias inolvidables en habitaciones de lujo y áreas sociales exclusivas.
        </p>
    </div>
</section>

<!-- ========================================== -->
<!-- SEARCH BAR (Buscador Flotante)             -->
<!-- ========================================== -->
<section class="relative z-20 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mt-[-60px]">
    <div class="bg-surface rounded-xl shadow-xl p-6 border border-neutral/10">
        <form onsubmit={handleSearch} class="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
            <Input 
                id="checkin" 
                label="Check-in" 
                type="date" 
                bind:value={checkIn} 
                required 
            />
            <Input 
                id="checkout" 
                label="Check-out" 
                type="date" 
                bind:value={checkOut} 
                required 
            />
            <Input 
                id="huespedes" 
                label="Huéspedes" 
                type="number" 
                min="1" 
                bind:value={huespedes} 
                required 
            />
            <Button type="submit" variant="primary" class="w-full h-[42px]">
                Buscar Disponibilidad
            </Button>
        </form>
    </div>
</section>

<!-- ========================================== -->
<!-- CATÁLOGO DESTACADO                         -->
<!-- ========================================== -->
<section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
    <div class="text-center mb-12">
        <h2 class="font-serif text-3xl md:text-4xl font-bold text-secondary mb-4">Habitaciones Destacadas</h2>
        <p class="text-neutral max-w-2xl mx-auto">
            Diseñadas para tu máximo confort. Explora nuestras opciones más populares y reserva tu estadía hoy mismo.
        </p>
    </div>

    <!-- Manejo de Errores del Servidor -->
    {#if data.error}
        <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative text-center">
            {data.error}
        </div>
    {:else if data.rooms.length === 0}
        <div class="text-center text-neutral py-10">
            No hay habitaciones disponibles en este momento.
        </div>
    {:else}
        <!-- Grid de Tarjetas -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {#each data.rooms as room}
                <Card>
                    <!-- Snippet del Encabezado (Imagen) -->
                    {#snippet header()}
                        <img 
                            src={`http://localhost:8082/api/v1${room.imagenPrincipal}`} 
                            alt={room.nombre} 
                            class="w-full h-56 object-cover"
                            onerror={(e) => e.currentTarget.src = 'https://via.placeholder.com/800x600?text=Sin+Imagen'}
                        />
                    {/snippet}

                    <!-- Cuerpo de la Tarjeta -->
                    <div class="mb-2 flex justify-between items-start">
                        <h3 class="font-serif text-xl font-bold text-dark">{room.nombre}</h3>
                        <span class="bg-secondary/10 text-secondary text-xs px-2 py-1 rounded-full font-medium">
                            {room.tipo}
                        </span>
                    </div>
                    
                    <p class="text-neutral text-sm mb-4 line-clamp-2">
                        {room.descripcion || 'Una hermosa habitación equipada con todo lo necesario para tu descanso.'}
                    </p>
                    
                    <div class="flex gap-4 text-sm text-neutral font-medium">
                        <span>👤 {room.capacidadAdultos} Adultos</span>
                        {#if room.capacidadNinos > 0}
                            <span>👶 {room.capacidadNinos} Niños</span>
                        {/if}
                    </div>

                    <!-- Snippet del Pie de Página (Precio y Botón) -->
                    {#snippet footer()}
                        <div class="flex justify-between items-center w-full">
                            <div class="flex flex-col">
                                <span class="text-xs text-neutral">Desde</span>
                                <span class="font-bold text-lg text-primary">
                                    ${room.precioNoche.toLocaleString('es-CO')} <span class="text-sm font-normal text-neutral">/ noche</span>
                                </span>
                            </div>
                            <a href={`/rooms/${room.id}`}>
                                <Button variant="outline">Ver Detalles</Button>
                            </a>
                        </div>
                    {/snippet}
                </Card>
            {/each}
        </div>
    {/if}
</section>