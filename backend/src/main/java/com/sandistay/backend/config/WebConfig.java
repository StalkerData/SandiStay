package com.sandistay.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.time.Duration;

/**
 * Configuración global de Spring MVC.
 * Maneja el enrutamiento de recursos estáticos (imágenes subidas por los administradores).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Obtenemos la ruta absoluta de la carpeta 'uploads' en la raíz del proyecto
        String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();

        /*
         * Nota Arquitectónica: En un entorno Enterprise real (AWS/GCP), 
         * estas imágenes deberían ir a un bucket S3. 
         * Al usar almacenamiento local, dependemos de Docker Volumes para la persistencia.
         */
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                // OPTIMIZACIÓN DE RENDIMIENTO: Cacheamos las imágenes en el navegador por 7 días
                // Esto mejora drásticamente el LCP (Largest Contentful Paint) en el Frontend
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());
    }
}