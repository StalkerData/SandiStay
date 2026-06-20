<script lang="ts">
    import type { PageData, ActionData } from './$types';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import { enhance } from '$app/forms';

    let { data, form }: { data: PageData, form: ActionData } = $props();
    let isSubmitting = $state(false);
    let documento = $state(form?.documento || '');
    
    // Verificamos si es Admin para mostrar el selector
    let isAdmin = $derived(data.userRole === 'ADMIN');
</script>

<svelte:head>
    <title>CRM Clientes | SandiStay Admin</title>
</svelte:head>

<div class="max-w-6xl mx-auto space-y-8">
    
    <div>
        <h2 class="text-2xl font-bold text-dark">CRM & Fidelización</h2>
        <p class="text-sm text-neutral">Consulta el historial, comportamiento y reservas de los huéspedes del hotel.</p>
    </div>

    <div class="bg-surface p-6 rounded-xl shadow-sm border border-neutral/10">
        <form method="POST" action="?/searchClient" class="flex flex-col md:flex-row items-end gap-4" use:enhance={() => {
            isSubmitting = true;
            return async ({ update }) => { await update(); isSubmitting = false; };
        }}>
            <div class="flex-1 w-full">
                <Input id="documento" name="documento" label="Número de Documento del Cliente" type="text" bind:value={documento} required placeholder="Ej: 123456789" />
            </div>
            <Button type="submit" variant="primary" class="h-[42px] w-full md:w-auto" disabled={isSubmitting}>
                {isSubmitting ? 'Buscando...' : '🔍 Buscar Cliente'}
            </Button>
        </form>
    </div>

    {#if form?.error}
        <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-md shadow-sm animate-fade-in">{form.error}</div>
    {/if}
    {#if form?.message}
        <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded-md shadow-sm animate-fade-in">{form.message}</div>
    {/if}

    {#if form?.success && form?.history}
        <div class="bg-surface p-8 rounded-xl shadow-lg border border-neutral/10 border-t-4 border-t-secondary animate-fade-in">
            
            <div class="flex justify-between items-start mb-8 pb-6 border-b border-neutral/10">
                <div>
                    <h3 class="text-2xl font-bold text-dark">{form.clientName}</h3>
                    <p class="text-neutral">Documento: {form.documento}</p>
                </div>
                
                <div class="flex items-center gap-4">
                    <!-- Etiqueta Visual -->
                    <span class="bg-secondary/10 text-secondary px-4 py-2 rounded-full font-bold text-lg tracking-wider">
                        {form.idTipoCliente === 2 ? '⭐ PREMIUM' : form.idTipoCliente === 3 ? '🏢 CORPORATIVO' : 'REGULAR'}
                    </span>
                    
                    <!-- Selector Manual (Solo Admin) -->
                    {#if isAdmin}
                        <form method="POST" action="?/updateCategory" use:enhance>
                            <input type="hidden" name="idCliente" value={form.idCliente} />
                            <select name="idTipoCliente" class="text-sm border-neutral/30 rounded-md bg-base/50 focus:ring-secondary" onchange={(e) => e.currentTarget.form.submit()}>
                                <option value="1" selected={form.idTipoCliente === 1}>Regular (0%)</option>
                                <option value="2" selected={form.idTipoCliente === 2}>Premium (10%)</option>
                                <option value="3" selected={form.idTipoCliente === 3}>Corporativo (15%)</option>
                            </select>
                        </form>
                    {/if}
                </div>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                <div class="bg-base/50 p-6 rounded-xl text-center border border-neutral/5">
                    <p class="text-4xl font-bold text-dark mb-2">{form.history.totalReservas}</p>
                    <p class="text-xs text-neutral uppercase tracking-wider font-bold">Reservas Totales</p>
                </div>
                <div class="bg-green-50 p-6 rounded-xl text-center border border-green-100">
                    <p class="text-4xl font-bold text-green-600 mb-2">{form.history.reservasCompletadas}</p>
                    <p class="text-xs text-green-800 uppercase tracking-wider font-bold">Completadas</p>
                </div>
                <div class="bg-red-50 p-6 rounded-xl text-center border border-red-100">
                    <p class="text-4xl font-bold text-red-600 mb-2">{form.history.reservasCanceladas}</p>
                    <p class="text-xs text-red-800 uppercase tracking-wider font-bold">Canceladas</p>
                </div>
                <div class="bg-blue-50 p-6 rounded-xl text-center border border-blue-100">
                    <p class="text-3xl font-bold text-primary mb-2">${(form.history.totalGastado).toLocaleString('es-CO')}</p>
                    <p class="text-xs text-blue-800 uppercase tracking-wider font-bold">Total Gastado</p>
                </div>
            </div>
            
            <div class="bg-base p-6 rounded-xl flex flex-col md:flex-row justify-between items-center text-sm border border-neutral/10 mb-8">
                <div class="mb-4 md:mb-0 text-center md:text-left">
                    <p class="text-neutral mb-1">Última visita registrada:</p>
                    <p class="font-bold text-lg text-dark">{form.history.ultimaReserva || 'Sin registros recientes'}</p>
                </div>
                <div class="text-center md:text-right">
                    <p class="text-neutral mb-1">Habitación / Área Favorita:</p>
                    <p class="font-bold text-lg text-secondary">{form.history.habitacionFavorita || 'N/A'}</p>
                </div>
            </div>

            <div>
                <h4 class="text-lg font-bold text-dark mb-4">Historial de Reservas</h4>
                {#if form.clientBookings && form.clientBookings.length > 0}
                    <div class="overflow-x-auto">
                        <table class="w-full text-left text-sm border-collapse">
                            <thead>
                                <tr class="bg-base/50 border-b border-neutral/10 text-neutral uppercase tracking-wider">
                                    <th class="px-4 py-2 font-semibold">Código</th>
                                    <th class="px-4 py-2 font-semibold">Tipo</th>
                                    <th class="px-4 py-2 font-semibold">Fecha</th>
                                    <th class="px-4 py-2 font-semibold">Estado</th>
                                    <th class="px-4 py-2 font-semibold text-right">Acción</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-neutral/10">
                                {#each form.clientBookings as res}
                                    <tr class="hover:bg-base/30 transition-colors">
                                        <td class="px-4 py-3 font-bold text-primary">{res.codigoReserva}</td>
                                        <td class="px-4 py-3">{res.tipoReserva}</td>
                                        <td class="px-4 py-3">{new Date(res.fechaReserva).toLocaleDateString('es-CO')}</td>
                                        <td class="px-4 py-3">
                                            <span class="px-2 py-1 rounded-full text-xs font-bold border" style="color: {res.estadoColor}; border-color: {res.estadoColor}; background-color: {res.estadoColor}15;">
                                                {res.estado}
                                            </span>
                                        </td>
                                        <td class="px-4 py-3 text-right">
                                            <a href={`/dashboard/bookings/${res.idReserva}`} class="text-secondary hover:underline font-medium">Ver Detalle →</a>
                                        </td>
                                    </tr>
                                {/each}
                            </tbody>
                        </table>
                    </div>
                {:else}
                    <p class="text-sm text-neutral bg-base/50 p-4 rounded-lg text-center">No hay reservas registradas para este cliente.</p>
                {/if}
            </div>

        </div>
    {/if}
</div>