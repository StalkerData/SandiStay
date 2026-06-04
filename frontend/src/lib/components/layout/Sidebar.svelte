<script lang="ts">
    // Recibimos la ruta actual para resaltar el menú activo
    let { currentPath = '' } : { currentPath?: string } = $props();

    // Lista de menús (Más adelante la filtraremos por Rol)
const menuItems =[
        { name: 'Dashboard', path: '/dashboard', icon: '📊' },
        { name: 'Reservas', path: '/dashboard/bookings', icon: '📅' },
        { name: 'Catálogo', path: '/settings/catalog', icon: '🛏️' }, 
        { name: 'Reportes', path: '/reports', icon: '📈' },
        { name: 'Usuarios', path: '/settings/users', icon: '👥' }
    ];
</script>

<aside class="w-64 bg-dark text-base h-screen flex flex-col sticky top-0">
    <!-- Cabecera del Sidebar -->
    <div class="h-16 flex items-center px-6 border-b border-base/10">
        <span class="font-serif text-xl font-bold text-accent">SandiStay Admin</span>
    </div>

    <!-- Enlaces de Navegación -->
    <nav class="flex-1 py-4 px-3 space-y-1 overflow-y-auto">
        {#each menuItems as item}
            <a 
                href={item.path}
                class="flex items-center gap-3 px-3 py-2 rounded-md transition-colors
                       {currentPath.startsWith(item.path) 
                           ? 'bg-secondary text-white' 
                           : 'text-base/70 hover:bg-base/10 hover:text-white'}"
            >
                <span>{item.icon}</span>
                <span class="font-medium">{item.name}</span>
            </a>
        {/each}
    </nav>

<!-- Pie del Sidebar (Usuario actual y Logout) -->
    <div class="p-4 border-t border-base/10">
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold">
                    A
                </div>
                <div class="flex flex-col">
                    <span class="text-sm font-bold text-white">Admin</span>
                    <span class="text-xs text-base/50">En línea</span>
                </div>
            </div>
            
            <!-- Formulario de Logout usando Server Actions -->
            <form method="POST" action="/dashboard?/logout">
                <button 
                    type="submit" 
                    class="text-base/50 hover:text-primary transition-colors p-2 rounded-md hover:bg-base/10"
                    title="Cerrar Sesión"
                >
                    <!-- Icono de Logout -->
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                </button>
            </form>
        </div>
    </div>
</aside>