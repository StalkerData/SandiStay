<script lang="ts">
    import Sidebar from '$lib/components/layout/Sidebar.svelte';
    import { page } from '$app/stores'; // Para saber en qué ruta estamos
    
    let { children } = $props();
</script>

<div class="flex min-h-screen bg-neutral/5">
    <!-- Inyectamos el menú lateral oscuro -->
    <!-- Le pasamos la ruta actual para que resalte el botón correcto -->
    <Sidebar currentPath={$page.url.pathname} />

    <!-- El área de contenido principal (a la derecha del sidebar) -->
    <div class="flex-1 flex flex-col overflow-hidden">
        
        <!-- Cabecera superior del panel admin (Opcional, para el título de la página) -->
        <header class="h-16 bg-surface border-b border-neutral/10 flex items-center px-8 shadow-sm z-10">
            <h1 class="text-xl font-bold text-secondary">Panel de Control</h1>
        </header>

        <!-- El contenido de la página (Dashboard, Reportes, etc.) -->
        <main class="flex-1 overflow-y-auto p-8">
            {@render children()}
        </main>
    </div>
</div>