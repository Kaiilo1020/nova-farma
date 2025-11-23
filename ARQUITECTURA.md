# 🏗️ Arquitectura del Sistema - Nova Farma

Documento técnico que describe la estructura y diseño del sistema.

---

## 📐 Arquitectura General

```
┌─────────────────────────────────────────────────────────┐
│                   CAPA DE PRESENTACIÓN                   │
│                     (Java Swing)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ LoginFrame   │  │  Dashboard   │  │UserCreation  │  │
│  │              │→ │              │  │   Dialog     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────┐
│                   CAPA DE LÓGICA                         │
│  ┌──────────────┐         │         ┌──────────────┐   │
│  │SecurityHelper│←────────┴────────→│  User Model  │   │
│  │  (SHA-256)   │                   │              │   │
│  └──────────────┘                   └──────────────┘   │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────┐
│                   CAPA DE DATOS                          │
│  ┌──────────────────────┐ │                             │
│  │ DatabaseConnection   │ │  (JDBC)                     │
│  │   (Singleton)        │─┘                             │
│  └──────────────────────┘                               │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────┐
│                   BASE DE DATOS                          │
│                    (PostgreSQL)                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ usuarios │  │productos │  │  ventas  │              │
│  └──────────┘  └──────────┘  └──────────┘              │
└─────────────────────────────────────────────────────────┘
```

---

## 📦 Estructura de Paquetes

```
com.novafarma/
│
├── MainApp.java                    # Punto de entrada
│
├── model/                          # Modelos de datos (POJOs)
│   ├── User.java                   # Usuario con roles
│   │   ├── Atributos: id, username, passwordHash, rol
│   │   └── Enum: UserRole (ADMINISTRADOR, TRABAJADOR)
│   ├── Product.java                # Producto del inventario
│   └── Sale.java                   # Venta registrada
│
├── dao/                            # Data Access Object (Capa de Datos)
│   ├── UserDAO.java                # CRUD de usuarios
│   ├── ProductDAO.java             # CRUD de productos
│   └── SaleDAO.java                # CRUD de ventas
│
├── service/                        # Lógica de Negocio (Capa de Servicios)
│   ├── UserService.java            # Gestión y validación de usuarios
│   │   ├── hasSales()              # Verifica si usuario tiene ventas
│   │   ├── getSalesCount()         # Cuenta ventas del usuario
│   │   └── deleteUser()            # Elimina usuario con validaciones
│   ├── ProductService.java         # Validaciones y reglas de productos
│   │   ├── findProductByName()     # Busca productos duplicados
│   │   └── validateSellableProduct() # Valida producto para venta
│   └── SaleService.java            # Procesamiento de ventas
│       └── processMultipleSales()  # Transacciones atómicas
│
├── util/                           # Utilidades
│   ├── SecurityHelper.java         # Encriptación SHA-256
│   │   ├── encryptPassword()       # Convierte texto → hash
│   │   └── verifyPassword()        # Valida contraseña
│   │
│   └── DatabaseConnection.java     # Gestión de conexión JDBC
│       ├── Patrón: Singleton
│       └── getConnection()         # Retorna Connection
│
└── ui/                             # Interfaces gráficas (Capa de Presentación)
    ├── LoginFrame.java             # Ventana de autenticación
    │   ├── performLogin()          # Login con SHA-256
    │   └── showPasswordRecovery()  # Recuperación de contraseña
    │
    ├── Dashboard.java              # Panel principal (Coordinador)
    │   ├── applyRolePermissions()  # Control de acceso
    │   ├── createUsersPanel()     # Panel de gestión de usuarios
    │   ├── loadUsersData()         # Carga tabla de usuarios
    │   ├── deleteUser()            # Elimina usuario con validaciones
    │   └── Tabs: Inventario, Ventas, Usuarios, Alertas
    │
    ├── UserCreationDialog.java     # Crear usuarios
    │   └── createUser()            # INSERT con hash
    │
    ├── ProductExpirationRenderer.java # Alertas visuales (colores)
    │
    └── panels/                     # Paneles modulares (FASE B)
        ├── InventoryPanel.java     # Panel de inventario
        ├── SalesPanel.java         # Panel de ventas/facturación unificado
        └── AlertsPanel.java        # Panel de alertas de vencimiento
```

