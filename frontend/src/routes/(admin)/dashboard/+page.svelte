<script lang="ts">
    import { invalidateAll } from '$app/navigation';
    import { enhance } from '$app/forms';
    import type { PageData, ActionData } from './$types';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';

    // --- PROPS & ESTADO DERIVADO ---
    let { data, form }: { data: PageData, form: ActionData } = $props();
    let reservas = $derived(data.reservas);

    // --- ESTADO DEL MODAL DE CANCELACIÓN ---
    let showModal = $state(false);
    let reservaSeleccionada = $state<number | null>(null);
    let motivoCancelacion = $state('');
    let isSubmitting = $state(false);
    let isRefreshing = $state(false);

    /**
     * @description Abre el modal y prepara el estado para la cancelación.
     */
    function abrirModal(id: number) {
        reservaSeleccionada = id;
        motivoCancelacion = '';
        showModal = true;
    }

    /**
     * @description Cierra el modal y limpia el estado.
     */
    function cerrarModal() {
        showModal = false;
        reservaSeleccionada = null;
    }

    /**
     * @description Refresca los datos de la tabla sin recargar la página (SPA approach).
     */
    async function actualizarDatos() {
        isRefreshing = true;
        await invalidateAll();
        isRefreshing = false;
    }
</script>

<svelte:head>
    <title>Dashboard | SandiStay Admin</title>
</svelte:head>

