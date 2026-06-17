<script lang="ts">
    import { page } from '$app/stores';

    let { currentPath = '' } : { currentPath?: string } = $props();

    let userRole = $derived($page.data.userRole || 'STAFF');
    let username = $derived($page.data.username || 'Usuario');

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

<aside class="w-64 bg-dark text-base h-screen flex flex-col sticky top-0">
    <div class="h-16 flex items-center px-6 border-b border-base/10">
        <span class="font-serif text-xl font-bold text-accent">SandiStay Admin</span>
    </div>

    <nav class="flex-1 py-4 px-3 space-y-1 overflow-y-auto">
        {#each menuItems as item}
            <a 
                href={item.path}
                class="flex items-center gap-3 px-3 py-2 rounded-md transition-colors
                       {isActive(item.path, currentPath) 
                           ? 'bg-secondary text-white' 
                           : 'text-base/70 hover:bg-base/10 hover:text-white'}"
            >
                <span>{item.icon}</span>
                <span class="font-medium">{item.name}</span>
            </a>
        {/each}
    </nav>

    <div class="p-4 border-t border-base/10">
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold uppercase">
                    {username.charAt(0)}
                </div>
                <div class="flex flex-col">
                    <span class="text-sm font-bold text-white capitalize">{username}</span>
                    <span class="text-xs text-base/50">{userRole}</span>
                </div>
            </div>
            
            <form method="POST" action="/dashboard?/logout">
                <button type="submit" class="text-base/50 hover:text-primary transition-colors p-2 rounded-md hover:bg-base/10" title="Cerrar Sesión">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                    </svg>
                </button>
            </form>
        </div>
    </div>
</aside>