---

## 🔐 Flujo de Seguridad (SHA-256)

### 1. Registro de Usuario

```
┌─────────────┐
│ Admin crea  │
│   usuario   │
└──────┬──────┘
       │
       │ password = "admin123"
       ↓
┌─────────────────────────────────┐
│ SecurityHelper.encryptPassword()│
│                                 │
│ MessageDigest.getInstance(      │
│    "SHA-256")                   │
│ .digest("admin123".bytes)       │
└─────────────┬───────────────────┘
              │
              │ hash = "240be51..."
              ↓
┌─────────────────────────────────┐
│ INSERT INTO usuarios            │
│ (username, password_hash, rol)  │
│ VALUES ('admin', '240be51...', │
│         'ADMINISTRADOR')        │
└─────────────────────────────────┘
```

### 2. Login

```
┌─────────────┐
│ Usuario     │
│ ingresa     │
│ contraseña  │
└──────┬──────┘
       │
       │ input = "admin123"
       ↓
┌─────────────────────────────────┐
│ SecurityHelper.encryptPassword()│
│ hash_input = "240be51..."       │
└─────────────┬───────────────────┘
              │
              ↓
┌─────────────────────────────────┐
│ SELECT * FROM usuarios          │
│ WHERE username = 'admin'        │
│   AND password_hash = '240be...'│
└─────────────┬───────────────────┘
              │
              ↓
        ┌─────┴─────┐
        │ ¿Existe?  │
        └─────┬─────┘
              │
      ┌───────┴────────┐
      │                │
   Sí │                │ No
      ↓                ↓
┌──────────┐    ┌──────────┐
│ Login OK │    │  Error   │
└──────────┘    └──────────┘
```

### 3. Recuperación de Contraseña

```
┌─────────────┐
│ Usuario     │
│ olvida      │
│ contraseña  │
└──────┬──────┘
       │
       ↓
┌─────────────────────────────────┐
│ 1. Verificar que usuario existe │
│    SELECT id FROM usuarios      │
│    WHERE username = ?           │
└─────────────┬───────────────────┘
              │
              ↓
┌─────────────────────────────────┐
│ 2. Solicitar nueva contraseña   │
│    nueva_pass = "nuevapass123"  │
└─────────────┬───────────────────┘
              │
              ↓
┌─────────────────────────────────┐
│ 3. Encriptar con SHA-256        │
│    nuevo_hash = SecurityHelper  │
│        .encryptPassword(...)    │
└─────────────┬───────────────────┘
              │
              ↓
┌─────────────────────────────────┐
│ 4. UPDATE usuarios              │
│    SET password_hash = ?        │
│    WHERE username = ?           │
└─────────────────────────────────┘
```

---

## 🎭 Control de Roles

### Modelo de Roles

```
┌────────────────────────────────────────────────────┐
│                     User                           │
├────────────────────────────────────────────────────┤
│ - id: int                                          │
│ - username: String                                 │
│ - passwordHash: String                             │
│ - rol: UserRole                                    │
├────────────────────────────────────────────────────┤
│ + isAdministrador(): boolean                       │
│ + isTrabajador(): boolean                          │
└─────────────┬──────────────────────────────────────┘
              │
              │ enum UserRole
              │
        ┌─────┴─────┐
        │           │
   ┌────┴────┐ ┌───┴────────┐
   │  ADMIN  │ │ TRABAJADOR │
   └────┬────┘ └───┬────────┘
        │          │
        │          │
┌───────┴──────────┴───────────────────────┐
│         Matriz de Permisos               │
├──────────────────┬───────────┬───────────┤
│ Operación        │   Admin   │Trabajador │
├──────────────────┼───────────┼───────────┤
│ Ver inventario   │     ✅    │    ✅     │
│ Agregar producto │     ✅    │    ❌     │
│ Editar producto  │     ✅    │    ❌     │
│ Eliminar producto│     ✅    │    ❌     │
│ Crear usuario    │     ✅    │    ❌     │
│ Eliminar usuario │     ✅    │    ❌     │
│ Registrar venta  │     ✅    │    ✅     │
│ Ver alertas      │     ✅    │    ✅     │
└──────────────────┴───────────┴───────────┘
```

