<script lang="ts">
    import Navbar from '$lib/components/layout/Navbar.svelte';
    import Footer from '$lib/components/layout/Footer.svelte';
    
    let { children } = $props();
</script>

<!-- 
    Enlace de salto para Accesibilidad (Screen Readers).
    Permanece oculto hasta que el usuario presiona la tecla TAB.
-->
<a href="#main-content" class="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-50 focus:px-4 focus:py-2 focus:bg-primary focus:text-white focus:rounded-md focus:shadow-lg focus:outline-none">
    Saltar al contenido principal
</a>

<!-- Inyectamos la barra de navegación pública -->
<Navbar />

<!-- El contenido principal de la página -->
<div id="main-content" class="flex-grow flex flex-col" tabindex="-1">
    {@render children()}
</div>

<!-- Inyectamos el pie de página -->
<Footer />