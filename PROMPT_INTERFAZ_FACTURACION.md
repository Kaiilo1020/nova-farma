# 🎨 Prompt para Crear Interfaz de Facturación - Estilo Nova Farma

## 📝 Contexto del Proyecto

Estoy desarrollando un sistema de gestión farmacéutica llamado **Nova Farma** en Java Swing con PostgreSQL. El proyecto tiene:

- **Tecnología**: Java Swing + PostgreSQL
- **Arquitectura**: Modelo-Vista-Controlador (MVC)
- **Conexión BD**: Clase `DatabaseConnection` con pool de conexiones
- **Sistema de Roles**: ADMINISTRADOR y TRABAJADOR
- **Módulos actuales**: Inventario, Ventas (POS), Usuarios, Alertas

## 🎯 Objetivo

Necesito crear un nuevo módulo de **Facturación** con una interfaz visual similar a la imagen de referencia que te proporcioné, con las siguientes características:

## 🖼️ Descripción de la Interfaz (Basada en la Imagen)

### **Layout Principal**

La interfaz debe tener un **diseño ordenado y profesional** con tres secciones principales:

#### **1. Panel Superior: Campos de Entrada (BorderLayout.NORTH)**

Debe contener campos de texto alineados horizontalmente para capturar:

- **RUC**: Campo de texto para número de identificación (8-11 dígitos)
- **Empresa**: Campo de texto para nombre de la empresa/cliente
- **Producto**: Campo de texto o combobox para seleccionar producto
- **Unitario**: Campo numérico para precio unitario
- **Precio**: Campo numérico calculado automáticamente (Cantidad × Unitario)

**Estilo visual**:
- Usar `GridBagLayout` o `GridLayout` para alinear los campos horizontalmente
- Labels en fuente Arial, negrita, tamaño 12
- TextFields con borde gris claro (`BorderFactory.createLineBorder(Color.LIGHT_GRAY)`)
- Fondo blanco o color neutro (`new Color(245, 245, 245)`)
- Espaciado uniforme entre campos (10px padding)

#### **2. Panel Central: Tabla de Facturas (BorderLayout.CENTER)**

Una **JTable** que muestre las facturas agregadas con las siguientes columnas:

- **Ruc**: VARCHAR (identificador del cliente)
- **Empresa**: VARCHAR (nombre de la empresa)
- **Producto**: VARCHAR (nombre del producto)
- **Unidades**: INTEGER (cantidad de unidades)
- **Precio Unitario**: DECIMAL (precio por unidad)
- **PrecioTotal**: DECIMAL (calculado: Unidades × Precio Unitario)

**Estilo visual de la tabla**:
- Header con fondo gris medio: `new Color(200, 200, 200)`
- Fuente del header: Arial Bold, tamaño 12
- Filas con altura de 25px
- Borde gris alrededor de la tabla
- Alternar colores de filas (opcional): blanco y gris muy claro
- No editable directamente (usar botones para modificar)

#### **3. Panel Derecho: Botones de Acción (BorderLayout.EAST)**

Un panel vertical con los siguientes botones:

1. **🗂️ Lista**: Ver todas las facturas guardadas
2. **➕ Adicionar**: Agregar una nueva factura a la tabla
3. **🗑️ Eliminar Factura**: Eliminar la factura seleccionada
4. **🧹 Eliminar todo**: Limpiar toda la tabla

**Estilo visual de los botones**:
- Tamaño uniforme: 140px × 40px
- Espaciado vertical entre botones: 10-15px
- Colores específicos:
  - **Lista**: Azul claro `new Color(52, 152, 219)` con texto blanco
  - **Adicionar**: Verde `new Color(46, 204, 113)` con texto blanco
  - **Eliminar Factura**: Naranja/Amarillo `new Color(243, 156, 18)` con texto blanco
  - **Eliminar todo**: Rojo `new Color(231, 76, 60)` con texto blanco
- Fuente: Arial Bold, tamaño 12
- Sin borde pintado (`setFocusPainted(false)`, `setBorderPainted(false)`)
- Cursor de mano al pasar por encima (`Cursor.HAND_CURSOR`)

