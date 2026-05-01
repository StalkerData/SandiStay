<script lang="ts">
	import type { HTMLInputAttributes } from 'svelte/elements';

	interface Props extends HTMLInputAttributes {
		label: string;
		id: string;
		value?: string | number;
		error?: string; // Para mostrar mensajes de validación
	}

	// Usamos $bindable() para que el valor pueda viajar de ida y vuelta (Two-way binding)
	let { label, id, value = $bindable(), error, class: className = '', ...rest }: Props = $props();
</script>

<div class="flex flex-col gap-1 {className}">
	<!-- Etiqueta del campo -->
	<label for={id} class="text-sm font-medium text-dark">
		{label}
	</label>

	<!-- Campo de texto -->
	<input
		{id}
		bind:value
		class="rounded-md border px-3 py-2 transition-colors focus:border-transparent focus:ring-2 focus:ring-secondary focus:outline-none
               {error ? 'border-red-500 focus:ring-red-500' : 'border-neutral/30'}"
		{...rest}
	/>

	<!-- Mensaje de error (solo se muestra si hay un error) -->
	{#if error}
		<span class="text-xs text-red-500">{error}</span>
	{/if}
</div>
