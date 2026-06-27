<script lang="ts">
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import { enhance } from '$app/forms';
    import type { PageData, ActionData } from './$types';

    // --- PROPS & ESTADO DERIVADO ---
    let { data, form }: { data: PageData, form: ActionData } = $props();
    let rooms = $derived(data.rooms);
    let areas = $derived(data.areas);

    // --- ESTADO DE UI ---
    let isSubmitting = $state(false);
    let tipoReserva = $state('Habitacion');
    let itemId = $state('');

    /**
     * @description Resetea el ítem seleccionado si el usuario cambia entre Habitación y Área.
     */
    function handleTipoCambio() {
        itemId = '';
    }
</script>

<svelte:head>
    <title>Nueva Reserva (Walk-in) | SandiStay Admin</title>
</svelte:head>

<main class="space-y-6 max-w-4xl mx-auto">
    
    <header class="flex justify-between items-center">
        <div>
            <h1 class="text-2xl font-bold text-dark">Registrar Reserva (Walk-in)</h1>
            <p class="text-sm text-neutral">Crea una reserva directa recibiendo el pago en efectivo en recepción.</p>
        </div>
        <a href="/dashboard" aria-label="Volver al dashboard">
            <Button variant="outline" class="transition-colors">← Volver</Button>
        </a>
    </header>

    <!-- ALERTAS (ARIA Live) -->
    <div aria-live="polite">
        {#if form?.error}
            <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-md shadow-sm animate-fade-in mb-6" role="alert">
                {form.error}
            </div>
        {/if}
        {#if form?.success}
            <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded-md shadow-sm animate-fade-in mb-6" role="status">
                {form.message}
            </div>
        {/if}
    </div>

    <!-- FORMULARIO CON SERVER ACTIONS -->
    <form 
        method="POST" 
        action="?/createBooking"
        class="bg-surface p-8 rounded-xl shadow-sm border border-neutral/10 space-y-8"
        use:enhance={() => {
            isSubmitting = true;
            return async ({ update, result }) => {
                await update({ reset: result.type === 'success' }); // Solo limpia el form si fue exitoso
                isSubmitting = false;
                if (result.type === 'success') {
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                }
            };
        }}
    >
        
        <!-- 1. SELECCIÓN DE ACTIVO -->
        <section>
            <h2 class="text-lg font-bold text-secondary mb-4 border-b border-neutral/10 pb-2">1. ¿Qué desea reservar?</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="flex flex-col gap-1">
                    <label for="tipoReserva" class="text-sm font-medium text-dark">Tipo</label>
                    <select id="tipoReserva" name="tipoReserva" bind:value={tipoReserva} onchange={handleTipoCambio} class="px-3 py-2 border border-neutral/30 rounded-md focus:ring-secondary bg-base/50">
                        <option value="Habitacion">Habitación</option>
                        <option value="Area">Área Social</option>
                    </select>
                </div>
                <div class="flex flex-col gap-1">
                    <label for="itemId" class="text-sm font-medium text-dark">Seleccione el Ítem</label>
                    <select id="itemId" name="itemId" bind:value={itemId} required class="px-3 py-2 border border-neutral/30 rounded-md focus:ring-secondary bg-base/50">
                        <option value="" disabled>Seleccione...</option>
                        {#if tipoReserva === 'Habitacion'}
                            {#each rooms as room}
                                <option value={room.idHabitacion}>{room.nombre} ({room.codigo}) - ${room.precioNoche?.toLocaleString('es-CO')}/noche</option>
                            {/each}
                        {:else}
                            {#each areas as area}
                                <option value={area.idArea}>{area.nombre} ({area.codigo}) - ${area.precioHora?.toLocaleString('es-CO')}/hora</option>
                            {/each}
                        {/if}
                    </select>
                </div>
            </div>
        </section>

        <!-- 2. FECHAS -->
        <section>
            <h2 class="text-lg font-bold text-secondary mb-4 border-b border-neutral/10 pb-2">2. Fechas y Ocupación</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                {#if tipoReserva === 'Habitacion'}
                    <Input id="fechaInicio" name="fechaInicio" label="Check-in" type="date" required />
                    <Input id="fechaFin" name="fechaFin" label="Check-out" type="date" required />
                {:else}
                    <Input id="fechaInicio" name="fechaInicio" label="Fecha del Evento" type="date" required />
                    <div class="grid grid-cols-2 gap-2">
                        <Input id="horaInicio" name="horaInicio" label="Hora Inicio" type="time" value="08:00" required />
                        <Input id="horaFin" name="horaFin" label="Hora Fin" type="time" value="12:00" required />
                    </div>
                {/if}
                <Input id="adultos" name="numeroAdultos" label={tipoReserva === 'Habitacion' ? 'Adultos' : 'Asistentes'} type="number" min="1" value="1" required />
                {#if tipoReserva === 'Habitacion'}
                    <Input id="ninos" name="numeroNinos" label="Niños" type="number" min="0" value="0" required />
                {/if}
            </div>
        </section>

        <!-- 3. DATOS DEL CLIENTE -->
        <section>
            <h2 class="text-lg font-bold text-secondary mb-4 border-b border-neutral/10 pb-2">3. Datos del Cliente</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input id="nombres" name="nombresHuesped" label="Nombres" type="text" required />
                <Input id="apellidos" name="apellidosHuesped" label="Apellidos" type="text" required />
                <div class="flex flex-col gap-1">
                    <label for="idTipoDocumento" class="text-sm font-medium text-dark">Tipo Doc.</label>
                    <select id="idTipoDocumento" name="idTipoDocumento" class="px-3 py-2 border border-neutral/30 rounded-md focus:ring-secondary bg-base/50">
                        <option value={1}>Cédula de Ciudadanía</option>
                        <option value={2}>Cédula de Extranjería</option>
                        <option value={3}>Pasaporte</option>
                        <option value={4}>NIT</option>
                    </select>
                </div>
                <Input id="documento" name="documentoHuesped" label="Número de Documento" type="text" required />
                <Input id="email" name="emailHuesped" label="Correo Electrónico" type="email" required />
                <Input id="telefono" name="telefonoHuesped" label="Teléfono" type="tel" required />
                <div class="flex flex-col gap-1 md:col-span-2">
                    <label for="idPais" class="text-sm font-medium text-dark">País de Origen</label>
                    <select id="idPais" name="idPais" class="px-3 py-2 border border-neutral/30 rounded-md focus:ring-secondary bg-base/50">
                        <option value={1}>Colombia</option>
                        <option value={2}>Venezuela</option>
                        <option value={3}>Estados Unidos</option>
                    </select>
                </div>
            </div>
        </section>

        <!-- BOTÓN DE ACCIÓN -->
        <footer class="pt-6 border-t border-neutral/10 flex flex-col sm:flex-row justify-end items-center gap-4">
            <p class="text-sm text-neutral italic text-center sm:text-left">
                Al confirmar, el sistema registrará el pago en <strong class="text-dark">EFECTIVO</strong> automáticamente.
            </p>
            <Button type="submit" variant="primary" class="w-full sm:w-auto px-8 py-3 text-lg transition-transform active:scale-95" disabled={isSubmitting}>
                {isSubmitting ? 'Procesando...' : '💵 Recibir Efectivo y Reservar'}
            </Button>
        </footer>
    </form>
</main>