<main class="space-y-6 relative">
    
    <header class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
            <h1 class="text-2xl font-bold text-dark">Reservas Recientes</h1>
            <p class="text-sm text-neutral">Gestiona las llegadas y salidas del hotel.</p>
        </div>
        
        <!-- BOTONES DE ACCIÓN PRINCIPALES -->
        <div class="flex items-center gap-3">
            <Button variant="outline" onclick={actualizarDatos} disabled={isRefreshing}>
                {isRefreshing ? '↻ Actualizando...' : '↻ Actualizar'}
            </Button>
            
            <a href="/dashboard/bookings/new" aria-label="Crear nueva reserva">
                <Button variant="primary" class="shadow-md shadow-primary/20 transition-transform active:scale-95">
                    + Nueva Reserva (Walk-in)
                </Button>
            </a>
        </div>
    </header>

    <!-- ALERTAS DE ÉXITO O ERROR (Con ARIA Live Regions para Accesibilidad) -->
    <div aria-live="polite" class="space-y-2">
        {#if form?.error}
            <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-md shadow-sm animate-fade-in" role="alert">
                <p class="font-bold">Error de Operación</p>
                <p>{form.error}</p>
            </div>
        {/if}
        {#if form?.success}
            <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded-md shadow-sm animate-fade-in" role="status">
                <p class="font-bold">Operación Exitosa</p>
                <p>{form.message}</p>
            </div>
        {/if}
    </div>

    <!-- TABLA DE DATOS -->
    <section class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden">
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse" aria-label="Lista de reservas recientes">
                <thead>
                    <tr class="bg-base/50 border-b border-neutral/10 text-sm text-neutral uppercase tracking-wider">
                        <th scope="col" class="px-6 py-4 font-semibold">Código</th>
                        <th scope="col" class="px-6 py-4 font-semibold">Huésped</th>
                        <th scope="col" class="px-6 py-4 font-semibold">Tipo</th>
                        <th scope="col" class="px-6 py-4 font-semibold">Fecha Reserva</th>
                        <th scope="col" class="px-6 py-4 font-semibold">Estado</th>
                        <th scope="col" class="px-6 py-4 font-semibold text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-neutral/10">
                    {#if reservas.length === 0}
                        <tr>
                            <td colspan="6" class="px-6 py-8 text-center text-neutral">No hay reservas registradas.</td>
                        </tr>
                    {:else}
                        {#each reservas as reserva (reserva.idReserva)}
                            <tr class="hover:bg-base/30 transition-colors">
                                <td class="px-6 py-4"><span class="font-bold text-primary">{reserva.codigoReserva}</span></td>
                                <td class="px-6 py-4">
                                    <div class="flex flex-col">
                                        <span class="font-medium text-dark">{reserva.nombreHuesped}</span>
                                        <span class="text-xs text-neutral">Titular: {reserva.nombreCliente}</span>
                                    </div>
                                </td>
                                <td class="px-6 py-4"><span class="text-sm text-dark">{reserva.tipoReserva}</span></td>
                                <td class="px-6 py-4">
                                    <span class="text-sm text-neutral">
                                        {new Date(reserva.fechaReserva).toLocaleDateString('es-CO', { year: 'numeric', month: 'short', day: 'numeric' })}
                                    </span>
                                </td>
                                <td class="px-6 py-4">
                                    <span class="px-3 py-1 rounded-full text-xs font-bold border" style="color: {reserva.estadoColor}; border-color: {reserva.estadoColor}; background-color: {reserva.estadoColor}15;">
                                        {reserva.estado}
                                    </span>
                                </td>
                                
                                <!-- COLUMNA DE ACCIONES -->
                                <td class="px-6 py-4 text-right flex items-center justify-end gap-2">
                                    
                                    <!-- Botón Check-out (Solo si está Confirmada o En Curso) -->
                                    {#if reserva.estado === 'Confirmada' || reserva.estado === 'En Curso'}
                                        <form method="POST" action="?/checkout" use:enhance={() => {
                                            isSubmitting = true;
                                            return async ({ update }) => { await update(); isSubmitting = false; };
                                        }}>
                                            <input type="hidden" name="id" value={reserva.idReserva} />
                                            <Button type="submit" variant="outline" class="px-3 py-1 text-xs" disabled={isSubmitting}>
                                                Check-out
                                            </Button>
                                        </form>
                                    {/if}

                                    <!-- Botón Cancelar (Solo si está Pendiente o Confirmada) -->
                                    {#if reserva.estado === 'Pendiente' || reserva.estado === 'Confirmada'}
                                        <Button variant="danger" class="px-3 py-1 text-xs" onclick={() => abrirModal(reserva.idReserva)}>
                                            Cancelar
                                        </Button>
                                    {/if}

                                    <a href={`/dashboard/bookings/${reserva.idReserva}`} class="text-secondary hover:text-primary font-medium text-sm transition-colors ml-2" aria-label={`Ver detalle de la reserva ${reserva.codigoReserva}`}>
                                        Ver Detalle →
                                    </a>
                                </td>
                            </tr>
                        {/each}
                    {/if}
                </tbody>
            </table>
        </div>
    </section>
</main>

<!-- ========================================== -->
<!-- MODAL DE CANCELACIÓN (Accesible)           -->
<!-- ========================================== -->
{#if showModal}
    <div class="fixed inset-0 bg-dark/60 flex items-center justify-center z-50 px-4 animate-fade-in" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <div class="bg-surface rounded-xl shadow-2xl max-w-md w-full p-6 border border-neutral/10">
            <h2 id="modal-title" class="text-xl font-bold text-dark mb-2">Cancelar Reserva</h2>
            <p class="text-sm text-neutral mb-6">
                Por favor, ingresa el motivo de la cancelación. Esta acción aplicará las políticas de penalidad correspondientes y no se puede deshacer.
            </p>
            
            <form method="POST" action="?/cancelar" use:enhance={() => {
                isSubmitting = true;
                return async ({ update }) => {
                    await update();
                    isSubmitting = false;
                    cerrarModal(); // Cerramos el modal cuando el servidor responda
                };
            }}>
                <input type="hidden" name="id" value={reservaSeleccionada} />
                
                <div class="mb-6">
                    <Input 
                        id="motivo" 
                        name="motivo" 
                        label="Motivo de Cancelación" 
                        type="text" 
                        bind:value={motivoCancelacion} 
                        required 
                        placeholder="Ej: Solicitud del cliente por enfermedad"
                    />
                </div>
                
                <div class="flex justify-end gap-3">
                    <Button type="button" variant="outline" onclick={cerrarModal} disabled={isSubmitting}>
                        Volver
                    </Button>
                    <Button type="submit" variant="danger" disabled={isSubmitting}>
                        {isSubmitting ? 'Procesando...' : 'Confirmar Cancelación'}
                    </Button>
                </div>
            </form>
        </div>
    </div>
{/if}