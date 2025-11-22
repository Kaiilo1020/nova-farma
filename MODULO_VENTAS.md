# 🛒 Módulo de Ventas (POS) - Nova Farma

## 📋 Descripción

Sistema completo de Punto de Venta (POS) implementado en la pestaña "Ventas" del Dashboard.

---

## 🎨 Interfaz (JSplitPane)

### **Panel Izquierdo: Catálogo de Productos**
- 🔍 **Buscador**: Filtra productos en tiempo real por nombre
- 📊 **Tabla**: Muestra ID, Nombre, Precio y Stock disponible
- ➕ **Botón Agregar**: Agrega producto seleccionado al carrito

### **Panel Derecho: Carrito de Compras**
- 🛒 **Tabla**: Muestra productos agregados con cantidad y subtotales
- 💰 **Total**: Calcula automáticamente el monto total
- 🗑️ **Limpiar Carrito**: Vacía el carrito completo
- 💳 **Finalizar Venta**: Procesa la transacción

---

## 🔄 Flujo de Ventas

### **1. Buscar Producto**
- Escribe en el buscador para filtrar productos
- O explora la lista completa del catálogo

### **2. Agregar al Carrito**
- Selecciona un producto de la tabla
- Clic en "Agregar al Carrito"
- Ingresa la cantidad deseada
- El sistema valida que no supere el stock disponible

### **3. Revisar Carrito**
- Verifica los productos agregados
- El total se calcula automáticamente
- Puedes limpiar el carrito si te equivocaste

### **4. Finalizar Venta**
- Clic en "Finalizar Venta"
- Confirma la transacción
- El sistema:
  1. Inserta cada producto en la tabla `ventas`
  2. El trigger de PostgreSQL actualiza el stock automáticamente
  3. Muestra mensaje de éxito
  4. Limpia el carrito
  5. Recarga el catálogo con stock actualizado

---

## 🔧 Implementación Técnica

### **Arquitectura**

```
Dashboard.java
├── createSalesPanel()          → Crea la interfaz JSplitPane
├── cargarCatalogo()            → Carga productos disponibles (stock > 0)
├── filtrarCatalogo()           → Filtra por nombre en tiempo real
├── agregarAlCarrito()          → Valida y agrega productos al carrito
├── actualizarTotal()           → Recalcula el total de la venta
├── limpiarCarrito()            → Vacía el carrito
└── finalizarVenta()            → Procesa la transacción

CarritoItem.java
└── Modelo de datos para items del carrito
```

---

## 🗄️ Base de Datos

### **Tabla: ventas**

```sql
CREATE TABLE ventas (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER NOT NULL REFERENCES productos(id),
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10, 2) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### **Trigger: Actualización Automática de Stock**

```sql
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
```

**IMPORTANTE:** Java NO hace UPDATE al stock. El trigger lo hace automáticamente.

---

## ✅ Validaciones Implementadas

### **Validaciones de UI:**
1. ✅ Verifica que se seleccione un producto
2. ✅ Valida que la cantidad sea mayor a 0
3. ✅ Valida que la cantidad no supere el stock disponible
4. ✅ Previene agregar más unidades de las disponibles
5. ✅ Valida que el carrito no esté vacío al finalizar

### **Validaciones de Base de Datos:**
1. ✅ Foreign keys en `producto_id` y `usuario_id`
2. ✅ CHECK constraint en cantidad (> 0)
3. ✅ Trigger valida stock negativo

---

## 🎯 Flujo Transaccional

```java
// Al hacer clic en "Finalizar Venta"
for (cada producto en el carrito) {
    INSERT INTO ventas 
    (producto_id, usuario_id, cantidad, precio_unitario, total) 
    VALUES (?, ?, ?, ?, ?);
    
    // El TRIGGER se ejecuta automáticamente:
    // UPDATE productos SET stock = stock - cantidad 
    // WHERE id = producto_id;
}
```

---

## 👥 Permisos por Rol

| Operación           | ADMINISTRADOR | TRABAJADOR |
|---------------------|---------------|------------|
| Ver catálogo        | ✅            | ✅         |
| Buscar productos    | ✅            | ✅         |
| Agregar al carrito  | ✅            | ✅         |
| Finalizar venta     | ✅            | ✅         |

**Ambos roles** tienen acceso completo al módulo de ventas.

---

## 🚀 Cómo Usar

### **Paso 1: Ejecutar la Aplicación**

```cmd
.\compile.bat
```

### **Paso 2: Login**

- Usuario: `admin` o `trabajador1`
- Contraseña: según corresponda

### **Paso 3: Ir a la Pestaña "Ventas"**

### **Paso 4: Realizar una Venta**

1. **Busca** "Paracetamol" en el buscador
2. **Selecciona** el producto de la tabla
3. **Clic** en "Agregar al Carrito"
4. **Ingresa** cantidad: `2`
5. **Verifica** que aparece en el carrito con el subtotal
6. **Repite** para más productos si deseas
7. **Clic** en "Finalizar Venta"
8. **Confirma** la transacción
9. **Observa** que el stock se actualiza automáticamente en el catálogo

---

## 🔍 Ejemplo de Uso

```
CATÁLOGO INICIAL:
ID | Nombre              | Precio  | Stock
1  | Paracetamol 500mg  | $5.50   | 100

