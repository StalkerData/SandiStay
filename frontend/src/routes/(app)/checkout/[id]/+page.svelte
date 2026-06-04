<script lang="ts">
    import { goto } from '$app/navigation';
    import { page } from '$app/stores';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import type { PageData } from './$types';

    // 1. Recibimos los datos y usamos $derived para mantener la reactividad en Svelte 5
    let { data }: { data: PageData } = $props();
    let item = $derived(data.room);
    
    // Extraemos el tipo de reserva de la URL (ej: ?type=Habitacion)
    let tipoReserva = $derived($page.url.searchParams.get('type') || 'Habitacion');

    // --- ESTADO DEL WIZARD ---
    let pasoActual = $state(1); // 1 = Datos, 2 = Pago

    // --- ESTADO DEL FORMULARIO ---
    let fechaInicio = $state('');
    let fechaFin = $state('');
    let horaInicio = $state('08:00');
    let horaFin = $state('12:00');
    let numeroAdultos = $state(2);
    let numeroNinos = $state(0);
    
    let nombreHuesped = $state('');
    let documentoHuesped = $state('');
    let emailHuesped = $state('');
    let telefonoHuesped = $state('');

    let metodoPago = $state('NEQUI');
    let isSubmitting = $state(false);
    let errorMessage = $state('');

    // Avanzar al paso de pago
    function irAlPago(e: Event) {
        e.preventDefault();
        pasoActual = 2;
        window.scrollTo(0, 0);
    }

    // Volver a los datos
    function volverADatos() {
        pasoActual = 1;
    }

    // --- LÓGICA DE ENVÍO AL BACKEND ---
    async function handlePayment() {
        isSubmitting = true;
        errorMessage = '';

        const payload = {
            tipoReserva: tipoReserva,
            itemId: item.id || item.idArea, // Soporta ambos IDs
            fechaInicio: fechaInicio,
            fechaFin: tipoReserva === 'Habitacion' ? fechaFin : null,
            horaInicio: tipoReserva === 'Area' ? horaInicio + ':00' : null, // Formato HH:mm:ss
            horaFin: tipoReserva === 'Area' ? horaFin + ':00' : null,
            numeroAdultos: numeroAdultos,
            numeroNinos: tipoReserva === 'Habitacion' ? numeroNinos : 0,
            nombreHuesped: nombreHuesped,
            documentoHuesped: documentoHuesped,
            emailHuesped: emailHuesped,
            telefonoHuesped: telefonoHuesped,
            paymentTxId: `MOCK-${Math.floor(Math.random() * 1000000)}`
        };

        try {
            const res = await fetch('http://localhost:8082/api/v1/bookings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                const result = await res.json();
                goto(`/checkout/success?code=${result.codigoReserva}`);
            } else {
                const errData = await res.json();
                errorMessage = errData.message || 'Error al procesar la reserva.';
                pasoActual = 1; // Lo devolvemos si hay error de validación
            }
        } catch (err) {
            errorMessage = 'Error de conexión con el servidor.';
        } finally {
            isSubmitting = false;
        }
    }
</script>

<svelte:head>
    <title>Checkout | SandiStay</title>
</svelte:head>

