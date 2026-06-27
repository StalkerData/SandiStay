<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		header?: Snippet;
		children: Snippet;
		footer?: Snippet;
		class?: string;
	}

	let { header, children, footer, class: className = '' }: Props = $props();
</script>

<!-- Usamos <article> para mejor semántica HTML5 -->
<article class="overflow-hidden rounded-xl border border-neutral/10 bg-surface shadow-sm transition-all hover:shadow-md {className}">
	
	{#if header}
		<header class="border-b border-neutral/10">
			{@render header()}
		</header>
	{/if}

	<div class="p-5">
		{@render children()}
	</div>

	{#if footer}
		<footer class="flex items-center justify-end gap-3 border-t border-neutral/10 bg-base/30 px-5 py-4">
			{@render footer()}
		</footer>
	{/if}
</article>
