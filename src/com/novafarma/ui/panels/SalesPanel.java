package com.novafarma.ui.panels;

import com.novafarma.model.Product;
import com.novafarma.model.Sale;
import com.novafarma.model.User;
import com.novafarma.service.ProductService;
import com.novafarma.service.SaleService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de Ventas (Punto de Venta - POS)
 * 
 * RESPONSABILIDADES:
 * - Mostrar catálogo de productos vendibles
 * - Gestionar carrito de compras
 * - Procesar ventas con validaciones
 * - Búsqueda en tiempo real
 * 
 * ARQUITECTURA:
 * - Usa ProductService para obtener productos
 * - Usa SaleService para validar y procesar ventas
 * - UI separada de lógica de negocio
 * - Callback para notificar finalización de venta
 * 
 * @author Nova Farma Development Team
 * @version 2.0 (Refactorizado con Arquitectura en Capas)
 */
public class SalesPanel extends JPanel {
    
    // Servicios
    private ProductService productService;
    private SaleService saleService;
    
    // Usuario actual
    private User currentUser;
    
    // Componentes UI - Facturación (Parte Superior)
    private JComboBox<String> cmbTipoComprobante;
    private JTextField txtCliente;
    private JTextField txtDniRuc;
    
    // Componentes UI - Catálogo
    private JTable tableCatalogo;
    private DefaultTableModel modelCatalogo;
    private JTextField txtBuscador;
    
    // Componentes UI - Carrito
    private JTable tableCarrito;
    private DefaultTableModel modelCarrito;
    private JLabel lblTotal;
    private double totalVenta;
    
    // Callback para notificar finalización de venta (para recargar inventario)
    private Runnable onVentaFinalizada;
    
    // ==================== CONSTRUCTOR ====================
    
    public SalesPanel(User currentUser, ProductService productService, SaleService saleService) {
        this.currentUser = currentUser;
        this.productService = productService;
        this.saleService = saleService;
        this.totalVenta = 0.0;
        
        initializeUI();
        cargarCatalogo();
    }
    
    // ==================== INICIALIZACIÓN DE UI ====================
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // ==================== PANEL SUPERIOR: DATOS DE FACTURACIÓN ====================
        
