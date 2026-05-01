<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		header?: Snippet; // Opcional: Para el título o imagen superior
		children: Snippet; // Obligatorio: El contenido principal de la tarjeta
		footer?: Snippet; // Opcional: Para los botones de acción abajo
		class?: string; // Clases extra de Tailwind
	}

	let { header, children, footer, class: className = '' }: Props = $props();
</script>

<!-- Contenedor principal de la tarjeta -->
<!-- Usamos bg-surface (blanco) para que resalte sobre el bg-base (crema) del body -->
<div
	class="overflow-hidden rounded-lg border border-neutral/10 bg-surface shadow-md transition-shadow hover:shadow-lg {className}"
>
	<!-- Renderizamos el encabezado solo si el desarrollador lo envía -->
	{#if header}
		<div class="border-b border-neutral/10">
			{@render header()}
		</div>
	{/if}

	<!-- Cuerpo principal de la tarjeta -->
	<div class="p-5">
		{@render children()}
	</div>

	<!-- Renderizamos el pie de página solo si el desarrollador lo envía -->
	{#if footer}
		<div
			class="flex items-center justify-end gap-2 border-t border-neutral/10 bg-base/50 px-5 py-3"
		>
			{@render footer()}
		</div>
	{/if}
</div>
