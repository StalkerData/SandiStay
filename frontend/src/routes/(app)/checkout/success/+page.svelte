<script lang="ts">
    import { page } from '$app/stores';
    import Button from '$lib/components/ui/Button.svelte';

    // Extraemos el código de reserva de la URL (?code=RES-XXXX)
    let codigoReserva = $derived($page.url.searchParams.get('code'));
</script>

<svelte:head>
    <title>Reserva Exitosa | SandiStay</title>
</svelte:head>

<div class="min-h-[60vh] flex items-center justify-center px-4 py-12">
    <div class="bg-surface p-8 md:p-12 rounded-2xl shadow-xl max-w-lg w-full text-center border border-neutral/10 animate-fade-in">
        
        <!-- Icono de Éxito -->
        <div class="w-24 h-24 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-8">
            <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path>
            </svg>
        </div>
        
        <h1 class="font-serif text-3xl md:text-4xl font-bold text-dark mb-4">¡Reserva Confirmada!</h1>
        <p class="text-neutral text-lg mb-8">Tu pago ha sido procesado exitosamente y tu habitación está asegurada.</p>
        
        <!-- Tarjeta con el Código -->
        <div class="bg-base/50 p-6 rounded-xl mb-10 border border-neutral/20">
            <p class="text-sm text-neutral mb-2 uppercase tracking-wider font-bold">Tu código de reserva es:</p>
            <p class="text-3xl md:text-4xl font-bold text-primary tracking-widest">{codigoReserva}</p>
            <p class="text-xs text-neutral mt-4">Guarda este código. Lo necesitarás para hacer el Check-in en recepción.</p>
        </div>

        <a href="/" class="block w-full">
            <Button variant="outline" class="w-full py-3 text-lg">Volver al Inicio</Button>
        </a>
    </div>
</div>