        JPanel facturacionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        facturacionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("📋 Datos del Cliente / Comprobante"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Tipo de Comprobante
        JLabel lblTipoComprobante = new JLabel("Tipo:");
        lblTipoComprobante.setFont(new Font("Arial", Font.BOLD, 12));
        cmbTipoComprobante = new JComboBox<>(new String[]{"BOLETA", "FACTURA"});
        cmbTipoComprobante.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbTipoComprobante.setPreferredSize(new Dimension(120, 30));
        
        // Cliente / Razón Social
        JLabel lblCliente = new JLabel("Cliente / Razón Social:");
        lblCliente.setFont(new Font("Arial", Font.BOLD, 12));
        txtCliente = new JTextField(25);
        txtCliente.setFont(new Font("Arial", Font.PLAIN, 12));
        txtCliente.setToolTipText("Nombre del cliente o razón social de la empresa");
        
        // DNI / RUC
        JLabel lblDniRuc = new JLabel("DNI / RUC:");
        lblDniRuc.setFont(new Font("Arial", Font.BOLD, 12));
        txtDniRuc = new JTextField(15);
        txtDniRuc.setFont(new Font("Arial", Font.PLAIN, 12));
        txtDniRuc.setToolTipText("DNI para boletas o RUC para facturas");
        
        facturacionPanel.add(lblTipoComprobante);
        facturacionPanel.add(cmbTipoComprobante);
        facturacionPanel.add(Box.createHorizontalStrut(10));
        facturacionPanel.add(lblCliente);
        facturacionPanel.add(txtCliente);
        facturacionPanel.add(Box.createHorizontalStrut(10));
        facturacionPanel.add(lblDniRuc);
        facturacionPanel.add(txtDniRuc);
        
        add(facturacionPanel, BorderLayout.NORTH);
        
        // ==================== SPLIT PANE: CATÁLOGO | CARRITO ====================
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(550);
        splitPane.setDividerSize(5);
        
        // ==================== PANEL IZQUIERDO: CATÁLOGO ====================
        
        JPanel catalogoPanel = new JPanel(new BorderLayout(10, 10));
        catalogoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("📦 Catálogo de Productos"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Buscador
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        JLabel lblBuscar = new JLabel("🔍 Buscar:");
        lblBuscar.setFont(new Font("Arial", Font.BOLD, 12));
        txtBuscador = new JTextField();
        txtBuscador.setFont(new Font("Arial", Font.PLAIN, 14));
        txtBuscador.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtrarCatalogo();
            }
        });
        
        searchPanel.add(lblBuscar, BorderLayout.WEST);
        searchPanel.add(txtBuscador, BorderLayout.CENTER);
        catalogoPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Tabla de catálogo
        String[] columnsCatalogo = {"ID", "Nombre", "Precio", "Stock"};
        modelCatalogo = new DefaultTableModel(columnsCatalogo, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableCatalogo = new JTable(modelCatalogo);
        applyTableStyle(tableCatalogo);
        
        // Ajustar anchos de columnas
        tableCatalogo.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableCatalogo.getColumnModel().getColumn(1).setPreferredWidth(250);
        tableCatalogo.getColumnModel().getColumn(2).setPreferredWidth(80);
        tableCatalogo.getColumnModel().getColumn(3).setPreferredWidth(80);
        
        JScrollPane scrollCatalogo = new JScrollPane(tableCatalogo);
        catalogoPanel.add(scrollCatalogo, BorderLayout.CENTER);
        
        // Botón Agregar al Carrito
        JPanel btnCatalogoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAgregar = new JButton("➕ Agregar al Carrito");
        btnAgregar.setFont(new Font("Arial", Font.PLAIN, 13));
        btnAgregar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAgregar.addActionListener(e -> agregarAlCarrito());
        
        btnCatalogoPanel.add(btnAgregar);
        catalogoPanel.add(btnCatalogoPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(catalogoPanel);
        
        // ==================== PANEL DERECHO: CARRITO ====================
        
        JPanel carritoPanel = new JPanel(new BorderLayout(10, 10));
        carritoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("🛒 Carrito de Compras"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Tabla del carrito
        String[] columnsCarrito = {"ID", "Producto", "Cant.", "Precio U.", "Subtotal"};
        modelCarrito = new DefaultTableModel(columnsCarrito, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableCarrito = new JTable(modelCarrito);
        applyTableStyle(tableCarrito);
        
        // Ajustar anchos de columnas
        tableCarrito.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableCarrito.getColumnModel().getColumn(1).setPreferredWidth(150);
        tableCarrito.getColumnModel().getColumn(2).setPreferredWidth(50);
        tableCarrito.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableCarrito.getColumnModel().getColumn(4).setPreferredWidth(80);
        
        JScrollPane scrollCarrito = new JScrollPane(tableCarrito);
        carritoPanel.add(scrollCarrito, BorderLayout.CENTER);
        
        // Panel inferior: Total y botones
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        
        // Panel de Total
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        totalPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblTotalTexto = new JLabel("TOTAL:");
        lblTotalTexto.setFont(new Font("Arial", Font.BOLD, 16));
        
        lblTotal = new JLabel("$0.00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 20));
        
        totalPanel.add(lblTotalTexto);
        totalPanel.add(lblTotal);
        
        bottomPanel.add(totalPanel, BorderLayout.NORTH);
        
        // Panel de botones
        JPanel btnCarritoPanel = new JPanel();
        btnCarritoPanel.setLayout(new BoxLayout(btnCarritoPanel, BoxLayout.Y_AXIS));
        
        // Primera fila: Quitar y Limpiar
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        JButton btnEliminarItem = new JButton("❌ Quitar");
        btnEliminarItem.setFont(new Font("Arial", Font.PLAIN, 11));
        btnEliminarItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminarItem.addActionListener(e -> quitarDelCarrito());
        
        JButton btnLimpiar = new JButton("🗑️ Limpiar");
        btnLimpiar.setFont(new Font("Arial", Font.PLAIN, 11));
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiar.addActionListener(e -> limpiarCarrito());
        
        fila1.add(btnEliminarItem);
        fila1.add(btnLimpiar);
        
        // Segunda fila: Registrar Venta / Emitir Comprobante
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        JButton btnFinalizar = new JButton("✅ Registrar Venta / Emitir Comprobante");
        btnFinalizar.setFont(new Font("Arial", Font.BOLD, 13));
        btnFinalizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFinalizar.addActionListener(e -> finalizarVenta());
        
        fila2.add(btnFinalizar);
        
        btnCarritoPanel.add(fila1);
        btnCarritoPanel.add(fila2);
        
        bottomPanel.add(btnCarritoPanel, BorderLayout.CENTER);
        
        carritoPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        splitPane.setRightComponent(carritoPanel);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    // ==================== MÉTODOS DE ACCIÓN ====================
    
    /**
     * Carga productos activos en el catálogo de ventas
     */
    public void cargarCatalogo() {
        try {
            modelCatalogo.setRowCount(0);
            
            // Usar ProductService (ARQUITECTURA: Capa de Servicios)
            List<Product> products = productService.getAllActiveProducts();
            
            // Filtrar solo productos con stock > 0 (vendibles)
            for (Product product : products) {
                if (product.getStock() > 0) {
                    Object[] row = {
                        product.getId(),
                        product.getNombre(),
                        String.format("$%.2f", product.getPrecio()),
                        product.getStock()
                    };
                    modelCatalogo.addRow(row);
                }
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar catálogo: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Filtra el catálogo según el texto del buscador
     */
    private void filtrarCatalogo() {
        String filtro = txtBuscador.getText().toLowerCase().trim();
        
        if (filtro.isEmpty()) {
            cargarCatalogo();
            return;
        }
        
        try {
            modelCatalogo.setRowCount(0);
            
            // Usar ProductService (ARQUITECTURA: Capa de Servicios)
            List<Product> products = productService.getAllActiveProducts();
            
            // Filtrar por nombre (case-insensitive)
            for (Product product : products) {
                if (product.getStock() > 0 && 
                    product.getNombre().toLowerCase().contains(filtro)) {
                    Object[] row = {
                        product.getId(),
                        product.getNombre(),
                        String.format("$%.2f", product.getPrecio()),
                        product.getStock()
                    };
                    modelCatalogo.addRow(row);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Agrega el producto seleccionado al carrito
     */
    private void agregarAlCarrito() {
        int selectedRow = tableCatalogo.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, selecciona un producto del catálogo",
                "Producto No Seleccionado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtener datos del producto
        int productoId = (int) modelCatalogo.getValueAt(selectedRow, 0);
        String nombreProducto = (String) modelCatalogo.getValueAt(selectedRow, 1);
        String precioStr = (String) modelCatalogo.getValueAt(selectedRow, 2);
        int stockDisponible = (int) modelCatalogo.getValueAt(selectedRow, 3);
        
        // Extraer el precio
        double precio = Double.parseDouble(precioStr.replace("$", ""));
        
        // Pedir cantidad
        String cantidadStr = JOptionPane.showInputDialog(this,
            "Producto: " + nombreProducto + "\n" +
            "Precio: " + precioStr + "\n" +
            "Stock disponible: " + stockDisponible + "\n\n" +
            "Ingresa la cantidad:",
            "Agregar al Carrito",
            JOptionPane.QUESTION_MESSAGE);
        
        if (cantidadStr == null || cantidadStr.trim().isEmpty()) {
            return;
        }
        
        try {
            int cantidad = Integer.parseInt(cantidadStr.trim());
            
            // Validar cantidad
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this,
                    "La cantidad debe ser mayor a 0",
                    "Cantidad Inválida",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (cantidad > stockDisponible) {
                JOptionPane.showMessageDialog(this,
                    "Stock insuficiente.\n" +
                    "Disponible: " + stockDisponible + " unidades",
                    "Stock Insuficiente",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Calcular subtotal
            double subtotal = cantidad * precio;
            
            // Verificar si el producto ya está en el carrito
            boolean productoExiste = false;
            for (int i = 0; i < modelCarrito.getRowCount(); i++) {
                int idEnCarrito = (int) modelCarrito.getValueAt(i, 0);
                if (idEnCarrito == productoId) {
                    // Actualizar cantidad y subtotal
                    int cantidadActual = (int) modelCarrito.getValueAt(i, 2);
                    int nuevaCantidad = cantidadActual + cantidad;
                    
                    if (nuevaCantidad > stockDisponible) {
                        JOptionPane.showMessageDialog(this,
                            "No puedes agregar más unidades.\n" +
                            "Ya tienes " + cantidadActual + " en el carrito.\n" +
                            "Stock disponible: " + stockDisponible,
                            "Stock Insuficiente",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    double nuevoSubtotal = nuevaCantidad * precio;
                    modelCarrito.setValueAt(nuevaCantidad, i, 2);
                    modelCarrito.setValueAt(String.format("$%.2f", nuevoSubtotal), i, 4);
                    productoExiste = true;
                    break;
                }
            }
            
            // Si no existe, agregar nueva fila
            if (!productoExiste) {
                Object[] row = {
                    productoId,
                    nombreProducto,
                    cantidad,
                    precioStr,
                    String.format("$%.2f", subtotal)
                };
                modelCarrito.addRow(row);
            }
            
            // Actualizar total
            actualizarTotal();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Por favor, ingresa un número válido",
                "Cantidad Inválida",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Actualiza el total de la venta
     */
    private void actualizarTotal() {
        totalVenta = 0.0;
        
        for (int i = 0; i < modelCarrito.getRowCount(); i++) {
            String subtotalStr = (String) modelCarrito.getValueAt(i, 4);
            double subtotal = Double.parseDouble(subtotalStr.replace("$", ""));
            totalVenta += subtotal;
        }
        
        lblTotal.setText(String.format("$%.2f", totalVenta));
    }
    
    /**
     * Quita un producto del carrito
     */
    private void quitarDelCarrito() {
        int selectedRow = tableCarrito.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, selecciona un producto del carrito para quitarlo.",
                "Ningún Producto Seleccionado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String nombreProducto = (String) modelCarrito.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Quitar del carrito?\n\n" + nombreProducto,
            "Confirmar",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            modelCarrito.removeRow(selectedRow);
            actualizarTotal();
            
            // Si aún hay productos, preguntar si desea finalizar
            if (modelCarrito.getRowCount() > 0) {
                int unidadesRestantes = 0;
                for (int i = 0; i < modelCarrito.getRowCount(); i++) {
                    unidadesRestantes += (int) modelCarrito.getValueAt(i, 2);
                }
                
                int finalizarAhora = JOptionPane.showConfirmDialog(this,
                    "Producto removido del carrito.\n\n" +
                    "¿Deseas finalizar la venta ahora con los productos restantes?\n\n" +
                    "Líneas: " + modelCarrito.getRowCount() + "\n" +
                    "Unidades: " + unidadesRestantes + "\n" +
                    "Total: " + lblTotal.getText(),
                    "Finalizar Venta",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (finalizarAhora == JOptionPane.YES_OPTION) {
                    finalizarVenta();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Producto removido.\n\nEl carrito está vacío.",
                    "Carrito Vacío",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    /**
     * Limpia todos los items del carrito
     */
    private void limpiarCarrito() {
        if (modelCarrito.getRowCount() == 0) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Deseas limpiar el carrito?\nSe perderán todos los productos agregados.",
            "Confirmar",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            modelCarrito.setRowCount(0);
            totalVenta = 0.0;
            lblTotal.setText("$0.00");
            limpiarCamposFacturacion(); // Limpiar campos de facturación
        }
    }
    
    /**
     * Finaliza la venta procesando todos los productos del carrito
     * 
     * ARQUITECTURA: Usa SaleService para validación y procesamiento
     */
    private void finalizarVenta() {
        // Validar que haya productos en el carrito
        if (modelCarrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "El carrito está vacío.\nAgrega productos antes de finalizar la venta.",
                "Carrito Vacío",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Convertir el carrito a lista de Sales
        List<Sale> sales = new ArrayList<>();
        int usuarioId = currentUser.getId();
        
        for (int i = 0; i < modelCarrito.getRowCount(); i++) {
            int productoId = (int) modelCarrito.getValueAt(i, 0);
            int cantidad = (int) modelCarrito.getValueAt(i, 2);
            String precioStr = (String) modelCarrito.getValueAt(i, 3);
            double precioUnitario = Double.parseDouble(precioStr.replace("$", ""));
            
            Sale sale = new Sale(productoId, usuarioId, cantidad, precioUnitario);
            sales.add(sale);
        }
        
        // Validar carrito usando SaleService (ARQUITECTURA: Capa de Servicios)
        List<String> errores = saleService.validateCart(sales);
        
        // Si hay errores, BLOQUEAR venta
        if (!errores.isEmpty()) {
            StringBuilder mensaje = new StringBuilder("⚠️ NO SE PUEDE COMPLETAR LA VENTA ⚠️\n\n");
            mensaje.append("Se encontraron los siguientes problemas:\n\n");
            for (String error : errores) {
                mensaje.append(error).append("\n");
            }
            mensaje.append("\nVender productos vencidos es:\n");
            mensaje.append("❌ Ilegal\n");
            mensaje.append("❌ Peligroso para la salud del cliente\n");
            mensaje.append("❌ Sujeto a sanciones legales\n\n");
            mensaje.append("¿Deseas ir al carrito para quitar los productos problemáticos?");
            
            int opcion = JOptionPane.showOptionDialog(this,
                mensaje.toString(),
                "Validación de Carrito Fallida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new Object[]{"Ir al Carrito", "Cancelar Venta"},
                "Ir al Carrito");
            
            if (opcion == 0) {
                // Ya estamos en el panel de ventas, solo mostrar instrucción
                JOptionPane.showMessageDialog(this,
                    "Selecciona los productos problemáticos en el carrito\n" +
                    "y haz click en '❌ Quitar Seleccionado'.\n\n" +
                    "Luego, intenta finalizar la venta de nuevo.",
                    "Instrucciones",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            return;
        }
        
        // Obtener datos de facturación (solo para mostrar, no afecta la lógica)
        String tipoComprobante = (String) cmbTipoComprobante.getSelectedItem();
        String cliente = txtCliente.getText().trim();
        String dniRuc = txtDniRuc.getText().trim();
        
        // Confirmar venta con información de facturación
        int unidadesTotales = sales.stream().mapToInt(Sale::getCantidad).sum();
        
        StringBuilder mensajeConfirmacion = new StringBuilder("¿Confirmar venta?\n\n");
        mensajeConfirmacion.append("Tipo de Comprobante: ").append(tipoComprobante).append("\n");
        if (!cliente.isEmpty()) {
            mensajeConfirmacion.append("Cliente: ").append(cliente).append("\n");
        }
        if (!dniRuc.isEmpty()) {
            mensajeConfirmacion.append("DNI/RUC: ").append(dniRuc).append("\n");
        }
        mensajeConfirmacion.append("\n");
        mensajeConfirmacion.append("Total: ").append(lblTotal.getText()).append("\n");
        mensajeConfirmacion.append("Líneas de productos: ").append(sales.size()).append("\n");
        mensajeConfirmacion.append("Unidades totales: ").append(unidadesTotales);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            mensajeConfirmacion.toString(),
            "Registrar Venta / Emitir Comprobante",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Procesar venta con SaleService (ARQUITECTURA: Capa de Servicios)
        SaleService.SaleResult result = saleService.processMultipleSales(sales);
        
        // Mostrar resultado con información de facturación
        if (result.isSuccess()) {
            StringBuilder mensajeExito = new StringBuilder("✅ VENTA REGISTRADA EXITOSAMENTE\n\n");
            mensajeExito.append("Tipo de Comprobante: ").append(tipoComprobante).append("\n");
            if (!cliente.isEmpty()) {
                mensajeExito.append("Cliente: ").append(cliente).append("\n");
            }
            if (!dniRuc.isEmpty()) {
                mensajeExito.append("DNI/RUC: ").append(dniRuc).append("\n");
            }
            mensajeExito.append("\n");
            mensajeExito.append("Total: ").append(String.format("$%.2f", result.getTotalAmount())).append("\n");
            mensajeExito.append("Líneas de productos: ").append(result.getSuccessfulSales()).append("\n");
            mensajeExito.append("Unidades vendidas: ").append(result.getTotalUnits()).append("\n\n");
            mensajeExito.append("El stock se actualizó automáticamente.");
            
            JOptionPane.showMessageDialog(this,
                mensajeExito.toString(),
                "Venta Completada",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Limpiar carrito, campos de facturación y recargar catálogo
            modelCarrito.setRowCount(0);
            totalVenta = 0.0;
            lblTotal.setText("$0.00");
            limpiarCamposFacturacion(); // Limpiar campos de facturación
            cargarCatalogo();
            
            // Notificar a Dashboard para recargar inventario
            if (onVentaFinalizada != null) {
                onVentaFinalizada.run();
            }
            
        } else {
            StringBuilder errorMsg = new StringBuilder(result.getMessage());
            if (!result.getErrors().isEmpty()) {
                errorMsg.append("\n\nDetalles:\n");
                for (String error : result.getErrors()) {
                    errorMsg.append("• ").append(error).append("\n");
                }
            }
            
            JOptionPane.showMessageDialog(this,
                errorMsg.toString(),
                "Error en la Venta",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    /**
     * Limpia los campos de facturación
     */
    private void limpiarCamposFacturacion() {
        cmbTipoComprobante.setSelectedIndex(0); // Volver a "BOLETA"
        txtCliente.setText("");
        txtDniRuc.setText("");
    }
    
    /**
     * Establece el callback que se ejecuta cuando se finaliza una venta
     * Útil para recargar el inventario en Dashboard
     */
    public void setOnVentaFinalizada(Runnable callback) {
        this.onVentaFinalizada = callback;
    }
    
    // ==================== MÉTODO AUXILIAR ====================
    
    /**
     * Aplica estilo limpio y profesional a las tablas
     */
    private void applyTableStyle(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setGridColor(new Color(200, 200, 200));
        table.setForeground(Color.BLACK);
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setSelectionForeground(Color.BLACK);
        
        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            table.getTableHeader().setBackground(Color.WHITE);
            table.getTableHeader().setForeground(Color.BLACK);
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        }
    }
}

