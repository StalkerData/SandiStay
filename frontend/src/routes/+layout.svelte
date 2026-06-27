<script lang="ts">
    import './layout.css';
    
    let { children } = $props();
</script>

<svelte:head>
    <!-- Meta tags globales para PWA y renderizado móvil -->
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=5" />
    <meta name="theme-color" content="#ffffff" />
</svelte:head>

<!-- 
    Contenedor principal. 
    antialiased: Mejora el renderizado de fuentes en macOS/iOS.
    selection: Personaliza el color al sombrear texto.
-->
<div class="min-h-screen flex flex-col bg-base font-sans text-dark antialiased selection:bg-primary/30 selection:text-primary">
    {@render children()}
</div>