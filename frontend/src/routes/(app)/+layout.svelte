<script lang="ts">
    import Navbar from '$lib/components/layout/Navbar.svelte';
    import Footer from '$lib/components/layout/Footer.svelte';
    
    let { children } = $props();
</script>

<!-- Inyectamos la barra de navegación pública -->
<Navbar />

<!-- El contenido principal de la página (crece para empujar el footer hacia abajo) -->
<main class="flex-grow">
    {@render children()}
</main>

<!-- Inyectamos el pie de página -->
<Footer />