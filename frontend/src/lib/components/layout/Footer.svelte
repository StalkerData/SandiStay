<script lang="ts">
    const currentYear = new Date().getFullYear();
</script>

<footer class="bg-dark text-base py-10 mt-auto border-t border-neutral/10" aria-labelledby="footer-heading">
    <h2 id="footer-heading" class="sr-only">Pie de página</h2>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex flex-col md:flex-row justify-between items-center gap-6">
            
            <div class="text-center md:text-left">
                <h3 class="font-serif text-2xl font-bold text-accent mb-2">SandiStay Hotel</h3>
                <p class="text-sm text-base/70 max-w-xs">Lujo, comodidad y experiencias inolvidables a tu alcance.</p>
            </div>

            <nav class="flex flex-wrap justify-center gap-x-8 gap-y-4 text-sm text-base/70 font-medium" aria-label="Enlaces legales y de contacto">
                <a href="/terminos" class="hover:text-accent transition-colors focus:outline-none focus:ring-2 focus:ring-accent rounded-sm px-1">Términos y Condiciones</a>
                <a href="/privacidad" class="hover:text-accent transition-colors focus:outline-none focus:ring-2 focus:ring-accent rounded-sm px-1">Política de Privacidad</a>
                <a href="/contacto" class="hover:text-accent transition-colors focus:outline-none focus:ring-2 focus:ring-accent rounded-sm px-1">Contacto</a>
            </nav>

        </div>
        
        <div class="mt-10 pt-6 border-t border-base/10 text-center text-xs text-base/50 flex flex-col md:flex-row justify-between items-center gap-4">
            <p>&copy; {currentYear} SandiStay. Todos los derechos reservados.</p>
            <p>Desarrollado con <span class="text-red-500" aria-hidden="true">♥</span> para la excelencia hotelera.</p>
        </div>
    </div>
</footer>