<script lang="ts">
    import { enhance } from '$app/forms';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import type { ActionData } from './$types';

    /**
     * @description Recibe los datos de error si el Server Action falla (ej. credenciales inválidas).
     */
    let { form }: { form: ActionData } = $props();
    
    // Estado reactivo para deshabilitar el botón y evitar doble submit
    let isSubmitting = $state(false);
</script>

<svelte:head>
    <title>Acceso Administrativo | SandiStay</title>
</svelte:head>

<main class="min-h-screen flex items-center justify-center bg-base px-4 sm:px-6 lg:px-8">
    <section class="max-w-md w-full space-y-8 bg-surface p-10 rounded-2xl shadow-xl border border-neutral/10">
        
        <!-- Cabecera -->
        <header class="text-center">
            <div class="w-16 h-16 bg-secondary rounded-xl flex items-center justify-center text-white font-serif font-bold text-3xl mx-auto mb-4 shadow-md" aria-hidden="true">
                S
            </div>
            <h1 class="mt-2 text-3xl font-serif font-bold text-dark">
                SandiStay Admin
            </h1>
            <p class="mt-2 text-sm text-neutral">
                Ingresa tus credenciales para acceder al panel de control.
            </p>
        </header>

        <!-- 
            Formulario usando Server Actions.
            use:enhance previene la recarga completa de la página (Progressive Enhancement).
        -->
        <form 
            method="POST" 
            action="?/login" 
            class="mt-8 space-y-6"
            use:enhance={() => {
                isSubmitting = true;
                return async ({ update }) => {
                    await update(); // Actualiza el DOM con los errores si los hay
                    isSubmitting = false;
                };
            }}
        >
            <!-- Mensaje de Error del Servidor -->
            {#if form?.error}
                <div class="bg-red-50 border-l-4 border-red-500 p-4 rounded-md animate-fade-in" role="alert" aria-live="assertive">
                    <div class="flex">
                        <div class="flex-shrink-0">
                            <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                            </svg>
                        </div>
                        <div class="ml-3">
                            <p class="text-sm text-red-700 font-medium">
                                {form.error}
                            </p>
                        </div>
                    </div>
                </div>
            {/if}

            <div class="space-y-4">
                <Input 
                    id="username" 
                    name="username" 
                    label="Usuario" 
                    type="text" 
                    required 
                    autocomplete="username"
                    placeholder="ej. admin"
                    aria-invalid={form?.error ? 'true' : 'false'}
                />
                
                <Input 
                    id="password" 
                    name="password" 
                    label="Contraseña" 
                    type="password" 
                    required 
                    autocomplete="current-password"
                    placeholder="••••••••"
                    aria-invalid={form?.error ? 'true' : 'false'}
                />
            </div>

            <div class="flex items-center justify-between">
                <div class="flex items-center">
                    <input id="remember-me" name="remember-me" type="checkbox" class="h-4 w-4 text-secondary focus:ring-secondary border-neutral/30 rounded cursor-pointer">
                    <label for="remember-me" class="ml-2 block text-sm text-neutral cursor-pointer">
                        Recordarme
                    </label>
                </div>

                <div class="text-sm">
                    <!-- TODO: Implementar flujo de recuperación de contraseña -->
                    <button type="button" class="font-medium text-secondary hover:text-primary transition-colors" onclick={() => alert('Contacte al administrador del sistema para restablecer su contraseña.')}>
                        ¿Olvidaste tu contraseña?
                    </button>
                </div>
            </div>

            <div>
                <Button type="submit" variant="primary" class="w-full py-3 text-lg shadow-lg shadow-primary/20 transition-transform active:scale-95" disabled={isSubmitting}>
                    {isSubmitting ? 'Verificando...' : 'Iniciar Sesión'}
                </Button>
            </div>
        </form>
        
        <footer class="text-center mt-4">
            <a href="/" class="text-sm text-neutral hover:text-dark underline transition-colors">
                ← Volver a la web pública
            </a>
        </footer>
    </section>
</main>