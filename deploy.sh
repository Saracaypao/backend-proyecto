#!/bin/bash

echo "🚀 Iniciando deploy de BudgetBuddy Backend..."

# Verificar que Docker esté instalado
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado. Por favor instala Docker primero."
    exit 1
fi

# Verificar que Docker Compose esté instalado
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose no está instalado. Por favor instala Docker Compose primero."
    exit 1
fi

echo "📦 Construyendo la aplicación..."
cd backend

# Limpiar y compilar el proyecto
echo "🔨 Compilando con Maven..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Error al compilar el proyecto"
    exit 1
fi

echo "✅ Compilación exitosa"

cd ..

echo "🐳 Construyendo y ejecutando contenedores..."
docker-compose up --build -d

if [ $? -ne 0 ]; then
    echo "❌ Error al ejecutar Docker Compose"
    exit 1
fi

echo "✅ Deploy completado exitosamente!"
echo "🌐 La aplicación está disponible en: http://localhost:8081"
echo "🗄️  La base de datos PostgreSQL está disponible en: localhost:5432"
echo ""
echo "📋 Comandos útiles:"
echo "   - Ver logs: docker-compose logs -f"
echo "   - Detener servicios: docker-compose down"
echo "   - Reiniciar servicios: docker-compose restart" 