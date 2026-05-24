<script lang="ts">
    import type { PageData } from './$types';
    import Button from '$lib/components/ui/Button.svelte';

    let { data }: { data: PageData } = $props();
    
    let reserva = $derived(data.detalle.reserva);
    let items = $derived(data.detalle.items);
    let pagos = $derived(data.detalle.pagos);
    let balance = $derived(data.balance);
</script>

<svelte:head>
    <title>Detalle Reserva {reserva.codigoReserva} | SandiStay Admin</title>
</svelte:head>

<div class="space-y-6">
    
    <!-- Cabecera y Botón de Volver -->
    <div class="flex justify-between items-center">
        <div class="flex items-center gap-4">
            <a href="/dashboard" class="text-neutral hover:text-primary transition-colors">
                ← Volver
            </a>
            <h2 class="text-2xl font-bold text-dark">Reserva {reserva.codigoReserva}</h2>
            <span class="px-3 py-1 rounded-full text-xs font-bold border" style="color: {reserva.estadoColor}; border-color: {reserva.estadoColor}; background-color: {reserva.estadoColor}15;">
                {reserva.estado}
            </span>
        </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        <!-- COLUMNA IZQUIERDA: Datos del Cliente y Activos -->
        <div class="lg:col-span-2 space-y-6">
            
            <!-- Tarjeta Cliente -->
            <div class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10">
                <h3 class="text-lg font-bold text-secondary mb-4">Información del Huésped</h3>
                <div class="grid grid-cols-2 gap-4 text-sm">
                    <div>
                        <p class="text-neutral">Titular de la Reserva</p>
                        <p class="font-medium text-dark">{reserva.nombreCliente}</p>
                    </div>
                    <div>
                        <p class="text-neutral">Huésped Principal</p>
                        <p class="font-medium text-dark">{reserva.nombreHuesped}</p>
                    </div>
                    <div>
                        <p class="text-neutral">Fecha de Registro</p>
                        <p class="font-medium text-dark">{new Date(reserva.fechaReserva).toLocaleString('es-CO')}</p>
                    </div>
                    <div>
                        <p class="text-neutral">Tipo de Reserva</p>
                        <p class="font-medium text-dark">{reserva.tipoReserva}</p>
                    </div>
                </div>
            </div>

            <!-- Tarjeta Ítems (Habitaciones/Áreas) -->
            <div class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10">
                <h3 class="text-lg font-bold text-secondary mb-4">Detalle de Alojamiento</h3>
                <div class="overflow-x-auto">
                    <table class="w-full text-left text-sm">
                        <thead class="border-b border-neutral/10 text-neutral">
                            <tr>
                                <th class="pb-2">Activo</th>
                                <th class="pb-2">Check-in</th>
                                <th class="pb-2">Check-out</th>
                                <th class="pb-2 text-right">Total</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-neutral/10">
                            {#each items as item}
                                <tr>
                                    <td class="py-3 font-medium text-dark">{item.nombreHabitacion} ({item.codigoHabitacion})</td>
                                    <td class="py-3">{item.fechaInicio}</td>
                                    <td class="py-3">{item.fechaFin}</td>
                                    <td class="py-3 text-right font-medium">${item.totalReserva.toLocaleString('es-CO')}</td>
                                </tr>
                            {/each}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- COLUMNA DERECHA: Finanzas y Pagos -->
        <div class="lg:col-span-1 space-y-6">
            
            <!-- Tarjeta Balance Financiero -->
            {#if balance}
                <div class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10">
                    <h3 class="text-lg font-bold text-secondary mb-4">Estado de Cuenta</h3>
                    <div class="space-y-3 text-sm">
                        <div class="flex justify-between">
                            <span class="text-neutral">Total Reserva</span>
                            <span class="font-medium text-dark">${balance.totalReserva.toLocaleString('es-CO')}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-neutral">Total Pagado</span>
                            <span class="font-medium text-green-600">${balance.totalPagado.toLocaleString('es-CO')}</span>
                        </div>
                        <div class="pt-3 border-t border-neutral/10 flex justify-between items-center">
                            <span class="font-bold text-dark">Saldo Pendiente</span>
                            <span class="font-bold text-lg {balance.saldoPendiente > 0 ? 'text-red-600' : 'text-dark'}">
                                ${balance.saldoPendiente.toLocaleString('es-CO')}
                            </span>
                        </div>
                    </div>
                </div>
            {/if}

            <!-- Tarjeta Historial de Pagos -->
            <div class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10">
                <h3 class="text-lg font-bold text-secondary mb-4">Historial de Transacciones</h3>
                {#if pagos.length === 0}
                    <p class="text-sm text-neutral">No hay pagos registrados.</p>
                {:else}
                    <div class="space-y-4">
                        {#each pagos as pago}
                            <div class="p-3 rounded-lg border {pago.idEstadoPago === 4 ? 'bg-red-50 border-red-100' : 'bg-base/50 border-neutral/10'}">
                                <div class="flex justify-between items-start mb-1">
                                    <span class="text-xs font-bold text-neutral">{pago.codigoPago}</span>
                                    <span class="text-sm font-bold text-dark">${pago.monto.toLocaleString('es-CO')}</span>
                                </div>
                                <p class="text-xs text-neutral mb-1">Ref: {pago.numeroReferencia || 'N/A'}</p>
                                <p class="text-xs text-neutral italic">{pago.observaciones}</p>
                            </div>
                        {/each}
                    </div>
                {/if}
            </div>

        </div>
    </div>
</div>