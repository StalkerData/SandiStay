<script lang="ts">
    import type { PageData } from './$types';
    import Button from '$lib/components/ui/Button.svelte';
    import { enhance } from '$app/forms';
    import Input from '$lib/components/ui/Input.svelte';
    import type { ActionData } from './$types';

    let { data, form }: { data: PageData, form: ActionData } = $props();
    
    let reserva = $derived(data.detalle.reserva);
    let items = $derived(data.detalle.items);
    let pagos = $derived(data.detalle.pagos);
    let balance = $derived(data.balance);
    let clientHistory = $derived(data.clientHistory);

    // Variables para el Modal de Pago
    let showPaymentModal = $state(false);
    let isSubmitting = $state(false);
    let metodoPago = $state('EFECTIVO');
    let referencia = $state('');


    
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

            <!-- NUEVA TARJETA: Perfil del Cliente (CRM) -->
            {#if clientHistory}
                <div class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10 border-l-4 border-l-secondary animate-fade-in">
                    <div class="flex justify-between items-center mb-4">
                        <h3 class="text-lg font-bold text-secondary">Perfil de {reserva.nombreCliente} (CRM)</h3>
                        <span class="bg-secondary/10 text-secondary text-xs px-2 py-1 rounded-full font-bold">
                            {clientHistory.totalReservas > 5 ? 'VIP' : 'Regular'}
                        </span>
                    </div>
                    
                    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                        <div class="bg-base/50 p-3 rounded-lg text-center">
                            <p class="text-2xl font-bold text-dark">{clientHistory.totalReservas}</p>
                            <p class="text-xs text-neutral uppercase tracking-wider">Reservas Totales</p>
                        </div>
                        <div class="bg-base/50 p-3 rounded-lg text-center">
                            <p class="text-2xl font-bold text-green-600">{clientHistory.reservasCompletadas}</p>
                            <p class="text-xs text-neutral uppercase tracking-wider">Completadas</p>
                        </div>
                        <div class="bg-base/50 p-3 rounded-lg text-center">
                            <p class="text-2xl font-bold text-red-600">{clientHistory.reservasCanceladas}</p>
                            <p class="text-xs text-neutral uppercase tracking-wider">Canceladas</p>
                        </div>
                        <div class="bg-base/50 p-3 rounded-lg text-center">
                            <p class="text-lg font-bold text-primary">${(clientHistory.totalGastado / 1000000).toFixed(1)}M</p>
                            <p class="text-xs text-neutral uppercase tracking-wider">Total Gastado</p>
                        </div>
                    </div>
                    
                    <div class="mt-4 pt-4 border-t border-neutral/10 flex justify-between text-sm">
                        <p><span class="text-neutral">Última visita:</span> <span class="font-medium text-dark">{clientHistory.ultimaReserva || 'N/A'}</span></p>
                        <p><span class="text-neutral">Favorita:</span> <span class="font-medium text-dark">{clientHistory.habitacionFavorita || 'N/A'}</span></p>
                    </div>
                </div>
            {/if}

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
                    
                    <!-- Alertas -->
                    {#if form?.error}
                        <div class="bg-red-100 text-red-700 p-3 rounded-md text-sm mb-4">{form.error}</div>
                    {/if}
                    {#if form?.success}
                        <div class="bg-green-100 text-green-700 p-3 rounded-md text-sm mb-4">{form.message}</div>
                    {/if}

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

<!-- MODAL DE PAGO DE SALDO -->
{#if showPaymentModal}
    <div class="fixed inset-0 bg-dark/60 flex items-center justify-center z-50 px-4 animate-fade-in">
        <div class="bg-surface rounded-xl shadow-2xl max-w-md w-full p-6 border border-neutral/10">
            <h3 class="text-xl font-bold text-dark mb-2">Registrar Pago</h3>
            <p class="text-sm text-neutral mb-6">
                El cliente debe <strong class="text-red-600">${balance.saldoPendiente.toLocaleString('es-CO')}</strong>.
            </p>
            
            <form method="POST" action="?/payBalance" use:enhance={() => {
                isSubmitting = true;
                return async ({ update }) => {
                    await update();
                    isSubmitting = false;
                    showPaymentModal = false;
                };
            }}>
                <div class="space-y-4 mb-6">
                    <div class="flex flex-col gap-1">
                        <label for="metodoPago" class="text-sm font-medium text-dark">Método de Pago</label>
                        <select name="metodoPago" bind:value={metodoPago} class="px-3 py-2 border border-neutral/30 rounded-md">
                            <option value="EFECTIVO">Efectivo</option>
                            <option value="DATAFONO">Tarjeta / Datáfono</option>
                            <option value="TRANSFERENCIA">Transferencia Bancaria</option>
                        </select>
                    </div>
                    
                    {#if metodoPago !== 'EFECTIVO'}
                        <Input 
                            id="referencia" 
                            name="referencia" 
                            label="Número de Aprobación / Referencia" 
                            type="text" 
                            bind:value={referencia} 
                            required 
                        />
                    {/if}
                </div>
                
                <div class="flex justify-end gap-3">
                    <Button type="button" variant="outline" onclick={() => showPaymentModal = false} disabled={isSubmitting}>
                        Cancelar
                    </Button>
                    <Button type="submit" variant="primary" disabled={isSubmitting}>
                        {isSubmitting ? 'Procesando...' : 'Confirmar Pago'}
                    </Button>
                </div>
            </form>
        </div>
    </div>
{/if}