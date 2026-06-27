<script lang="ts">
    import type { PageData, ActionData } from './$types';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import { enhance } from '$app/forms';

    // --- PROPS & ESTADO DERIVADO ---
    let { data, form }: { data: PageData, form: ActionData } = $props();
    
    let rooms = $derived(data.rooms);
    let areas = $derived(data.areas);
    let isAdmin = $derived(data.userRole === 'ADMIN');

    let isSubmitting = $state(false);

    const estados = [
        { id: 1, nombre: 'Disponible' },
        { id: 2, nombre: 'Ocupada' },
        { id: 3, nombre: 'Mantenimiento' },
        { id: 4, nombre: 'Limpieza' }
    ];

    // --- ESTADOS DE MODALES ---
    let showRoomModal = $state(false);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let editingRoom = $state<any>(null);

    let showAreaModal = $state(false);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let editingArea = $state<any>(null);

    let showImageModal = $state(false);
    let uploadTargetId = $state('');
    let uploadTargetType = $state('');

    // --- CONTROLADORES DE MODALES ---
    function openRoomModal(room: any = null) {
        editingRoom = room; // Si es null, es creación nueva
        showRoomModal = true;
    }

    function openAreaModal(area: any = null) {
        editingArea = area; // Si es null, es creación nueva
        showAreaModal = true;
    }

    function openImageModal(id: string, tipo: string) {
        uploadTargetId = id;
        uploadTargetType = tipo;
        showImageModal = true;
    }
</script>

<svelte:head>
    <title>Gestión de Catálogo | SandiStay Admin</title>
</svelte:head>