### Implementación del Control

```java
// Dashboard.java - Método crítico
private void applyRolePermissions() {
    if (currentUser.isTrabajador()) {
        // Capa 1: UI (deshabilitar botones)
        btnAddProduct.setEnabled(false);
        btnEditProduct.setEnabled(false);
        btnDeleteProduct.setEnabled(false);
        
        // Capa 2: Visual (color gris)
        btnAddProduct.setBackground(Color.LIGHT_GRAY);
    }
}

// Capa 3: Lógica (validación en métodos)
private void addProduct() {
    if (currentUser.isTrabajador()) {
        JOptionPane.showMessageDialog(this,
            "ACCESO DENEGADO",
            JOptionPane.ERROR_MESSAGE);
        return; // Detener ejecución
    }
    // ... código de agregar producto
}
```

---

## 🗄️ Modelo de Base de Datos

### Diagrama ER (Entidad-Relación)

```
┌─────────────────────┐
│      usuarios       │
├─────────────────────┤
│ PK id (SERIAL)      │
│    username         │
│    password_hash    │
│    rol              │
│    fecha_creacion   │
└──────────┬──────────┘
           │
           │ 1
           │
           │ N
           ↓
┌─────────────────────┐        ┌─────────────────────┐
│       ventas        │   N:1  │     productos       │
├─────────────────────┤────────├─────────────────────┤
│ PK id               │        │ PK id               │
│ FK producto_id      │←───────│    nombre           │
│ FK usuario_id       │        │    descripcion      │
│    cantidad         │        │    precio           │
│    precio_unitario  │        │    stock            │
│    total            │        │    fecha_vencimiento│
│    fecha_venta      │        │    fecha_creacion   │
└─────────────────────┘        └─────────────────────┘
```

### Tipos de Datos

```sql
-- usuarios
CREATE TABLE usuarios (
    id                SERIAL PRIMARY KEY,
    username          VARCHAR(50) UNIQUE NOT NULL,
    password_hash     VARCHAR(64) NOT NULL,  -- SHA-256 = 64 chars
    rol               VARCHAR(20) CHECK (rol IN ('ADMINISTRADOR', 'TRABAJADOR')),
    fecha_creacion    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- productos
CREATE TABLE productos (
    id                SERIAL PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL,
    descripcion       TEXT,
    precio            DECIMAL(10, 2) CHECK (precio >= 0),
    stock             INTEGER CHECK (stock >= 0),
    fecha_vencimiento DATE,
    fecha_creacion    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ventas
CREATE TABLE ventas (
    id                SERIAL PRIMARY KEY,
    producto_id       INTEGER REFERENCES productos(id),
    usuario_id        INTEGER REFERENCES usuarios(id),
    cantidad          INTEGER CHECK (cantidad > 0),
    precio_unitario   DECIMAL(10, 2),
    total             DECIMAL(10, 2),
    fecha_venta       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🔄 Flujo de Ejecución

### Flujo Principal de la Aplicación

```
┌──────────────┐
│  MainApp     │
│  .main()     │
└──────┬───────┘
       │
       ↓
┌──────────────────────────┐
│  Configurar Look & Feel  │
└──────────┬───────────────┘
           │
           ↓
┌──────────────────────────┐
│  new LoginFrame()        │
└──────────┬───────────────┘
           │
           ↓
┌──────────────────────────┐
│  Usuario ingresa datos   │
└──────────┬───────────────┘
           │
           ↓
