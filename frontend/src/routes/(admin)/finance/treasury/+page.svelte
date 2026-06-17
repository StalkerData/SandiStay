<script lang="ts">
    import type { PageData, ActionData } from './$types';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import { enhance } from '$app/forms';

    let { data, form }: { data: PageData, form: ActionData } = $props();
    
    let receivables = $derived(data.receivables);
    let refunds = $derived(data.refunds);

    let isSubmitting = $state(false);

    // --- ESTADO DEL MODAL DE PAGO ---
    let showPaymentModal = $state(false);
    let selectedReserva = $state<any>(null);
    
    // Variables del formulario de pago
    let monto = $state('');
    let idMetodoPago = $state(1); // 1 = Efectivo por defecto
    let referencia = $state('');
    let observaciones = $state('');

    function openPaymentModal(reserva: any) {
        selectedReserva = reserva;
        monto = reserva.saldoPendiente.toString(); // Sugerimos pagar el saldo total por defecto
        referencia = '';
        observaciones = '';
        showPaymentModal = true;
    }
</script>

<svelte:head>
    <title>Tesorería | SandiStay Admin</title>
</svelte:head>

<div class="space-y-8 relative">
    
    <div>
        <h2 class="text-2xl font-bold text-dark">Tesorería y Cartera</h2>
        <p class="text-sm text-neutral">Gestión de cuentas por cobrar (saldos pendientes) y cuentas por pagar (reembolsos).</p>
    </div>

    {#if form?.error}
        <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-md shadow-sm animate-fade-in">{form.error}</div>
    {/if}
    {#if form?.success}
        <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded-md shadow-sm animate-fade-in">{form.message}</div>
    {/if}

    <div class="grid grid-cols-1 xl:grid-cols-2 gap-8">
        
        <!-- PANEL A: CUENTAS POR COBRAR -->
        <div class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden flex flex-col">
            <div class="bg-base/50 px-6 py-4 border-b border-neutral/10 flex justify-between items-center">
                <h3 class="font-bold text-secondary text-lg">Cuentas por Cobrar</h3>
                <span class="bg-red-100 text-red-700 text-xs px-2 py-1 rounded-full font-bold">{receivables.length} Pendientes</span>
            </div>
            
            <div class="overflow-x-auto flex-1">
                <table class="w-full text-left border-collapse">
                    <thead>
                        <tr class="bg-base/20 border-b border-neutral/10 text-xs text-neutral uppercase tracking-wider">
                            <th class="px-4 py-3 font-semibold">Reserva</th>
                            <th class="px-4 py-3 font-semibold">Cliente</th>
                            <th class="px-4 py-3 font-semibold text-right">Saldo Pendiente</th>
                            <th class="px-4 py-3 font-semibold text-center">Acción</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-neutral/10">
                        {#if receivables.length === 0}
                            <tr><td colspan="4" class="px-4 py-8 text-center text-neutral">No hay saldos pendientes por cobrar.</td></tr>
                        {:else}
                            {#each receivables as rec}
                                <tr class="hover:bg-base/30 transition-colors">
                                    <td class="px-4 py-3">
                                        <a href={`/dashboard/bookings/${rec.idReserva}`} class="font-bold text-primary hover:underline">{rec.codigoReserva}</a>
                                        <p class="text-xs text-neutral">{rec.tipoReserva}</p>
                                    </td>
                                    <td class="px-4 py-3 text-sm font-medium text-dark">{rec.nombreCliente}</td>
                                    <td class="px-4 py-3 text-right font-bold text-red-600">${rec.saldoPendiente.toLocaleString('es-CO')}</td>
                                    <td class="px-4 py-3 text-center">
                                        <Button variant="outline" class="px-3 py-1 text-xs" onclick={() => openPaymentModal(rec)}>
                                            💵 Cobrar
                                        </Button>
                                    </td>
                                </tr>
                            {/each}
                        {/if}
                    </tbody>
                </table>
            </div>
        </div>

        <!-- PANEL B: REEMBOLSOS PENDIENTES -->
        <div class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden flex flex-col">
            <div class="bg-base/50 px-6 py-4 border-b border-neutral/10 flex justify-between items-center">
                <h3 class="font-bold text-secondary text-lg">Reembolsos Pendientes</h3>
                <span class="bg-orange-100 text-orange-700 text-xs px-2 py-1 rounded-full font-bold">{refunds.length} Por Devolver</span>
            </div>
            
            <div class="overflow-x-auto flex-1">
                <table class="w-full text-left border-collapse">
                    <thead>
                        <tr class="bg-base/20 border-b border-neutral/10 text-xs text-neutral uppercase tracking-wider">
                            <th class="px-4 py-3 font-semibold">Código Pago</th>
                            <th class="px-4 py-3 font-semibold">Reserva ID</th>
                            <th class="px-4 py-3 font-semibold text-right">Monto a Devolver</th>
                            <th class="px-4 py-3 font-semibold text-center">Acción</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-neutral/10">
                        {#if refunds.length === 0}
                            <tr><td colspan="4" class="px-4 py-8 text-center text-neutral">No hay reembolsos pendientes.</td></tr>
                        {:else}
                            {#each refunds as ref}
                                <tr class="hover:bg-base/30 transition-colors">
                                    <td class="px-4 py-3">
                                        <span class="font-bold text-dark">{ref.codigoPago}</span>
                                        <p class="text-xs text-neutral truncate max-w-[150px]" title={ref.observaciones}>{ref.observaciones}</p>
                                    </td>
                                    <td class="px-4 py-3 text-sm text-primary font-medium">
                                        <a href={`/dashboard/bookings/${ref.idReserva}`} class="hover:underline">Ver Reserva {ref.idReserva}</a>
                                    </td>
                                    <td class="px-4 py-3 text-right font-bold text-orange-600">${ref.monto.toLocaleString('es-CO')}</td>
                                    <td class="px-4 py-3 text-center">
                                        <form method="POST" action="?/processRefund" use:enhance={() => {
                                            if(!confirm('¿Confirmas que ya realizaste la transferencia bancaria al cliente?')) return ({ cancel }) => cancel();
                                            isSubmitting = true;
                                            return async ({ update }) => { await update(); isSubmitting = false; };
                                        }}>
                                            <input type="hidden" name="idPago" value={ref.idPago} />
                                            <Button type="submit" variant="primary" class="px-3 py-1 text-xs" disabled={isSubmitting}>
                                                ✓ Marcar Devuelto
                                            </Button>
                                        </form>
                                    </td>
                                </tr>
                            {/each}
                        {/if}
                    </tbody>
                </table>
            </div>
        </div>

    </div>
</div>

<!-- ========================================== -->
<!-- MODAL: REGISTRAR PAGO / ABONO              -->
<!-- ========================================== -->
{#if showPaymentModal}
    <div class="fixed inset-0 bg-dark/60 flex items-center justify-center z-50 px-4 animate-fade-in">
        <div class="bg-surface rounded-xl shadow-2xl max-w-md w-full p-6 border border-neutral/10">
            <h3 class="text-xl font-bold text-dark mb-2">Registrar Pago</h3>
            <p class="text-sm text-neutral mb-6">
                Reserva: <span class="font-bold text-primary">{selectedReserva?.codigoReserva}</span><br>
                Saldo Pendiente: <span class="font-bold text-red-600">${selectedReserva?.saldoPendiente.toLocaleString('es-CO')}</span>
            </p>
            
            <form method="POST" action="?/registerPayment" use:enhance={() => {
                isSubmitting = true;
                return async ({ update }) => { await update(); isSubmitting = false; showPaymentModal = false; };
            }}>
                <input type="hidden" name="idReserva" value={selectedReserva?.idReserva} />
                
                <div class="space-y-4 mb-6">
                    <Input 
                        id="monto" 
                        name="monto" 
                        label="Monto a Pagar ($)" 
                        type="number" 
                        step="0.01" 
                        max={selectedReserva?.saldoPendiente}
                        bind:value={monto} 
                        required 
                    />
                    
                    <div class="flex flex-col gap-1">
                        <label for="idMetodoPago" class="text-sm font-medium text-dark">Método de Pago</label>
                        <select name="idMetodoPago" bind:value={idMetodoPago} class="px-3 py-2 border border-neutral/30 rounded-md">
                            <option value={1}>Efectivo</option>
                            <option value={2}>Nequi</option>
                            <option value={3}>Daviplata</option>
                        </select>
                    </div>

                    <Input id="referencia" name="referencia" label="Número de Referencia (Opcional)" type="text" bind:value={referencia} placeholder="Ej: Transferencia #12345" />
                    
                    <div class="flex flex-col gap-1">
                        <label for="observaciones" class="text-sm font-medium text-dark">Observaciones</label>
                        <textarea name="observaciones" bind:value={observaciones} rows="2" class="px-3 py-2 border border-neutral/30 rounded-md"></textarea>
                    </div>
                </div>
                
                <div class="flex justify-end gap-3">
                    <Button type="button" variant="outline" onclick={() => showPaymentModal = false}>Cancelar</Button>
                    <Button type="submit" variant="primary" disabled={isSubmitting}>Registrar Pago</Button>
                </div>
            </form>
        </div>
    </div>
{/if}