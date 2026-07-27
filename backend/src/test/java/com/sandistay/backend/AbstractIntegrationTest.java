package com.sandistay.backend;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;

/**
 * Clase base para todas las pruebas de integración.
 * Levanta un contenedor Docker real de PostgreSQL 17 y un servidor HTTP falso (MockWebServer).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    // 1. Contenedor de PostgreSQL 17
    // Copiamos toda la carpeta 'init-db' al entrypoint de Postgres.
    // Postgres ejecutará los archivos 01 al 06 en orden alfabético automáticamente.
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("sandistay_test_db")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init-db"),
                    "/docker-entrypoint-initdb.d/"
            );

    // 2. Servidor HTTP falso para simular el microservicio de Python
    public static MockWebServer mockBackEnd;

    @BeforeAll
    static void beforeAll() throws IOException {
        // Iniciamos Docker (Aquí se ejecutarán tus 6 scripts SQL)
        postgres.start();

        // Iniciamos el servidor falso
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockBackEnd.shutdown();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Conexión a la BD de Docker
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // CRÍTICO: Apagamos el DDL-AUTO de Hibernate porque tus 6 scripts ya crearon TODO
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        // Redirigimos el PaymentClient hacia nuestro servidor falso
        registry.add("integration.payment.url", () -> mockBackEnd.url("/api/v1/bank/statement/verify").toString());
    }
}