┌──────────────────────────┐
│  performLogin()          │
│  - Encriptar password    │
│  - Query a BD            │
│  - Validar credenciales  │
└──────────┬───────────────┘
           │
           ↓
     ┌─────┴─────┐
     │ ¿Válido?  │
     └─────┬─────┘
           │
    ┌──────┴───────┐
    │              │
  Sí│              │No
    ↓              ↓
┌────────┐   ┌─────────┐
│Dashboard│   │ Error   │
└────┬───┘   └─────────┘
     │
     ↓
┌─────────────────────────┐
│ applyRolePermissions()  │
│ - Habilitar/Deshabilitar│
│   según rol             │
└─────────┬───────────────┘
          │
          ↓
┌─────────────────────────┐
│ Usuario interactúa con  │
│ el Dashboard            │
└─────────────────────────┘
```

---

## 🛡️ Capas de Seguridad

### 1. Capa de Transporte
- JDBC usa conexiones seguras a PostgreSQL

### 2. Capa de Datos
- Contraseñas hasheadas con SHA-256
- No se almacena texto plano
- PreparedStatement (previene SQL injection)

### 3. Capa de Lógica
- Validación de roles en cada método
- Doble verificación (UI + código)

### 4. Capa de Presentación
- Botones deshabilitados según rol
- Tooltips informativos

---

## 🎯 Patrones de Diseño Implementados

### 1. Singleton (DatabaseConnection)

```java
public class DatabaseConnection {
    private static Connection connection = null;
    
    // Constructor privado
    private DatabaseConnection() {}
    
    // Método estático para obtener instancia
    public static Connection getConnection() {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(...);
        }
        return connection;
    }
}
```

**Ventaja:** Una única conexión compartida.

### 2. MVC (Modelo-Vista-Controlador)

- **Modelo:** `User.java`
- **Vista:** `LoginFrame.java`, `Dashboard.java`
- **Controlador:** Métodos de acción en las vistas

### 3. Factory (UserRole)

```java
public enum UserRole {
    ADMINISTRADOR, TRABAJADOR;
    
    public static UserRole fromString(String str) {
        // Convierte String → UserRole
    }
}
```

---

## 📊 Diagramas de Secuencia

### Login Exitoso

```
Usuario    LoginFrame    SecurityHelper    DatabaseConnection    PostgreSQL
  │             │                │                  │                  │
  │─ingresa─────>│                │                  │                  │
  │ credenciales│                │                  │                  │
  │             │─encryptPassword>│                  │                  │
  │             │<─hash───────────│                  │                  │
  │             │──getConnection──────────────────> │                  │
  │             │<─Connection─────────────────────── │                  │
  │             │──SELECT * WHERE username=? AND password_hash=?───────>│
  │             │<─ResultSet (1 fila)──────────────────────────────────│
  │             │─openDashboard────────────────────> │                  │
  │<─Dashboard──│                │                  │                  │
```

### Agregar Producto (Admin)

```
Admin      Dashboard      DatabaseConnection    PostgreSQL
  │             │                  │                  │
  │─clic Agregar│                  │                  │
  │─────────────>│                  │                  │
  │             │─¿isAdmin?(✓)     │                  │
  │             │─showDialog───>   │                  │
  │<─formulario─│                  │                  │
  │─llenar datos│                  │                  │
  │─────────────>│                  │                  │
  │             │─getConnection────>│                  │
  │             │<─Connection───────│                  │
  │             │─INSERT INTO productos───────────────>│
  │             │<─Success──────────────────────────── │
  │<─Mensaje OK─│                  │                  │
```

### Agregar Producto (Trabajador)

```
Trabajador  Dashboard
  │             │
  │─clic Agregar│
  │─────────────>│
  │             │─¿isAdmin?(✗)
  │             │
  │<─"DENEGADO"─│
  │             │
  (fin)
