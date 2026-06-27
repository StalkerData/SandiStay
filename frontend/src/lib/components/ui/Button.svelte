<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { HTMLButtonAttributes } from 'svelte/elements';

	interface Props extends HTMLButtonAttributes {
		variant?: 'primary' | 'secondary' | 'outline' | 'danger';
		isLoading?: boolean; // Nueva prop para estado de carga
		children?: Snippet;
	}

	let { 
		variant = 'primary', 
		isLoading = false, 
		type = 'button', // Previene submits accidentales
		children, 
		class: className = '', 
		disabled,
		...rest 
	}: Props = $props();

	const baseClasses =
		'inline-flex items-center justify-center px-4 py-2 rounded-md font-medium transition-all active:scale-95 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed disabled:active:scale-100';

	const variantClasses = {
		primary: 'bg-primary text-white hover:bg-primary/90 focus:ring-primary shadow-sm',
		secondary: 'bg-secondary text-white hover:bg-secondary/90 focus:ring-secondary shadow-sm',
		outline: 'border-2 border-secondary text-secondary hover:bg-secondary hover:text-white focus:ring-secondary',
		danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-600 shadow-sm'
	};
</script>

<button 
	{type}
	class="{baseClasses} {variantClasses[variant]} {className}" 
	disabled={disabled || isLoading}
	aria-disabled={disabled || isLoading}
	{...rest}
>
	<!-- Spinner de carga SVG -->
	{#if isLoading}
		<svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-current" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" aria-hidden="true">
			<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
			<path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
		</svg>
	{/if}
	
	{@render children?.()}
</button>
