#!/bin/bash

# ==============================================================================
# Script de Limpieza y Re-aprovisionamiento - SandiStay
# Elimina artefactos de compilación, cachés, datos persistentes
# y re-genera certificados SSL para un despliegue 'Clean Slate'.
# ==============================================================================

# Si se pasa una IP como argumento la usa, sino usa la de la VM por defecto
SERVER_IP="${1:-192.168.122.182}"

echo "🧹 Iniciando limpieza profunda del proyecto SandiStay..."

# 1. Detener contenedores y destruir volúmenes de Docker
echo "🐳 Deteniendo contenedores y eliminando volúmenes de Docker..."
docker compose -f docker-compose.prod.yml down -v 2>/dev/null
docker compose -f docker-compose.yml down -v 2>/dev/null

# 2. Limpiar Backend (Java/Gradle)
echo "☕ Limpiando artefactos del Backend..."
rm -rf backend/bin backend/build backend/.gradle backend/out

# 3. Limpiar Frontend (Node.js/SvelteKit)
echo "🌐 Limpiando artefactos del Frontend..."
rm -rf frontend/node_modules frontend/build frontend/dist frontend/.svelte-kit

# 4. Limpiar reportes de pruebas (Playwright y ZAP)
echo "🧪 Limpiando reportes de pruebas..."
rm -rf frontend/playwright-report frontend/test-results
rm -f reporte_*.html zap.yaml
rm -rf Documentation_Methodology


# 5. Limpiar datos persistentes locales y uploads
echo "💾 Limpiando volúmenes locales y uploads..."
sudo rm -rf volumes/pg_data
sudo rm -rf uploads/*
mkdir -p uploads/Habitaciones uploads/Areas
chmod -R a+w uploads

# 6. Limpiar scripts temporales de pruebas y configs obsoletas
echo "🗑️ Eliminando scripts temporales y redundantes..."
rm -f SQL_Files/07_seed_test_performance.sql
rm -f default.conf

# 7. Re-generación de Certificados SSL (HTTPS)
echo "🔐 Generando certificados SSL autofirmados para IP: ${SERVER_IP}..."
rm -rf nginx/ssl
mkdir -p nginx/ssl

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/server.key \
  -out nginx/ssl/server.crt \
  -subj "/C=CO/ST=Valle/L=Cali/O=SandiStay/OU=DevOps/CN=${SERVER_IP}" 2>/dev/null

echo "✨ ¡Limpieza y configuración completadas exitosamente!"
echo "👉 Para desplegar ejecuta: docker compose -f docker-compose.prod.yml up -d --build"
