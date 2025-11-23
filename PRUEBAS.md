# 🧪 Guía de Pruebas - Nova Farma

Este documento describe cómo probar y demostrar que el sistema cumple con todos los requisitos críticos del profesor.

---

## 📋 Requisitos a Demostrar

1. ✅ Encriptación SHA-256 de contraseñas
2. ✅ Recuperación de contraseña
3. ✅ Control de roles (Admin vs Trabajador)

---

## 1. Prueba de Encriptación SHA-256

### Objetivo
Demostrar que las contraseñas NO se guardan en texto plano y que se usa SHA-256.

### Pasos

1. **Ejecutar la clase SecurityHelper standalone:**
   ```bash
   java -cp bin com.novafarma.util.SecurityHelper
   ```

2. **Resultado esperado:**
   ```
   === DEMOSTRACIÓN DE ENCRIPTACIÓN SHA-256 ===

   Contraseña: admin123
   Hash SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
   Longitud: 64 caracteres

   Contraseña: trabajador456
   Hash SHA-256: [otro hash diferente]
   Longitud: 64 caracteres

   === DEMOSTRACIÓN DE VERIFICACIÓN ===
   Verificando 'admin123' (correcta): true
   Verificando 'admin124' (incorrecta): false
   ```

3. **Verificar en la base de datos:**
   ```sql
   psql -U postgres -d nova_farma_db
   
   SELECT username, password_hash FROM usuarios;
   ```

4. **Resultado esperado:**
   ```
      username   |                         password_hash                        
   --------------+--------------------------------------------------------------
    admin        | 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
    trabajador1  | e29ad9f2e3e0eb0e82d1a33e52e2d0e1d53c8f19e2d3e4a5b6c7d8e9f0a1b2c3
   ```

### ✅ Puntos a Destacar

- Las contraseñas en la BD son hashes de 64 caracteres hexadecimales
- Es **imposible** revertir el hash a la contraseña original
- La misma contraseña siempre genera el mismo hash
- Contraseñas diferentes generan hashes completamente distintos

---

## 2. Prueba de Login con SHA-256

### Objetivo
Demostrar que el login encripta la contraseña ingresada antes de compararla.

### Pasos

1. **Iniciar la aplicación:**
   ```bash
   java -cp "bin:lib/postgresql-42.7.8.jar" com.novafarma.MainApp
   ```

2. **Intentar login con credenciales correctas:**
   - Usuario: `admin`
   - Contraseña: `admin123`

3. **Observar la consola:**
   Deberías ver el mensaje:
   ```
   ✓ Bienvenido, admin!
   ```

4. **Código relevante (LoginFrame.java, línea ~200):**
   ```java
   // PASO CRÍTICO: Encriptar la contraseña con SHA-256
   String passwordHash = SecurityHelper.encryptPassword(password);
   
   // Consulta a la BD con el HASH, no la contraseña plana
   String sql = "SELECT ... WHERE username = ? AND password_hash = ?";
   stmt.setString(2, passwordHash);
   ```

5. **Intentar login con contraseña incorrecta:**
   - Usuario: `admin`
   - Contraseña: `incorrecta`

6. **Resultado esperado:**
   ```
   Usuario o contraseña incorrectos
   ```

### ✅ Puntos a Destacar

- La contraseña se encripta **antes** de enviarla a la BD
- Nunca se transmite la contraseña en texto plano
- La comparación es: `hash_ingresado == hash_bd`

---

## 3. Prueba de Recuperación de Contraseña

### Objetivo
Demostrar el flujo completo de recuperación con encriptación.

### Pasos

1. **En la ventana de login, clic en "¿Olvidaste tu contraseña?"**

2. **Ingresar un usuario existente:**
   - Escribe: `trabajador1`
   - Clic en "Aceptar"

3. **Ingresar nueva contraseña:**
   - Nueva contraseña: `nuevapass123`
   - Confirmar contraseña: `nuevapass123`
   - Clic en "Aceptar"

4. **Resultado esperado:**
   ```
   ¡Contraseña actualizada exitosamente!
   Ya puedes iniciar sesión con tu nueva contraseña.
   ```

5. **Verificar en la base de datos que cambió el hash:**
   ```sql
   SELECT username, password_hash FROM usuarios WHERE username = 'trabajador1';
   ```
   
   El hash será **diferente** al original.

6. **Probar el login con la nueva contraseña:**
   - Usuario: `trabajador1`
   - Contraseña: `nuevapass123`
   - Debe funcionar correctamente

7. **Código relevante (LoginFrame.java, línea ~280):**
   ```java
   // PASO 4: Encriptar la nueva contraseña con SHA-256
   String newPasswordHash = SecurityHelper.encryptPassword(newPassword);
   
   // PASO 5: Actualizar en la base de datos
   String updateSql = "UPDATE usuarios SET password_hash = ? WHERE username = ?";
   stmt.setString(1, newPasswordHash);
   ```