<main class="space-y-8 relative">
    
    <header class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
            <h1 class="text-2xl font-bold text-dark">Gestión de Catálogo</h1>
            <p class="text-sm text-neutral">Administra el inventario de habitaciones y áreas sociales.</p>
        </div>
        
        {#if isAdmin}
            <div class="flex gap-2">
                <Button variant="outline" onclick={() => openAreaModal()}>+ Nueva Área</Button>
                <Button variant="primary" onclick={() => openRoomModal()}>+ Nueva Habitación</Button>
            </div>
        {/if}
    </header>

    <!-- ALERTAS (ARIA Live) -->
    <div aria-live="polite">
        {#if form?.error}
            <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-md shadow-sm animate-fade-in mb-4" role="alert">
                {form.error}
            </div>
        {/if}
        {#if form?.success}
            <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded-md shadow-sm animate-fade-in mb-4" role="status">
                {form.message}
            </div>
        {/if}
    </div>

    <!-- SECCIÓN: HABITACIONES -->
    <section class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden">
        <header class="bg-base/50 px-6 py-4 border-b border-neutral/10">
            <h2 class="font-bold text-secondary text-lg">Habitaciones</h2>
        </header>
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse" aria-label="Inventario de habitaciones">
                <thead>
                    <tr class="bg-base/20 border-b border-neutral/10 text-sm text-neutral uppercase tracking-wider">
                        <th scope="col" class="px-6 py-3 font-semibold">Código</th>
                        <th scope="col" class="px-6 py-3 font-semibold">Nombre</th>
                        <th scope="col" class="px-6 py-3 font-semibold">Precio/Noche</th>
                        <th scope="col" class="px-6 py-3 font-semibold">Estado Operativo</th>
                        {#if isAdmin}
                            <th scope="col" class="px-6 py-3 font-semibold text-right">Acciones</th>
                        {/if}
                    </tr>
                </thead>
                <tbody class="divide-y divide-neutral/10">
                    {#each rooms as room (room.idHabitacion)}
                        <tr class="hover:bg-base/30 transition-colors">
                            <td class="px-6 py-4 font-bold text-dark">{room.codigo}</td>
                            <td class="px-6 py-4">
                                <div class="flex flex-col">
                                    <span class="font-medium">{room.nombre}</span>
                                    <span class="text-xs text-neutral">Capacidad: {room.capacidadMaxima}</span>
                                </div>
                            </td>
                            <td class="px-6 py-4 font-medium text-primary">${room.precioNoche.toLocaleString('es-CO')}</td>
                            
                            <td class="px-6 py-4">
                                <form method="POST" action="?/updateStatus" use:enhance={() => {
                                    isSubmitting = true;
                                    return async ({ update }) => { await update(); isSubmitting = false; };
                                }}>
                                    <input type="hidden" name="id" value={room.idHabitacion} />
                                    <input type="hidden" name="tipo" value="room" />
                                    <div class="flex items-center gap-2">
                                        <select name="idEstado" class="text-sm border-neutral/30 rounded-md bg-base/50 focus:ring-secondary" disabled={isSubmitting} aria-label="Cambiar estado">
                                            {#each estados as estado}
                                                <option value={estado.id} selected={room.idEstadoLugar === estado.id}>{estado.nombre}</option>
                                            {/each}
                                        </select>
                                        <Button type="submit" variant="outline" class="px-2 py-1 text-xs" disabled={isSubmitting}>Guardar</Button>
                                    </div>
                                </form>
                            </td>

                            {#if isAdmin}
                                <td class="px-6 py-4 text-right">
                                    <div class="flex flex-col gap-2 items-end">
                                        <div class="flex gap-2">
                                            <Button variant="outline" class="px-3 py-1 text-xs" onclick={() => openImageModal(room.idHabitacion, 'room')}>📷 Foto</Button>
                                            <Button variant="outline" class="px-3 py-1 text-xs" onclick={() => openRoomModal(room)}>Editar</Button>
                                            <form method="POST" action="?/deleteItem" use:enhance={({ cancel }) => {
                                                if(!confirm('¿Estás seguro de eliminar esta habitación?')) {
                                                    cancel();
                                                    return;
                                                }
                                                isSubmitting = true;
                                                return async ({ update }) => { await update(); isSubmitting = false; };
                                            }}>
                                                <input type="hidden" name="id" value={room.idHabitacion} />
                                                <input type="hidden" name="tipo" value="room" />
                                                <Button type="submit" variant="danger" class="px-3 py-1 text-xs" disabled={isSubmitting}>Eliminar</Button>
                                            </form>
                                        </div>

                                        <!-- LISTA DE IMÁGENES REALES PARA BORRAR -->
                                        {#if room.imagenes && room.imagenes.length > 0}
                                            <div class="mt-2 p-2 bg-base/50 rounded-md border border-neutral/10 w-full text-left">
                                                <p class="text-xs font-bold text-neutral mb-1">Galería ({room.imagenes.length}):</p>
                                                <div class="flex flex-wrap gap-2">
                                                    {#each room.imagenes as img}
                                                        <form method="POST" action="?/deleteImage" use:enhance={({ cancel }) => {
                                                            if(!confirm('¿Borrar esta imagen del servidor?')) {
                                                                cancel();
                                                                return;
                                                            }
                                                            return async ({ update }) => { await update(); };
                                                        }}>
                                                            <input type="hidden" name="idImagen" value={img.idImagen} />
                                                            <input type="hidden" name="tipo" value="room" />
                                                            <button type="submit" class="text-xs bg-red-100 text-red-700 px-2 py-1 rounded hover:bg-red-200 transition-colors" title={img.url}>
                                                                🗑️ ID: {img.idImagen}
                                                            </button>
                                                        </form>
                                                    {/each}
                                                </div>
                                            </div>
                                        {/if}
                                    </div>
                                </td>
                            {/if}
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    </section>

    <!-- SECCIÓN: ÁREAS SOCIALES -->
    <section class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden">
        <header class="bg-base/50 px-6 py-4 border-b border-neutral/10">
            <h2 class="font-bold text-secondary text-lg">Áreas Sociales</h2>
        </header>
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse" aria-label="Inventario de áreas sociales">
                <thead>
                    <tr class="bg-base/20 border-b border-neutral/10 text-sm text-neutral uppercase tracking-wider">
                        <th scope="col" class="px-6 py-3 font-semibold">Código</th>
                        <th scope="col" class="px-6 py-3 font-semibold">Nombre</th>
                        <th scope="col" class="px-6 py-3 font-semibold">Precio/Hora</th>
                        <th scope="col" class="px-6 py-3 font-semibold">Estado Operativo</th>
                        {#if isAdmin}
                            <th scope="col" class="px-6 py-3 font-semibold text-right">Acciones</th>
                        {/if}
                    </tr>
                </thead>
                <tbody class="divide-y divide-neutral/10">
                    {#each areas as area (area.idArea)}
                        <tr class="hover:bg-base/30 transition-colors">
                            <td class="px-6 py-4 font-bold text-dark">{area.codigo}</td>
                            <td class="px-6 py-4">
                                <div class="flex flex-col">
                                    <span class="font-medium">{area.nombre}</span>
                                    <span class="text-xs text-neutral">Capacidad: {area.capacidadPersonas}</span>
                                </div>
                            </td>
                            <td class="px-6 py-4 font-medium text-primary">${area.precioHora.toLocaleString('es-CO')}</td>
                            
                            <td class="px-6 py-4">
                                <form method="POST" action="?/updateStatus" use:enhance={() => {
                                    isSubmitting = true;
                                    return async ({ update }) => { await update(); isSubmitting = false; };
                                }}>
                                    <input type="hidden" name="id" value={area.idArea} />
                                    <input type="hidden" name="tipo" value="area" />
                                    <div class="flex items-center gap-2">
                                        <select name="idEstado" class="text-sm border-neutral/30 rounded-md bg-base/50 focus:ring-secondary" disabled={isSubmitting} aria-label="Cambiar estado">
                                            {#each estados as estado}
                                                <option value={estado.id} selected={area.idEstadoLugar === estado.id}>{estado.nombre}</option>
                                            {/each}
                                        </select>
                                        <Button type="submit" variant="outline" class="px-2 py-1 text-xs" disabled={isSubmitting}>Guardar</Button>
                                    </div>
                                </form>
                            </td>

                            {#if isAdmin}
                                <td class="px-6 py-4 text-right">
                                    <div class="flex flex-col gap-2 items-end">
                                        <div class="flex gap-2">
                                            <Button variant="outline" class="px-3 py-1 text-xs" onclick={() => openImageModal(area.idArea, 'area')}>📷 Foto</Button>
                                            <Button variant="outline" class="px-3 py-1 text-xs" onclick={() => openAreaModal(area)}>Editar</Button>
                                            <form method="POST" action="?/deleteItem" use:enhance={({ cancel }) => {
                                                if(!confirm('¿Estás seguro de eliminar esta área?')) {
                                                    cancel();
                                                    return;
                                                }
                                                isSubmitting = true;
                                                return async ({ update }) => { await update(); isSubmitting = false; };
                                            }}>
                                                <input type="hidden" name="id" value={area.idArea} />
                                                <input type="hidden" name="tipo" value="area" />
                                                <Button type="submit" variant="danger" class="px-3 py-1 text-xs" disabled={isSubmitting}>Eliminar</Button>
                                            </form>
                                        </div>

                                        <!-- LISTA DE IMÁGENES REALES PARA BORRAR -->
                                        {#if area.imagenes && area.imagenes.length > 0}
                                            <div class="mt-2 p-2 bg-base/50 rounded-md border border-neutral/10 w-full text-left">
                                                <p class="text-xs font-bold text-neutral mb-1">Galería ({area.imagenes.length}):</p>
                                                <div class="flex flex-wrap gap-2">
                                                    {#each area.imagenes as img}
                                                        <form method="POST" action="?/deleteImage" use:enhance={({ cancel }) => {
                                                            if(!confirm('¿Borrar esta imagen del servidor?')) {
                                                                cancel();
                                                                return;
                                                            }
                                                            return async ({ update }) => { await update(); };
                                                        }}>
                                                            <input type="hidden" name="idImagen" value={img.idImagen} />
                                                            <input type="hidden" name="tipo" value="area" />
                                                            <button type="submit" class="text-xs bg-red-100 text-red-700 px-2 py-1 rounded hover:bg-red-200 transition-colors" title={img.url}>
                                                                🗑️ ID: {img.idImagen}
                                                            </button>
                                                        </form>
                                                    {/each}
                                                </div>
                                            </div>
                                        {/if}
                                    </div>
                                </td>
                            {/if}
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    </section>

</main>

<!-- ========================================== -->
<!-- MODAL: CREAR / EDITAR HABITACIÓN           -->
<!-- ========================================== -->
{#if showRoomModal}
    <div class="fixed inset-0 bg-dark/60 flex items-center justify-center z-50 px-4 animate-fade-in" role="dialog" aria-modal="true" aria-labelledby="modal-room-title">
        <div class="bg-surface rounded-xl shadow-2xl max-w-2xl w-full p-6 border border-neutral/10 max-h-[90vh] overflow-y-auto">
            <h2 id="modal-room-title" class="text-xl font-bold text-dark mb-6">{editingRoom ? 'Editar Habitación' : 'Nueva Habitación'}</h2>
            
            <form method="POST" action="?/saveRoom" use:enhance={() => {
                isSubmitting = true;
                return async ({ update, result }) => { 
                    await update(); 
                    isSubmitting = false; 
                    if (result.type === 'success') showRoomModal = false; 
                };
            }}>
                {#if editingRoom}
                    <input type="hidden" name="id" value={editingRoom.idHabitacion} />
                {/if}

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                    <Input id="codigo" name="codigo" label="Código (Ej: H105)" value={editingRoom?.codigo || ''} required />
                    <Input id="numero" name="numero" label="Número" value={editingRoom?.numero || ''} required />
                    <Input id="nombre" name="nombre" label="Nombre" value={editingRoom?.nombre || ''} required />
                    
                    <div class="flex flex-col gap-1">
                        <label for="idTipoHabitacion" class="text-sm font-medium text-dark">Tipo de Habitación</label>
                        <select id="idTipoHabitacion" name="idTipoHabitacion" class="px-3 py-2 border border-neutral/30 rounded-md focus:ring-secondary bg-base/50" required>
                            <option value="1" selected={editingRoom?.idTipoHabitacion === 1}>Sencilla</option>
                            <option value="2" selected={editingRoom?.idTipoHabitacion === 2}>Doble</option>
                            <option value="3" selected={editingRoom?.idTipoHabitacion === 3}>Suite</option>
                        </select>
                    </div>

                    <Input id="capacidadAdultos" name="capacidadAdultos" label="Adultos" type="number" value={editingRoom?.capacidadAdultos || 1} required />
                    <Input id="capacidadNinos" name="capacidadNinos" label="Niños" type="number" value={editingRoom?.capacidadNinos || 0} required />
                    <Input id="capacidadMaxima" name="capacidadMaxima" label="Capacidad Máxima" type="number" value={editingRoom?.capacidadMaxima || 2} required />
                    
                    <Input id="precioNoche" name="precioNoche" label="Precio Noche ($)" type="number" step="0.01" value={editingRoom?.precioNoche || ''} required />
                    <Input id="precioDia" name="precioDia" label="Precio Día ($)" type="number" step="0.01" value={editingRoom?.precioDia || ''} required />
                </div>

                <div class="mb-6">
                    <label for="descripcion" class="text-sm font-medium text-dark">Descripción</label>
                    <textarea id="descripcion" name="descripcion" rows="3" class="w-full px-3 py-2 border border-neutral/30 rounded-md mt-1 focus:ring-secondary bg-base/50" required>{editingRoom?.descripcion || ''}</textarea>
                </div>
                
                <div class="flex justify-end gap-3">
                    <Button type="button" variant="outline" onclick={() => showRoomModal = false} disabled={isSubmitting}>Cancelar</Button>
                    <Button type="submit" variant="primary" disabled={isSubmitting}>
                        {isSubmitting ? 'Guardando...' : 'Guardar Habitación'}
                    </Button>
                </div>
            </form>
        </div>
    </div>
{/if}

<!-- ========================================== -->
<!-- MODAL: CREAR / EDITAR ÁREA                 -->
<!-- ========================================== -->
{#if showAreaModal}
    <div class="fixed inset-0 bg-dark/60 flex items-center justify-center z-50 px-4 animate-fade-in" role="dialog" aria-modal="true" aria-labelledby="modal-area-title">
        <div class="bg-surface rounded-xl shadow-2xl max-w-2xl w-full p-6 border border-neutral/10 max-h-[90vh] overflow-y-auto">
            <h2 id="modal-area-title" class="text-xl font-bold text-dark mb-6">{editingArea ? 'Editar Área' : 'Nueva Área'}</h2>
            
            <form method="POST" action="?/saveArea" use:enhance={() => {
                isSubmitting = true;
                return async ({ update, result }) => { 
                    await update(); 
                    isSubmitting = false; 
                    if (result.type === 'success') showAreaModal = false; 
                };
            }}>
                {#if editingArea}
                    <input type="hidden" name="id" value={editingArea.idArea} />
                {/if}

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                    <Input id="codigoArea" name="codigo" label="Código (Ej: A005)" value={editingArea?.codigo || ''} required />
                    <Input id="nombreArea" name="nombre" label="Nombre" value={editingArea?.nombre || ''} required />
                    
                    <div class="flex flex-col gap-1">
                        <label for="idTipoArea" class="text-sm font-medium text-dark">Tipo de Área</label>
                        <select id="idTipoArea" name="idTipoArea" class="px-3 py-2 border border-neutral/30 rounded-md focus:ring-secondary bg-base/50" required>
                            <option value="1" selected={editingArea?.idTipoArea === 1}>Salón de Eventos</option>
                            <option value="2" selected={editingArea?.idTipoArea === 2}>Piscina</option>
                            <option value="3" selected={editingArea?.idTipoArea === 3}>Gimnasio</option>
                        </select>
                    </div>

                    <Input id="capacidadPersonas" name="capacidadPersonas" label="Capacidad Personas" type="number" value={editingArea?.capacidadPersonas || 10} required />
                    <Input id="precioHora" name="precioHora" label="Precio Hora ($)" type="number" step="0.01" value={editingArea?.precioHora || ''} required />
                </div>

                <div class="mb-6">
                    <label for="descripcionArea" class="text-sm font-medium text-dark">Descripción</label>
                    <textarea id="descripcionArea" name="descripcion" rows="3" class="w-full px-3 py-2 border border-neutral/30 rounded-md mt-1 focus:ring-secondary bg-base/50" required>{editingArea?.descripcion || ''}</textarea>
                </div>
                
                <div class="flex justify-end gap-3">
                    <Button type="button" variant="outline" onclick={() => showAreaModal = false} disabled={isSubmitting}>Cancelar</Button>
                    <Button type="submit" variant="primary" disabled={isSubmitting}>
                        {isSubmitting ? 'Guardando...' : 'Guardar Área'}
                    </Button>
                </div>
            </form>
        </div>
    </div>
{/if}

<!-- ========================================== -->
<!-- MODAL: SUBIR IMAGEN                        -->
<!-- ========================================== -->
{#if showImageModal}
    <div class="fixed inset-0 bg-dark/60 flex items-center justify-center z-50 px-4 animate-fade-in" role="dialog" aria-modal="true" aria-labelledby="modal-image-title">
        <div class="bg-surface rounded-xl shadow-2xl max-w-md w-full p-6 border border-neutral/10">
            <h2 id="modal-image-title" class="text-xl font-bold text-dark mb-4">Subir Fotografía</h2>
            
            <form method="POST" action="?/uploadImage" enctype="multipart/form-data" use:enhance={() => {
                isSubmitting = true;
                return async ({ update, result }) => { 
                    await update(); 
                    isSubmitting = false; 
                    if (result.type === 'success') showImageModal = false; 
                };
            }}>
                <input type="hidden" name="id" value={uploadTargetId} />
                <input type="hidden" name="tipo" value={uploadTargetType} />
                
                <div class="mb-6">
                    <label for="file" class="block text-sm font-medium text-dark mb-2">Selecciona una imagen (JPG/PNG)</label>
                    <input id="file" type="file" name="file" accept="image/*" class="w-full border border-neutral/30 p-2 rounded-md bg-base/50" required />
                </div>
                
                <div class="flex justify-end gap-3">
                    <Button type="button" variant="outline" onclick={() => showImageModal = false} disabled={isSubmitting}>Cancelar</Button>
                    <Button type="submit" variant="primary" disabled={isSubmitting}>
                        {isSubmitting ? 'Subiendo...' : 'Subir Imagen'}
                    </Button>
                </div>
            </form>
        </div>
    </div>
{/if}