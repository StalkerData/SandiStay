<script lang="ts">
    import type { ActionData } from './$types';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import { enhance } from '$app/forms';

    let { form }: { form: ActionData } = $props();
    
    let isSubmitting = $state(false);
    let oldPassword = $state('');
    let newPassword = $state('');
    let confirmPassword = $state('');

    $effect(() => {
        if (form?.success) {
            oldPassword = ''; newPassword = ''; confirmPassword = '';
        }
    });
</script>

<svelte:head>
    <title>Mi Perfil | SandiStay Admin</title>
</svelte:head>

<div class="max-w-2xl mx-auto space-y-8">
    <div>
        <h2 class="text-2xl font-bold text-dark">Mi Perfil</h2>
        <p class="text-sm text-neutral">Gestiona tus credenciales de acceso al sistema.</p>
    </div>

    {#if form?.error}
        <div class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-md shadow-sm animate-fade-in">{form.error}</div>
    {/if}
    {#if form?.success}
        <div class="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 rounded-md shadow-sm animate-fade-in">{form.message}</div>
    {/if}

    <div class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden">
        <div class="bg-base/50 px-6 py-4 border-b border-neutral/10">
            <h3 class="font-bold text-secondary text-lg">Cambiar Contraseña</h3>
        </div>
        
        <form method="POST" action="?/changePassword" class="p-6 space-y-6" use:enhance={() => {
            isSubmitting = true;
            return async ({ update }) => { await update(); isSubmitting = false; };
        }}>
            <div class="space-y-4">
                <Input id="oldPassword" name="oldPassword" label="Contraseña Actual" type="password" bind:value={oldPassword} required />
                <Input id="newPassword" name="newPassword" label="Nueva Contraseña" type="password" bind:value={newPassword} required />
                <Input id="confirmPassword" name="confirmPassword" label="Confirmar Nueva Contraseña" type="password" bind:value={confirmPassword} required />
            </div>

            <div class="pt-4 border-t border-neutral/10 flex justify-end">
                <Button type="submit" variant="primary" class="px-8" disabled={isSubmitting}>
                    {isSubmitting ? 'Actualizando...' : 'Actualizar Contraseña'}
                </Button>
            </div>
        </form>
    </div>
</div>