### ✅ Puntos a Destacar

- El sistema valida que el usuario existe ANTES de permitir cambiar contraseña
- La nueva contraseña se encripta con SHA-256 antes del UPDATE
- No hay forma de "recuperar" la contraseña antigua (es irreversible)

---

## 4. Prueba de Control de Roles - ADMINISTRADOR

### Objetivo
Demostrar que el administrador tiene acceso completo.

### Pasos

1. **Login como administrador:**
   - Usuario: `admin`
   - Contraseña: `admin123`

2. **Verificar en el Dashboard:**
   - En la parte superior debe decir: **"Rol: Administrador"**

3. **Verificar permisos en la pestaña "Inventario":**
   - ✅ Botón "➕ Agregar Producto" → **HABILITADO** (color verde)
   - ✅ Botón "✏️ Editar Producto" → **HABILITADO** (color azul)
   - ✅ Botón "🗑️ Eliminar Producto" → **HABILITADO** (color rojo)

4. **Probar agregar un producto:**
   - Clic en "➕ Agregar Producto"
   - Llenar el formulario:
     - Nombre: `Aspirina 500mg`
     - Descripción: `Analgésico`
     - Precio: `8.50`
     - Stock: `100`
     - Fecha Venc: `2026-12-31`
   - Clic en "Aceptar"
   - El producto debe aparecer en la tabla

5. **Verificar pestaña "Usuarios":**
   - La pestaña "👥 Usuarios" debe estar **VISIBLE**
   - Clic en "➕ Crear Usuario"
   - El diálogo debe abrirse sin restricciones

6. **Crear un nuevo usuario:**
   - Username: `prueba_admin`
   - Contraseña: `test123`
   - Confirmar: `test123`
   - Rol: `TRABAJADOR`
   - Clic en "Crear Usuario"
   - Mensaje: "Usuario 'prueba_admin' creado exitosamente"

### ✅ Puntos a Destacar

- El administrador puede hacer INSERT, UPDATE y DELETE en productos
- El administrador puede crear nuevos usuarios
- Todos los botones están habilitados

---

## 5. Prueba de Control de Roles - TRABAJADOR

### Objetivo
Demostrar que el trabajador tiene restricciones.

### Pasos

1. **Cerrar sesión**

2. **Login como trabajador:**
   - Usuario: `trabajador1`
   - Contraseña: `trabajador123` (o la que hayas establecido)

3. **Verificar en el Dashboard:**
   - En la parte superior debe decir: **"Rol: Trabajador"**

4. **Verificar restricciones en la pestaña "Inventario":**
   - ❌ Botón "➕ Agregar Producto" → **DESHABILITADO** (color gris)
   - ❌ Botón "✏️ Editar Producto" → **DESHABILITADO** (color gris)
   - ❌ Botón "🗑️ Eliminar Producto" → **DESHABILITADO** (color gris)

5. **Verificar tooltip al pasar el mouse:**
   - Pasar el mouse sobre "➕ Agregar Producto"
   - Debe aparecer: "Solo los administradores pueden agregar productos"

6. **Intentar hacer clic en "Agregar Producto":**
   - El botón no debe responder (está deshabilitado)

7. **Verificar que NO existe la pestaña "Usuarios":**
   - Las pestañas visibles deben ser solo:
     - 📦 Inventario
     - 💰 Ventas
     - ⚠️ Alertas
   - La pestaña "👥 Usuarios" **NO debe estar visible**

8. **Verificar acceso a ventas:**
   - Clic en pestaña "💰 Ventas"
   - Botón "💳 Nueva Venta" → **HABILITADO** ✅
   - Los trabajadores SÍ pueden vender

9. **Código relevante (Dashboard.java, línea ~500):**
   ```java
   private void applyRolePermissions() {
       if (currentUser.isTrabajador()) {
           // DESHABILITAR botones de modificación
           btnAddProduct.setEnabled(false);
           btnEditProduct.setEnabled(false);
           btnDeleteProduct.setEnabled(false);
           
           // Cambiar color a gris
           btnAddProduct.setBackground(Color.LIGHT_GRAY);
           
           // El trabajador SÍ puede vender
           btnNewSale.setEnabled(true);
       }
   }
   ```

### ✅ Puntos a Destacar

- El trabajador **NO puede modificar** productos (los botones están deshabilitados)
- El trabajador **NO puede crear** usuarios (la pestaña no existe)
- El trabajador **SÍ puede vender** (operación permitida)
- El trabajador **SÍ puede ver** el inventario (solo visualización)

---

## 6. Prueba de Validación de Permisos (Doble Seguridad)

### Objetivo
Demostrar que existe validación adicional en el código, no solo UI.

### Pasos

1. **Login como trabajador**

2. **Intentar forzar la acción de agregar producto:**
   - Aunque el botón está deshabilitado, supongamos que alguien intenta ejecutar la función directamente

