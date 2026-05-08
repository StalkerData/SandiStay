<script lang="ts">
    import type { PageData } from './$types';
    import Button from '$lib/components/ui/Button.svelte';

    // Recibimos los datos inyectados por el servidor (SSR)
    let { data }: { data: PageData } = $props();
    
    // Usamos $derived para mantener la reactividad
    let reservas = $derived(data.reservas);
</script>

<svelte:head>
    <title>Dashboard | SandiStay Admin</title>
</svelte:head>

<div class="space-y-6">
    
    <!-- Cabecera del Dashboard -->
    <div class="flex justify-between items-center">
        <div>
            <h2 class="text-2xl font-bold text-dark">Reservas Recientes</h2>
            <p class="text-sm text-neutral">Gestiona las llegadas y salidas del hotel.</p>
        </div>
        
        <!-- Botón para recargar datos (Opcional, útil para la secretaria) -->
        <Button variant="outline" onclick={() => window.location.reload()}>
            ↻ Actualizar
        </Button>
    </div>

    <!-- Tabla de Datos -->
    <div class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden">
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
                
                <!-- Encabezados de Tabla -->
                <thead>
                    <tr class="bg-base/50 border-b border-neutral/10 text-sm text-neutral uppercase tracking-wider">
                        <th class="px-6 py-4 font-semibold">Código</th>
                        <th class="px-6 py-4 font-semibold">Huésped</th>
                        <th class="px-6 py-4 font-semibold">Tipo</th>
                        <th class="px-6 py-4 font-semibold">Fecha Reserva</th>
                        <th class="px-6 py-4 font-semibold">Estado</th>
                        <th class="px-6 py-4 font-semibold text-right">Acciones</th>
                    </tr>
                </thead>
                
                <!-- Cuerpo de la Tabla -->
                <tbody class="divide-y divide-neutral/10">
                    {#if reservas.length === 0}
                        <tr>
                            <td colspan="6" class="px-6 py-8 text-center text-neutral">
                                No hay reservas registradas en el sistema.
                            </td>
                        </tr>
                    {:else}
                        {#each reservas as reserva}
                            <tr class="hover:bg-base/30 transition-colors">
                                
                                <!-- Código -->
                                <td class="px-6 py-4">
                                    <span class="font-bold text-primary">{reserva.codigoReserva}</span>
                                </td>
                                
                                <!-- Huésped -->
                                <td class="px-6 py-4">
                                    <div class="flex flex-col">
                                        <span class="font-medium text-dark">{reserva.nombreHuesped}</span>
                                        <span class="text-xs text-neutral">Titular: {reserva.nombreCliente}</span>
                                    </div>
                                </td>
                                
                                <!-- Tipo (Habitación o Área) -->
                                <td class="px-6 py-4">
                                    <span class="text-sm text-dark">{reserva.tipoReserva}</span>
                                </td>
                                
                                <!-- Fecha -->
                                <td class="px-6 py-4">
                                    <span class="text-sm text-neutral">
                                        <!-- Formateamos la fecha para que se vea bonita -->
                                        {new Date(reserva.fechaReserva).toLocaleDateString('es-CO', { year: 'numeric', month: 'short', day: 'numeric' })}
                                    </span>
                                </td>
                                
                                <!-- Estado (Badge Dinámico) -->
                                <td class="px-6 py-4">
                                    <!-- Usamos el color que viene de la BD para el borde y el texto -->
                                    <span 
                                        class="px-3 py-1 rounded-full text-xs font-bold border"
                                        style="color: {reserva.estadoColor}; border-color: {reserva.estadoColor}; background-color: {reserva.estadoColor}15;"
                                    >
                                        {reserva.estado}
                                    </span>
                                </td>
                                
                                <!-- Acciones -->
                                <td class="px-6 py-4 text-right">
                                    <a href={`/dashboard/bookings/${reserva.codigoReserva}`} class="text-secondary hover:text-primary font-medium text-sm transition-colors">
                                        Ver Detalle →
                                    </a>
                                </td>
                                
                            </tr>
                        {/each}
                    {/if}
                </tbody>
            </table>
        </div>
    </div>
</div>