<script lang="ts">
    import { goto } from '$app/navigation';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import type { PageData } from './$types';
    import { PUBLIC_API_URL } from '$env/static/public';

    let { data }: { data: PageData } = $props();
    let rooms = $derived(data.rooms);
    let areas = $derived(data.areas);

    // --- ESTADO DEL FORMULARIO ---
    let tipoReserva = $state('Habitacion');
    let itemId = $state('');
    
    let fechaInicio = $state('');
    let fechaFin = $state('');
    let horaInicio = $state('08:00');
    let horaFin = $state('12:00');
    let numeroAdultos = $state(1);
    let numeroNinos = $state(0);
    
    let nombresHuesped = $state('');
    let apellidosHuesped = $state('');
    let idTipoDocumento = $state(1);
    let documentoHuesped = $state('');
    let emailHuesped = $state('');
    let telefonoHuesped = $state('');
    let idPais = $state(1);

    let isSubmitting = $state(false);
    let errorMessage = $state('');
    let successMessage = $state('');

    async function handleReservaEfectivo(e: Event) {
        e.preventDefault();
        isSubmitting = true;
        errorMessage = '';
        successMessage = '';

        const payload = {
            tipoReserva,
            itemId: parseInt(itemId),
            fechaInicio,
            fechaFin: tipoReserva === 'Habitacion' ? fechaFin : null,
            horaInicio: tipoReserva === 'Area' ? horaInicio + ':00' : null,
            horaFin: tipoReserva === 'Area' ? horaFin + ':00' : null,
            numeroAdultos,
            numeroNinos: tipoReserva === 'Habitacion' ? numeroNinos : 0,
            nombresHuesped,
            apellidosHuesped,
            idTipoDocumento,
            documentoHuesped,
            emailHuesped,
            telefonoHuesped,
            idPais,
            // ¡LA MAGIA DEL STAFF! Enviamos "EFECTIVO" para saltarnos el Mock
            paymentTxId: 'EFECTIVO' 
        };

        try {
            // Como estamos en el Dashboard, enviamos el Token de la Secretaria
            // (Aunque el endpoint es público, enviarlo ayuda a la auditoría)
            const res = await fetch(`${PUBLIC_API_URL}/bookings`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                const result = await res.json();
                successMessage = `¡Reserva creada exitosamente! Código: ${result.codigoReserva}`;
                // Limpiamos el formulario
                documentoHuesped = ''; nombresHuesped = ''; apellidosHuesped = '';
            } else {
                const errData = await res.json();
                errorMessage = errData.message || 'Error al procesar la reserva.';
            }
        } catch (err) {
            errorMessage = 'Error de conexión con el servidor.';
        } finally {
            isSubmitting = false;
        }
    }
</script>

<svelte:head>
    <title>Nueva Reserva (Staff) | SandiStay Admin</title>
</svelte:head>

