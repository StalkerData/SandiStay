#!/bin/bash

# ==============================================================================
# Script de Limpieza Profunda - SandiStay
# Elimina artefactos de compilación, cachés y volúmenes de Docker.
# ==============================================================================

echo "🧹 Iniciando limpieza profunda del proyecto SandiStay..."

# 1. Detener contenedores y borrar volúmenes de Docker (si están corriendo)
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

# 5. Limpiar datos persistentes locales (Requiere sudo para pg_data)
echo "💾 Limpiando volúmenes locales y uploads..."
sudo rm -rf volumes/pg_data
rm -rf uploads/*

# 6. Limpiar scripts temporales de pruebas
echo "🗑️ Eliminando scripts temporales..."
rm -f SQL_Files/07_seed_test_performance.sql
rm -f default.conf

echo "✨ ¡Limpieza completada! El repositorio está en estado 'Clean Slate'."
