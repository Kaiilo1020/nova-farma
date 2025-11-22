# 📄 Módulo de Facturación - Nova Farma

## 🎨 Descripción

Sistema completo de facturación para empresas y clientes implementado con interfaz profesional siguiendo el diseño especificado.

---

## ✅ Implementación Completada

### **Archivos Creados:**

1. ✅ `src/com/novafarma/ui/FacturacionPanel.java` - Panel principal
2. ✅ `database/agregar_tabla_facturas.sql` - Script SQL
3. ✅ Tabla `facturas` agregada a `database/schema.sql`
4. ✅ Integración en `Dashboard.java` - Nueva pestaña "📄 Facturación"

---

## 🖼️ Interfaz

### **Layout Principal (BorderLayout)**

```
┌────────────────────────────────────────────────────────────────┐
│  NORTE: Panel de Campos de Entrada (GridBagLayout)            │
│  [RUC] [Empresa] [Producto] [Unidades] [P.Unitario] [Total]  │
└────────────────────────────────────────────────────────────────┘
┌────────────────────────────────────┬───────────────────────────┐
│  CENTRO: Tabla de Facturas         │  ESTE: Botones de Acción │
│  ┌──────────────────────────────┐  │  ┌─────────────────────┐ │
│  │ ID │ RUC │ Empresa │ ...    │  │  │  🗂️ Lista          │ │
│  ├────┼─────┼─────────┼────────┤  │  ├─────────────────────┤ │
│  │ 1  │ ... │ ...     │ ...    │  │  │  ➕ Adicionar       │ │
│  │ 2  │ ... │ ...     │ ...    │  │  ├─────────────────────┤ │
│  └────────────────────────────────┘  │  │  🗑️ Eliminar        │ │
│                                      │  ├─────────────────────┤ │
│                                      │  │  🧹 Limpiar Todo    │ │
│                                      │  └─────────────────────┘ │
└────────────────────────────────────┴───────────────────────────┘
```

---

## 📋 Características Implementadas

### **1. Panel de Campos de Entrada**

#### **Campos:**
- **RUC**: Campo de texto (8-11 dígitos numéricos)
- **Empresa**: Campo de texto (máx. 100 caracteres)
- **Producto**: Campo de texto
- **Unidades**: Campo numérico (por defecto: 1)
- **Precio Unitario**: Campo numérico decimal
- **Precio Total**: Campo calculado automáticamente (NO editable)

#### **Estilos Visuales:**
- Fondo blanco con borde gris claro
- Labels en Arial Bold, tamaño 12
- TextFields con padding interno de 5px
- Precio Total con borde verde y fuente en negrita
- Cálculo automático en tiempo real

---

### **2. Tabla de Facturas**

#### **Columnas:**
1. **ID**: Identificador único (auto-generado)
2. **RUC**: Número de identificación del cliente
3. **Empresa**: Nombre de la empresa
4. **Producto**: Nombre del producto facturado
5. **Unidades**: Cantidad de unidades
6. **P. Unitario**: Precio por unidad (formato: $0.00)
7. **P. Total**: Precio total (formato: $0.00)
8. **Fecha**: Fecha y hora de la factura (dd/MM/yyyy HH:mm)

#### **Estilos Visuales:**
- Header gris medio con fuente Arial Bold
- Altura de filas: 25px
- No editable directamente
- Selección de una sola fila
- Borde con título "📋 Facturas Registradas"

---

### **3. Botones de Acción**

| Botón | Color | Función |
|-------|-------|---------|
| 🗂️ **Lista** | Azul (`#3498DB`) | Carga todas las facturas de la BD |
| ➕ **Adicionar** | Verde (`#2ECC71`) | Agrega nueva factura |
| 🗑️ **Eliminar** | Naranja (`#F39C12`) | Elimina factura seleccionada |
| 🧹 **Limpiar Todo** | Rojo (`#E74C3C`) | Limpia la tabla visual |

**Características:**
- Tamaño uniforme: 140px × 40px
- Cursor de mano al pasar por encima
- Sin borde pintado
- Espaciado vertical de 15px

---

## 🔧 Funcionalidades

### **1️⃣ Adicionar Factura**

