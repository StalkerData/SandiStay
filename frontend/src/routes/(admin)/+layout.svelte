<script lang="ts">
    import Sidebar from '$lib/components/layout/Sidebar.svelte';
    import { page } from '$app/stores'; 
    import type { LayoutData } from './$types';
    
    // Recibimos los datos inyectados por routes/(admin)/+layout.server.ts
    let { data, children }: { data: LayoutData, children: any } = $props();
    
    // Extraemos el usuario validado por el hooks.server.ts
    let user = $derived(data.user);
</script>

<div class="flex min-h-screen bg-neutral/5">
    
    <!-- Inyectamos el menú lateral oscuro -->
    <Sidebar currentPath={$page.url.pathname} />

    <!-- El área de contenido principal -->
    <div class="flex-1 flex flex-col overflow-hidden relative">
        
        <!-- Cabecera superior del panel admin -->
        <header class="h-16 bg-surface border-b border-neutral/10 flex items-center justify-between px-4 sm:px-8 shadow-sm z-10">
            <div class="flex items-center gap-4">
                <h1 class="text-xl font-bold text-secondary hidden sm:block">Panel de Control</h1>
            </div>
            
            <!-- Perfil del Usuario Logueado (Dinámico) -->
            {#if user}
                <div class="flex items-center gap-3 ml-auto">
                    <div class="text-right hidden sm:block">
                        <p class="text-sm font-bold text-dark leading-none">{user.username}</p>
                        <p class="text-xs text-neutral mt-1 font-medium tracking-wide">{user.role}</p>
                    </div>
                    <!-- Avatar generado con la inicial del usuario -->
                    <div class="h-10 w-10 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center text-primary font-bold shadow-sm" aria-hidden="true">
                        {user.username.charAt(0).toUpperCase()}
                    </div>
                </div>
            {/if}
        </header>

        <!-- El contenido de la página (Dashboard, Reportes, etc.) -->
        <main class="flex-1 overflow-y-auto p-4 sm:p-8">
            {@render children()}
        </main>
    </div>
</div>