<script lang="ts">
    import type { PageData, ActionData } from './$types';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import { enhance } from '$app/forms';

    let { data, form }: { data: PageData, form: ActionData } = $props();
    let users = $derived(data.users);
    
    let isSubmitting = $state(false);

    let nombres = $state(''); let apellidos = $state(''); let idTipoDocumento = $state(1);
    let numeroDocumento = $state(''); let email = $state(''); let idPais = $state(1);
    let username = $state(''); let password = $state(''); let rolCodigo = $state('STAFF');

    $effect(() => {
        if (form?.success) {
            nombres = ''; apellidos = ''; numeroDocumento = ''; email = '';
            username = ''; password = '';
        }
    });
</script>

<svelte:head>
    <title>Gestión de Usuarios | SandiStay Admin</title>
</svelte:head>

<div class="max-w-6xl mx-auto space-y-8">
    <div>
        <h2 class="text-2xl font-bold text-dark">Gestión de Usuarios</h2>
        <p class="text-sm text-neutral">Registra y administra el personal del hotel.</p>
    </div>

    {#if form?.error}
        <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-md shadow-sm animate-fade-in">{form.error}</div>
    {/if}
    {#if form?.success}
        <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded-md shadow-sm animate-fade-in">{form.message}</div>
    {/if}

    <!-- TABLA DE USUARIOS EXISTENTES -->
    <div class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden">
        <div class="bg-base/50 px-6 py-4 border-b border-neutral/10">
            <h3 class="font-bold text-secondary text-lg">Personal Registrado</h3>
        </div>
        <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
                <thead>
                    <tr class="bg-base/20 border-b border-neutral/10 text-sm text-neutral uppercase tracking-wider">
                        <th class="px-6 py-3 font-semibold">Usuario</th>
                        <th class="px-6 py-3 font-semibold">Nombre Completo</th>
                        <th class="px-6 py-3 font-semibold">Rol</th>
                        <th class="px-6 py-3 font-semibold">Estado</th>
                        <th class="px-6 py-3 font-semibold text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-neutral/10">
                    {#each users as user}
                        <tr class="hover:bg-base/30 transition-colors {user.usuarioActivo ? '' : 'opacity-50'}">
                            <td class="px-6 py-4 font-bold text-dark">{user.username}</td>
                            <td class="px-6 py-4">
                                <div class="flex flex-col">
                                    <span class="font-medium">{user.nombreCompleto}</span>
                                    <span class="text-xs text-neutral">{user.email}</span>
                                </div>
                            </td>
                            <td class="px-6 py-4"><span class="bg-secondary/10 text-secondary text-xs px-2 py-1 rounded-full font-bold">{user.rol}</span></td>
                            <td class="px-6 py-4">
                                <span class="px-2 py-1 rounded-full text-xs font-bold {user.usuarioActivo ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}">
                                    {user.usuarioActivo ? 'Activo' : 'Inactivo'}
                                </span>
                            </td>
                            <td class="px-6 py-4 text-right">
                                {#if user.usuarioActivo && user.username !== 'admin'}
                                    <form method="POST" action="?/deactivateUser" use:enhance={() => {
                                        if(!confirm('¿Desactivar este usuario? No podrá iniciar sesión.')) return ({ cancel }) => cancel();
                                        isSubmitting = true;
                                        return async ({ update }) => { await update(); isSubmitting = false; };
                                    }}>
                                        <input type="hidden" name="id" value={user.idUsuario} />
                                        <Button type="submit" variant="danger" class="px-3 py-1 text-xs" disabled={isSubmitting}>Desactivar</Button>
                                    </form>
                                {/if}
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    </div>

    <!-- FORMULARIO DE CREACIÓN -->
    <div class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden">
        <div class="bg-base/50 px-6 py-4 border-b border-neutral/10">
            <h3 class="font-bold text-secondary text-lg">Registrar Nuevo Usuario</h3>
        </div>
        
        <form method="POST" action="?/createUser" class="p-6 space-y-6" use:enhance={() => {
            isSubmitting = true;
            return async ({ update }) => { await update(); isSubmitting = false; };
        }}>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="space-y-4">
                    <h4 class="font-bold text-dark border-b pb-2">Datos Personales</h4>
                    <Input id="nombres" name="nombres" label="Nombres" bind:value={nombres} required />
                    <Input id="apellidos" name="apellidos" label="Apellidos" bind:value={apellidos} required />
                    <div class="flex flex-col gap-1">
                        <label for="idTipoDocumento" class="text-sm font-medium text-dark">Tipo de Documento</label>
                        <select name="idTipoDocumento" bind:value={idTipoDocumento} class="px-3 py-2 border border-neutral/30 rounded-md">
                            <option value={1}>Cédula de Ciudadanía</option>
                            <option value={2}>Cédula de Extranjería</option>
                            <option value={3}>Pasaporte</option>
                        </select>
                    </div>
                    <Input id="numeroDocumento" name="numeroDocumento" label="Número de Documento" bind:value={numeroDocumento} required />
                    <div class="flex flex-col gap-1">
                        <label for="idPais" class="text-sm font-medium text-dark">País</label>
                        <select name="idPais" bind:value={idPais} class="px-3 py-2 border border-neutral/30 rounded-md">
                            <option value={1}>Colombia</option>
                            <option value={2}>Venezuela</option>
                            <option value={3}>Estados Unidos</option>
                        </select>
                    </div>
                </div>

                <div class="space-y-4">
                    <h4 class="font-bold text-dark border-b pb-2">Credenciales de Acceso</h4>
                    <Input id="email" name="email" label="Correo Electrónico" type="email" bind:value={email} required />
                    <Input id="username" name="username" label="Nombre de Usuario (Login)" bind:value={username} required />
                    <Input id="password" name="password" label="Contraseña Temporal" type="password" bind:value={password} required />
                    <div class="flex flex-col gap-1">
                        <label for="rolCodigo" class="text-sm font-medium text-dark">Rol en el Sistema</label>
                        <select name="rolCodigo" bind:value={rolCodigo} class="px-3 py-2 border border-neutral/30 rounded-md bg-base/50">
                            <option value="STAFF">Secretaria / Recepción (STAFF)</option>
                        </select>
                        <p class="text-xs text-neutral mt-1">Solo se permite la creación de cuentas operativas.</p>
                    </div>
                </div> 
            </div>

            <div class="pt-4 border-t border-neutral/10 flex justify-end">
                <Button type="submit" variant="primary" class="px-8" disabled={isSubmitting}>
                    {isSubmitting ? 'Creando...' : 'Crear Usuario'}
                </Button>
            </div>
        </form>
    </div>
</div>