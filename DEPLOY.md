# 🚀 Guía de Deploy - BudgetBuddy Backend

## 📋 Prerrequisitos

- **Java 21** instalado
- **Docker** y **Docker Compose** instalados
- **Maven** (opcional, se incluye mvnw)

## 🐳 Deploy con Docker (Recomendado)

### Opción 1: Deploy Automático

```bash
# Dar permisos de ejecución al script
chmod +x deploy.sh

# Ejecutar el deploy
./deploy.sh
```

### Opción 2: Deploy Manual

```bash
# 1. Compilar el proyecto
cd backend
./mvnw clean package -DskipTests
cd ..

# 2. Construir y ejecutar con Docker Compose
docker-compose up --build -d
```

### Verificar el Deploy

```bash
# Ver logs en tiempo real
docker-compose logs -f

# Verificar que los contenedores estén corriendo
docker-compose ps

# Probar la API
curl http://localhost:8081/actuator/health
```

## ☁️ Deploy en la Nube

### Heroku

1. **Crear cuenta en Heroku**
2. **Instalar Heroku CLI**
3. **Configurar variables de entorno**

```bash
# Login a Heroku
heroku login

# Crear aplicación
heroku create tu-app-name

# Configurar base de datos PostgreSQL
heroku addons:create heroku-postgresql:mini

# Configurar variables de entorno
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set SERVER_PORT=$PORT

# Deploy
git push heroku main
```

### Railway

1. **Conectar repositorio de GitHub**
2. **Configurar variables de entorno**
3. **Deploy automático**

### Render

1. **Conectar repositorio de GitHub**
2. **Configurar como Web Service**
3. **Configurar variables de entorno**

## 🔧 Configuración de Variables de Entorno

### Variables Requeridas

```bash
# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password

# Servidor
SERVER_PORT=8081

# Perfil de Spring
SPRING_PROFILES_ACTIVE=prod
```

## 📊 Monitoreo

### Health Check

```bash
curl http://localhost:8081/actuator/health
```

### Logs

```bash
# Ver logs del backend
docker-compose logs backend

# Ver logs de la base de datos
docker-compose logs postgres
```

## 🛠️ Comandos Útiles

```bash
# Detener servicios
docker-compose down

# Reiniciar servicios
docker-compose restart

# Ver estado de contenedores
docker-compose ps

# Limpiar volúmenes (cuidado: borra datos)
docker-compose down -v

# Rebuild sin cache
docker-compose build --no-cache
```

## 🔒 Seguridad

- Cambiar contraseñas por defecto
- Configurar HTTPS en producción
- Usar variables de entorno para secretos
- Configurar firewall apropiadamente

## 📝 Troubleshooting

### Error de Conexión a Base de Datos
```bash
# Verificar que PostgreSQL esté corriendo
docker-compose logs postgres

# Verificar conectividad
docker-compose exec backend ping postgres
```

### Error de Puerto en Uso
```bash
# Verificar qué está usando el puerto
netstat -tulpn | grep 8081

# Cambiar puerto en docker-compose.yml si es necesario
```

### Error de Memoria
```bash
# Aumentar memoria para Docker
# En Docker Desktop: Settings > Resources > Memory
``` 