```

---

## 🧩 Componentes Clave

### SecurityHelper
- **Responsabilidad:** Encriptación y validación
- **Método principal:** `encryptPassword(String)`
- **Algoritmo:** SHA-256 (MessageDigest)
- **Output:** String de 64 caracteres hexadecimales

### DatabaseConnection
- **Responsabilidad:** Gestión de conexión JDBC
- **Patrón:** Singleton
- **Configuración:** localhost:5432/nova_farma_db

### User (Model)
- **Responsabilidad:** Representar datos de usuario
- **Atributos:** id, username, passwordHash, rol
- **Métodos:** isAdministrador(), isTrabajador()

### LoginFrame
- **Responsabilidad:** Autenticación
- **Funciones:** Login, Recuperación de contraseña

### Dashboard
- **Responsabilidad:** Interfaz principal y coordinador
- **Funciones:** Control de roles, Gestión de inventario, Gestión de usuarios
- **Paneles modulares:** InventoryPanel, SalesPanel, AlertsPanel

### UserService
- **Responsabilidad:** Lógica de negocio para usuarios
- **Funciones:** 
  - `hasSales(userId)`: Verifica si usuario tiene ventas
  - `getSalesCount(userId)`: Cuenta ventas del usuario
  - `deleteUser(userId)`: Elimina usuario con validaciones

### ProductService
- **Responsabilidad:** Lógica de negocio para productos
- **Funciones:**
  - `findProductByName()`: Detecta productos duplicados
  - `validateSellableProduct()`: Valida producto para venta

### SaleService
- **Responsabilidad:** Procesamiento de ventas
- **Funciones:**
  - `processMultipleSales()`: Transacciones atómicas
  - `validateCart()`: Valida carrito antes de vender

---

## 👥 Gestión de Usuarios

### Flujo de Eliminación de Usuario

```
┌─────────────┐
│ Admin hace  │
│ clic en     │
│ "Eliminar"  │
└──────┬──────┘
       │
       ↓
┌─────────────────────────────────┐
│ 1. Validar selección            │
│    ¿Usuario seleccionado?       │
└─────────────┬───────────────────┘
              │
              ↓
┌─────────────────────────────────┐
│ 2. Validar que no sea          │
│    el usuario actual            │
│    if (userId == currentUser.id)│
└─────────────┬───────────────────┘
              │
              ↓
┌─────────────────────────────────┐
│ 3. UserService.hasSales()       │
│    ¿Tiene ventas?               │
└─────────────┬───────────────────┘
              │
      ┌───────┴────────┐
      │                │
   Sí │                │ No
      ↓                ↓
┌──────────┐    ┌──────────────────┐
│ Error:   │    │ 4. Confirmar     │
│ "No se   │    │    eliminación   │
│ puede    │    └─────────┬─────────┘
│ eliminar │              │
│ porque   │              ↓
│ tiene    │    ┌──────────────────┐
│ ventas"  │    │ 5. UserDAO.delete()│
└──────────┘    │    DELETE FROM   │
                │    usuarios      │
                └─────────┬─────────┘
                         │
                         ↓
                ┌──────────────────┐
                │ 6. Recargar tabla│
                │    loadUsersData()│
                └──────────────────┘
```

### Validaciones Implementadas

- ✅ **No eliminar usuario actual:** Previene auto-eliminación
- ✅ **No eliminar usuarios con ventas:** Conserva historial del negocio
- ✅ **Solo administradores:** Control de acceso por rol
- ✅ **Mensajes descriptivos:** Explica por qué no se puede eliminar

---

## 📈 Escalabilidad Futura

### Mejoras Recomendadas

1. **Seguridad:**
   - Usar BCrypt en lugar de SHA-256 simple
   - Implementar salting (sal criptográfica)
   - Agregar HTTPS para conexiones remotas
   - Implementar campo `activo` para usuarios (soft delete)

2. **Arquitectura:**
   - ✅ **COMPLETADO:** Separar la lógica de negocio en capa Service
   - ✅ **COMPLETADO:** Implementar DAOs (Data Access Objects)
   - Usar un framework como Spring para inyección de dependencias
   - Implementar patrón Repository

3. **Funcionalidad:**
   - Reportes en PDF
   - Gráficos estadísticos
   - Sistema de backup automático
   - Logs de auditoría
   - Historial de cambios en productos

---

**Arquitectura diseñada para ser educativa, segura y escalable. 🏗️**

