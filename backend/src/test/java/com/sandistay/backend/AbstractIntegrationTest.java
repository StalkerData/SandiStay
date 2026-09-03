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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    // 1. Contenedor de PostgreSQL 17
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("sandistay_test_db")
            // CRÍTICO: Usamos el superusuario 'postgres' para poder ejecutar CREATE EXTENSION btree_gist
            .withUsername("postgres")
            .withPassword("postgres")
            // SOLUCIÓN DEFINITIVA: Copiamos el archivo físico al entrypoint de Postgres.
            // Esto evita el parser de Java y deja que Postgres ejecute el PL/pgSQL nativamente.
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init.sql"),
                    "/docker-entrypoint-initdb.d/init.sql"
            );

    public static MockWebServer mockBackEnd;

    @BeforeAll
    static void beforeAll() throws IOException {
        postgres.start();
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockBackEnd.shutdown();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Apagamos Hibernate porque init.sql ya creó todo
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        registry.add("integration.payment.url", () -> mockBackEnd.url("/api/v1/bank/statement/verify").toString());
    }
}
