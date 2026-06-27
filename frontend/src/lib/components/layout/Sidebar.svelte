<script lang="ts">
    import { page } from '$app/stores';

    let { currentPath = '' } : { currentPath?: string } = $props();

    // CRÍTICO: Actualizado para coincidir con la estructura de locals.user del hooks.server.ts
    let userRole = $derived($page.data.user?.role || 'STAFF');
    let username = $derived($page.data.user?.username || 'Usuario');

    const baseMenuItems = [
        { name: 'Reservas', path: '/dashboard', icon: '📅' },
        { name: 'Catálogo', path: '/settings/catalog', icon: '🛏️' },
        { name: 'CRM Clientes', path: '/crm', icon: '🤝' }, 
        { name: 'Reportes', path: '/reports', icon: '📈' },
        { name: 'Mi Perfil', path: '/settings/profile', icon: '👤' }
    ];

    const adminMenuItems = [
        { name: 'Tesorería', path: '/finance/treasury', icon: '💰' },
        { name: 'Usuarios', path: '/settings/users', icon: '👥' }
    ];

    let menuItems = $derived(userRole === 'ADMIN' ? [...baseMenuItems, ...adminMenuItems] : baseMenuItems);
    
    function isActive(itemPath: string, current: string) {
        if (itemPath === '/dashboard') {
            return current === '/dashboard' || current.startsWith('/dashboard/bookings');
        }
        return current.startsWith(itemPath);
    }
</script>

<aside class="w-64 bg-dark text-base h-screen flex flex-col sticky top-0 shadow-xl z-20" aria-label="Menú lateral administrativo">
    
    <header class="h-16 flex items-center px-6 border-b border-base/10">
        <span class="font-serif text-xl font-bold text-accent tracking-wide">SandiStay Admin</span>
    </header>

    <nav class="flex-1 py-6 px-3 space-y-1.5 overflow-y-auto">
        {#each menuItems as item}
            <a 
                href={item.path}
                class="flex items-center gap-3 px-3 py-2.5 rounded-md transition-all duration-200
                       {isActive(item.path, currentPath) 
                           ? 'bg-secondary text-white shadow-md shadow-secondary/20 font-bold' 
                           : 'text-base/70 hover:bg-base/10 hover:text-white font-medium'}"
                aria-current={isActive(item.path, currentPath) ? 'page' : undefined}
            >
                <span aria-hidden="true" class="text-lg">{item.icon}</span>
                <span>{item.name}</span>
            </a>
        {/each}
    </nav>

    <footer class="p-4 border-t border-base/10 bg-dark/50">
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-3 overflow-hidden">
                <div class="w-9 h-9 rounded-full bg-primary flex items-center justify-center text-white font-bold uppercase shadow-sm flex-shrink-0" aria-hidden="true">
                    {username.charAt(0)}
                </div>
                <div class="flex flex-col truncate">
                    <span class="text-sm font-bold text-white capitalize truncate" title={username}>{username}</span>
                    <span class="text-xs text-base/50 font-medium tracking-wider">{userRole}</span>
                </div>
            </div>
            
            <form method="POST" action="/dashboard?/logout">
                <button type="submit" class="text-base/50 hover:text-red-400 transition-colors p-2 rounded-md hover:bg-red-400/10 focus:outline-none focus:ring-2 focus:ring-red-400" aria-label="Cerrar Sesión" title="Cerrar Sesión">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                </button>
            </form>
        </div>
    </footer>
</aside>