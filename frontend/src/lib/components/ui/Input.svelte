<script lang="ts">
	import type { HTMLInputAttributes } from 'svelte/elements';

	interface Props extends HTMLInputAttributes {
		label: string;
		id: string;
		value?: string | number;
		error?: string;
	}

	let { label, id, value = $bindable(), error, class: className = '', ...rest }: Props = $props();
	
	// Generamos un ID único para el mensaje de error (Accesibilidad)
	let errorId = `${id}-error`;
</script>

<div class="flex flex-col gap-1.5 {className}">
	<label for={id} class="text-sm font-semibold text-dark">
		{label}
		<!-- Indicador visual de campo requerido -->
		{#if rest.required}
			<span class="text-red-500 ml-0.5" aria-hidden="true" title="Requerido">*</span>
		{/if}
	</label>

	<input
		{id}
		bind:value
		class="rounded-md border px-3 py-2.5 transition-all focus:outline-none focus:ring-2 focus:ring-offset-1
               {error ? 'border-red-500 focus:ring-red-500 bg-red-50/50' : 'border-neutral/30 focus:border-secondary focus:ring-secondary bg-surface'}"
		aria-invalid={!!error}
		aria-describedby={error ? errorId : undefined}
		{...rest}
	/>

	{#if error}
		<span id={errorId} class="text-xs font-medium text-red-600 animate-fade-in" role="alert">
			{error}
		</span>
	{/if}
</div>