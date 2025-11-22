# 🏥 NOVA FARMA - Sistema de Gestión Farmacéutica

Sistema Java Swing + PostgreSQL para gestión de farmacias con seguridad SHA-256 y control de roles.

---

## 🚀 INSTALACIÓN RÁPIDA

### 1. Base de Datos
```bash
# Crear BD
psql -U postgres
CREATE DATABASE nova_farma_db;
\q

# Ejecutar script
psql -U postgres -d nova_farma_db -f database/schema.sql
```

### 2. Configurar Conexión
Editar `src/com/novafarma/util/DatabaseConnection.java`:
```java
private static final String DB_PASSWORD = "TU_PASSWORD"; // Cambiar aquí
```

### 3. Ejecutar
- Abrir proyecto en IDE
- Agregar `lib/postgresql-42.X.X.jar` al Build Path
- Ejecutar `LoginFrame.java`

---

## 🔑 CREDENCIALES

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `admin` | `admin123` | ADMINISTRADOR (acceso total) |
| `trabajador1` | `trabajador123` | TRABAJADOR (solo ventas) |

---

## ✅ FUNCIONALIDADES IMPLEMENTADAS

### **1. Seguridad SHA-256**
- ✅ Contraseñas encriptadas con `SecurityHelper.encryptPassword()`
- ✅ Login con verificación de hash
- ✅ Recuperación de contraseña (click "¿Olvidaste tu contraseña?")

### **2. Control de Roles (RBAC)**
- ✅ **ADMINISTRADOR:** Puede agregar/editar/eliminar productos, crear usuarios, vender
- ✅ **TRABAJADOR:** Solo puede vender y ver inventario (botones de edición deshabilitados)

### **3. Inventario Inteligente**
- ✅ **Buscador en tiempo real:** Filtra tabla al escribir (TableRowSorter)
- ✅ **Alertas visuales de vencimiento:**
  - 🔴 ROJO = Vencido
  - 🟠 NARANJA = Vence en ≤ 30 días
  - 🟢 VERDE = Buen estado

### **4. Punto de Venta (POS)**
- ✅ Pantalla dividida: Catálogo | Carrito (JSplitPane)
- ✅ Validación de stock antes de agregar al carrito
- ✅ Cálculo automático de totales
- ✅ Stock actualizado por trigger de PostgreSQL (Java solo hace INSERT en ventas)

### **5. Gestión de Usuarios**
- ✅ Solo ADMINISTRADOR puede crear usuarios
- ✅ Contraseñas encriptadas con SHA-256 antes de guardar

---

## 🛡️ SEGURIDAD

- ✅ Contraseñas encriptadas (SHA-256, 64 caracteres hex)
- ✅ PreparedStatement en todos los queries (anti SQL Injection)
- ✅ Trigger de PostgreSQL actualiza stock (Java NO lo hace manualmente)
- ✅ Trazabilidad: Cada venta registra usuario_id

---

## 🗂️ ESTRUCTURA DEL CÓDIGO

```
src/com/novafarma/
├── model/
│   └── User.java                     # Modelo con roles (ADMINISTRADOR/TRABAJADOR)
├── util/
│   ├── DatabaseConnection.java       # Conexión PostgreSQL
│   └── SecurityHelper.java           # SHA-256
├── ui/
│   ├── LoginFrame.java               # Login + Recuperación contraseña
│   ├── Dashboard.java                # Dashboard con RBAC, inventario, POS
│   ├── ProductExpirationRenderer.java # Alertas visuales (colores)
│   └── UserCreationDialog.java       # Crear usuarios
└── MainApp.java
```

---

## 📋 ARCHIVOS CLAVE

### **SecurityHelper.java**
Encripta contraseñas con SHA-256:
```java
public static String encryptPassword(String password) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
    // Convierte a hexadecimal (64 caracteres)
    return toHex(hash);
}
```

### **Dashboard.java - Control de Roles**
```java
private void applyRolePermissions() {
    if (currentUser.isTrabajador()) {
        btnAddProduct.setEnabled(false);  // Deshabilitar edición
        btnEditProduct.setEnabled(false);
        btnDeleteProduct.setEnabled(false);
    }
}
```

### **ProductExpirationRenderer.java - Alertas Visuales**
```java
public Component getTableCellRendererComponent(...) {
    long diasRestantes = calcularDias(fechaVencimiento);
    
    if (diasRestantes < 0)
        cell.setBackground(COLOR_ROJO);      // Vencido
    else if (diasRestantes <= 30)
        cell.setBackground(COLOR_NARANJA);   // Por vencer
    else
        cell.setBackground(COLOR_VERDE);     // OK
}
```

### **Trigger de PostgreSQL (schema.sql)**
```sql
CREATE TRIGGER trigger_actualizar_stock
AFTER INSERT ON ventas
FOR EACH ROW
EXECUTE FUNCTION actualizar_stock_venta();

-- Java solo hace: INSERT INTO ventas (...)
-- El trigger actualiza el stock automáticamente
```

---

## 🧪 PROBAR ENCRIPTACIÓN SHA-256

```bash
java -cp src com.novafarma.util.SecurityHelper
```

Salida:
```
Contraseña: admin123
Hash SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
Longitud: 64 caracteres
```

---

## ⚙️ SOLUCIÓN DE PROBLEMAS

**Error de conexión:**
- Verificar que PostgreSQL esté ejecutándose
- Verificar contraseña en `DatabaseConnection.java`

**Driver no encontrado:**
- Descargar: https://jdbc.postgresql.org/download/
- Colocar `postgresql-42.X.X.jar` en `lib/`

**Usuario/contraseña incorrectos:**
- Usar credenciales de arriba o resetear en PostgreSQL:
```sql
UPDATE usuarios 
SET password_hash = '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9' 
WHERE username = 'admin';
-- Nueva contraseña: admin123
```

---

## 📊 REQUISITOS CUMPLIDOS

✅ SHA-256 implementado (`SecurityHelper`)  
✅ Login con contraseña encriptada  
✅ Recuperación de contraseña  
✅ RBAC (ADMINISTRADOR/TRABAJADOR)  
✅ Buscador con TableRowSorter  
✅ Renderer personalizado (alertas visuales)  
✅ JSplitPane en POS  
✅ Validación de stock  
✅ Trigger actualiza stock (Java NO lo hace)  
✅ PreparedStatement en todos los queries  
✅ CRUD completo (INSERT, UPDATE, DELETE)  

---

**Sistema listo para usar.** 🚀
