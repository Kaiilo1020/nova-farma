package com.novafarma.ui.panels;

import com.novafarma.model.Product;
import com.novafarma.model.User;
import com.novafarma.service.ProductService;
import com.novafarma.ui.ProductExpirationRenderer;
import com.novafarma.util.Mensajes;
import com.novafarma.util.PaginationHelper;
import com.novafarma.util.TableStyleHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
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
    
    // Paginación
    private static final int PAGE_SIZE = PaginationHelper.DEFAULT_PAGE_SIZE;
    private static final int PAGINATION_THRESHOLD = 100; // Activar paginación si hay más de 100 registros
    private int currentPage = 1;
    private int totalRecords = 0;
    private boolean paginationEnabled = false;
    
    // Controles de paginación
    private JButton btnFirstPage;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    private JButton btnLastPage;
    private JLabel lblPageInfo;
    
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
        JLabel lblSearch = new JLabel("Buscar:");
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
        
        btnAddProduct = new JButton("Agregar Producto");
        applyButtonStyle(btnAddProduct);
        btnAddProduct.addActionListener(e -> {
            if (onAddProduct != null) onAddProduct.run();
        });
        
        btnEditProduct = new JButton("Editar Producto");
        applyButtonStyle(btnEditProduct);
        btnEditProduct.addActionListener(e -> {
            if (onEditProduct != null) onEditProduct.run();
        });
        
        btnDeleteProduct = new JButton("Eliminar Producto");
        applyButtonStyle(btnDeleteProduct);
        btnDeleteProduct.addActionListener(e -> {
            if (onDeleteProduct != null) onDeleteProduct.run();
        });
        
        JButton btnRefresh = new JButton("Actualizar");
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
        TableStyleHelper.applyTableStyle(tableProducts);
        
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
        
        // Panel de paginación (se mostrará solo si hay muchos registros)
        createPaginationPanel();
    }
    
    /**
     * Crea el panel de controles de paginación
     */
    private void createPaginationPanel() {
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        btnFirstPage = new JButton("<< Primera");
        applyButtonStyle(btnFirstPage);
        btnFirstPage.addActionListener(e -> goToFirstPage());
        
        btnPrevPage = new JButton("< Anterior");
        applyButtonStyle(btnPrevPage);
        btnPrevPage.addActionListener(e -> goToPreviousPage());
        
        lblPageInfo = new JLabel("Página 1 de 1");
        lblPageInfo.setFont(new Font("Arial", Font.PLAIN, 12));
        
        btnNextPage = new JButton("Siguiente >");
        applyButtonStyle(btnNextPage);
        btnNextPage.addActionListener(e -> goToNextPage());
        
        btnLastPage = new JButton("Última >>");
        applyButtonStyle(btnLastPage);
        btnLastPage.addActionListener(e -> goToLastPage());
        
        paginationPanel.add(btnFirstPage);
        paginationPanel.add(btnPrevPage);
        paginationPanel.add(lblPageInfo);
        paginationPanel.add(btnNextPage);
        paginationPanel.add(btnLastPage);
        
        // Inicialmente oculto (se mostrará si hay muchos registros)
        paginationPanel.setVisible(false);
        add(paginationPanel, BorderLayout.SOUTH);
    }
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    /**
     * Carga productos activos desde ProductService
     * OPTIMIZACIÓN: Usa paginación automática si hay más de PAGINATION_THRESHOLD registros
     */
    public void loadProductsData() {
        try {
            modelProducts.setRowCount(0);
            
            // Contar total de registros
            totalRecords = productService.countActiveProducts();
            
            // Decidir si usar paginación
            if (totalRecords > PAGINATION_THRESHOLD) {
                paginationEnabled = true;
                loadProductsDataPaginated();
            } else {
                paginationEnabled = false;
                loadProductsDataAll();
            }
            
            updatePaginationControls();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                Mensajes.ERROR_CARGAR + ": " + e.getMessage(),
                Mensajes.ERROR_BD,
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Carga todos los productos (sin paginación)
     */
    private void loadProductsDataAll() {
        try {
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
                Mensajes.ERROR_CARGAR + ": " + e.getMessage(),
                Mensajes.ERROR_BD,
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Carga productos con paginación
     */
    private void loadProductsDataPaginated() {
        try {
            int offset = PaginationHelper.calculateOffset(currentPage, PAGE_SIZE);
            List<Product> products = productService.getActiveProductsPaginated(PAGE_SIZE, offset);
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
                Mensajes.ERROR_CARGAR + ": " + e.getMessage(),
                Mensajes.ERROR_BD,
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Actualiza los controles de paginación
     */
    private void updatePaginationControls() {
        if (!paginationEnabled) {
            // Ocultar controles si no hay paginación
            JPanel paginationPanel = (JPanel) getComponent(getComponentCount() - 1);
            paginationPanel.setVisible(false);
            return;
        }
        
        // Mostrar controles
        JPanel paginationPanel = (JPanel) getComponent(getComponentCount() - 1);
        paginationPanel.setVisible(true);
        
        int totalPages = PaginationHelper.calculateTotalPages(totalRecords, PAGE_SIZE);
        currentPage = PaginationHelper.validatePageNumber(currentPage, totalPages);
        
        // Actualizar label
        String range = PaginationHelper.getDisplayRange(currentPage, PAGE_SIZE, totalRecords);
        lblPageInfo.setText(String.format("Página %d de %d (%s)", currentPage, totalPages, range));
        
        // Habilitar/deshabilitar botones
        btnFirstPage.setEnabled(currentPage > 1);
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);
        btnLastPage.setEnabled(currentPage < totalPages);
    }
    
    // Métodos de navegación de paginación
    private void goToFirstPage() {
        currentPage = 1;
        loadProductsDataPaginated();
        updatePaginationControls();
    }
    
    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadProductsDataPaginated();
            updatePaginationControls();
        }
    }
    
    private void goToNextPage() {
        int totalPages = PaginationHelper.calculateTotalPages(totalRecords, PAGE_SIZE);
        if (currentPage < totalPages) {
            currentPage++;
            loadProductsDataPaginated();
            updatePaginationControls();
        }
    }
    
    private void goToLastPage() {
        int totalPages = PaginationHelper.calculateTotalPages(totalRecords, PAGE_SIZE);
        currentPage = totalPages;
        loadProductsDataPaginated();
        updatePaginationControls();
    }
    
    /**
     * OPTIMIZACIÓN: Actualiza solo una fila específica en lugar de recargar toda la tabla
     * 
     * @param product Producto actualizado desde la base de datos
     */
    public void updateProductRow(Product product) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        // Buscar la fila por ID
        int rowCount = modelProducts.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int productId = (Integer) modelProducts.getValueAt(i, 0);
            if (productId == product.getId()) {
                // Actualizar solo esta fila
                modelProducts.setValueAt(product.getId(), i, 0);
                modelProducts.setValueAt(product.getNombre(), i, 1);
                modelProducts.setValueAt(product.getDescripcion(), i, 2);
                modelProducts.setValueAt(String.format("$%.2f", product.getPrecio()), i, 3);
                modelProducts.setValueAt(product.getStock(), i, 4);
                modelProducts.setValueAt(
                    product.getFechaVencimiento() != null ? 
                        dateFormat.format(product.getFechaVencimiento()) : "N/A",
                    i, 5
                );
                return; // Fila actualizada, salir
            }
        }
        
        // Si no se encontró la fila (producto reactivado), agregarlo
        // Esto puede pasar si el producto estaba inactivo y ahora se reactivó
        if (product.getStock() > 0) {
            addProductRow(product);
        }
    }
    
    /**
     * OPTIMIZACIÓN: Agrega solo una nueva fila en lugar de recargar toda la tabla
     * 
     * @param product Producto nuevo a agregar
     */
    public void addProductRow(Product product) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
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
    
    /**
     * OPTIMIZACIÓN: Elimina solo una fila específica en lugar de recargar toda la tabla
     * 
     * @param productId ID del producto a eliminar de la tabla
     */
    public void removeProductRow(int productId) {
        int rowCount = modelProducts.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int id = (Integer) modelProducts.getValueAt(i, 0);
            if (id == productId) {
                modelProducts.removeRow(i);
                return; // Fila eliminada, salir
            }
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
    
    private void applyButtonStyle(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}

