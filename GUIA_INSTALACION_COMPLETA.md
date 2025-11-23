# 📚 Guía Completa de Instalación - Nova Farma

Guía paso a paso para que tus compañeros puedan instalar y ejecutar el proyecto sin problemas.

---

## 📋 Tabla de Contenidos

1. [Requisitos del Sistema](#1-requisitos-del-sistema)
2. [Instalación de Dependencias](#2-instalación-de-dependencias)
3. [Configuración de Base de Datos](#3-configuración-de-base-de-datos)
4. [Configuración del Proyecto](#4-configuración-del-proyecto)
5. [Compilación y Ejecución](#5-compilación-y-ejecución)
6. [Verificación](#6-verificación)
7. [Solución de Problemas](#7-solución-de-problemas)

---

## 1. Requisitos del Sistema

### Software Necesario

| Software | Versión Mínima | Descarga |
|----------|----------------|----------|
| Java JDK | 8 o superior | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) o [OpenJDK](https://adoptium.net/) |
| PostgreSQL | 12 o superior | [PostgreSQL](https://www.postgresql.org/download/) |
| Driver JDBC | 42.7.8 | [PostgreSQL JDBC](https://jdbc.postgresql.org/download/) |

### Verificar Instalaciones

**Java:**
```bash
java -version
javac -version
```

**PostgreSQL:**
```bash
psql --version
```

---

## 2. Instalación de Dependencias

### 2.1 Instalar Java JDK

**Windows:**
1. Descarga el instalador desde Oracle o Adoptium
2. Ejecuta el instalador
3. Agrega Java al PATH si es necesario

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install default-jdk
```

**Mac:**
```bash
brew install openjdk@11
```

### 2.2 Instalar PostgreSQL

**Windows:**
1. Descarga desde: https://www.postgresql.org/download/windows/
2. Ejecuta el instalador
3. Anota la contraseña del usuario `postgres`

**Linux:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

**Mac:**
```bash
brew install postgresql
brew services start postgresql
```

### 2.3 Descargar Driver JDBC

1. Visita: https://jdbc.postgresql.org/download/
2. Descarga `postgresql-42.7.8.jar` (o versión más reciente)
3. Coloca el archivo en la carpeta `lib/` del proyecto:
   ```
   BD2 - Proyecto/
   └── lib/
       └── postgresql-42.7.8.jar
   ```

---

## 3. Configuración de Base de Datos

### 3.1 Crear Base de Datos

**Windows:**
```cmd
psql -U postgres
```

**Linux/Mac:**
```bash
sudo -u postgres psql
```

Luego ejecuta:
```sql
CREATE DATABASE nova_farma_db;
\c nova_farma_db
```

### 3.2 Crear Tablas

Ejecuta el siguiente SQL en pgAdmin o psql:

```sql
-- Tabla usuarios
CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'TRABAJADOR')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla productos
CREATE TABLE productos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL CHECK (precio >= 0),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    fecha_vencimiento DATE,
    activo BOOLEAN DEFAULT TRUE
);

-- Tabla ventas
CREATE TABLE ventas (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER NOT NULL REFERENCES productos(id),
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10, 2) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trigger para actualizar stock automáticamente
CREATE OR REPLACE FUNCTION actualizar_stock_venta()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE productos 
    SET stock = stock - NEW.cantidad
    WHERE id = NEW.producto_id;
    
    IF (SELECT stock FROM productos WHERE id = NEW.producto_id) < 0 THEN
        RAISE EXCEPTION 'Stock insuficiente para el producto ID %', NEW.producto_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_actualizar_stock
AFTER INSERT ON ventas
FOR EACH ROW
EXECUTE FUNCTION actualizar_stock_venta();

-- Insertar usuarios de prueba
INSERT INTO usuarios (username, password_hash, rol) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRADOR'),
('trabajador1', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'TRABAJADOR');
-- Contraseña para ambos: admin123
```

### 3.3 Verificar Creación

```sql
\dt
```

Deberías ver: `usuarios`, `productos`, `ventas`

```sql
SELECT username, rol FROM usuarios;
```

Deberías ver: `admin` y `trabajador1`

---

## 4. Configuración del Proyecto

### 4.1 Configurar Contraseña de Base de Datos

Abre el archivo: `src/com/novafarma/util/DatabaseConnection.java`

Busca esta línea:
```java
private static final String DB_PASSWORD = "tu_password";
```

Cámbiala por tu contraseña real de PostgreSQL:
```java
private static final String DB_PASSWORD = "mi_password_real";
```

### 4.2 Verificar Estructura del Proyecto

```
BD2 - Proyecto/
├── src/
│   └── com/
│       └── novafarma/
│           ├── MainApp.java
│           ├── model/
│           ├── dao/
│           ├── service/
│           ├── util/
│           └── ui/
├── lib/
│   └── postgresql-42.7.8.jar
├── compile.bat (Windows)
└── compile.sh (Linux/Mac)
```

---

## 5. Compilación y Ejecución

### Opción A: Scripts Automatizados (Recomendado)

**Windows:**
```cmd
compile.bat
```

**Linux/Mac:**
```bash
chmod +x compile.sh
./compile.sh
```

### Opción B: Desde IDE (Cursor/VS Code/IntelliJ)

1. Abre el proyecto en tu IDE
2. Agrega `lib/postgresql-42.7.8.jar` al Build Path
3. Ejecuta `MainApp.java` o `LoginFrame.java`

---

## 6. Verificación

### 6.1 Probar Login

1. Ejecuta la aplicación
2. Ingresa:
   - **Usuario:** `admin`
   - **Contraseña:** `admin123`
3. Deberías ver el Dashboard

### 6.2 Probar Funcionalidades

**Como Administrador:**
- ✅ Ver inventario
- ✅ Agregar producto
- ✅ Editar producto
- ✅ Eliminar producto
- ✅ Crear usuario
- ✅ Eliminar usuario (si no tiene ventas)
- ✅ Realizar ventas

**Como Trabajador:**
- ✅ Ver inventario
- ✅ Realizar ventas
- ❌ NO puede agregar/editar/eliminar productos
- ❌ NO puede crear/eliminar usuarios

---

## 7. Solución de Problemas

### Error: "Driver not found"

**Solución:**
- Verifica que `postgresql-42.7.8.jar` esté en `lib/`
- Verifica que el classpath incluya el driver

### Error: "Connection refused"

**Solución:**
- Verifica que PostgreSQL esté corriendo
- **Windows:** Servicios → PostgreSQL → Iniciar
- **Linux:** `sudo systemctl start postgresql`

### Error: "Database nova_farma_db does not exist"

**Solución:**
```sql
psql -U postgres
CREATE DATABASE nova_farma_db;
\q
```

### Error: "Contraseña incorrecta"

**Solución:**
1. Verifica la contraseña en `DatabaseConnection.java`
2. Verifica la contraseña de PostgreSQL:
   ```sql
   sudo -u postgres psql
   ALTER USER postgres PASSWORD 'nueva_password';
   ```

### Error: "Could not find or load main class"

**Solución:**
- Ejecuta desde la raíz del proyecto
- Usa el comando completo con classpath:
  ```bash
  java -cp "bin:lib/postgresql-42.7.8.jar" com.novafarma.MainApp
  ```

---

## 📞 Ayuda Adicional

Si tienes problemas:

1. Revisa los logs de error en la consola
2. Verifica los logs de PostgreSQL
3. Consulta la documentación:
   - `README.md` - Información general
   - `INICIO_RAPIDO.md` - Guía rápida
   - `GUIA_INSTALACION_COMPLETA.md` - Instalación detallada
   - `ARQUITECTURA.md` - Estructura del código
   - `FAQ.md` - Preguntas frecuentes

---

## ✅ Checklist Final

Antes de presentar, verifica:

- [ ] Java JDK instalado y funcionando
- [ ] PostgreSQL instalado y corriendo
- [ ] Base de datos `nova_farma_db` creada
- [ ] Tablas creadas (usuarios, productos, ventas)
- [ ] Trigger `trigger_actualizar_stock` creado
- [ ] Usuarios de prueba insertados
- [ ] Driver JDBC descargado en `lib/`
- [ ] Contraseña actualizada en `DatabaseConnection.java`
- [ ] Proyecto compila sin errores
- [ ] Login funciona correctamente
- [ ] Control de roles funciona (admin vs trabajador)

---

**¡Listo para usar! 🚀**

