# 📦 Guía de Instalación Detallada - Nova Farma

Esta guía te llevará paso a paso por la instalación completa del sistema.

---

## 📋 Tabla de Contenidos

1. [Instalación de Java JDK](#1-instalación-de-java-jdk)
2. [Instalación de PostgreSQL](#2-instalación-de-postgresql)
3. [Descarga del Driver JDBC](#3-descarga-del-driver-jdbc)
4. [Configuración de la Base de Datos](#4-configuración-de-la-base-de-datos)
5. [Configuración del Proyecto](#5-configuración-del-proyecto)
6. [Compilación y Ejecución](#6-compilación-y-ejecución)
7. [Verificación](#7-verificación)

---

## 1. Instalación de Java JDK

### Windows

1. Descarga JDK 8 o superior desde:
   - https://www.oracle.com/java/technologies/downloads/
   - O usa OpenJDK: https://adoptium.net/

2. Ejecuta el instalador y sigue las instrucciones

3. Verifica la instalación:
   ```cmd
   java -version
   javac -version
   ```

4. Si los comandos no funcionan, agrega Java al PATH:
   - Panel de Control → Sistema → Configuración avanzada del sistema
   - Variables de entorno → Variable PATH
   - Agregar: `C:\Program Files\Java\jdk-XX\bin`

### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install default-jdk
java -version
javac -version
```

### Mac

```bash
brew install openjdk@11
java -version
javac -version
```

---

## 2. Instalación de PostgreSQL

### Windows

1. Descarga PostgreSQL desde: https://www.postgresql.org/download/windows/
2. Ejecuta el instalador
3. Durante la instalación:
   - **Puerto**: Deja el 5432 por defecto
   - **Contraseña**: Anota la contraseña del usuario `postgres`
   - **Locale**: Español o por defecto
4. Verifica la instalación:
   ```cmd
   psql --version
   ```

### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

Configurar contraseña:
```bash
sudo -u postgres psql
ALTER USER postgres PASSWORD 'tu_password';
\q
```

### Mac

```bash
brew install postgresql
brew services start postgresql
```

---

## 3. Descarga del Driver JDBC

El driver JDBC es necesario para que Java se comunique con PostgreSQL.

### Opción 1: Descarga Directa

1. Visita: https://jdbc.postgresql.org/download/
2. Descarga la versión más reciente (ej: `postgresql-42.7.1.jar`)
3. Guarda el archivo en la carpeta `lib/` de tu proyecto:
   ```
   BD2 - Proyecto/
   └── lib/
       └── postgresql-42.7.1.jar
   ```

### Opción 2: Maven (si usas Maven)

Agrega a tu `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>
```

---

## 4. Configuración de la Base de Datos

### Paso 1: Conectar a PostgreSQL

**Windows:**
```cmd
psql -U postgres
```

**Linux/Mac:**
```bash
sudo -u postgres psql
```

Ingresa la contraseña que configuraste durante la instalación.

### Paso 2: Crear la Base de Datos

```sql
CREATE DATABASE nova_farma_db;
```

Verifica:
```sql
\l
```

Deberías ver `nova_farma_db` en la lista.

### Paso 3: Conectar a la Base de Datos

```sql
\c nova_farma_db
```

### Paso 4: Ejecutar el Script SQL

**Opción A: Desde psql (dentro de la sesión)**

```sql
\i 'C:/ruta/completa/database/schema.sql'
```

**Opción B: Desde la terminal**

**Windows:**
```cmd
psql -U postgres -d nova_farma_db -f database\schema.sql
```

**Linux/Mac:**
```bash
psql -U postgres -d nova_farma_db -f database/schema.sql
```

### Paso 5: Verificar las Tablas

```sql
\dt
```

Deberías ver:
- usuarios
- productos
- ventas

### Paso 6: Verificar Datos de Prueba

```sql
SELECT username, rol FROM usuarios;
```

Deberías ver:
- admin (ADMINISTRADOR)
- trabajador1 (TRABAJADOR)

---

## 5. Configuración del Proyecto

### Paso 1: Actualizar la Contraseña de BD

Abre el archivo `src/com/novafarma/util/DatabaseConnection.java`

Busca esta línea:
```java
private static final String DB_PASSWORD = "tu_password"; // ¡CAMBIAR ESTO!
```

Cámbiala por tu contraseña real:
```java
private static final String DB_PASSWORD = "mi_password_real";
```

### Paso 2: Verificar la Estructura de Carpetas

```
BD2 - Proyecto/
├── src/
│   └── com/
│       └── novafarma/
│           ├── MainApp.java
│           ├── model/
│           │   └── User.java
│           ├── util/
│           │   ├── SecurityHelper.java
│           │   └── DatabaseConnection.java
│           └── ui/
│               ├── LoginFrame.java
│               ├── Dashboard.java
│               └── UserCreationDialog.java
├── database/
│   └── schema.sql
├── lib/
│   └── postgresql-42.7.1.jar
└── bin/  (se creará automáticamente)
```

---

## 6. Compilación y Ejecución

### Opción 1: Scripts Automatizados

**Windows:**
```cmd
compile.bat
```

**Linux/Mac:**
```bash
chmod +x compile.sh
./compile.sh
```

### Opción 2: Compilación Manual

#### Paso 1: Crear carpeta bin

**Windows:**
```cmd
mkdir bin
```

**Linux/Mac:**
```bash
mkdir -p bin
```

#### Paso 2: Compilar

**Windows:**
```cmd
javac -d bin -cp "lib\postgresql-42.7.1.jar" src\com\novafarma\**\*.java
```

**Linux/Mac:**
```bash
javac -d bin -cp "lib/postgresql-42.7.1.jar" src/com/novafarma/**/*.java
```

Si el comando anterior no funciona, compila archivo por archivo:
```bash
javac -d bin -cp "lib/postgresql-42.7.1.jar" src/com/novafarma/model/User.java
javac -d bin -cp "lib/postgresql-42.7.1.jar" src/com/novafarma/util/SecurityHelper.java
javac -d bin -cp "lib/postgresql-42.7.1.jar:bin" src/com/novafarma/util/DatabaseConnection.java
javac -d bin -cp "lib/postgresql-42.7.1.jar:bin" src/com/novafarma/ui/LoginFrame.java
javac -d bin -cp "lib/postgresql-42.7.1.jar:bin" src/com/novafarma/ui/UserCreationDialog.java
javac -d bin -cp "lib/postgresql-42.7.1.jar:bin" src/com/novafarma/ui/Dashboard.java
javac -d bin -cp "lib/postgresql-42.7.1.jar:bin" src/com/novafarma/MainApp.java
```

#### Paso 3: Ejecutar

**Windows:**
```cmd
java -cp "bin;lib\postgresql-42.7.1.jar" com.novafarma.MainApp
```

**Linux/Mac:**
```bash
java -cp "bin:lib/postgresql-42.7.1.jar" com.novafarma.MainApp
```

---

## 7. Verificación

### Prueba 1: Verificar Conexión a Base de Datos

```bash
java -cp "bin:lib/postgresql-42.7.1.jar" com.novafarma.util.DatabaseConnection
```

Deberías ver:
```
✓ Conexión establecida con PostgreSQL
```

### Prueba 2: Verificar Encriptación SHA-256

```bash
java -cp bin com.novafarma.util.SecurityHelper
```

Deberías ver hashes generados correctamente.

### Prueba 3: Login

1. Ejecuta la aplicación
2. Ingresa:
   - **Usuario**: admin
   - **Contraseña**: admin123
3. Deberías ver el Dashboard con todas las funcionalidades habilitadas

### Prueba 4: Probar Rol Trabajador

1. Cierra sesión
2. Ingresa:
   - **Usuario**: trabajador1
   - **Contraseña**: trabajador123
3. Los botones de "Agregar", "Editar" y "Eliminar" producto deben estar deshabilitados

### Prueba 5: Recuperación de Contraseña

1. En el login, clic en "¿Olvidaste tu contraseña?"
2. Ingresa: admin
3. Establece una nueva contraseña
4. Verifica que puedas hacer login con la nueva contraseña

---

## 🐛 Solución de Problemas Comunes

### Error: "javac no se reconoce como comando"

**Causa**: Java no está en el PATH

**Solución Windows**:
1. Busca donde está instalado Java: `C:\Program Files\Java\jdk-XX\bin`
2. Agrégalo al PATH (ver sección 1)

**Solución Linux/Mac**:
```bash
export PATH=$PATH:/usr/lib/jvm/java-11-openjdk-amd64/bin
```

---

### Error: "org.postgresql.Driver not found"

**Causa**: El driver JDBC no está en el classpath

**Solución**:
1. Verifica que `postgresql-XX.X.jar` esté en la carpeta `lib/`
2. Al compilar/ejecutar, incluye `-cp "lib/postgresql-42.7.1.jar"`

---

### Error: "Connection refused"

**Causa**: PostgreSQL no está corriendo

**Solución Windows**:
1. Servicios → PostgreSQL → Iniciar

**Solución Linux**:
```bash
sudo systemctl start postgresql
sudo systemctl status postgresql
```

---

### Error: "Base de datos nova_farma_db no existe"

**Solución**:
```sql
psql -U postgres
CREATE DATABASE nova_farma_db;
\q
```

---

### Error: "Contraseña incorrecta"

**Solución**:
1. Verifica la contraseña en `DatabaseConnection.java`
2. Verifica la contraseña de PostgreSQL:
   ```bash
   sudo -u postgres psql
   ALTER USER postgres PASSWORD 'nueva_password';
   ```

---

### Error: "Could not find or load main class"

**Causa**: La clase no está en la ubicación correcta

**Solución**:
1. Verifica que los archivos `.class` estén en `bin/com/novafarma/`
2. Ejecuta desde la raíz del proyecto
3. Usa el comando completo con classpath

---

## ✅ Checklist Final

Antes de presentar tu proyecto, verifica:

- [ ] Java JDK instalado y funcionando
- [ ] PostgreSQL instalado y corriendo
- [ ] Base de datos `nova_farma_db` creada
- [ ] Tablas creadas con el script SQL
- [ ] Usuarios de prueba insertados
- [ ] Driver JDBC descargado en `lib/`
- [ ] Contraseña actualizada en `DatabaseConnection.java`
- [ ] Proyecto compila sin errores
- [ ] Login funciona correctamente
- [ ] Control de roles funciona (admin vs trabajador)
- [ ] Recuperación de contraseña funciona
- [ ] Productos se pueden agregar/editar/eliminar (solo admin)

---

## 📞 ¿Necesitas Ayuda?

Si tienes problemas:
1. Revisa los logs de error en la consola
2. Verifica los logs de PostgreSQL
3. Consulta la documentación de cada clase (comentarios en el código)

**¡Éxito con tu instalación! 🚀**

