<script lang="ts">
	import { page } from '$app/stores';
	import Button from '$lib/components/ui/Button.svelte';

	// Estado reactivo para el menú móvil
	let isMobileMenuOpen = $state(false);
	
	// Derivamos la ruta actual para resaltar el enlace activo
	let currentPath = $derived($page.url.pathname);

	function toggleMobileMenu() {
		isMobileMenuOpen = !isMobileMenuOpen;
	}
</script>

<nav class="sticky top-0 z-50 bg-surface shadow-sm border-b border-neutral/10" aria-label="Navegación principal">
	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
		<div class="flex h-16 items-center justify-between">
			
			<!-- Logo y Nombre del Hotel -->
			<div class="flex flex-shrink-0 items-center gap-2">
				<div class="flex h-8 w-8 items-center justify-center rounded-md bg-secondary font-serif font-bold text-white shadow-sm" aria-hidden="true">
					S
				</div>
				<a href="/" class="font-serif text-2xl font-bold text-dark hover:text-primary transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 rounded-md"> 
					SandiStay 
				</a>
			</div>

			<!-- Enlaces de Navegación (Desktop) -->
			<div class="hidden items-center space-x-8 md:flex">
				<a href="/" class="font-medium transition-colors hover:text-secondary {currentPath === '/' ? 'text-primary font-bold' : 'text-neutral'}" aria-current={currentPath === '/' ? 'page' : undefined}>
					Inicio
				</a>
				<a href="/rooms" class="font-medium transition-colors hover:text-secondary {currentPath.startsWith('/rooms') ? 'text-primary font-bold' : 'text-neutral'}" aria-current={currentPath.startsWith('/rooms') ? 'page' : undefined}>
					Habitaciones
				</a>
				<a href="/areas" class="font-medium transition-colors hover:text-secondary {currentPath.startsWith('/areas') ? 'text-primary font-bold' : 'text-neutral'}" aria-current={currentPath.startsWith('/areas') ? 'page' : undefined}>
					Áreas Sociales
				</a>
			</div>

			<!-- Botón Menú Móvil (Hamburguesa) -->
			<div class="flex items-center md:hidden">
				<button 
					type="button"
					class="text-neutral hover:text-secondary focus:outline-none focus:ring-2 focus:ring-primary rounded-md p-2 transition-colors" 
					onclick={toggleMobileMenu}
					aria-expanded={isMobileMenuOpen}
					aria-controls="mobile-menu"
					aria-label="Abrir menú principal"
				>
					<svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
						{#if isMobileMenuOpen}
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
						{:else}
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
						{/if}
					</svg>
				</button>
			</div>
		</div>
	</div>

	<!-- Menú Desplegable (Móvil) -->
	{#if isMobileMenuOpen}
		<div class="md:hidden border-t border-neutral/10 bg-surface animate-fade-in" id="mobile-menu">
			<div class="space-y-1 px-4 pb-3 pt-2">
				<a href="/" class="block rounded-md px-3 py-2 text-base font-medium {currentPath === '/' ? 'bg-primary/10 text-primary' : 'text-neutral hover:bg-base hover:text-secondary'}" onclick={toggleMobileMenu}>
					Inicio
				</a>
				<a href="/rooms" class="block rounded-md px-3 py-2 text-base font-medium {currentPath.startsWith('/rooms') ? 'bg-primary/10 text-primary' : 'text-neutral hover:bg-base hover:text-secondary'}" onclick={toggleMobileMenu}>
					Habitaciones
				</a>
				<a href="/areas" class="block rounded-md px-3 py-2 text-base font-medium {currentPath.startsWith('/areas') ? 'bg-primary/10 text-primary' : 'text-neutral hover:bg-base hover:text-secondary'}" onclick={toggleMobileMenu}>
					Áreas Sociales
				</a>
			</div>
		</div>
	{/if}
</nav>