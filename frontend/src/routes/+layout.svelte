<script lang="ts">
    // Importamos el CSS global que contiene Tailwind y nuestras variables de color
    import './layout.css';
    
    // En Svelte 5, usamos {@render children()} en lugar de <slot />
    let { children } = $props();
</script>

<!-- El contenedor principal que ocupa toda la pantalla -->
<div class="min-h-screen flex flex-col bg-base font-sans text-dark">
    {@render children()}
</div>