**Flujo:**
1. Usuario llena los campos
2. Sistema valida:
   - ✅ Campos no vacíos
   - ✅ RUC de 8-11 dígitos
   - ✅ Empresa ≤ 100 caracteres
   - ✅ Unidades > 0
   - ✅ Precio Unitario > 0
3. Calcula Precio Total automáticamente
4. Inserta en la base de datos
5. Muestra mensaje de confirmación
6. Limpia los campos
7. Recarga la tabla

**Código SQL:**
```sql
INSERT INTO facturas 
(ruc, empresa, producto, unidades, precio_unitario, precio_total, usuario_id) 
VALUES (?, ?, ?, ?, ?, ?, ?);
```

---

### **2️⃣ Cargar Lista**

**Flujo:**
1. Consulta todas las facturas ordenadas por fecha descendente
2. Formatea precios con símbolo $ y 2 decimales
3. Formatea fechas a dd/MM/yyyy HH:mm
4. Muestra en la tabla

**Código SQL:**
```sql
SELECT id, ruc, empresa, producto, unidades, precio_unitario, precio_total, fecha_factura 
FROM facturas 
ORDER BY fecha_factura DESC;
```

---

### **3️⃣ Eliminar Factura**

**Flujo:**
1. Verifica que haya una fila seleccionada
2. Muestra confirmación con datos de la factura
3. Elimina de la base de datos
4. Recarga la tabla

**Código SQL:**
```sql
DELETE FROM facturas WHERE id = ?;
```

---

### **4️⃣ Limpiar Todo**

**Flujo:**
1. Limpia la tabla visual (NO elimina de BD)
2. Usuario puede recargar con botón "Lista"

**Nota:** Si se desea eliminar también de la BD, descomentar el código en el método `eliminarTodo()`.

---

## 🗄️ Base de Datos

### **Tabla: facturas**

```sql
CREATE TABLE facturas (
    id SERIAL PRIMARY KEY,
    ruc VARCHAR(20) NOT NULL,
    empresa VARCHAR(100) NOT NULL,
    producto VARCHAR(100) NOT NULL,
    unidades INTEGER NOT NULL CHECK (unidades > 0),
    precio_unitario DECIMAL(10, 2) NOT NULL CHECK (precio_unitario > 0),
    precio_total DECIMAL(10, 2) NOT NULL,
    fecha_factura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_id INTEGER REFERENCES usuarios(id)
);
```

### **Índices:**
- `idx_facturas_ruc` - Para búsquedas por RUC
- `idx_facturas_fecha` - Para consultas por fecha
- `idx_facturas_empresa` - Para búsquedas por empresa

---

## 🚀 Instalación y Uso

### **Paso 1: Agregar la Tabla a la BD**

Si ya tienes la base de datos creada:

```bash
# En pgAdmin Query Tool o psql:
\c nova_farma_db
\i database/agregar_tabla_facturas.sql
```

O si estás creando desde cero, el script `schema.sql` ya incluye la tabla.

---

### **Paso 2: Compilar y Ejecutar**

```cmd
.\compile.bat
```

---

### **Paso 3: Acceder al Módulo**

1. **Login** con admin o trabajador1
2. **Ir a la pestaña** "📄 Facturación"
3. ¡Listo para usar!

---

## 📖 Ejemplo de Uso

### **Caso: Facturar a una Farmacia**

**1. Llenar Campos:**
```
RUC:             20123456789
Empresa:         Farmacia Central
Producto:        Paracetamol 500mg
Unidades:        100
Precio Unitario: 5.50
Precio Total:    $550.00  (calculado automáticamente)
```

**2. Clic en "➕ Adicionar"**

**3. Resultado:**
```
✅ Factura agregada exitosamente

RUC: 20123456789
Empresa: Farmacia Central
Total: $550.00
```

**4. La tabla se actualiza:**
```
ID │ RUC          │ Empresa          │ Producto          │ Unid. │ P.Unit. │ P.Total │ Fecha
1  │ 20123456789  │ Farmacia Central │ Paracetamol 500mg │ 100   │ $5.50   │ $550.00 │ 21/11/2024 15:30
```

---

## ✅ Validaciones Implementadas

