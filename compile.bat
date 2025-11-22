@echo off
REM =====================================================
REM Script de Compilación y Ejecución - Nova Farma
REM Windows (CMD/PowerShell)
REM =====================================================

echo.
echo ╔══════════════════════════════════════════╗
echo ║     NOVA FARMA - Compilacion Automatica  ║
echo ╚══════════════════════════════════════════╝
echo.

REM Verificar que existe Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java no esta instalado o no esta en el PATH
    echo.
    echo Instala Java JDK 8 o superior desde:
    echo https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo [1/4] Verificando estructura de carpetas...
if not exist "src\com\novafarma" (
    echo [ERROR] No se encuentra la carpeta src/com/novafarma
    echo Asegurate de ejecutar este script desde la raiz del proyecto
    pause
    exit /b 1
)

if not exist "lib\postgresql-42.7.8.jar" (
    echo [ERROR] No se encuentra el driver JDBC de PostgreSQL
    echo.
    echo Descargalo desde: https://jdbc.postgresql.org/download/
    echo Y colocalo en la carpeta lib/ con el nombre postgresql-42.7.8.jar
    pause
    exit /b 1
)

echo [OK] Estructura verificada
echo.

REM Crear carpeta bin si no existe
echo [2/4] Creando carpeta de compilacion...
if not exist "bin" mkdir bin
echo [OK] Carpeta bin creada/verificada
echo.

REM Compilar
echo [3/4] Compilando codigo fuente...
echo.

REM Compilar todo de una sola vez (resuelve dependencias circulares)
javac -d bin -cp "lib\postgresql-42.7.8.jar" src\com\novafarma\model\*.java src\com\novafarma\util\*.java src\com\novafarma\ui\*.java src\com\novafarma\*.java
if %errorlevel% neq 0 goto error_compile

echo.
echo [OK] Compilacion exitosa
echo.

REM Ejecutar
echo [4/4] Ejecutando aplicacion...
echo.
echo =====================================================
echo.
java -cp "bin;lib\postgresql-42.7.8.jar" com.novafarma.MainApp

REM Si la aplicación se cerró
echo.
echo =====================================================
echo Aplicacion cerrada
pause
exit /b 0

:error_compile
echo.
echo [ERROR] Error durante la compilacion
echo.
echo Verifica:
echo 1. Que todos los archivos .java esten en su lugar
echo 2. Que no haya errores de sintaxis en el codigo
echo 3. Que el driver JDBC este en lib/postgresql-42.7.8.jar
echo.
pause
exit /b 1

