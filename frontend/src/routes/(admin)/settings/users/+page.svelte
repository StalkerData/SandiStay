<script lang="ts">
    import type { PageData, ActionData } from './$types';
    import type { UserViewDTO } from './+page.server';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import { enhance } from '$app/forms';

    // --- PROPS & ESTADO DERIVADO ---
    let { data, form }: { data: PageData, form: ActionData } = $props();
    let users: UserViewDTO[] = $derived(data.users);
    
    let isSubmitting = $state(false);

    // Variables del formulario
    let nombres = $state(''); 
    let apellidos = $state(''); 
    let idTipoDocumento = $state(1);
    let numeroDocumento = $state(''); 
    let email = $state(''); 
    let idPais = $state(1);
    let username = $state(''); 
    let password = $state(''); 
    let rolCodigo = $state('STAFF');
</script>

<svelte:head>
    <title>Gestión de Usuarios | SandiStay Admin</title>
</svelte:head>

<main class="max-w-6xl mx-auto space-y-8">
    
    <header>
        <h1 class="text-2xl font-bold text-dark">Gestión de Usuarios</h1>
        <p class="text-sm text-neutral">Registra y administra el personal del hotel.</p>
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

    <!-- TABLA DE USUARIOS EXISTENTES -->
    <section class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden">
        <header class="bg-base/50 px-6 py-4 border-b border-neutral/10">
            <h2 class="font-bold text-secondary text-lg">Personal Registrado</h2>
        </header>
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse" aria-label="Lista de usuarios del sistema">
                <thead>
                    <tr class="bg-base/20 border-b border-neutral/10 text-sm text-neutral uppercase tracking-wider">
                        <th scope="col" class="px-6 py-3 font-semibold">Usuario</th>
                        <th scope="col" class="px-6 py-3 font-semibold">Nombre Completo</th>
                        <th scope="col" class="px-6 py-3 font-semibold">Rol</th>
                        <th scope="col" class="px-6 py-3 font-semibold">Estado</th>
                        <th scope="col" class="px-6 py-3 font-semibold text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-neutral/10">
                    {#each users as user (user.idUsuario)}
                        <tr class="hover:bg-base/30 transition-colors {user.usuarioActivo ? '' : 'opacity-50 bg-neutral/5'}">
                            <td class="px-6 py-4 font-bold text-dark">{user.username}</td>
                            <td class="px-6 py-4">
                                <div class="flex flex-col">
                                    <span class="font-medium">{user.nombreCompleto}</span>
                                    <span class="text-xs text-neutral">{user.email}</span>
                                </div>
                            </td>
                            <td class="px-6 py-4">
                                <span class="bg-secondary/10 text-secondary text-xs px-3 py-1 rounded-full font-bold tracking-wide">
                                    {user.rol}
                                </span>
                            </td>
                            <td class="px-6 py-4">
                                <span class="px-3 py-1 rounded-full text-xs font-bold shadow-sm {user.usuarioActivo ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}">
                                    {user.usuarioActivo ? 'Activo' : 'Inactivo'}
                                </span>
                            </td>
                            <td class="px-6 py-4 text-right">
                                {#if user.usuarioActivo && user.username !== 'admin'}
                                    <form method="POST" action="?/deactivateUser" use:enhance={({ cancel }) => {
                                        if(!confirm('¿Desactivar este usuario? No podrá iniciar sesión.')) {
                                            cancel();
                                            return;
                                        }
                                        isSubmitting = true;
                                        return async ({ update }) => { await update(); isSubmitting = false; };
                                    }}>
                                        <input type="hidden" name="id" value={user.idUsuario} />
                                        <Button type="submit" variant="danger" class="px-3 py-1 text-xs transition-transform active:scale-95" disabled={isSubmitting}>
                                            Desactivar
                                        </Button>
                                    </form>
                                {/if}
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    </section>

    <!-- FORMULARIO DE CREACIÓN -->
    <section class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden">
        <header class="bg-base/50 px-6 py-4 border-b border-neutral/10">
            <h2 class="font-bold text-secondary text-lg">Registrar Nuevo Usuario</h2>
        </header>
        
        <form method="POST" action="?/createUser" class="p-6 space-y-6" use:enhance={() => {
            isSubmitting = true;
            return async ({ update, result }) => { 
                // SvelteKit limpia el formulario automáticamente si le pasamos reset: true
                await update({ reset: result.type === 'success' }); 
                isSubmitting = false; 
                
                // Limpiamos el estado reactivo si fue exitoso
                if (result.type === 'success') {
                    nombres = ''; apellidos = ''; numeroDocumento = ''; email = '';
                    username = ''; password = '';
                }
            };
        }}>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="space-y-4">
                    <h3 class="font-bold text-dark border-b border-neutral/10 pb-2">Datos Personales</h3>
                    <Input id="nombres" name="nombres" label="Nombres" bind:value={nombres} required />
                    <Input id="apellidos" name="apellidos" label="Apellidos" bind:value={apellidos} required />
                    <div class="flex flex-col gap-1">
                        <label for="idTipoDocumento" class="text-sm font-medium text-dark">Tipo de Documento</label>
                        <select id="idTipoDocumento" name="idTipoDocumento" bind:value={idTipoDocumento} class="px-3 py-2 border border-neutral/30 rounded-md focus:ring-secondary bg-base/50">
                            <option value={1}>Cédula de Ciudadanía</option>
                            <option value={2}>Cédula de Extranjería</option>
                            <option value={3}>Pasaporte</option>
                        </select>
                    </div>
                    <Input id="numeroDocumento" name="numeroDocumento" label="Número de Documento" bind:value={numeroDocumento} required />
                    <div class="flex flex-col gap-1">
                        <label for="idPais" class="text-sm font-medium text-dark">País</label>
                        <select id="idPais" name="idPais" bind:value={idPais} class="px-3 py-2 border border-neutral/30 rounded-md focus:ring-secondary bg-base/50">
                            <option value={1}>Colombia</option>
                            <option value={2}>Venezuela</option>
                            <option value={3}>Estados Unidos</option>
                        </select>
                    </div>
                </div>

                <div class="space-y-4">
                    <h3 class="font-bold text-dark border-b border-neutral/10 pb-2">Credenciales de Acceso</h3>
                    <Input id="email" name="email" label="Correo Electrónico" type="email" bind:value={email} required />
                    <Input id="username" name="username" label="Nombre de Usuario (Login)" bind:value={username} required />
                    <Input id="password" name="password" label="Contraseña Temporal" type="password" bind:value={password} required />
                    <div class="flex flex-col gap-1">
                        <label for="rolCodigo" class="text-sm font-medium text-dark">Rol en el Sistema</label>
                        <select id="rolCodigo" name="rolCodigo" bind:value={rolCodigo} class="px-3 py-2 border border-neutral/30 rounded-md bg-base/50 focus:ring-secondary">
                            <option value="STAFF">Secretaria / Recepción (STAFF)</option>
                        </select>
                        <p class="text-xs text-neutral mt-1 flex items-center gap-1">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                            Solo se permite la creación de cuentas operativas.
                        </p>
                    </div>
                </div> 
            </div>

            <footer class="pt-4 border-t border-neutral/10 flex justify-end">
                <Button type="submit" variant="primary" class="px-8 transition-transform active:scale-95" disabled={isSubmitting}>
                    {isSubmitting ? 'Creando...' : 'Crear Usuario'}
                </Button>
            </footer>
        </form>
    </section>
</main>