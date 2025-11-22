package com.novafarma.ui.panels;

import com.novafarma.model.Product;
import com.novafarma.model.User;
import com.novafarma.service.ProductService;
import com.novafarma.ui.ProductExpirationRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Panel de Inventario - Muestra y gestiona productos
 * 
 * RESPONSABILIDADES:
 * - Mostrar tabla de productos activos
 * - Búsqueda en tiempo real (TableRowSorter)
 * - Renderizado de alertas visuales (colores por vencimiento)
 * - Botones de CRUD (delega acciones a Dashboard)
 * 
 * ARQUITECTURA:
 * - Usa ProductService para obtener datos (sin SQL directo)
 * - UI separada de la lógica de negocio
 * - Callbacks para acciones (addProduct, editProduct, deleteProduct)
 * 
 * @author Nova Farma Development Team
 * @version 2.0 (Refactorizado con Arquitectura en Capas)
 */
public class InventoryPanel extends JPanel {
    
    // Servicios
    private ProductService productService;
    
    // Usuario actual (para permisos)
    private User currentUser;
    
    // Componentes UI
    private JTable tableProducts;
    private DefaultTableModel modelProducts;
    private TableRowSorter<DefaultTableModel> sorterProducts;
    private JTextField txtSearchProducts;
    
    // Botones (controlados por permisos)
    private JButton btnAddProduct;
    private JButton btnEditProduct;
    private JButton btnDeleteProduct;
    
    // Callbacks para acciones (delegadas a Dashboard)
    private Runnable onAddProduct;
    private Runnable onEditProduct;
    private Runnable onDeleteProduct;
    private Runnable onRefresh;
    
    // ==================== CONSTRUCTOR ====================
    
    public InventoryPanel(User currentUser, ProductService productService) {
        this.currentUser = currentUser;
        this.productService = productService;
        
        initializeUI();
    }
    
    // ==================== INICIALIZACIÓN DE UI ====================
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel superior: Buscador y botones
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        
        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblSearch = new JLabel("🔍 Buscar:");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 13));
        
        txtSearchProducts = new JTextField(25);
        txtSearchProducts.setFont(new Font("Arial", Font.PLAIN, 13));
        txtSearchProducts.setToolTipText("Busca por nombre, descripción o cualquier campo");
        txtSearchProducts.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtrarInventario();
            }
        });
        
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearchProducts);
        topPanel.add(searchPanel, BorderLayout.WEST);
        
        // Panel de botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        
        btnAddProduct = new JButton("➕ Agregar Producto");
        applyButtonStyle(btnAddProduct);
        btnAddProduct.addActionListener(e -> {
            if (onAddProduct != null) onAddProduct.run();
        });
        
        btnEditProduct = new JButton("✏️ Editar Producto");
        applyButtonStyle(btnEditProduct);
        btnEditProduct.addActionListener(e -> {
            if (onEditProduct != null) onEditProduct.run();
        });
        
        btnDeleteProduct = new JButton("🗑️ Eliminar Producto");
        applyButtonStyle(btnDeleteProduct);
        btnDeleteProduct.addActionListener(e -> {
            if (onDeleteProduct != null) onDeleteProduct.run();
        });
        
        JButton btnRefresh = new JButton("🔄 Actualizar");
        applyButtonStyle(btnRefresh);
        btnRefresh.addActionListener(e -> {
            loadProductsData();
            if (onRefresh != null) onRefresh.run();
        });
        
        btnPanel.add(btnAddProduct);
        btnPanel.add(btnEditProduct);
        btnPanel.add(btnDeleteProduct);
        btnPanel.add(btnRefresh);
        
        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // Tabla de productos
        String[] columns = {"ID", "Nombre", "Descripción", "Precio", "Stock", "Fecha Vencimiento"};
        modelProducts = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableProducts = new JTable(modelProducts);
        applyTableStyle(tableProducts);
        
        // Renderer de alertas visuales (colores por vencimiento)
        ProductExpirationRenderer expirationRenderer = new ProductExpirationRenderer(5);
        for (int i = 0; i < tableProducts.getColumnCount(); i++) {
            tableProducts.getColumnModel().getColumn(i).setCellRenderer(expirationRenderer);
        }
        
        // TableRowSorter para búsqueda en tiempo real
        sorterProducts = new TableRowSorter<>(modelProducts);
        tableProducts.setRowSorter(sorterProducts);
        
        JScrollPane scrollPane = new JScrollPane(tableProducts);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    /**
     * Carga productos activos desde ProductService
     */
    public void loadProductsData() {
        try {
            modelProducts.setRowCount(0);
            
            List<Product> products = productService.getAllActiveProducts();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            
            for (Product product : products) {
                Object[] row = {
                    product.getId(),
                    product.getNombre(),
                    product.getDescripcion(),
                    String.format("$%.2f", product.getPrecio()),
                    product.getStock(),
                    product.getFechaVencimiento() != null ? 
                        dateFormat.format(product.getFechaVencimiento()) : "N/A"
                };
                modelProducts.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar productos: " + e.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Aplica permisos basados en el rol del usuario
     */
    public void applyRolePermissions() {
        if (currentUser.isTrabajador()) {
            // TRABAJADOR: Solo puede ver, no puede modificar
            btnAddProduct.setEnabled(false);
            btnEditProduct.setEnabled(false);
            btnDeleteProduct.setEnabled(false);
        } else {
            // ADMINISTRADOR: Acceso completo
            btnAddProduct.setEnabled(true);
            btnEditProduct.setEnabled(true);
            btnDeleteProduct.setEnabled(true);
        }
    }
    
    /**
     * Obtiene el producto seleccionado en la tabla
     */
    public int getSelectedProductRow() {
        int selectedRow = tableProducts.getSelectedRow();
        if (selectedRow != -1) {
            return tableProducts.convertRowIndexToModel(selectedRow);
        }
        return -1;
    }
    
    /**
     * Obtiene el ID del producto seleccionado
     */
    public Integer getSelectedProductId() {
        int row = getSelectedProductRow();
        if (row != -1) {
            return (Integer) modelProducts.getValueAt(row, 0);
        }
        return null;
    }
    
    /**
     * Obtiene el modelo de la tabla (para acceso desde Dashboard)
     */
    public DefaultTableModel getTableModel() {
        return modelProducts;
    }
    
    /**
     * Obtiene la tabla (para acceso desde Dashboard)
     */
    public JTable getTable() {
        return tableProducts;
    }
    
    // ==================== SETTERS PARA CALLBACKS ====================
    
    public void setOnAddProduct(Runnable callback) {
        this.onAddProduct = callback;
    }
    
    public void setOnEditProduct(Runnable callback) {
        this.onEditProduct = callback;
    }
    
    public void setOnDeleteProduct(Runnable callback) {
        this.onDeleteProduct = callback;
    }
    
    public void setOnRefresh(Runnable callback) {
        this.onRefresh = callback;
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    /**
     * Filtra el inventario según el texto del buscador
     */
    private void filtrarInventario() {
        String texto = txtSearchProducts.getText().trim();
        
        if (texto.isEmpty()) {
            sorterProducts.setRowFilter(null);
        } else {
            sorterProducts.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        }
    }
    
    /**
     * Aplica estilo estándar a los botones
     */
    private void applyButtonStyle(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
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