<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
    
    <!-- Indicador de Pasos -->
    <div class="flex items-center justify-center mb-8">
        <div class="flex items-center gap-4">
            <div class="flex items-center gap-2 {pasoActual === 1 ? 'text-primary font-bold' : 'text-neutral'}">
                <span class="w-8 h-8 rounded-full flex items-center justify-center border-2 {pasoActual === 1 ? 'border-primary bg-primary/10' : 'border-neutral'}">1</span>
                <span>Tus Datos</span>
            </div>
            <div class="w-16 h-px bg-neutral/30"></div>
            <div class="flex items-center gap-2 {pasoActual === 2 ? 'text-primary font-bold' : 'text-neutral'}">
                <span class="w-8 h-8 rounded-full flex items-center justify-center border-2 {pasoActual === 2 ? 'border-primary bg-primary/10' : 'border-neutral'}">2</span>
                <span>Pago</span>
            </div>
        </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
        
        <!-- COLUMNA IZQUIERDA: Formularios dinámicos -->
        <div class="lg:col-span-2 space-y-8">
            
            {#if pasoActual === 1}
                <!-- PASO 1: FORMULARIO DE DATOS -->
                <form id="datosForm" onsubmit={irAlPago} class="space-y-8">
                    <section class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10">
                        <h2 class="text-xl font-bold text-secondary mb-4">Fechas de Estadía</h2>
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

                    <section class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10">
                        <h2 class="text-xl font-bold text-secondary mb-4">Tus Datos</h2>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <Input id="nombre" label="Nombre Completo" type="text" bind:value={nombreHuesped} required />
                            <Input id="documento" label="Documento de Identidad" type="text" bind:value={documentoHuesped} required />
                            <Input id="email" label="Correo Electrónico" type="email" bind:value={emailHuesped} required />
                            <Input id="telefono" label="Teléfono" type="tel" bind:value={telefonoHuesped} required />
                        </div>
                    </section>
                </form>
            {:else}
                <!-- PASO 2: MÉTODOS DE PAGO (QR) -->
                <section class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10 animate-fade-in">
                    <div class="flex justify-between items-center mb-6">
                        <h2 class="text-xl font-bold text-secondary">Selecciona tu método de pago</h2>
                        <button onclick={volverADatos} class="text-sm text-neutral hover:text-primary underline">
                            ← Volver a mis datos
                        </button>
                    </div>
                    
                    <div class="flex gap-4 mb-6">
                        <label class="flex items-center gap-2 cursor-pointer p-4 border rounded-lg hover:bg-base/50 {metodoPago === 'NEQUI' ? 'border-primary bg-primary/5' : 'border-neutral/20'}">
                            <input type="radio" bind:group={metodoPago} value="NEQUI" class="hidden" />
                            <span class="font-bold text-lg">Nequi</span>
                        </label>
                        <label class="flex items-center gap-2 cursor-pointer p-4 border rounded-lg hover:bg-base/50 {metodoPago === 'DAVIPLATA' ? 'border-primary bg-primary/5' : 'border-neutral/20'}">
                            <input type="radio" bind:group={metodoPago} value="DAVIPLATA" class="hidden" />
                            <span class="font-bold text-lg">Daviplata</span>
                        </label>
                    </div>

                    <div class="bg-base/50 p-6 rounded-lg flex flex-col items-center text-center border border-neutral/20">
                        <p class="text-sm text-neutral mb-4">Escanea el código QR desde tu celular para transferir.</p>
                        <img 
                            src={`/assets/qr/${metodoPago.toLowerCase()}.png`} 
                            alt={`QR de ${metodoPago}`} 
                            class="w-48 h-48 object-contain bg-white p-2 rounded-lg shadow-sm mb-4"
                            onerror={(e) => e.currentTarget.src = 'https://via.placeholder.com/200?text=QR+Mock'}
                        />
                        <p class="text-xs text-primary font-bold">
                            MODO PRUEBA: Al hacer clic en "Simular Pago", el sistema aprobará la transacción.
                        </p>
                    </div>
                </section>
            {/if}
        </div>

        <!-- COLUMNA DERECHA: Resumen de Compra (Sticky) -->
        <div class="lg:col-span-1">
            <div class="bg-surface rounded-xl shadow-lg p-6 border border-neutral/10 sticky top-24">
                <h3 class="font-serif text-xl font-bold text-dark mb-4">Resumen de Reserva</h3>
                
                <div class="flex gap-4 mb-6 pb-6 border-b border-neutral/10">
                    <img 
                        src={`http://localhost:8082/api/v1${item.imagenPrincipal}`} 
                        alt="Foto" 
                        class="w-20 h-20 object-cover rounded-md" 
                        onerror={(e) => e.currentTarget.src = 'https://via.placeholder.com/200?text=Sin+Imagen'}
                    />
                    <div>
                        <p class="font-bold text-dark">{item.nombre}</p>
                        <p class="text-sm text-neutral">{item.tipo}</p>
                    </div>
                </div>

                <div class="space-y-3 mb-6 text-sm">
                    <div class="flex justify-between">
                        <span class="text-neutral">Precio por noche</span>
                        <span class="font-medium">${item.precioNoche.toLocaleString('es-CO')}</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-neutral">Impuestos (IVA 19%)</span>
                        <span class="font-medium">Incluidos</span>
                    </div>
                </div>

                {#if errorMessage}
                    <div class="bg-red-100 text-red-700 p-3 rounded-md text-sm mb-4">
                        {errorMessage}
                    </div>
                {/if}

                {#if pasoActual === 1}
                    <Button type="submit" form="datosForm" variant="primary" class="w-full py-3 text-lg">
                        Continuar al Pago
                    </Button>
                {:else}
                    <Button onclick={handlePayment} variant="primary" class="w-full py-3 text-lg" disabled={isSubmitting}>
                        {isSubmitting ? 'Procesando...' : 'Simular Pago y Reservar'}
                    </Button>
                {/if}
            </div>
        </div>

    </div>
</div>