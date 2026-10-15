# ==========================================
# ETAPA 1: Constructor (Builder)
# ==========================================
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /build

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ==========================================
# ETAPA 2: Producción (Runner)
# ==========================================
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# SECURITY BY DESIGN: Crear un usuario sin privilegios de root
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /build/build/libs/*.jar app.jar

# Crear carpeta de uploads y darle permisos al usuario 'spring'
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Cambiar al usuario seguro
USER spring:spring

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
