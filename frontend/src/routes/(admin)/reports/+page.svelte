<script lang="ts">
	import { goto } from '$app/navigation';
	import Button from '$lib/components/ui/Button.svelte';
	import Input from '$lib/components/ui/Input.svelte';
	import type { PageData } from './$types';
	import type { OccupancyRow } from './+page.server';

	// --- PROPS & ESTADO DERIVADO ---
	let { data }: { data: PageData } = $props();
	let occupancy: OccupancyRow[] = $derived(data.occupancy);

	// Lógica de negocio extraída del HTML (Clean Code)
	let totalIngresos = $derived(
		occupancy.reduce((sum, row) => sum + row.out_ingreso, 0)
	);

	// --- ESTADO DE FILTROS ---
	let startDate = $state(data.start);
	let endDate = $state(data.end);

	// --- ESTADO DE FACTURACIÓN ---
	let codigoReserva = $state('');

	/**
	 * @description Recarga la página inyectando los parámetros de fecha en la URL.
	 * SvelteKit interceptará esto y re-ejecutará la función load del servidor.
	 */
	function filtrarOcupacion(e: Event) {
		e.preventDefault();
		if (startDate && endDate) {
			goto(`/reports?start=${startDate}&end=${endDate}`);
		}
	}

	/**
	 * @description Abre el endpoint proxy de SvelteKit en una nueva pestaña para descargar el PDF.
	 */
	function descargarFactura(e: Event) {
		e.preventDefault();
		if (codigoReserva.trim()) {
			window.open(`/reports/invoice/${codigoReserva.trim()}`, '_blank');
			codigoReserva = ''; // Limpiamos el input tras la acción
		}
	}
</script>

<svelte:head>
	<title>Reportes y Facturación | SandiStay Admin</title>
</svelte:head>

<main class="space-y-8">
	<header>
		<h1 class="text-2xl font-bold text-dark">Reportes y Facturación</h1>
		<p class="text-sm text-neutral">Métricas de ocupación y generación de documentos fiscales (PDF).</p>
	</header>

	<div class="grid grid-cols-1 gap-8 lg:grid-cols-3">
		
		<!-- SECCIÓN 1: GENERACIÓN DE FACTURAS (1/3 del ancho) -->
		<section class="lg:col-span-1">
			<div class="rounded-xl border border-neutral/10 bg-surface p-6 shadow-sm">
				<h2 class="mb-4 text-lg font-bold text-secondary">Generar Factura PDF</h2>
				<p class="mb-6 text-sm text-neutral">
					Ingresa el código de la reserva para compilar y descargar la factura comercial generada por JasperReports.
				</p>

				<form onsubmit={descargarFactura} class="space-y-4" aria-label="Formulario para descargar factura">
					<Input 
                        id="codigoReserva" 
                        label="Código de la Reserva" 
                        type="text" 
                        bind:value={codigoReserva} 
                        required 
                        placeholder="Ej: RES-A1B2C3D4"
                    />
					<Button type="submit" variant="primary" class="w-full transition-transform active:scale-95">
						Generar y Descargar PDF
					</Button>
				</form>
			</div>
		</section>

		<!-- SECCIÓN 2: REPORTE FINANCIERO Y DE OCUPACIÓN (2/3 del ancho) -->
		<section class="lg:col-span-2">
			<div class="rounded-xl border border-neutral/10 bg-surface p-6 shadow-sm">
				
				<header class="mb-6 flex flex-col items-start justify-between gap-4 md:flex-row md:items-center">
					<h2 class="text-lg font-bold text-secondary">Reporte Financiero Diario</h2>

					<form onsubmit={filtrarOcupacion} class="mt-4 flex w-full flex-wrap items-end gap-3 md:mt-0 md:w-auto" aria-label="Filtros de fecha para el reporte">
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
							<Button type="submit" variant="outline" class="h-[42px] flex-1 md:flex-none transition-colors">
								Filtrar
							</Button>
							<Button
								type="button"
								variant="primary"
								class="h-[42px] flex-1 whitespace-nowrap md:flex-none transition-transform active:scale-95"
								onclick={() => window.open(`/reports/occupancy/pdf?start=${startDate}&end=${endDate}`, '_blank')}
							>
								📄 Descargar PDF
							</Button>
						</div>
					</form>
				</header>

				<div class="overflow-x-auto rounded-lg border border-neutral/10">
					<table class="w-full border-collapse text-left" aria-label="Tabla de reporte financiero">
						<thead>
							<tr class="border-b border-neutral/10 bg-base/50 text-sm tracking-wider text-neutral uppercase">
								<th scope="col" class="px-4 py-3 font-semibold">Fecha</th>
								<th scope="col" class="px-4 py-3 font-semibold">Línea de Negocio</th>
								<th scope="col" class="px-4 py-3 text-center font-semibold">Cant. Ocupada</th>
								<th scope="col" class="px-4 py-3 text-right font-semibold">Ingreso Generado</th>
							</tr>
						</thead>
						<tbody class="divide-y divide-neutral/10">
							{#if occupancy.length === 0}
								<tr>
									<td colspan="4" class="px-4 py-8 text-center text-neutral bg-base/20">
										No hay ingresos registrados en estas fechas.
									</td>
								</tr>
							{:else}
								{#each occupancy as row}
									<tr class="transition-colors hover:bg-base/30">
										<td class="px-4 py-3 font-medium text-dark">{row.out_fecha}</td>
										<td class="px-4 py-3">
											<span class="rounded-full px-2.5 py-1 text-xs font-bold tracking-wide
												{row.out_tipo === 'Habitación' ? 'bg-blue-100 text-blue-700' : 
												 row.out_tipo === 'Área Social' ? 'bg-purple-100 text-purple-700' : 
												 'bg-red-100 text-red-700'}">
												{row.out_tipo}
											</span>
										</td>
										<td class="px-4 py-3 text-center font-bold text-dark">{row.out_ocupadas}</td>
										<td class="px-4 py-3 text-right font-bold text-primary">
											${row.out_ingreso.toLocaleString('es-CO')}
										</td>
									</tr>
								{/each}
							{/if}
						</tbody>
						
						<!-- PIE DE TABLA CON EL TOTAL DINÁMICO -->
						{#if occupancy.length > 0}
							<tfoot>
								<tr class="border-t-2 border-neutral/20 bg-base/50">
									<td colspan="3" class="px-4 py-4 text-right font-bold text-dark uppercase tracking-wider">
										Total del Período:
									</td>
									<td class="px-4 py-4 text-right text-lg font-bold text-green-600">
										${totalIngresos.toLocaleString('es-CO')}
									</td>
								</tr>
							</tfoot>
						{/if}
					</table>
				</div>
			</div>
		</section>
	</div>
</main>