AGREGAR AL CARRITO:
- Cantidad: 5 unidades

CARRITO:
ID | Producto           | Cant. | Precio U. | Subtotal
1  | Paracetamol 500mg  | 5     | $5.50     | $27.50

TOTAL: $27.50

FINALIZAR VENTA → ✅

CATÁLOGO ACTUALIZADO:
ID | Nombre              | Precio  | Stock
1  | Paracetamol 500mg  | $5.50   | 95  ← Stock actualizado automáticamente
```

---

## 📊 Consultas SQL Útiles

### **Ver ventas del día:**

```sql
SELECT 
    v.id,
    p.nombre AS producto,
    u.username AS vendedor,
    v.cantidad,
    v.total,
    v.fecha_venta
FROM ventas v
JOIN productos p ON v.producto_id = p.id
JOIN usuarios u ON v.usuario_id = u.id
WHERE DATE(v.fecha_venta) = CURRENT_DATE
ORDER BY v.fecha_venta DESC;
```

### **Reporte de ventas totales:**

```sql
SELECT 
    DATE(fecha_venta) AS fecha,
    COUNT(*) AS num_ventas,
    SUM(total) AS total_vendido
FROM ventas
GROUP BY DATE(fecha_venta)
ORDER BY fecha DESC;
```

### **Productos más vendidos:**

```sql
SELECT 
    p.nombre,
    SUM(v.cantidad) AS total_vendido,
    SUM(v.total) AS ingresos
FROM ventas v
JOIN productos p ON v.producto_id = p.id
GROUP BY p.nombre
ORDER BY total_vendido DESC
LIMIT 10;
```

---

## 🎓 Características Educativas

### **Conceptos Implementados:**

1. ✅ **JSplitPane**: Interfaz dividida profesional
2. ✅ **DefaultTableModel**: Manejo dinámico de tablas
3. ✅ **PreparedStatement**: Prevención de SQL Injection
4. ✅ **Triggers**: Lógica de negocio en la base de datos
5. ✅ **Validaciones**: UI y base de datos
6. ✅ **Transacciones**: Múltiples INSERT en una venta
7. ✅ **Modelo MVC**: Separación de capas

---

## 🐛 Solución de Problemas

### **Error: "Stock insuficiente"**

**Causa**: Intentas vender más unidades de las disponibles.

**Solución**: Reduce la cantidad o agrega más stock al producto desde el módulo de Inventario.

---

### **Error: "El carrito está vacío"**

**Causa**: Intentas finalizar venta sin productos.

**Solución**: Agrega al menos un producto al carrito.

---

### **Error: "Error al procesar la venta"**

**Causa**: Problema de conexión o el trigger no existe.

**Solución**: 
1. Verifica que el trigger esté creado: `\df` en psql
2. Verifica la conexión a PostgreSQL
3. Revisa los logs de la consola

---

## 💡 Mejoras Futuras

- [ ] Editar cantidades directamente en el carrito
- [ ] Eliminar productos individuales del carrito
- [ ] Aplicar descuentos y promociones
- [ ] Generar ticket de venta en PDF
- [ ] Historial de ventas del día
- [ ] Códigos de barras
- [ ] Métodos de pago múltiples

---

**¡Módulo de ventas listo para usar! 🎉**

