<script lang="ts">
    import type { ActionData } from './$types';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import { enhance } from '$app/forms';

    // --- PROPS & ESTADO ---
    let { form }: { form: ActionData } = $props();
    
    let isSubmitting = $state(false);
    let oldPassword = $state('');
    let newPassword = $state('');
    let confirmPassword = $state('');
</script>

<svelte:head>
    <title>Mi Perfil | SandiStay Admin</title>
</svelte:head>

<main class="max-w-2xl mx-auto space-y-8">
    
    <header>
        <h1 class="text-2xl font-bold text-dark">Mi Perfil</h1>
        <p class="text-sm text-neutral">Gestiona tus credenciales de acceso al sistema.</p>
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

    <section class="bg-surface rounded-xl shadow-sm border border-neutral/10 overflow-hidden">
        <header class="bg-base/50 px-6 py-4 border-b border-neutral/10">
            <h2 class="font-bold text-secondary text-lg">Cambiar Contraseña</h2>
        </header>
        
        <form method="POST" action="?/changePassword" class="p-6 space-y-6" use:enhance={() => {
            isSubmitting = true;
            return async ({ update, result }) => { 
                // SvelteKit limpia el formulario automáticamente si le pasamos reset: true
                await update({ reset: result.type === 'success' }); 
                isSubmitting = false; 
                
                // Limpiamos el estado reactivo si fue exitoso
                if (result.type === 'success') {
                    oldPassword = ''; 
                    newPassword = ''; 
                    confirmPassword = '';
                }
            };
        }}>
            <div class="space-y-4">
                <Input id="oldPassword" name="oldPassword" label="Contraseña Actual" type="password" bind:value={oldPassword} required />
                <Input id="newPassword" name="newPassword" label="Nueva Contraseña" type="password" bind:value={newPassword} required />
                <Input id="confirmPassword" name="confirmPassword" label="Confirmar Nueva Contraseña" type="password" bind:value={confirmPassword} required />
            </div>

            <footer class="pt-4 border-t border-neutral/10 flex justify-end">
                <Button type="submit" variant="primary" class="px-8 transition-transform active:scale-95" disabled={isSubmitting}>
                    {isSubmitting ? 'Actualizando...' : 'Actualizar Contraseña'}
                </Button>
            </footer>
        </form>
    </section>
</main>