| Campo | Validación |
|-------|------------|
| RUC | • No vacío<br>• Solo números<br>• Longitud 8-11 dígitos |
| Empresa | • No vacío<br>• Máximo 100 caracteres |
| Producto | • No vacío |
| Unidades | • Número entero<br>• Mayor a 0 |
| Precio Unitario | • Número decimal<br>• Mayor a 0 |
| Precio Total | • Calculado automáticamente<br>• No editable |

---

## 🎨 Paleta de Colores

```java
// Botones
new Color(52, 152, 219)   // Azul - Lista
new Color(46, 204, 113)   // Verde - Adicionar
new Color(243, 156, 18)   // Naranja - Eliminar
new Color(231, 76, 60)    // Rojo - Limpiar Todo

// Interfaz
new Color(245, 245, 245)  // Fondo general
new Color(200, 200, 200)  // Header tabla
new Color(236, 240, 241)  // Fondo deshabilitado
```

---

## 👥 Permisos por Rol

| Operación | ADMINISTRADOR | TRABAJADOR |
|-----------|---------------|------------|
| Ver facturas | ✅ | ✅ |
| Adicionar factura | ✅ | ✅ |
| Eliminar factura | ✅ | ✅ |

**Ambos roles** tienen acceso completo al módulo de facturación.

---

## 📊 Consultas SQL Útiles

### **Facturas del día:**
```sql
SELECT * FROM facturas 
WHERE DATE(fecha_factura) = CURRENT_DATE 
ORDER BY fecha_factura DESC;
```

### **Total facturado por empresa:**
```sql
SELECT empresa, COUNT(*) AS num_facturas, SUM(precio_total) AS total 
FROM facturas 
GROUP BY empresa 
ORDER BY total DESC;
```

### **Facturas de un RUC específico:**
```sql
SELECT * FROM facturas 
WHERE ruc = '20123456789' 
ORDER BY fecha_factura DESC;
```

### **Reporte mensual:**
```sql
SELECT 
    TO_CHAR(fecha_factura, 'YYYY-MM') AS mes,
    COUNT(*) AS num_facturas,
    SUM(precio_total) AS total_facturado
FROM facturas
GROUP BY mes
ORDER BY mes DESC;
```

---

## 🔍 Solución de Problemas

### **Error: "Tabla facturas no existe"**

**Solución:**
```bash
# Ejecutar en pgAdmin:
\i database/agregar_tabla_facturas.sql
```

---

### **Error: "RUC inválido"**

**Causa:** RUC debe tener entre 8 y 11 dígitos numéricos.

**Solución:** Verifica que solo contenga números y la longitud correcta.

---

### **Error: "Precio Total no se calcula"**

**Causa:** Valores no numéricos en Unidades o Precio Unitario.

**Solución:** Ingresa solo números válidos (usa punto `.` para decimales).

---

## 💡 Mejoras Futuras

- [ ] Exportar facturas a PDF
- [ ] Búsqueda avanzada por RUC o Empresa
- [ ] Gráficos de facturación mensual
- [ ] Editar facturas existentes
- [ ] Filtros por rango de fechas
- [ ] Impresión directa de facturas
- [ ] Generación de reportes Excel
- [ ] Calculadora de IGV/IVA
- [ ] Historial de modificaciones

---

## 📝 Checklist de Implementación

- [x] Crear `FacturacionPanel.java`
- [x] Diseñar panel superior con campos de entrada
- [x] Crear tabla con modelo no editable
- [x] Implementar panel de botones con estilos
- [x] Crear tabla `facturas` en PostgreSQL
- [x] Implementar método `adicionarFactura()`
- [x] Implementar método `cargarFacturas()`
- [x] Implementar método `eliminarFactura()`
- [x] Implementar método `eliminarTodo()`
- [x] Agregar validaciones de entrada
- [x] Integrar con `Dashboard.java`
- [x] Probar con datos de ejemplo
- [x] Verificar permisos por rol

---

## 🎉 **¡Módulo Completamente Implementado!**

El módulo de facturación está **100% funcional** y listo para usar, con:

✅ Interfaz profesional y limpia  
✅ Validaciones robustas  
✅ Cálculo automático de totales  
✅ Persistencia en PostgreSQL  
✅ Integración perfecta con Nova Farma  
✅ Control de permisos por rol  

---

**¡Listo para facturar! 📄💰**