## 🔧 Requisitos Técnicos

### **Estructura de Clases**

Crear un nuevo archivo: `src/com/novafarma/ui/FacturacionPanel.java`

```java
package com.novafarma.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import com.novafarma.util.DatabaseConnection;

public class FacturacionPanel extends JPanel {
    
    // Componentes UI
    private JTextField txtRuc;
    private JTextField txtEmpresa;
    private JTextField txtProducto;
    private JTextField txtUnitario;
    private JTextField txtPrecio;
    private JTable tableFacturas;
    private DefaultTableModel modelFacturas;
    
    // Botones
    private JButton btnLista;
    private JButton btnAdicionar;
    private JButton btnEliminarFactura;
    private JButton btnEliminarTodo;
    
    public FacturacionPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel superior: Campos de entrada
        add(createInputPanel(), BorderLayout.NORTH);
        
        // Panel central: Tabla
        add(createTablePanel(), BorderLayout.CENTER);
        
        // Panel derecho: Botones
        add(createButtonPanel(), BorderLayout.EAST);
    }
    
    // ... Implementar los métodos createInputPanel(), createTablePanel(), createButtonPanel()
}
```

### **Base de Datos**

Crear una nueva tabla en PostgreSQL:

```sql
CREATE TABLE IF NOT EXISTS facturas (
    id SERIAL PRIMARY KEY,
    ruc VARCHAR(20) NOT NULL,
    empresa VARCHAR(100) NOT NULL,
    producto VARCHAR(100) NOT NULL,
    unidades INTEGER NOT NULL CHECK (unidades > 0),
    precio_unitario DECIMAL(10, 2) NOT NULL,
    precio_total DECIMAL(10, 2) NOT NULL,
    fecha_factura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_id INTEGER REFERENCES usuarios(id)
);

CREATE INDEX idx_facturas_ruc ON facturas(ruc);
CREATE INDEX idx_facturas_fecha ON facturas(fecha_factura);
```

### **Funcionalidades Requeridas**

1. **Adicionar Factura**:
   - Validar que todos los campos estén llenos
   - Validar que RUC tenga entre 8-11 dígitos
   - Validar que Unitario y Precio sean números válidos
   - Calcular automáticamente el Precio Total
   - Insertar en la base de datos: `INSERT INTO facturas (...) VALUES (...)`
   - Agregar a la tabla visual
   - Limpiar campos después de agregar

2. **Lista**:
   - Cargar todas las facturas desde la base de datos
   - Query: `SELECT * FROM facturas ORDER BY fecha_factura DESC`
   - Mostrar en la tabla con formato de precios: `$%.2f`

3. **Eliminar Factura**:
   - Verificar que haya una fila seleccionada
   - Confirmar con `JOptionPane.showConfirmDialog`
   - Eliminar de la base de datos: `DELETE FROM facturas WHERE id = ?`
   - Actualizar la tabla visual

4. **Eliminar Todo**:
   - Confirmar acción con diálogo
   - Limpiar la tabla visual (no borrar de BD, solo la vista)
   - O si se prefiere: `DELETE FROM facturas` (con confirmación extra)

### **Validaciones**

- ✅ RUC: Solo números, longitud 8-11
- ✅ Empresa: No vacío, máximo 100 caracteres
- ✅ Producto: No vacío
- ✅ Unitario: Número decimal positivo
- ✅ Precio Total: Calculado automáticamente (Unidades × Unitario)

### **Paleta de Colores del Proyecto Nova Farma**

Para mantener consistencia con el resto del sistema:

- **Azul Oscuro (Header)**: `new Color(52, 73, 94)`
- **Azul Claro (Acción primaria)**: `new Color(52, 152, 219)`
- **Verde (Éxito/Agregar)**: `new Color(46, 204, 113)`
- **Rojo (Eliminar)**: `new Color(231, 76, 60)`
- **Naranja (Advertencia)**: `new Color(243, 156, 18)`
- **Gris Claro (Fondo)**: `new Color(236, 240, 241)`
- **Gris Medio (Bordes)**: `new Color(149, 165, 166)`

