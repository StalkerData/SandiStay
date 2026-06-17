<script lang="ts">
	import { goto } from '$app/navigation';
	import Button from '$lib/components/ui/Button.svelte';
	import Input from '$lib/components/ui/Input.svelte';
	import type { PageData } from './$types';

	let { data }: { data: PageData } = $props();
	let occupancy = $derived(data.occupancy);

	// Estado para los filtros de ocupación
	let startDate = $state(data.start);
	let endDate = $state(data.end);

	// Estado para la generación de factura
	let idReservaFactura = $state('');

	// Recarga la página con los nuevos parámetros en la URL
	function filtrarOcupacion(e: Event) {
		e.preventDefault();
		if (startDate && endDate) {
			goto(`/reports?start=${startDate}&end=${endDate}`);
		}
	}

	// Abre el endpoint proxy en una nueva pestaña para ver el PDF
	function descargarFactura(e: Event) {
		e.preventDefault();
		if (idReservaFactura) {
			window.open(`/reports/invoice/${idReservaFactura}`, '_blank');
			idReservaFactura = ''; // Limpiamos el input
		}
	}
</script>

<svelte:head>
	<title>Reportes | SandiStay Admin</title>
</svelte:head>

<div class="space-y-8">
	<div>
		<h2 class="text-2xl font-bold text-dark">Reportes y Facturación</h2>
		<p class="text-sm text-neutral">Métricas de ocupación y generación de documentos fiscales.</p>
	</div>

	<div class="grid grid-cols-1 gap-8 lg:grid-cols-3">
		<!-- SECCIÓN 1: GENERACIÓN DE FACTURAS (1/3 del ancho) -->
		<div class="lg:col-span-1">
			<div class="rounded-xl border border-neutral/10 bg-surface p-6 shadow-sm">
				<h3 class="mb-4 text-lg font-bold text-secondary">Generar Factura PDF</h3>
				<p class="mb-6 text-sm text-neutral">
					Ingresa el ID interno de la reserva para compilar y descargar la factura comercial.
				</p>

				<form onsubmit={descargarFactura} class="space-y-4">
					<Input
						id="idReserva"
						label="ID de la Reserva"
						type="number"
						min="1"
						bind:value={idReservaFactura}
						required
						placeholder="Ej: 1"
					/>
					<Button type="submit" variant="primary" class="w-full">Generar y Descargar PDF</Button>
				</form>
			</div>
		</div>

		<!-- SECCIÓN 2: REPORTE FINANCIERO Y DE OCUPACIÓN (2/3 del ancho) -->
		<div class="lg:col-span-2">
			<div class="rounded-xl border border-neutral/10 bg-surface p-6 shadow-sm">
				<div
					class="mb-6 flex flex-col items-start justify-between gap-4 md:flex-row md:items-center"
				>
					<h3 class="text-lg font-bold text-secondary">Reporte Financiero Diario</h3>

					<form
						onsubmit={filtrarOcupacion}
						class="mt-4 flex w-full flex-wrap items-end gap-3 md:mt-0 md:w-auto"
					>
						<div class="flex w-full gap-2 md:w-auto">
							<Input
								id="start"
								label="Desde"
								type="date"
								bind:value={startDate}
								required
								class="w-full md:w-auto"
							/>
							<Input
								id="end"
								label="Hasta"
								type="date"
								bind:value={endDate}
								required
								class="w-full md:w-auto"
							/>
						</div>
						<div class="mt-2 flex w-full gap-2 md:mt-0 md:w-auto">
							<Button type="submit" variant="outline" class="h-[42px] flex-1 md:flex-none"
								>Filtrar</Button
							>
							<Button
								type="button"
								variant="primary"
								class="h-[42px] flex-1 whitespace-nowrap md:flex-none"
								onclick={() =>
									window.open(`/reports/occupancy/pdf?start=${startDate}&end=${endDate}`, '_blank')}
							>
								📄 Descargar PDF
							</Button>
						</div>
					</form>
				</div>

				<div class="overflow-x-auto">
					<table class="w-full border-collapse text-left">
						<thead>
							<tr
								class="border-b border-neutral/10 bg-base/50 text-sm tracking-wider text-neutral uppercase"
							>
								<th class="px-4 py-3 font-semibold">Fecha</th>
								<th class="px-4 py-3 font-semibold">Línea de Negocio</th>
								<th class="px-4 py-3 text-center font-semibold">Cant. Ocupada</th>
								<th class="px-4 py-3 text-right font-semibold">Ingreso Generado</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-neutral/10">
							{#if occupancy.length === 0}
								<tr
									><td colspan="4" class="px-4 py-6 text-center text-neutral"
										>No hay ingresos registrados en estas fechas.</td
									></tr
								>
							{:else}
								{#each occupancy as row}
									<tr class="transition-colors hover:bg-base/30">
										<td class="px-4 py-3 font-medium text-dark">{row.out_fecha}</td>
										<td class="px-4 py-3">
											<span
												class="rounded-full px-2 py-1 text-xs font-bold
    {row.out_tipo === 'Habitación'
													? 'bg-blue-100 text-blue-700'
													: row.out_tipo === 'Área Social'
														? 'bg-purple-100 text-purple-700'
														: 'bg-red-100 text-red-700'}"
											>
												{row.out_tipo}
											</span>
										</td>
										<td class="px-4 py-3 text-center font-bold text-dark">{row.out_ocupadas}</td>
										<td class="px-4 py-3 text-right font-bold text-primary"
											>${row.out_ingreso.toLocaleString('es-CO')}</td
										>
									</tr>
								{/each}
							{/if}
						</tbody>
						<!-- PIE DE TABLA CON EL TOTAL DINÁMICO -->
						{#if occupancy.length > 0}
							<tfoot>
								<tr class="border-t-2 border-neutral/20 bg-base/50">
									<td colspan="3" class="px-4 py-4 text-right font-bold text-dark uppercase"
										>Total del Período:</td
									>
									<td class="px-4 py-4 text-right text-lg font-bold text-green-600">
										${occupancy
											.reduce((sum, row) => sum + row.out_ingreso, 0)
											.toLocaleString('es-CO')}
									</td>
								</tr>
							</tfoot>
						{/if}
					</table>
				</div>
			</div>
		</div>
	</div>
</div>
