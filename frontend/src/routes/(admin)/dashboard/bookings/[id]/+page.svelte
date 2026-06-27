<script lang="ts">
    import type { PageData, ActionData } from './$types';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import { enhance } from '$app/forms';

    // --- PROPS & ESTADO DERIVADO ---
    let { data, form }: { data: PageData, form: ActionData } = $props();
    
    let reserva = $derived(data.detalle.reserva);
    let items = $derived(data.detalle.items);
    let pagos = $derived(data.detalle.pagos);
    let balance = $derived(data.balance);
    let clientHistory = $derived(data.clientHistory);

    // --- ESTADO DEL MODAL DE PAGO ---
    let showPaymentModal = $state(false);
    let isSubmitting = $state(false);
    let metodoPago = $state('EFECTIVO');
    let referencia = $state('');
</script>

<svelte:head>
    <title>Detalle Reserva {reserva.codigoReserva} | SandiStay Admin</title>
</svelte:head>

<main class="space-y-6">
    
    <!-- Cabecera y Botón de Volver -->
    <header class="flex justify-between items-center">
        <div class="flex items-center gap-4">
            <a href="/dashboard" class="text-neutral hover:text-primary font-medium transition-colors flex items-center">
                <svg class="w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
                Volver
            </a>
            <h1 class="text-2xl font-bold text-dark">Reserva {reserva.codigoReserva}</h1>
            <span class="px-3 py-1 rounded-full text-xs font-bold border shadow-sm" style="color: {reserva.estadoColor}; border-color: {reserva.estadoColor}; background-color: {reserva.estadoColor}15;">
                {reserva.estado}
            </span>
        </div>
    </header>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        <!-- COLUMNA IZQUIERDA: Datos del Cliente y Activos -->
        <div class="lg:col-span-2 space-y-6">
            
            <!-- Tarjeta Cliente -->
            <section class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10">
                <h2 class="text-lg font-bold text-secondary mb-4">Información del Huésped</h2>
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
            </section>

            <!-- Tarjeta Perfil del Cliente (CRM) -->
            {#if clientHistory}
                <section class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10 border-l-4 border-l-secondary animate-fade-in">
                    <header class="flex justify-between items-center mb-4">
                        <h2 class="text-lg font-bold text-secondary">Perfil de {reserva.nombreCliente} (CRM)</h2>
                        <span class="bg-secondary/10 text-secondary text-xs px-3 py-1 rounded-full font-bold tracking-wide">
                            {clientHistory.totalReservas > 5 ? '⭐ VIP' : 'REGULAR'}
                        </span>
                    </header>
                    
                    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                        <div class="bg-base/50 p-3 rounded-lg text-center border border-neutral/5">
                            <p class="text-2xl font-bold text-dark">{clientHistory.totalReservas}</p>
                            <p class="text-xs text-neutral uppercase tracking-wider font-bold">Reservas Totales</p>
                        </div>
                        <div class="bg-green-50 p-3 rounded-lg text-center border border-green-100">
                            <p class="text-2xl font-bold text-green-600">{clientHistory.reservasCompletadas}</p>
                            <p class="text-xs text-green-800 uppercase tracking-wider font-bold">Completadas</p>
                        </div>
                        <div class="bg-red-50 p-3 rounded-lg text-center border border-red-100">
                            <p class="text-2xl font-bold text-red-600">{clientHistory.reservasCanceladas}</p>
                            <p class="text-xs text-red-800 uppercase tracking-wider font-bold">Canceladas</p>
                        </div>
                        <div class="bg-blue-50 p-3 rounded-lg text-center border border-blue-100">
                            <p class="text-lg font-bold text-primary">${(clientHistory.totalGastado / 1000000).toFixed(1)}M</p>
                            <p class="text-xs text-blue-800 uppercase tracking-wider font-bold">Total Gastado</p>
                        </div>
                    </div>
                    
                    <footer class="mt-4 pt-4 border-t border-neutral/10 flex flex-col sm:flex-row justify-between text-sm gap-2">
                        <p><span class="text-neutral">Última visita:</span> <span class="font-medium text-dark">{clientHistory.ultimaReserva || 'N/A'}</span></p>
                        <p><span class="text-neutral">Favorita:</span> <span class="font-medium text-dark">{clientHistory.habitacionFavorita || 'N/A'}</span></p>
                    </footer>
                </section>
            {/if}

            <!-- Tarjeta Ítems (Habitaciones/Áreas) -->
            <section class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10">
                <h2 class="text-lg font-bold text-secondary mb-4">Detalle de Alojamiento</h2>
                <div class="overflow-x-auto">
                    <table class="w-full text-left text-sm">
                        <thead class="border-b border-neutral/10 text-neutral uppercase tracking-wider text-xs">
                            <tr>
                                <th scope="col" class="pb-2 font-semibold">Activo</th>
                                <th scope="col" class="pb-2 font-semibold">Check-in</th>
                                <th scope="col" class="pb-2 font-semibold">Check-out</th>
                                <th scope="col" class="pb-2 font-semibold text-right">Total</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-neutral/10">
                            {#each items as item}
                                <tr class="hover:bg-base/30 transition-colors">
                                    <td class="py-3 font-medium text-dark">{item.nombreHabitacion} ({item.codigoHabitacion})</td>
                                    <td class="py-3">{item.fechaInicio}</td>
                                    <td class="py-3">{item.fechaFin}</td>
                                    <td class="py-3 text-right font-bold text-primary">${item.totalReserva.toLocaleString('es-CO')}</td>
                                </tr>
                            {/each}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>

        <!-- COLUMNA DERECHA: Finanzas y Pagos -->
        <aside class="lg:col-span-1 space-y-6">
            
            <!-- Tarjeta Balance Financiero -->
            {#if balance}
                <section class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10">
                    <h2 class="text-lg font-bold text-secondary mb-4">Estado de Cuenta</h2>
                    
                    <!-- Alertas (ARIA Live) -->
                    <div aria-live="polite">
                        {#if form?.error}
                            <div class="bg-red-100 text-red-700 p-3 rounded-md text-sm mb-4 border border-red-200" role="alert">{form.error}</div>
                        {/if}
                        {#if form?.success}
                            <div class="bg-green-100 text-green-700 p-3 rounded-md text-sm mb-4 border border-green-200" role="status">{form.message}</div>
                        {/if}
                    </div>

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

                    <!-- BUG FIX: Botón para abrir el modal de pago -->
                    {#if balance.saldoPendiente > 0 && (reserva.estado === 'Pendiente' || reserva.estado === 'Confirmada' || reserva.estado === 'En Curso')}
                        <div class="mt-6 pt-4 border-t border-neutral/10">
                            <Button variant="primary" class="w-full" onclick={() => { showPaymentModal = true; form = null; }}>
                                Registrar Pago
                            </Button>
                        </div>
                    {/if}
                </section>
            {/if}

            <!-- Tarjeta Historial de Pagos -->
            <section class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10">
                <h2 class="text-lg font-bold text-secondary mb-4">Historial de Transacciones</h2>
                {#if pagos.length === 0}
                    <p class="text-sm text-neutral bg-base/50 p-4 rounded-lg text-center">No hay pagos registrados.</p>
                {:else}
                    <div class="space-y-4">
                        {#each pagos as pago}
                            <div class="p-3 rounded-lg border {pago.idEstadoPago === 4 ? 'bg-red-50 border-red-200' : 'bg-base/50 border-neutral/20'} shadow-sm">
                                <div class="flex justify-between items-start mb-1">
                                    <span class="text-xs font-bold text-neutral">{pago.codigoPago}</span>
                                    <span class="text-sm font-bold {pago.idEstadoPago === 4 ? 'text-red-600 line-through' : 'text-dark'}">
                                        ${pago.monto.toLocaleString('es-CO')}
                                    </span>
                                </div>
                                <p class="text-xs text-neutral mb-1">Ref: {pago.numeroReferencia || 'N/A'}</p>
                                {#if pago.observaciones}
                                    <p class="text-xs text-neutral italic bg-white/50 p-1 rounded mt-1">{pago.observaciones}</p>
                                {/if}
                            </div>
                        {/each}
                    </div>
                {/if}
            </section>

        </aside>
    </div>
</main>

<!-- ========================================== -->
<!-- MODAL DE PAGO DE SALDO (Accesible)         -->
<!-- ========================================== -->
{#if showPaymentModal && balance}
    <div class="fixed inset-0 bg-dark/60 flex items-center justify-center z-50 px-4 animate-fade-in" role="dialog" aria-modal="true" aria-labelledby="modal-pago-title">
        <div class="bg-surface rounded-xl shadow-2xl max-w-md w-full p-6 border border-neutral/10">
            <h2 id="modal-pago-title" class="text-xl font-bold text-dark mb-2">Registrar Pago</h2>
            <p class="text-sm text-neutral mb-6">
                El cliente debe <strong class="text-red-600">${balance.saldoPendiente.toLocaleString('es-CO')}</strong>.
            </p>
            
            <form method="POST" action="?/payBalance" use:enhance={() => {
                isSubmitting = true;
                return async ({ update }) => {
                    await update();
                    isSubmitting = false;
                    // Solo cerramos el modal si la operación fue exitosa
                    if (form?.success) {
                        showPaymentModal = false;
                    }
                };
            }}>
                <div class="space-y-4 mb-6">
                    <div class="flex flex-col gap-1">
                        <label for="metodoPago" class="text-sm font-medium text-dark">Método de Pago</label>
                        <select id="metodoPago" name="metodoPago" bind:value={metodoPago} class="px-3 py-2 border border-neutral/30 rounded-md focus:ring-secondary bg-base/50">
                            <option value="EFECTIVO">Efectivo</option>
                            <option value="DATAFONO">Tarjeta / Datáfono</option>
                            <option value="TRANSFERENCIA">Transferencia Bancaria</option>
                        </select>
                    </div>
                    
                    {#if metodoPago !== 'EFECTIVO'}
                        <div class="animate-fade-in">
                            <Input 
                                id="referencia" 
                                name="referencia" 
                                label="Número de Aprobación / Referencia" 
                                type="text" 
                                bind:value={referencia} 
                                required 
                                placeholder="Ej: AP-123456"
                            />
                        </div>
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