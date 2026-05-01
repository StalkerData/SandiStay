<script lang="ts">
    const currentYear = new Date().getFullYear();
</script>

<footer class="bg-dark text-base py-8 mt-auto">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex flex-col md:flex-row justify-between items-center gap-4">
            
            <div class="text-center md:text-left">
                <h3 class="font-serif text-xl font-bold text-accent mb-2">SandiStay Hotel</h3>
                <p class="text-sm text-base/70">Lujo y comodidad a tu alcance.</p>
            </div>

            <div class="flex gap-6 text-sm text-base/70">
                <a href="/terminos" class="hover:text-accent transition-colors">Términos y Condiciones</a>
                <a href="/privacidad" class="hover:text-accent transition-colors">Política de Privacidad</a>
                <a href="/contacto" class="hover:text-accent transition-colors">Contacto</a>
            </div>

        </div>
        
        <div class="mt-8 pt-4 border-t border-base/20 text-center text-xs text-base/50">
            &copy; {currentYear} SandiStay. Todos los derechos reservados.
        </div>
    </div>
</footer>