3. **Código relevante (Dashboard.java, línea ~580):**
   ```java
   private void addProduct() {
       // VALIDACIÓN DE ROL (Doble seguridad)
       if (currentUser.isTrabajador()) {
           JOptionPane.showMessageDialog(this,
               "ACCESO DENEGADO\n\nSolo los ADMINISTRADORES pueden agregar productos.",
               "Permiso Denegado",
               JOptionPane.ERROR_MESSAGE);
           return;  // Detener ejecución
       }
       
       // ... resto del código de agregar producto
   }
   ```

### ✅ Puntos a Destacar

- Hay **doble capa de seguridad**:
  1. Los botones se deshabilitan (UI)
  2. Los métodos validan el rol (lógica)
- Aunque alguien modificara la UI, la lógica lo bloquearía

---

## 7. Checklist de Demostración Completa

### Para presentar al profesor:

- [ ] **Mostrar el código de `SecurityHelper.java`**
  - Línea 45-75: Método `encryptPassword()`
  - Explicar uso de `MessageDigest` y SHA-256

- [ ] **Ejecutar `SecurityHelper.main()`**
  - Mostrar los hashes generados

- [ ] **Mostrar la base de datos**
  ```sql
  SELECT username, password_hash FROM usuarios;
  ```

- [ ] **Mostrar el código de `LoginFrame.java`**
  - Línea 200: Encriptación antes de la consulta
  - Línea 275: Flujo de recuperación con encriptación

- [ ] **Demostrar login exitoso**
  - Con admin y con trabajador

- [ ] **Demostrar recuperación de contraseña**
  - Cambiar la contraseña de un usuario
  - Verificar que el hash cambia en la BD
  - Probar login con nueva contraseña

- [ ] **Mostrar el código de `Dashboard.java`**
  - Línea 500: Método `applyRolePermissions()`
  - Línea 580: Validación en `addProduct()`

- [ ] **Demostrar como Administrador**
  - Agregar un producto
  - Crear un usuario
  - Mostrar que todos los botones funcionan

- [ ] **Demostrar como Trabajador**
  - Mostrar botones deshabilitados
  - Intentar agregar producto (mostrar "Acceso Denegado")
  - Mostrar que puede vender

---

## 8. Preguntas Frecuentes del Profesor

### P: ¿Cómo sé que realmente usa SHA-256?

**R:** Muestra el código de `SecurityHelper.java` línea 48:
```java
MessageDigest digest = MessageDigest.getInstance("SHA-256");
```

Y ejecuta:
```bash
java -cp bin com.novafarma.util.SecurityHelper
```

### P: ¿Qué pasa si alguien hackea la UI y habilita los botones?

**R:** Hay validación de rol dentro de cada método. Muestra el código de `Dashboard.java` línea 580:
```java
if (currentUser.isTrabajador()) {
    JOptionPane.showMessageDialog(this, "ACCESO DENEGADO...");
    return;
}
```

### P: ¿Cómo funciona la recuperación de contraseña?

**R:** Muestra `LoginFrame.java` línea 275-320. El flujo es:
1. Valida que el usuario existe (SELECT)
2. Solicita nueva contraseña
3. La encripta con SHA-256
4. Hace UPDATE con el nuevo hash

### P: ¿Por qué usar SHA-256 y no otro método?

**R:** SHA-256 es:
- Estándar de la industria
- Unidireccional (no se puede revertir)
- Rápido de calcular
- Produce hashes únicos de 64 caracteres

### P: ¿Qué diferencia hay entre Admin y Trabajador?

**R:** Muestra esta tabla:

| Operación             | Admin | Trabajador |
|-----------------------|-------|------------|
| Ver inventario        | ✅    | ✅         |
| Agregar producto      | ✅    | ❌         |
| Editar producto       | ✅    | ❌         |
| Eliminar producto     | ✅    | ❌         |
| Crear usuario         | ✅    | ❌         |
| Registrar venta       | ✅    | ✅         |
| Ver alertas           | ✅    | ✅         |

---

## 🎯 Criterios de Evaluación Cumplidos

| Requisito | ✅ Cumplido | Evidencia |
|-----------|-------------|-----------|
| SHA-256 implementado | ✅ | `SecurityHelper.java` línea 45-75 |
| No contraseñas en texto plano | ✅ | BD: `password_hash` VARCHAR(64) |
| Login con hash | ✅ | `LoginFrame.java` línea 200 |
| Recuperación de contraseña | ✅ | `LoginFrame.java` línea 275-320 |
| Admin puede modificar | ✅ | `Dashboard.java` botones habilitados |
| Trabajador solo opera | ✅ | `Dashboard.java` línea 500 |
| Validación de rol en código | ✅ | `Dashboard.java` línea 580 |

---

**¡Éxito en tu presentación! 🚀**

