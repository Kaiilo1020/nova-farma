package com.novafarma.ui;

import com.novafarma.model.Product;
import com.novafarma.model.Sale;
import com.novafarma.model.User;
import com.novafarma.service.ProductService;
import com.novafarma.service.SaleService;
import com.novafarma.service.UserService;
import com.novafarma.ui.panels.InventoryPanel;
import com.novafarma.ui.panels.AlertsPanel;
import com.novafarma.ui.panels.SalesPanel;
import com.novafarma.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard principal de la aplicación Nova Farma
 * 
 * CONTROL DE ROLES IMPLEMENTADO:
 * 
 * ROL ADMINISTRADOR:
 * ✓ Puede gestionar productos (INSERT, UPDATE, DELETE)
 * ✓ Puede crear nuevos usuarios
 * ✓ Puede realizar ventas
 * ✓ Puede ver inventario y alertas
 * 
 * ROL TRABAJADOR:
 * ✓ Puede realizar ventas (INSERT en tabla ventas)
 * ✓ Puede ver inventario (SELECT)
 * ✓ Puede ver alertas de vencimiento
 * ✗ NO puede modificar productos
 * ✗ NO puede crear usuarios
 * ✗ NO puede eliminar registros
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class Dashboard extends JFrame {
    
    // ==================== ATRIBUTOS ====================
    
    private User currentUser;
    
    // Servicios de lógica de negocio (Arquitectura en capas)
    private ProductService productService;
    private SaleService saleService;
    private UserService userService;
    
    // Paneles modulares (FASE B: UI dividida en componentes)
    private InventoryPanel inventoryPanel;
    private AlertsPanel alertsPanel;
    private SalesPanel salesPanel;
    
    // Componentes UI
    private JLabel lblWelcome;
    private JLabel lblRole;
    private JTabbedPane tabbedPane;
    
    // ==================== CONSTRUCTOR ====================
    
    public Dashboard(User user) {
        this.currentUser = user;
        
        // Inicializar servicios (Arquitectura en capas)
        this.productService = new ProductService();
        this.saleService = new SaleService();
        this.userService = new UserService();
        
        // Inicializar paneles modulares (FASE B: Componentes separados)
        initializePanels();
        
        initializeUI();
        applyRolePermissions(); // CRÍTICO: Aplica restricciones según el rol
        
        // Cargar datos iniciales
        inventoryPanel.loadProductsData();
        salesPanel.cargarCatalogo();
        alertsPanel.cargarAlertas();
    }
    
    /**
     * Inicializa los paneles modulares y configura callbacks
     * (FASE B: UI dividida en componentes)
     */
    private void initializePanels() {
        // Crear InventoryPanel
        inventoryPanel = new InventoryPanel(currentUser, productService);
        inventoryPanel.setOnAddProduct(() -> addProduct());
        inventoryPanel.setOnEditProduct(() -> editProduct());
        inventoryPanel.setOnDeleteProduct(() -> deleteProduct());
        inventoryPanel.setOnRefresh(() -> {
            // Callback vacío, el panel ya recarga sus propios datos
        });
        
        // Crear AlertsPanel
        alertsPanel = new AlertsPanel(currentUser, productService);
        alertsPanel.setOnEliminarVencidos(() -> eliminarProductosVencidos());
        
        // Crear SalesPanel
        salesPanel = new SalesPanel(currentUser, productService, saleService);
        salesPanel.setOnVentaFinalizada(() -> {
            // Recargar inventario cuando se finaliza una venta
            inventoryPanel.loadProductsData();
        });
    }
    
    // ==================== INICIALIZACIÓN DE UI ====================
    
    private void initializeUI() {
        // Configuración de la ventana
        setTitle("Nova Farma - Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel superior (Header)
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Panel central (Tabs)
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Tab 1: Inventario (FASE B: Panel modular)
        tabbedPane.addTab("📦 Inventario", inventoryPanel);
        
        // Tab 2: Ventas / Facturación (FASE B: Panel modular unificado)
        // NOTA: Facturación fusionada con Ventas - Single Source of Truth
        tabbedPane.addTab("💰 Ventas / Facturación", salesPanel);
        
        // Tab 3: Usuarios (solo visible para ADMIN)
        if (currentUser.isAdministrador()) {
            JPanel usersPanel = createUsersPanel();
            tabbedPane.addTab("👥 Usuarios", usersPanel);
        }
        
        // Tab 5: Alertas (FASE B: Panel modular)
        tabbedPane.addTab("⚠️ Alertas", alertsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Panel inferior (Footer)
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    // ==================== PANEL DE ENCABEZADO ====================
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(1000, 80));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Lado izquierdo: Bienvenida
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        
        lblWelcome = new JLabel("Bienvenido, " + currentUser.getUsername());
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 18));
        
        lblRole = new JLabel("Rol: " + currentUser.getRol().getDisplayName());
        lblRole.setFont(new Font("Arial", Font.PLAIN, 13));
        
        leftPanel.add(lblWelcome);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(lblRole);
        
        // Lado derecho: Botón de cerrar sesión
        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setFont(new Font("Arial", Font.PLAIN, 12));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(btnLogout, BorderLayout.EAST);
        
        return panel;
    }
    
    // ==================== PANEL DE INVENTARIO ====================
    // NOTA: createInventoryPanel() y filtrarInventario() eliminados - ahora se usa InventoryPanel.java
    
    // ==================== PANEL DE VENTAS ====================
    
    // Variables para el módulo de ventas
    // Variables de ventas eliminadas - ahora están en SalesPanel
    
    /**
     * Crea el panel de ventas (POS) con JSplitPane
     * 
     * ESTRUCTURA:
     * - Izquierda: Catálogo de productos (buscador + tabla + botón agregar)
     * - Derecha: Carrito de compras (tabla + total + botón finalizar)
     * 
     * FLUJO DE VENTA:
     * 1. Usuario busca y selecciona productos del catálogo
     * 2. Agrega productos al carrito (con validación de stock)
     * 3. Finaliza venta (INSERT a tabla ventas)
     * 4. Trigger de PostgreSQL actualiza el stock automáticamente
     */
    // createSalesPanel() eliminado - ahora se usa SalesPanel.java
    
    // ==================== PANEL DE USUARIOS ====================
    
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel de botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        JButton btnCreateUser = new JButton("➕ Crear Usuario");
        styleButton(btnCreateUser);
        btnCreateUser.addActionListener(e -> createUser());
        
        JButton btnDeleteUser = new JButton("🗑️ Eliminar Usuario");
        styleButton(btnDeleteUser);
        btnDeleteUser.addActionListener(e -> deleteUser());
        
        JButton btnRefreshUsers = new JButton("🔄 Actualizar");
        styleButton(btnRefreshUsers);
        btnRefreshUsers.addActionListener(e -> loadUsersData());
        
        btnPanel.add(btnCreateUser);
        btnPanel.add(btnDeleteUser);
        btnPanel.add(btnRefreshUsers);
        
        panel.add(btnPanel, BorderLayout.NORTH);
        
        // Tabla de usuarios
        String[] columnNames = {"ID", "Usuario", "Rol", "Ventas Registradas"};
        usersTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
            }
        };
        
        usersTable = new JTable(usersTableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.setRowHeight(25);
        usersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        usersTable.setFont(new Font("Arial", Font.PLAIN, 12));
        usersTable.setFillsViewportHeight(true);
        
        // Ajustar ancho de columnas
        usersTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        usersTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        usersTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        usersTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Usuarios"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Información en la parte inferior
        JLabel lblInfo = new JLabel(
            "<html><body style='padding: 10px;'>" +
            "<b>Nota:</b> Solo se pueden eliminar usuarios que NO tengan ventas registradas.<br>" +
            "Si un trabajador tiene ventas, se conservan para el historial del negocio." +
            "</body></html>"
        );
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panel.add(lblInfo, BorderLayout.SOUTH);
        
        // Cargar datos iniciales
        loadUsersData();
        
        return panel;
    }
    
    /**
     * Carga los usuarios en la tabla
     */
    private void loadUsersData() {
        try {
            // Limpiar tabla
            usersTableModel.setRowCount(0);
            
            // Obtener todos los usuarios
            List<User> users = userService.getAllUsers();
            
            // Llenar tabla
            for (User user : users) {
                // Contar ventas del usuario
                int salesCount = userService.getSalesCount(user.getId());
                
                Object[] row = {
                    user.getId(),
                    user.getUsername(),
                    user.getRol().getDisplayName(),
                    salesCount + " venta(s)"
                };
                usersTableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar usuarios:\n" + e.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // ==================== PANEL DE ALERTAS ====================
    // NOTA: createAlertsPanel() y cargarAlertas() eliminados - ahora se usa AlertsPanel.java
    
    /**
     * Desactiva productos vencidos (NO los elimina de la BD)
     * 
     * OPCIONES:
     * 1. Desactivar producto seleccionado (stock = 0, activo = FALSE)
     * 2. Desactivar TODOS los productos vencidos
     * 
     * Cuando llega nuevo lote, el admin edita el producto y lo reactiva.
     * 
     * RESTRICCIÓN: Solo ADMINISTRADOR puede ejecutar esto
     */
    private void eliminarProductosVencidos() {
        // Validación de rol (doble verificación)
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(this,
                "ACCESO DENEGADO\n\nSolo los ADMINISTRADORES pueden eliminar productos.",
                "Permiso Denegado",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar si hay un producto seleccionado (ARQUITECTURA: Usar AlertsPanel)
        Integer productId = alertsPanel.getSelectedProductId();
        
        if (productId != null) {
            // OPCIÓN 1: Eliminar solo el producto seleccionado
            eliminarProductoSeleccionado(productId);
        } else {
            // OPCIÓN 2: Eliminar TODOS los productos vencidos
            eliminarTodosLosVencidos();
        }
    }
    
    /**
     * Desactiva un producto seleccionado (NO lo elimina de la BD)
     * 
     * Pone stock = 0 y activo = FALSE
     * El producto se conserva para historial de ventas
     */
    private void eliminarProductoSeleccionado(int productoId) {
        try {
            // Obtener datos del producto desde ProductService (ARQUITECTURA: Capa de Servicios)
            Product product = productService.getProductById(productoId);
            if (product == null) {
                JOptionPane.showMessageDialog(this,
                    "Producto no encontrado",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String nombreProducto = product.getNombre();
            long diasRestantes = product.getDaysUntilExpiration();
            String diasStr = diasRestantes < 0 ? 
                Math.abs(diasRestantes) + " días atrás" : 
                diasRestantes + " días";
            
            // Confirmar desactivación
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Retirar este producto del inventario?\n\n" +
                "Producto: " + nombreProducto + "\n" +
                "Estado: " + diasRestantes + "\n\n" +
                "El producto se marcará como INACTIVO y con stock 0.\n" +
                "NO se eliminará de la base de datos (se conserva el historial).\n\n" +
                "Cuando llegue un nuevo lote, podrás editarlo y reactivarlo.",
                "Confirmar Desactivación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Usar ProductService para desactivar (soft delete) el producto
                boolean success = productService.retireProduct(productoId);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "✅ Producto desactivado exitosamente\n\n" +
                        "Producto: " + nombreProducto + "\n" +
                        "Stock: 0\n" +
                        "Activo: NO\n\n" +
                        "Cuando llegue un nuevo lote:\n" +
                        "1. Ve a Inventario\n" +
                        "2. Busca el producto\n" +
                        "3. Edítalo con el nuevo stock y fecha\n" +
                        "4. Se reactivará automáticamente",
                        "Producto Desactivado",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Actualizar tablas (ARQUITECTURA: Usar paneles modulares)
                    alertsPanel.cargarAlertas();
                    inventoryPanel.loadProductsData();
                    salesPanel.cargarCatalogo();
                }
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al desactivar producto: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Desactiva TODOS los productos vencidos (NO los elimina)
     */
    /**
     * Desactiva TODOS los productos vencidos (NO los elimina)
     * 
     * ARQUITECTURA: Usa ProductService para obtener y retirar productos vencidos
     */
    private void eliminarTodosLosVencidos() {
        try {
            // Usar ProductService para obtener productos vencidos
            List<Product> productsExpired = productService.getExpiredProducts();
            int totalVencidos = productsExpired.size();
            
            if (totalVencidos == 0) {
                JOptionPane.showMessageDialog(this,
                    "✅ No hay productos vencidos activos para retirar.",
                    "Sin Productos Vencidos",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Confirmar desactivación masiva
            int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ RETIRAR PRODUCTOS VENCIDOS ⚠️\n\n" +
                "Se encontraron " + totalVencidos + " productos vencidos.\n\n" +
                "Se marcarán como INACTIVOS (stock = 0, activo = FALSE)\n" +
                "NO se eliminarán de la base de datos.\n\n" +
                "¿Continuar?",
                "Confirmar Desactivación Masiva",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Usar ProductService para desactivar todos los vencidos
                int rowsUpdated = productService.retireAllExpiredProducts();
                
                JOptionPane.showMessageDialog(this,
                    "✅ Operación completada\n\n" +
                    "Productos desactivados: " + rowsUpdated + "\n\n" +
                    "Estos productos se conservan en la base de datos\n" +
                    "y pueden reactivarse cuando llegue un nuevo lote.\n\n" +
                    "Para reactivar:\n" +
                    "1. Inventario → Buscar el producto\n" +
                    "2. Editar → Nuevo stock y fecha\n" +
                    "3. Se reactiva automáticamente",
                    "Productos Desactivados",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Actualizar tablas (ARQUITECTURA: Usar paneles modulares)
                alertsPanel.cargarAlertas();
                inventoryPanel.loadProductsData();
                salesPanel.cargarCatalogo();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al desactivar productos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // ==================== PANEL DE PIE ====================
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(1000, 30));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        
        JLabel lblFooter = new JLabel("Nova Farma © 2024 - Sistema de Gestión Farmacéutica");
        lblFooter.setFont(new Font("Arial", Font.PLAIN, 10));
        
        panel.add(lblFooter);
        
        return panel;
    }
    
    // ==================== CONTROL DE PERMISOS POR ROL ====================
    
    /**
     * Aplica las restricciones según el rol del usuario
     * 
     * ESTE ES EL MÉTODO CRÍTICO QUE IMPLEMENTA EL REQUISITO:
     * "El sistema debe distinguir permisos de Modificación vs Operación"
     */
    /**
     * Aplica permisos basados en el rol del usuario
     * (FASE B: Delega a los paneles modulares)
     */
    private void applyRolePermissions() {
        // Aplicar permisos a paneles modulares
        inventoryPanel.applyRolePermissions();
        alertsPanel.applyRolePermissions();
        
        // Nota: El módulo de ventas (POS) está disponible para todos los roles
        // Los trabajadores pueden vender, pero no pueden modificar productos
    }
    
    // ==================== MÉTODOS DE ACCIÓN ====================
    
    /**
     * Carga los productos activos en la tabla de inventario
     * 
     * ARQUITECTURA: Usa ProductService (capa de negocio) en lugar de SQL directo
     */
    private void loadProductsData() {
        // ARQUITECTURA: Delegar a InventoryPanel
        inventoryPanel.loadProductsData();
    }
    
    /**
     * Agrega un nuevo producto al inventario
     * 
     * MEJORA IMPLEMENTADA: Detección de duplicados
     * Antes de crear un producto nuevo, verifica si ya existe uno con el mismo nombre.
     * Si existe (aunque esté inactivo), pregunta al usuario si desea:
     * - Actualizar el producto existente (recomendado para nuevos lotes)
     * - Crear un producto nuevo (para casos especiales)
     * 
     * FLUJO:
     * 1. Validar permisos (solo administrador)
     * 2. Mostrar formulario de entrada
     * 3. Verificar si existe producto con mismo nombre
     * 4. Si existe → Mostrar diálogo de confirmación
     * 5. Si no existe → Crear producto nuevo normalmente
     */
    private void addProduct() {
        // VALIDACIÓN DE ROL
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(this,
                "ACCESO DENEGADO\n\nSolo los ADMINISTRADORES pueden agregar productos.",
                "Permiso Denegado",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Formulario de agregar producto
        JTextField txtNombre = new JTextField();
        JTextField txtDescripcion = new JTextField();
        JTextField txtPrecio = new JTextField();
        JTextField txtStock = new JTextField();
        JTextField txtFechaVenc = new JTextField("2025-12-31");
        
        Object[] message = {
            "Nombre:", txtNombre,
            "Descripción:", txtDescripcion,
            "Precio:", txtPrecio,
            "Stock:", txtStock,
            "Fecha Venc. (YYYY-MM-DD):", txtFechaVenc
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, 
            "Agregar Producto", JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            try {
                // Obtener datos del formulario
                String nombre = txtNombre.getText().trim();
                String descripcion = txtDescripcion.getText().trim();
                double precio = Double.parseDouble(txtPrecio.getText());
                int stock = Integer.parseInt(txtStock.getText());
                Date fechaVenc = Date.valueOf(txtFechaVenc.getText());
                
                // ==================== VERIFICACIÓN DE DUPLICADOS ====================
                // Buscar si ya existe un producto con el mismo nombre (case-insensitive)
                Product existingProduct = productService.findProductByName(nombre);
                
                if (existingProduct != null) {
                    // PRODUCTO DUPLICADO ENCONTRADO
                    // Construir mensaje informativo
                    String estado = existingProduct.isActivo() ? "ACTIVO" : "INACTIVO";
                    String mensajeDuplicado = String.format(
                        "⚠️ PRODUCTO DUPLICADO DETECTADO ⚠️\n\n" +
                        "Ya existe un producto llamado '%s':\n\n" +
                        "ID: %d\n" +
                        "Estado: %s\n" +
                        "Stock actual: %d\n" +
                        "Precio actual: $%.2f\n\n" +
                        "¿Qué deseas hacer?\n\n" +
                        "• ACTUALIZAR: Reactivará el producto existente con el nuevo lote\n" +
                        "  (Recomendado para cuando llega un nuevo lote del mismo producto)\n\n" +
                        "• CREAR NUEVO: Creará un producto diferente con el mismo nombre\n" +
                        "  (Solo si realmente son productos diferentes)",
                        nombre,
                        existingProduct.getId(),
                        estado,
                        existingProduct.getStock(),
                        existingProduct.getPrecio()
                    );
                    
                    // Diálogo con 3 opciones
                    Object[] opciones = {"Actualizar Existente", "Crear Nuevo", "Cancelar"};
                    int respuesta = JOptionPane.showOptionDialog(
                        this,
                        mensajeDuplicado,
                        "Producto Duplicado",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        opciones,
                        opciones[0]  // "Actualizar Existente" como opción por defecto
                    );
                    
                    if (respuesta == 0) {
                        // OPCIÓN 1: ACTUALIZAR PRODUCTO EXISTENTE
                        // Actualizar el producto existente con los nuevos datos del lote
                        existingProduct.setNombre(nombre);  // Por si cambió la capitalización
                        existingProduct.setDescripcion(descripcion);
                        existingProduct.setPrecio(precio);
                        existingProduct.setStock(stock);  // Nuevo stock del lote
                        existingProduct.setFechaVencimiento(fechaVenc);  // Nueva fecha de vencimiento
                        // Nota: updateProduct() automáticamente reactiva el producto si stock > 0
                        
                        boolean success = productService.updateProduct(existingProduct);
                        
                        if (success) {
                            JOptionPane.showMessageDialog(this,
                                String.format(
                                    "✅ Producto actualizado exitosamente\n\n" +
                                    "ID: %d\n" +
                                    "Nombre: %s\n" +
                                    "Stock: %d unidades\n" +
                                    "El producto ha sido reactivado automáticamente.",
                                    existingProduct.getId(),
                                    nombre,
                                    stock
                                ),
                                "Producto Actualizado",
                                JOptionPane.INFORMATION_MESSAGE);
                            loadProductsData(); // Recargar tabla
                        } else {
                            JOptionPane.showMessageDialog(this,
                                "⚠️ No se pudo actualizar el producto",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                        return; // Salir del método (ya procesamos la acción)
                        
                    } else if (respuesta == 1) {
                        // OPCIÓN 2: CREAR PRODUCTO NUEVO
                        // El usuario confirmó que quiere crear un producto diferente
                        // Proceder con la creación normal (código continúa abajo)
                        
                    } else {
                        // OPCIÓN 3: CANCELAR
                        // El usuario canceló la operación
                        return;
                    }
                }
                
                // ==================== CREAR PRODUCTO NUEVO ====================
                // Si llegamos aquí, significa que:
                // 1. No existe un producto con ese nombre, O
                // 2. El usuario eligió "Crear Nuevo" explícitamente
                
                // Crear objeto Product (ARQUITECTURA: Usar modelo)
                Product newProduct = new Product(nombre, descripcion, precio, stock, fechaVenc);
                
                // Usar ProductService en lugar de SQL directo (ARQUITECTURA: Capa de Servicios)
                boolean success = productService.createProduct(newProduct);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "✅ Producto agregado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadProductsData(); // Recargar tabla
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "⚠️ No se pudo agregar el producto",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (IllegalArgumentException e) {
                // Errores de validación de negocio
                JOptionPane.showMessageDialog(this, 
                    "❌ Validación fallida:\n" + e.getMessage(),
                    "Datos Inválidos",
                    JOptionPane.WARNING_MESSAGE);
            } catch (SQLException e) {
                // Errores de base de datos
                JOptionPane.showMessageDialog(this, 
                    "❌ Error de base de datos:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                // Otros errores (parseo de números, etc.)
                JOptionPane.showMessageDialog(this, 
                    "❌ Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Edita un producto existente
     * 
     * FLUJO:
     * 1. Validar que el usuario sea ADMINISTRADOR
     * 2. Validar que haya un producto seleccionado
     * 3. Cargar los datos actuales del producto en el formulario
     * 4. Permitir editar los campos
     * 5. Ejecutar UPDATE en la base de datos
     * 6. Recargar la tabla
     */
    private void editProduct() {
        // VALIDACIÓN DE ROL
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(this,
                "ACCESO DENEGADO\n\nSolo los ADMINISTRADORES pueden editar productos.",
                "Permiso Denegado",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validar selección (ARQUITECTURA: Usar InventoryPanel)
        int realRow = inventoryPanel.getSelectedProductRow();
        if (realRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, selecciona un producto de la tabla para editar.",
                "Ningún Producto Seleccionado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Obtener datos actuales del producto desde InventoryPanel
            DefaultTableModel model = inventoryPanel.getTableModel();
            int productId = (int) model.getValueAt(realRow, 0);
            String nombreActual = (String) model.getValueAt(realRow, 1);
            String descripcionActual = (String) model.getValueAt(realRow, 2);
            String precioStr = (String) model.getValueAt(realRow, 3);
            int stockActual = (int) model.getValueAt(realRow, 4);
            String fechaVencStr = (String) model.getValueAt(realRow, 5);
            
            // Extraer el precio (quitar el símbolo $)
            double precioActual = Double.parseDouble(precioStr.replace("$", ""));
            
            // Convertir fecha a formato YYYY-MM-DD para el formulario
            String fechaVencActual = fechaVencStr;
            if (!fechaVencStr.equalsIgnoreCase("N/A")) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                fechaVencActual = outputFormat.format(inputFormat.parse(fechaVencStr));
            }
            
            // ==================== FORMULARIO DE EDICIÓN ====================
            
            JTextField txtNombre = new JTextField(nombreActual);
            JTextField txtDescripcion = new JTextField(descripcionActual);
            JTextField txtPrecio = new JTextField(String.valueOf(precioActual));
            JTextField txtStock = new JTextField(String.valueOf(stockActual));
            JTextField txtFechaVenc = new JTextField(fechaVencActual);
            
            Object[] message = {
                "Nombre:", txtNombre,
                "Descripción:", txtDescripcion,
                "Precio:", txtPrecio,
                "Stock:", txtStock,
                "Fecha Venc. (YYYY-MM-DD):", txtFechaVenc
            };
            
            int option = JOptionPane.showConfirmDialog(this, message, 
                "Editar Producto ID: " + productId, JOptionPane.OK_CANCEL_OPTION);
            
            if (option == JOptionPane.OK_OPTION) {
                // Obtener nuevos valores
                String nuevoNombre = txtNombre.getText().trim();
                String nuevaDescripcion = txtDescripcion.getText().trim();
                double nuevoPrecio = Double.parseDouble(txtPrecio.getText().trim());
                int nuevoStock = Integer.parseInt(txtStock.getText().trim());
                String nuevaFechaVenc = txtFechaVenc.getText().trim();
                
                // Crear objeto Product actualizado (ARQUITECTURA: Usar modelo)
                Product updatedProduct = new Product();
                updatedProduct.setId(productId);
                updatedProduct.setNombre(nuevoNombre);
                updatedProduct.setDescripcion(nuevaDescripcion);
                updatedProduct.setPrecio(nuevoPrecio);
                updatedProduct.setStock(nuevoStock);
                
                // Manejar fecha de vencimiento
                if (nuevaFechaVenc.equalsIgnoreCase("N/A") || nuevaFechaVenc.isEmpty()) {
                    updatedProduct.setFechaVencimiento(null);
                } else {
                    updatedProduct.setFechaVencimiento(Date.valueOf(nuevaFechaVenc));
                }
                
                // El ProductService automáticamente activa el producto si stock > 0
                
                // Usar ProductService en lugar de SQL directo (ARQUITECTURA: Capa de Servicios)
                boolean success = productService.updateProduct(updatedProduct);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "✅ Producto actualizado exitosamente\n\n" +
                        "ID: " + productId + "\n" +
                        "Nombre: " + nuevoNombre,
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Recargar la tabla para mostrar los cambios (ARQUITECTURA: Usar InventoryPanel)
                    inventoryPanel.loadProductsData();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "⚠️ No se pudo actualizar el producto",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al editar el producto:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void deleteProduct() {
        // VALIDACIÓN DE ROL
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(this,
                "ACCESO DENEGADO\n\nSolo los ADMINISTRADORES pueden eliminar productos.",
                "Permiso Denegado",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validar selección (ARQUITECTURA: Usar InventoryPanel)
        Integer productId = inventoryPanel.getSelectedProductId();
        if (productId == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de eliminar este producto?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                
                // Usar ProductService para realizar "soft delete" (ARQUITECTURA: Capa de Servicios)
                // Esto establece activo = FALSE y stock = 0, preservando el historial
                boolean success = productService.retireProduct(productId);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "✅ Producto desactivado exitosamente\n\n" +
                        "El producto se marcó como INACTIVO (stock = 0).\n" +
                        "Se preserva en la base de datos para el historial de ventas.\n\n" +
                        "Para reactivarlo:\n" +
                        "• Editar el producto\n" +
                        "• Establecer nuevo stock\n" +
                        "• Se reactivará automáticamente",
                        "Producto Retirado",
                        JOptionPane.INFORMATION_MESSAGE);
                    // Recargar tabla (ARQUITECTURA: Usar InventoryPanel)
                    inventoryPanel.loadProductsData();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "⚠️ No se pudo desactivar el producto",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "❌ Error al desactivar el producto:\n" + e.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // ==================== MÉTODOS DEL MÓDULO DE VENTAS ====================
    // NOTA: Todos los métodos de ventas fueron movidos a SalesPanel.java
    // (createSalesPanel, cargarCatalogo, filtrarCatalogo, agregarAlCarrito,
    //  quitarDelCarrito, limpiarCarrito, finalizarVenta, actualizarTotal)
    
    // ==================== MÉTODOS DE ACCIÓN (Callbacks para paneles) ====================
    
    private void createUser() {
        // VALIDACIÓN DE ROL
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(this,
                "ACCESO DENEGADO\n\nSolo los ADMINISTRADORES pueden crear usuarios.",
                "Permiso Denegado",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Mostrar diálogo de creación de usuario
        UserCreationDialog dialog = new UserCreationDialog(this);
        dialog.setVisible(true);
        
        // Recargar tabla después de crear usuario
        loadUsersData();
    }
    
    /**
     * Elimina un usuario seleccionado de la tabla
     */
    private void deleteUser() {
        // VALIDACIÓN DE ROL
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(this,
                "ACCESO DENEGADO\n\nSolo los ADMINISTRADORES pueden eliminar usuarios.",
                "Permiso Denegado",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validar selección
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, selecciona un usuario de la tabla para eliminar.",
                "Selección Requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtener datos del usuario seleccionado
        int userId = (Integer) usersTableModel.getValueAt(selectedRow, 0);
        String username = (String) usersTableModel.getValueAt(selectedRow, 1);
        
        // NO permitir eliminar el usuario actual
        if (userId == currentUser.getId()) {
            JOptionPane.showMessageDialog(this,
                "No puedes eliminar tu propio usuario mientras estás conectado.\n\n" +
                "Cierra sesión primero o usa otra cuenta de administrador.",
                "Operación No Permitida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Confirmar eliminación
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de eliminar al usuario '" + username + "' (ID: " + userId + ")?\n\n" +
            "Esta acción NO se puede deshacer.",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Intentar eliminar
        try {
            UserService.DeleteUserResult result = userService.deleteUser(userId);
            
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                    result.getMessage(),
                    "Usuario Eliminado",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Recargar tabla
                loadUsersData();
            } else {
                JOptionPane.showMessageDialog(this,
                    result.getMessage(),
                    "No Se Puede Eliminar",
                    JOptionPane.WARNING_MESSAGE);
            }
            
        } catch (SQLException e) {
            // Manejar errores de integridad referencial u otros
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("foreign key")) {
                JOptionPane.showMessageDialog(this,
                    "No se puede eliminar el usuario porque tiene ventas registradas.\n\n" +
                    "Las ventas deben conservarse para el historial del negocio.",
                    "Error de Integridad",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar usuario:\n" + errorMsg,
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
            }
            e.printStackTrace();
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Deseas cerrar sesión?",
            "Confirmar Cierre de Sesión",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Aplica el estilo estándar a los botones de la aplicación.
     * Usa Look & Feel por defecto de Swing sin personalizaciones.
     * 
     * @param button El botón a estilizar
     */
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    /**
     * Aplica un estilo limpio y profesional a las tablas JTable.
     * 
     * ESPECIFICACIONES:
     * - Encabezado: Fondo blanco, texto negro en negrita, sin borde 3D
     * - Cuerpo: Fuente Arial 12pt, altura de fila 28px, grid gris suave
     * - Texto: Negro para buena legibilidad sobre fondos de colores
     * 
     * @param table La tabla a la que se aplicará el estilo
     */
    private void applyTableStyle(JTable table) {
        // Configuración del cuerpo de la tabla
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setGridColor(new Color(200, 200, 200)); // Grid gris suave
        table.setForeground(Color.BLACK); // Texto negro para legibilidad
        table.setSelectionBackground(new Color(184, 207, 229)); // Azul suave para selección
        table.setSelectionForeground(Color.BLACK);
        
        // Configuración del encabezado (header)
        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            table.getTableHeader().setBackground(Color.WHITE); // Fondo blanco
            table.getTableHeader().setForeground(Color.BLACK); // Texto negro
            table.getTableHeader().setReorderingAllowed(false);
            
            // Eliminar el efecto 3D del encabezado (borde plano)
            table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        }
    }
}

