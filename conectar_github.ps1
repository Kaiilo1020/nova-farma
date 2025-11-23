# Script para conectar el proyecto local con GitHub
# Ejecuta este script DESPUÉS de crear el repositorio en GitHub

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Conectar Nova Farma con GitHub" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Solicitar el nombre de usuario de GitHub
$usuario = Read-Host "Ingresa tu nombre de usuario de GitHub"

# Solicitar el nombre del repositorio
$repo = Read-Host "Ingresa el nombre del repositorio (ej: nova-farma)"

# Solicitar el método de conexión
Write-Host ""
Write-Host "Método de conexión:" -ForegroundColor Yellow
Write-Host "1. HTTPS (recomendado para principiantes)"
Write-Host "2. SSH (requiere claves configuradas)"
$metodo = Read-Host "Selecciona (1 o 2)"

# Construir la URL según el método
if ($metodo -eq "1") {
    $url = "https://github.com/$usuario/$repo.git"
} else {
    $url = "git@github.com:$usuario/$repo.git"
}

Write-Host ""
Write-Host "Conectando con: $url" -ForegroundColor Green
Write-Host ""

# Verificar si ya existe un remote
$remoteExists = git remote -v 2>$null
if ($remoteExists) {
    Write-Host "Ya existe un remote configurado." -ForegroundColor Yellow
    $sobrescribir = Read-Host "¿Deseas sobrescribirlo? (s/n)"
    if ($sobrescribir -eq "s" -or $sobrescribir -eq "S") {
        git remote remove origin
    } else {
        Write-Host "Operación cancelada." -ForegroundColor Red
        exit
    }
}

# Agregar el remote
Write-Host "Agregando remote 'origin'..." -ForegroundColor Cyan
git remote add origin $url

# Cambiar a rama main
Write-Host "Cambiando a rama 'main'..." -ForegroundColor Cyan
git branch -M main

# Mostrar información
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Configuración completada!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Para subir tu código a GitHub, ejecuta:" -ForegroundColor Yellow
Write-Host "  git push -u origin main" -ForegroundColor White
Write-Host ""
Write-Host "Si GitHub te pide autenticación:" -ForegroundColor Yellow
Write-Host "  - Usa un Personal Access Token (no tu contraseña)" -ForegroundColor White
Write-Host "  - Crea uno en: https://github.com/settings/tokens" -ForegroundColor White
Write-Host ""

