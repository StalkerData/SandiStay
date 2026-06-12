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

	let nombresHuesped = $state('');
	let apellidosHuesped = $state('');
	let idTipoDocumento = $state(1); // 1 = CC por defecto
	let documentoHuesped = $state('');
	let emailHuesped = $state('');
	let telefonoHuesped = $state('');
	let idPais = $state(1); // 1 = Colombia por defecto
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
			itemId: item.id || item.idArea,
			fechaInicio: fechaInicio,
			fechaFin: tipoReserva === 'Habitacion' ? fechaFin : null,
			horaInicio: tipoReserva === 'Area' ? horaInicio + ':00' : null,
			horaFin: tipoReserva === 'Area' ? horaFin + ':00' : null,
			numeroAdultos: numeroAdultos,
			numeroNinos: tipoReserva === 'Habitacion' ? numeroNinos : 0,
			nombresHuesped: nombresHuesped,
			apellidosHuesped: apellidosHuesped,
			idTipoDocumento: idTipoDocumento,
			documentoHuesped: documentoHuesped,
			emailHuesped: emailHuesped,
			telefonoHuesped: telefonoHuesped,
			idPais: idPais,
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

<div class="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
	<!-- Indicador de Pasos -->
	<div class="mb-8 flex items-center justify-center">
		<div class="flex items-center gap-4">
			<div
				class="flex items-center gap-2 {pasoActual === 1
					? 'font-bold text-primary'
					: 'text-neutral'}"
			>
				<span
					class="flex h-8 w-8 items-center justify-center rounded-full border-2 {pasoActual === 1
						? 'border-primary bg-primary/10'
						: 'border-neutral'}">1</span
				>
				<span>Tus Datos</span>
			</div>
			<div class="h-px w-16 bg-neutral/30"></div>
			<div
				class="flex items-center gap-2 {pasoActual === 2
					? 'font-bold text-primary'
					: 'text-neutral'}"
			>
				<span
					class="flex h-8 w-8 items-center justify-center rounded-full border-2 {pasoActual === 2
						? 'border-primary bg-primary/10'
						: 'border-neutral'}">2</span
				>
				<span>Pago</span>
			</div>
		</div>
	</div>

	<div class="grid grid-cols-1 gap-12 lg:grid-cols-3">
		<!-- COLUMNA IZQUIERDA: Formularios dinámicos -->
		<div class="space-y-8 lg:col-span-2">
			{#if pasoActual === 1}
				<!-- PASO 1: FORMULARIO DE DATOS -->
				<form id="datosForm" onsubmit={irAlPago} class="space-y-8">
					<section class="rounded-xl border border-neutral/10 bg-surface p-6 shadow-sm">
						<h2 class="mb-4 text-xl font-bold text-secondary">Fechas de Estadía</h2>
						<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
							{#if tipoReserva === 'Habitacion'}
								<Input
									id="fechaInicio"
									label="Check-in"
									type="date"
									bind:value={fechaInicio}
									required
								/>
								<Input id="fechaFin" label="Check-out" type="date" bind:value={fechaFin} required />
							{:else}
								<Input
									id="fechaInicio"
									label="Fecha del Evento"
									type="date"
									bind:value={fechaInicio}
									required
								/>
								<div class="grid grid-cols-2 gap-2">
									<Input
										id="horaInicio"
										label="Hora Inicio"
										type="time"
										bind:value={horaInicio}
										required
									/>
									<Input id="horaFin" label="Hora Fin" type="time" bind:value={horaFin} required />
								</div>
							{/if}
							<Input
								id="adultos"
								label={tipoReserva === 'Habitacion' ? 'Adultos' : 'Asistentes'}
								type="number"
								min="1"
								bind:value={numeroAdultos}
								required
							/>
							{#if tipoReserva === 'Habitacion'}
								<Input
									id="ninos"
									label="Niños"
									type="number"
									min="0"
									bind:value={numeroNinos}
									required
								/>
							{/if}
						</div>
					</section>

					<section class="rounded-xl border border-neutral/10 bg-surface p-6 shadow-sm">
						<h2 class="mb-4 text-xl font-bold text-secondary">2. Tus Datos</h2>
						<div class="grid grid-cols-1 gap-4 md:grid-cols-2">
							<Input
								id="nombres"
								label="Nombres"
								type="text"
								bind:value={nombresHuesped}
								required
							/>
							<Input
								id="apellidos"
								label="Apellidos"
								type="text"
								bind:value={apellidosHuesped}
								required
							/>

							<div class="flex flex-col gap-1">
								<label for="idTipoDocumento" class="text-sm font-medium text-dark"
									>Tipo de Documento</label
								>
								<select
									bind:value={idTipoDocumento}
									class="rounded-md border border-neutral/30 px-3 py-2 focus:ring-secondary"
								>
									<option value={1}>Cédula de Ciudadanía</option>
									<option value={2}>Cédula de Extranjería</option>
									<option value={3}>Pasaporte</option>
									<option value={4}>NIT</option>
								</select>
							</div>
							<Input
								id="documento"
								label="Número de Documento"
								type="text"
								bind:value={documentoHuesped}
								required
							/>

							<Input
								id="email"
								label="Correo Electrónico"
								type="email"
								bind:value={emailHuesped}
								required
							/>
							<Input
								id="telefono"
								label="Teléfono"
								type="tel"
								bind:value={telefonoHuesped}
								required
							/>

							<div class="flex flex-col gap-1 md:col-span-2">
								<label for="idPais" class="text-sm font-medium text-dark">País de Origen</label>
								<select
									bind:value={idPais}
									class="rounded-md border border-neutral/30 px-3 py-2 focus:ring-secondary"
								>
									<option value={1}>Colombia</option>
									<option value={2}>Venezuela</option>
									<option value={3}>Estados Unidos</option>
								</select>
							</div>
						</div>
					</section>
				</form>
			{:else}
				<!-- PASO 2: MÉTODOS DE PAGO (QR) -->
				<section
					class="animate-fade-in rounded-xl border border-neutral/10 bg-surface p-6 shadow-sm"
				>
					<div class="mb-6 flex items-center justify-between">
						<h2 class="text-xl font-bold text-secondary">Selecciona tu método de pago</h2>
						<button
							onclick={volverADatos}
							class="text-sm text-neutral underline hover:text-primary"
						>
							← Volver a mis datos
						</button>
					</div>

					<div class="mb-6 flex gap-4">
						<label
							class="flex cursor-pointer items-center gap-2 rounded-lg border p-4 hover:bg-base/50 {metodoPago ===
							'NEQUI'
								? 'border-primary bg-primary/5'
								: 'border-neutral/20'}"
						>
							<input type="radio" bind:group={metodoPago} value="NEQUI" class="hidden" />
							<span class="text-lg font-bold">Nequi</span>
						</label>
						<label
							class="flex cursor-pointer items-center gap-2 rounded-lg border p-4 hover:bg-base/50 {metodoPago ===
							'DAVIPLATA'
								? 'border-primary bg-primary/5'
								: 'border-neutral/20'}"
						>
							<input type="radio" bind:group={metodoPago} value="DAVIPLATA" class="hidden" />
							<span class="text-lg font-bold">Daviplata</span>
						</label>
					</div>

					<div
						class="flex flex-col items-center rounded-lg border border-neutral/20 bg-base/50 p-6 text-center"
					>
						<p class="mb-4 text-sm text-neutral">
							Escanea el código QR desde tu celular para transferir.
						</p>
						<img
							src={`/assets/qr/${metodoPago.toLowerCase()}.png`}
							alt={`QR de ${metodoPago}`}
							class="mb-4 h-48 w-48 rounded-lg bg-white object-contain p-2 shadow-sm"
							onerror={(e) =>
								(e.currentTarget.src = 'https://via.placeholder.com/200?text=QR+Mock')}
						/>
						<p class="text-xs font-bold text-primary">
							MODO PRUEBA: Al hacer clic en "Simular Pago", el sistema aprobará la transacción.
						</p>
					</div>
				</section>
			{/if}
		</div>

		<!-- COLUMNA DERECHA: Resumen de Compra (Sticky) -->
		<div class="lg:col-span-1">
			<div class="sticky top-24 rounded-xl border border-neutral/10 bg-surface p-6 shadow-lg">
				<h3 class="mb-4 font-serif text-xl font-bold text-dark">Resumen de Reserva</h3>

				<div class="mb-6 flex gap-4 border-b border-neutral/10 pb-6">
					<img
						src={`http://localhost:8082/api/v1${item.imagenPrincipal}`}
						alt="Foto"
						class="h-20 w-20 rounded-md object-cover"
						onerror={(e) =>
							(e.currentTarget.src = 'https://via.placeholder.com/200?text=Sin+Imagen')}
					/>
					<div>
						<p class="font-bold text-dark">{item.nombre}</p>
						<p class="text-sm text-neutral">{item.tipo}</p>
					</div>
				</div>

				<div class="mb-6 space-y-3 text-sm">
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
					<div class="mb-4 rounded-md bg-red-100 p-3 text-sm text-red-700">
						{errorMessage}
					</div>
				{/if}

				{#if pasoActual === 1}
					<Button type="submit" form="datosForm" variant="primary" class="w-full py-3 text-lg">
						Continuar al Pago
					</Button>
				{:else}
					<Button
						onclick={handlePayment}
						variant="primary"
						class="w-full py-3 text-lg"
						disabled={isSubmitting}
					>
						{isSubmitting ? 'Procesando...' : 'Simular Pago y Reservar'}
					</Button>
				{/if}
			</div>
		</div>
	</div>
</div>