### **Integración con el Dashboard**

Agregar la nueva pestaña en `Dashboard.java`:

```java
// En el método initializeUI(), agregar:
JPanel facturacionPanel = new FacturacionPanel();
tabbedPane.addTab("📄 Facturación", facturacionPanel);
```

## 🎨 Detalles Visuales Específicos (Según la Imagen)

### **Campos de Entrada**

- Disposición: Horizontal en una sola fila
- Ancho de campos: 
  - RUC: 120px
  - Empresa: 200px
  - Producto: 150px
  - Unitario: 100px
  - Precio: 100px
- Altura uniforme: 30px
- Padding interno: 5px
- Borde: Línea gris de 1px

### **Tabla**

- Ancho de columnas (proporcional):
  - Ruc: 10%
  - Empresa: 25%
  - Producto: 25%
  - Unidades: 10%
  - Precio Unitario: 15%
  - PrecioTotal: 15%
- Sin líneas verticales entre columnas (opcional)
- Selección de fila completa al hacer clic

### **Panel de Botones**

- Ancho fijo: 160px
- Alineación vertical: espacio uniforme entre botones
- Margen superior del primer botón: 20px
- Los botones deben tener efecto hover (cambio de cursor)

## 📚 Ejemplo de Uso

**Flujo típico**:
1. Usuario ingresa RUC: `20123456789`
2. Usuario ingresa Empresa: `Farmacia Central`
3. Usuario selecciona/escribe Producto: `Paracetamol 500mg`
4. Usuario ingresa Unidades: `10`
5. Usuario ingresa Precio Unitario: `5.50`
6. Sistema calcula Precio Total automáticamente: `55.00`
7. Usuario hace clic en **Adicionar**
8. Factura se agrega a la tabla y se guarda en BD
9. Campos se limpian para nueva entrada

## 🔍 Consideraciones Adicionales

- **Responsividad**: La tabla debe ajustarse al tamaño de la ventana
- **Mensajes de Error**: Usar `JOptionPane.showMessageDialog` con iconos apropiados
- **Mensajes de Éxito**: Confirmación visual al agregar/eliminar
- **Formato de Números**: Usar `DecimalFormat` para mostrar precios: `$0.00`
- **Fecha**: Opcional, mostrar fecha de facturación en formato `dd/MM/yyyy HH:mm`

## ✅ Checklist de Implementación

- [ ] Crear `FacturacionPanel.java`
- [ ] Diseñar panel superior con campos de entrada
- [ ] Crear tabla con modelo no editable
- [ ] Implementar panel de botones con estilos
- [ ] Crear tabla `facturas` en PostgreSQL
- [ ] Implementar método `adicionarFactura()`
- [ ] Implementar método `cargarFacturas()`
- [ ] Implementar método `eliminarFactura()`
- [ ] Implementar método `eliminarTodo()`
- [ ] Agregar validaciones de entrada
- [ ] Integrar con `Dashboard.java`
- [ ] Probar con datos de ejemplo
- [ ] Verificar permisos por rol (ADMIN y TRABAJADOR)

---

## 🚀 Resultado Esperado

Una interfaz limpia, profesional y funcional que replica el estilo de la imagen proporcionada, integrada perfectamente con el sistema Nova Farma existente, con todas las validaciones necesarias y persistencia en PostgreSQL.

**Características clave**:
- ✅ Diseño limpio y ordenado
- ✅ Colores consistentes con el proyecto
- ✅ Funcionalidad completa CRUD
- ✅ Validaciones robustas
- ✅ Integración con base de datos
- ✅ Control de permisos por rol

---

**Nota**: Este prompt está diseñado para ser utilizado con una IA de codificación como Cursor, GitHub Copilot, o ChatGPT. Contiene toda la información necesaria para implementar el módulo completo manteniendo la consistencia con el proyecto existente.

