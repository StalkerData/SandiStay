<script lang="ts">
	import Button from '$lib/components/ui/Button.svelte';

	// En SvelteKit, usamos la etiqueta <a> normal para navegar.
	// El framework intercepta el clic y hace la navegación sin recargar la página (SPA).
</script>

<nav class="sticky top-0 z-50 bg-surface shadow-sm">
	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
		<div class="flex h-16 items-center justify-between">
			<!-- Logo y Nombre del Hotel -->
			<div class="flex flex-shrink-0 cursor-pointer items-center gap-2">
				<!-- TODO: Reemplazar con el logo real cuando esté en static/assets/ -->
				<div
					class="flex h-8 w-8 items-center justify-center rounded-md bg-secondary font-serif font-bold text-white"
				>
					S
				</div>
				<a href="/" class="font-serif text-2xl font-bold text-dark"> SandiStay </a>
			</div>

			<!-- Enlaces de Navegación (Desktop) -->
			<div class="hidden items-center space-x-8 md:flex">
				<a href="/" class="font-medium text-neutral transition-colors hover:text-secondary"
					>Inicio</a
				>
				<a href="/rooms" class="font-medium text-neutral transition-colors hover:text-secondary"
					>Habitaciones</a
				>
				<a href="/areas" class="font-medium text-neutral transition-colors hover:text-secondary"
					>Áreas Sociales</a
				>
			</div>

			<!-- Botón Menú Móvil (Hamburguesa) - Solo visual por ahora -->
			<div class="flex items-center md:hidden">
				<button class="text-neutral hover:text-secondary focus:outline-none">
					<svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M4 6h16M4 12h16M4 18h16"
						/>
					</svg>
				</button>
			</div>
		</div>
	</div>
</nav>
