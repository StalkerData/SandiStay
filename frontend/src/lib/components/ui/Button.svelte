<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { HTMLButtonAttributes } from 'svelte/elements';

	// Definimos las propiedades que aceptará nuestro botón
	interface Props extends HTMLButtonAttributes {
		variant?: 'primary' | 'secondary' | 'outline' | 'danger';
		children?: Snippet; // Esto representa el texto o iconos dentro del botón
	}

	// Usamos la Rune $props() de Svelte 5
	let { variant = 'primary', children, class: className = '', ...rest }: Props = $props();

	// Clases base de Tailwind que todos los botones comparten
	const baseClasses =
		'inline-flex items-center justify-center px-4 py-2 rounded-md font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed';

	// Clases específicas según la variante elegida (usando nuestros colores del theme)
	const variantClasses = {
		primary: 'bg-primary text-white hover:opacity-90 focus:ring-primary',
		secondary: 'bg-secondary text-white hover:opacity-90 focus:ring-secondary',
		outline:
			'border-2 border-secondary text-secondary hover:bg-secondary hover:text-white focus:ring-secondary',
		danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-600'
	};
</script>

<!-- El botón HTML real -->
<button class="{baseClasses} {variantClasses[variant]} {className}" {...rest}>
	<!-- Renderizamos lo que sea que pongan dentro del botón -->
	{@render children?.()}
</button>
