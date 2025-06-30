Write-Host "🚀 Iniciando deploy de BudgetBuddy Backend..." -ForegroundColor Green

# Verificar que Docker esté instalado
try {
    docker --version | Out-Null
    Write-Host "✅ Docker encontrado" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker no está instalado. Por favor instala Docker Desktop primero." -ForegroundColor Red
    exit 1
}

# Verificar que Docker Compose esté instalado
try {
    docker-compose --version | Out-Null
    Write-Host "✅ Docker Compose encontrado" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker Compose no está instalado. Por favor instala Docker Compose primero." -ForegroundColor Red
    exit 1
}

Write-Host "📦 Construyendo la aplicación..." -ForegroundColor Yellow
Set-Location backend

# Limpiar y compilar el proyecto
Write-Host "🔨 Compilando con Maven..." -ForegroundColor Yellow
if (Test-Path "mvnw.cmd") {
    .\mvnw.cmd clean package -DskipTests
} else {
    mvn clean package -DskipTests
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al compilar el proyecto" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Compilación exitosa" -ForegroundColor Green

Set-Location ..

Write-Host "🐳 Construyendo y ejecutando contenedores..." -ForegroundColor Yellow
docker-compose up --build -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al ejecutar Docker Compose" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Deploy completado exitosamente!" -ForegroundColor Green
Write-Host "🌐 La aplicación está disponible en: http://localhost:8081" -ForegroundColor Cyan
Write-Host "🗄️  La base de datos PostgreSQL está disponible en: localhost:5432" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Comandos útiles:" -ForegroundColor Yellow
Write-Host "   - Ver logs: docker-compose logs -f" -ForegroundColor White
Write-Host "   - Detener servicios: docker-compose down" -ForegroundColor White
Write-Host "   - Reiniciar servicios: docker-compose restart" -ForegroundColor White 