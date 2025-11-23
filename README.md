# Nova Farma - Sistema de Gestión Farmacéutica

Sistema de escritorio desarrollado en Java Swing con PostgreSQL para la gestión integral de farmacias, incluyendo inventario, ventas, control de vencimientos y gestión de usuarios con seguridad SHA-256.

---

## 📋 Tabla de Contenidos

- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Funcionalidades](#funcionalidades)
- [Seguridad](#seguridad)
- [Base de Datos](#base-de-datos)
- [Solución de Problemas](#solución-de-problemas)

---

## Requisitos

| Software | Versión Mínima | Descarga |
|----------|----------------|----------|
| Java JDK | 8 o superior | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) o [OpenJDK](https://adoptium.net/) |
| PostgreSQL | 12 o superior | [PostgreSQL](https://www.postgresql.org/download/) |
| Driver JDBC | 42.7.8 | [PostgreSQL JDBC](https://jdbc.postgresql.org/download/) |

---

## Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/Kaiilo1020/nova-farma.git
cd nova-farma
```

### 2. Instalar Dependencias

**Java JDK:**
- Windows: Descargar e instalar desde Oracle o Adoptium
- Linux: `sudo apt install default-jdk`
- Mac: `brew install openjdk@11`

**PostgreSQL:**
- Windows: Descargar instalador desde postgresql.org
- Linux: `sudo apt install postgresql postgresql-contrib`
- Mac: `brew install postgresql`

**Driver JDBC:**
1. Descargar `postgresql-42.7.8.jar` desde [jdbc.postgresql.org](https://jdbc.postgresql.org/download/)
2. Colocar en la carpeta `lib/` del proyecto

### 3. Configurar Base de Datos

**Crear Base de Datos:**
```sql
psql -U postgres
CREATE DATABASE nova_farma_db;
\c nova_farma_db
```

**Crear Tablas:**
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

---

## Configuración

### Configurar Conexión a Base de Datos

Editar `src/com/novafarma/config/DatabaseConfig.java`:

```java
public class DatabaseConfig {
    public static final String DB_HOST = "localhost";
    public static final String DB_PORT = "5432";
    public static final String DB_NAME = "nova_farma_db";
    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "tu_password_postgresql"; // ← Cambiar aquí
    // ...
}
```

**Nota:** La configuración está separada de la lógica de conexión para mejor mantenibilidad.

---

## Ejecución

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

### Opción 2: Desde IDE

1. Abrir proyecto en IDE (IntelliJ, VS Code, Cursor)
2. Agregar `lib/postgresql-42.7.8.jar` al Build Path
3. Ejecutar `MainApp.java`

### Credenciales de Acceso

| Usuario       | Contraseña | Rol |
|---------------|------------|-----|
| `admin`       | `1234`     | ADMINISTRADOR |
| `trabajador1` | `1234`     | TRABAJADOR |

---

## Estructura del Proyecto

```
src/com/novafarma/
├── config/                   # Configuración
│   └── DatabaseConfig.java   # Configuración de conexión BD
├── model/                     # Modelos de datos (POJOs)
│   ├── User.java             # Usuario con roles
│   ├── Product.java          # Producto con lógica de vencimiento
│   └── Sale.java             # Venta
├── dao/                       # Data Access Object (Acceso a BD)
│   ├── UserDAO.java          # CRUD usuarios (con paginación)
│   ├── ProductDAO.java       # CRUD productos (con paginación)
│   └── SaleDAO.java          # CRUD ventas (con paginación)
├── service/                   # Lógica de negocio
│   ├── UserService.java      # Gestión y validación de usuarios
│   ├── ProductService.java   # Validaciones y reglas de productos
│   └── SaleService.java      # Procesamiento de ventas
├── util/                      # Utilidades
│   ├── DatabaseConnection.java   # Conexión PostgreSQL (Singleton)
│   ├── SecurityHelper.java       # Encriptación SHA-256
│   ├── TableStyleHelper.java     # Estilos de tablas (centralizado)
│   ├── PaginationHelper.java     # Lógica de paginación
│   └── Mensajes.java             # Mensajes UI centralizados
├── ui/                        # Interfaz gráfica
│   ├── LoginFrame.java       # Autenticación
│   ├── Dashboard.java        # Panel principal (refactorizado)
│   ├── ProductDialog.java    # Diálogo para crear/editar productos
│   ├── UserCreationDialog.java
│   ├── ProductExpirationRenderer.java  # Colores de alerta
│   ├── handlers/             # Handlers de lógica UI
│   │   ├── ProductHandler.java    # Lógica de productos
│   │   └── UserHandler.java       # Lógica de usuarios
│   └── panels/               # Paneles modulares
│       ├── InventoryPanel.java    # Gestión de inventario (con paginación)
│       ├── SalesPanel.java       # Punto de venta (con paginación)
│       └── AlertsPanel.java      # Alertas de vencimiento
└── MainApp.java              # Punto de entrada
```

### Arquitectura en Capas

```
UI (Interfaz) → Handlers (Lógica UI) → Service (Lógica) → DAO (Datos) → Database (PostgreSQL)
```

- **UI**: Presentación y eventos del usuario
- **Handlers**: Lógica específica de UI (ProductHandler, UserHandler)
- **Service**: Validaciones y reglas de negocio
- **DAO**: Operaciones SQL (SELECT, INSERT, UPDATE, DELETE)
- **Database**: Almacenamiento persistente

### Optimizaciones Implementadas

✅ **Queries N+1 Resueltos**: Usuarios con conteo de ventas en una sola query  
✅ **Paginación**: Carga de datos en chunks (50 registros por página) para mejor rendimiento  
✅ **Recargas Optimizadas**: Actualización de filas individuales en lugar de recargar toda la tabla  
✅ **Dashboard Refactorizado**: Lógica separada en handlers para mejor mantenibilidad  
✅ **Formularios Mejorados**: Diálogos dedicados (ProductDialog) en lugar de JOptionPane  
✅ **Código Centralizado**: Mensajes, estilos y utilidades reutilizables

---

## Funcionalidades

### 1. Autenticación y Seguridad
- Login con contraseñas encriptadas SHA-256
- Recuperación de contraseña
- Control de acceso por roles (RBAC)

### 2. Gestión de Inventario
- CRUD completo de productos
- **Diálogo dedicado** para crear/editar productos (mejor UX)
- Buscador en tiempo real
- **Paginación automática** cuando hay más de 100 productos
- Alertas visuales de vencimiento:
  - 🔴 Rojo: Producto vencido
  - 🟠 Naranja: Vence en ≤30 días
  - 🟢 Verde: Buen estado
- Detección y prevención de duplicados
- Soft delete (productos inactivos, no eliminados)
- **Actualización optimizada**: Solo se actualiza la fila modificada

### 3. Punto de Venta
- Catálogo de productos con stock disponible
- **Paginación automática** cuando hay más de 100 productos
- Carrito de compras con cálculo automático
- Campos de facturación (tipo comprobante, cliente, DNI/RUC)
- Validación de stock antes de vender
- Validación de productos vencidos
- Actualización automática de stock mediante trigger de PostgreSQL

### 4. Gestión de Usuarios
- Creación de usuarios (solo administradores)
- Tabla de usuarios con conteo de ventas (**optimizado**: una sola query)
- **Paginación automática** cuando hay más de 100 usuarios
- Eliminación con validaciones (no permite eliminar usuarios con ventas)

### 5. Alertas de Vencimiento
- Panel dedicado para productos próximos a vencer
- Eliminación masiva de productos vencidos

---

## Seguridad

### SHA-256

Las contraseñas se encriptan usando SHA-256 antes de almacenarse:

```java
// SecurityHelper.java
public static String encryptPassword(String password) {
    MessageDigest sha = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = sha.digest(password.getBytes());
    // Convierte a hexadecimal (64 caracteres)
    return resultado.toString();
}
```

**Características:**
- Unidireccional: No se puede revertir
- Determinístico: Misma contraseña = mismo hash
- 64 caracteres hexadecimales

### Control de Roles

**ADMINISTRADOR:**
- Acceso total al sistema
- Puede crear/editar/eliminar productos
- Puede crear/eliminar usuarios
- Puede realizar ventas

**TRABAJADOR:**
- Solo puede realizar ventas
- Puede ver inventario
- No puede modificar productos ni usuarios

**Implementación:**
```java
// Dashboard.java
if (currentUser.isTrabajador()) {
    btnAddProduct.setEnabled(false);
    btnEditProduct.setEnabled(false);
    btnDeleteProduct.setEnabled(false);
}
```

### Prevención de SQL Injection

Todos los queries usan `PreparedStatement`:

```java
String sql = "SELECT * FROM usuarios WHERE username = ? AND password_hash = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setString(1, username);
pstmt.setString(2, passwordHash);
```

---

## Base de Datos

### Modelo de Datos

**Tabla: usuarios**
- `id` (SERIAL PRIMARY KEY)
- `username` (VARCHAR, UNIQUE)
- `password_hash` (VARCHAR(64)) - Hash SHA-256
- `rol` (VARCHAR) - ADMINISTRADOR o TRABAJADOR
- `fecha_creacion` (TIMESTAMP)

**Tabla: productos**
- `id` (SERIAL PRIMARY KEY)
- `nombre` (VARCHAR)
- `descripcion` (TEXT)
- `precio` (DECIMAL)
- `stock` (INTEGER)
- `fecha_vencimiento` (DATE)
- `activo` (BOOLEAN)

**Tabla: ventas**
- `id` (SERIAL PRIMARY KEY)
- `producto_id` (INTEGER, FK → productos)
- `usuario_id` (INTEGER, FK → usuarios)
- `cantidad` (INTEGER)
- `precio_unitario` (DECIMAL)
- `total` (DECIMAL)
- `fecha_venta` (TIMESTAMP)

### Trigger de Actualización de Stock

El trigger `trigger_actualizar_stock` se ejecuta automáticamente después de cada INSERT en `ventas`, actualizando el stock del producto. Java solo hace INSERT, no UPDATE manual del stock.

---

## Solución de Problemas

### Error: "Driver not found"
- Verificar que `postgresql-42.7.8.jar` esté en `lib/`
- Verificar que el IDE tenga el JAR en el Build Path

### Error: "Connection refused"
- Verificar que PostgreSQL esté ejecutándose
- Windows: Servicios → PostgreSQL → Iniciar
- Linux: `sudo systemctl start postgresql`

### Error: "Database nova_farma_db does not exist"
```sql
psql -U postgres
CREATE DATABASE nova_farma_db;
```

### Error: "Contraseña incorrecta"
- Verificar contraseña en `DatabaseConfig.java` (no en DatabaseConnection)
- Verificar contraseña de PostgreSQL:
```sql
ALTER USER postgres PASSWORD 'nueva_password';
```

### Resetear Contraseña de Usuario
```sql
UPDATE usuarios 
SET password_hash = '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9' 
WHERE username = 'admin';
-- Nueva contraseña: admin123
```

---

## Notas Técnicas

### Comportamiento de IDs en PostgreSQL
- Los IDs con `SERIAL` no se reutilizan cuando eliminas registros
- Si eliminas un registro con ID 5, el siguiente tendrá ID 8 (comportamiento normal)

### Eliminación de Usuarios
- Solo se pueden eliminar usuarios sin ventas registradas
- Esto preserva el historial del negocio
- Si un trabajador ya no trabaja, simplemente no le permitas iniciar sesión

### Arquitectura y Patrones de Diseño
- **Patrón Singleton**: `DatabaseConnection` (una única conexión)
- **Patrón DAO**: Separación de acceso a datos
- **Service Layer**: Lógica de negocio separada de la UI
- **Handlers**: Separación de lógica UI (ProductHandler, UserHandler)
- **Arquitectura en Capas**: Model → DAO → Service → Handlers → UI
- **Separación de Configuración**: `DatabaseConfig` separado de `DatabaseConnection`
- **Utilidades Centralizadas**: Mensajes, estilos y helpers reutilizables

---

## Requisitos Cumplidos

### Funcionalidades Base
✅ Encriptación SHA-256  
✅ Login con contraseña encriptada  
✅ Recuperación de contraseña  
✅ Control de roles (RBAC)  
✅ Buscador con TableRowSorter  
✅ Renderer personalizado (alertas visuales)  
✅ JSplitPane en punto de venta  
✅ Validación de stock  
✅ Trigger de PostgreSQL para actualización de stock  
✅ PreparedStatement en todos los queries  
✅ CRUD completo (INSERT, UPDATE, DELETE)  
✅ Soft delete de productos  
✅ Prevención de duplicados

### Optimizaciones y Mejoras
✅ Queries N+1 resueltos (usuarios con ventas)  
✅ Paginación implementada (inventario, ventas, usuarios)  
✅ Recargas optimizadas (actualización de filas individuales)  
✅ Dashboard refactorizado (handlers separados)  
✅ Formularios mejorados (diálogos dedicados)  
✅ Código centralizado (Mensajes, TableStyleHelper, PaginationHelper)  
✅ Configuración separada (DatabaseConfig)  
✅ Código limpio (sin residuos ni clases no utilizadas)  

---

## Versión Actual

**v2.0 - Optimizada y Refactorizada**

- Arquitectura mejorada con handlers y separación de responsabilidades
- Optimizaciones de rendimiento (paginación, queries optimizados)
- Mejor UX con diálogos dedicados
- Código más mantenible y escalable

**Desarrollado con Java Swing y PostgreSQL** 🚀
