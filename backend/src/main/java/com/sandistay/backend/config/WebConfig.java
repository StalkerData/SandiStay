package com.sandistay.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.time.Duration;

/**
 * Configuración global de Spring MVC.
 * Maneja el enrutamiento de recursos estáticos (imágenes subidas).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Obtenemos la ruta absoluta de la carpeta 'uploads'
        String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();

        // CORRECCIÓN CRÍTICA: Spring MVC exige que los directorios terminen en '/'
        // Si no termina en '/', Spring no puede resolver las subcarpetas y da 404
        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());
    }
}
