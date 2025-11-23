#!/bin/bash
# =====================================================
# Script de Compilación y Ejecución - Nova Farma
# Linux/Mac (Bash)
# =====================================================

echo ""
echo "╔══════════════════════════════════════════╗"
echo "║     NOVA FARMA - Compilación Automática  ║"
echo "╚══════════════════════════════════════════╝"
echo ""

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar que existe Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}[ERROR]${NC} Java no está instalado o no está en el PATH"
    echo ""
    echo "Instala Java JDK 8 o superior:"
    echo "  Ubuntu/Debian: sudo apt install default-jdk"
    echo "  Mac: brew install openjdk@11"
    exit 1
fi

if ! command -v javac &> /dev/null; then
    echo -e "${RED}[ERROR]${NC} javac (compilador de Java) no encontrado"
    echo "Asegúrate de tener el JDK instalado, no solo el JRE"
    exit 1
fi

echo -e "${YELLOW}[1/4]${NC} Verificando estructura de carpetas..."

if [ ! -d "src/com/novafarma" ]; then
    echo -e "${RED}[ERROR]${NC} No se encuentra la carpeta src/com/novafarma"
    echo "Asegúrate de ejecutar este script desde la raíz del proyecto"
    exit 1
fi

if [ ! -f "lib/postgresql-42.7.8.jar" ]; then
    echo -e "${RED}[ERROR]${NC} No se encuentra el driver JDBC de PostgreSQL"
    echo ""
    echo "Descárgalo desde: https://jdbc.postgresql.org/download/"
    echo "Y colócalo en la carpeta lib/ con el nombre postgresql-42.7.8.jar"
    exit 1
fi

echo -e "${GREEN}[OK]${NC} Estructura verificada"
echo ""

# Crear carpeta bin si no existe
echo -e "${YELLOW}[2/4]${NC} Creando carpeta de compilación..."
mkdir -p bin
echo -e "${GREEN}[OK]${NC} Carpeta bin creada/verificada"
echo ""

# Compilar
echo -e "${YELLOW}[3/4]${NC} Compilando código fuente..."
echo ""

# Compilar todo de una sola vez (resuelve dependencias circulares)
# Orden de compilación: model -> util -> dao -> service -> ui -> MainApp
javac -d bin -cp "lib/postgresql-42.7.8.jar" src/com/novafarma/model/*.java src/com/novafarma/util/*.java src/com/novafarma/dao/*.java src/com/novafarma/service/*.java src/com/novafarma/ui/*.java src/com/novafarma/ui/panels/*.java src/com/novafarma/*.java
if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR]${NC} Error al compilar"
    exit 1
fi

echo ""
echo -e "${GREEN}[OK]${NC} Compilación exitosa"
echo ""

# Ejecutar
echo -e "${YELLOW}[4/4]${NC} Ejecutando aplicación..."
echo ""
echo "====================================================="
echo ""

java -cp "bin:lib/postgresql-42.7.8.jar" com.novafarma.MainApp

# Si la aplicación se cerró
echo ""
echo "====================================================="
echo "Aplicación cerrada"