<div class="space-y-6 max-w-4xl mx-auto">
    <div class="flex justify-between items-center">
        <div>
            <h2 class="text-2xl font-bold text-dark">Registrar Reserva (Walk-in)</h2>
            <p class="text-sm text-neutral">Crea una reserva directa recibiendo el pago en efectivo en recepción.</p>
        </div>
        <a href="/dashboard"><Button variant="outline">← Volver</Button></a>
    </div>

    {#if errorMessage}
        <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-md shadow-sm animate-fade-in">{errorMessage}</div>
    {/if}
    {#if successMessage}
        <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded-md shadow-sm animate-fade-in">{successMessage}</div>
    {/if}

    <form onsubmit={handleReservaEfectivo} class="bg-surface p-8 rounded-xl shadow-sm border border-neutral/10 space-y-8">
        
        <!-- 1. SELECCIÓN DE ACTIVO -->
        <section>
            <h3 class="text-lg font-bold text-secondary mb-4 border-b pb-2">1. ¿Qué desea reservar?</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="flex flex-col gap-1">
                    <label class="text-sm font-medium text-dark">Tipo</label>
                    <select bind:value={tipoReserva} class="px-3 py-2 border border-neutral/30 rounded-md">
                        <option value="Habitacion">Habitación</option>
                        <option value="Area">Área Social</option>
                    </select>
                </div>
                <div class="flex flex-col gap-1">
                    <label class="text-sm font-medium text-dark">Seleccione el Ítem</label>
                    <select bind:value={itemId} class="px-3 py-2 border border-neutral/30 rounded-md" required>
                        <option value="" disabled selected>Seleccione...</option>
                        {#if tipoReserva === 'Habitacion'}
                            {#each rooms as room}
                                <option value={room.idHabitacion}>{room.nombre} ({room.codigo}) - ${room.precioNoche}/noche</option>
                            {/each}
                        {:else}
                            {#each areas as area}
                                <option value={area.idArea}>{area.nombre} ({area.codigo}) - ${area.precioHora}/hora</option>
                            {/each}
                        {/if}
                    </select>
                </div>
            </div>
        </section>

        <!-- 2. FECHAS -->
        <section>
            <h3 class="text-lg font-bold text-secondary mb-4 border-b pb-2">2. Fechas y Ocupación</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                {#if tipoReserva === 'Habitacion'}
                    <Input id="fechaInicio" label="Check-in" type="date" bind:value={fechaInicio} required />
                    <Input id="fechaFin" label="Check-out" type="date" bind:value={fechaFin} required />
                {:else}
                    <Input id="fechaInicio" label="Fecha del Evento" type="date" bind:value={fechaInicio} required />
                    <div class="grid grid-cols-2 gap-2">
                        <Input id="horaInicio" label="Hora Inicio" type="time" bind:value={horaInicio} required />
                        <Input id="horaFin" label="Hora Fin" type="time" bind:value={horaFin} required />
                    </div>
                {/if}
                <Input id="adultos" label={tipoReserva === 'Habitacion' ? 'Adultos' : 'Asistentes'} type="number" min="1" bind:value={numeroAdultos} required />
                {#if tipoReserva === 'Habitacion'}
                    <Input id="ninos" label="Niños" type="number" min="0" bind:value={numeroNinos} required />
                {/if}
            </div>
        </section>

        <!-- 3. DATOS DEL CLIENTE -->
        <section>
            <h3 class="text-lg font-bold text-secondary mb-4 border-b pb-2">3. Datos del Cliente</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input id="nombres" label="Nombres" type="text" bind:value={nombresHuesped} required />
                <Input id="apellidos" label="Apellidos" type="text" bind:value={apellidosHuesped} required />
                <div class="flex flex-col gap-1">
                    <label class="text-sm font-medium text-dark">Tipo Doc.</label>
                    <select bind:value={idTipoDocumento} class="px-3 py-2 border border-neutral/30 rounded-md">
                        <option value={1}>Cédula de Ciudadanía</option>
                        <option value={2}>Cédula de Extranjería</option>
                        <option value={3}>Pasaporte</option>
                        <option value={4}>NIT</option>
                    </select>
                </div>
                <Input id="documento" label="Número de Documento" type="text" bind:value={documentoHuesped} required />
                <Input id="email" label="Correo Electrónico" type="email" bind:value={emailHuesped} required />
                <Input id="telefono" label="Teléfono" type="tel" bind:value={telefonoHuesped} required />
            </div>
        </section>

        <!-- BOTÓN DE ACCIÓN -->
        <div class="pt-6 border-t border-neutral/10 flex justify-end items-center gap-4">
            <p class="text-sm text-neutral italic">Al confirmar, el sistema registrará el pago en EFECTIVO automáticamente.</p>
            <Button type="submit" variant="primary" class="px-8 py-3 text-lg" disabled={isSubmitting}>
                {isSubmitting ? 'Procesando...' : '💵 Recibir Efectivo y Reservar'}
            </Button>
        </div>
    </form>
</div>