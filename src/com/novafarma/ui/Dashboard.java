package com.novafarma.ui;

import com.novafarma.model.User;
import com.novafarma.service.ProductService;
import com.novafarma.service.SaleService;
import com.novafarma.service.UserService;
import com.novafarma.ui.panels.InventoryPanel;
import com.novafarma.ui.panels.AlertsPanel;
import com.novafarma.ui.panels.SalesPanel;
import com.novafarma.ui.handlers.ProductHandler;
import com.novafarma.ui.handlers.UserHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

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
    
    // Paneles modulares
    private InventoryPanel inventoryPanel;
    private AlertsPanel alertsPanel;
    private SalesPanel salesPanel;
    
    // Handlers para separar lógica
    private ProductHandler productHandler;
    private UserHandler userHandler;
    
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
        
        // Inicializar paneles y handlers
        initializePanels();
        initializeHandlers();
        
        initializeUI();
        applyRolePermissions();
        
        // Cargar datos iniciales
        inventoryPanel.loadProductsData();
        salesPanel.cargarCatalogo();
        alertsPanel.cargarAlertas();
    }
    
    /**
     * Inicializa los paneles modulares y configura callbacks
     */
    private void initializePanels() {
        inventoryPanel = new InventoryPanel(currentUser, productService);
        alertsPanel = new AlertsPanel(currentUser, productService);
        salesPanel = new SalesPanel(currentUser, productService, saleService);
    }
    
    private void initializeHandlers() {
        productHandler = new ProductHandler(this, currentUser, productService, inventoryPanel);
        productHandler.setAlertsPanel(alertsPanel);
        productHandler.setSalesPanel(salesPanel);
        
        userHandler = new UserHandler(this, currentUser, userService);
        
        inventoryPanel.setOnAddProduct(() -> productHandler.agregar());
        inventoryPanel.setOnEditProduct(() -> productHandler.editar());
        inventoryPanel.setOnDeleteProduct(() -> productHandler.eliminar());
        inventoryPanel.setOnRefresh(() -> inventoryPanel.loadProductsData());
        
        alertsPanel.setOnEliminarVencidos(() -> productHandler.eliminarVencidos());
        
        salesPanel.setOnVentaFinalizada(() -> inventoryPanel.loadProductsData());
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
        
        tabbedPane.addTab("Inventario", inventoryPanel);
        tabbedPane.addTab("Ventas / Facturación", salesPanel);
        
        if (currentUser.isAdministrador()) {
            JPanel usersPanel = createUsersPanel();
            tabbedPane.addTab("Usuarios", usersPanel);
        }
        
        tabbedPane.addTab("Alertas", alertsPanel);
        
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
    
    
    // ==================== PANEL DE USUARIOS ====================
    
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel de botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        JButton btnCreateUser = new JButton("Crear Usuario");
        styleButton(btnCreateUser);
        btnCreateUser.addActionListener(e -> userHandler.crear());
        
        JButton btnDeleteUser = new JButton("Eliminar Usuario");
        styleButton(btnDeleteUser);
        btnDeleteUser.addActionListener(e -> userHandler.eliminar());
        
        JButton btnRefreshUsers = new JButton("Actualizar");
        styleButton(btnRefreshUsers);
        btnRefreshUsers.addActionListener(e -> userHandler.cargarDatos());
        
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
        
        // Panel inferior: Paginación e información
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        
        // Panel de paginación
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        JButton btnFirstPage = new JButton("<< Primera");
        styleButton(btnFirstPage);
        
        JButton btnPrevPage = new JButton("< Anterior");
        styleButton(btnPrevPage);
        
        JLabel lblPageInfo = new JLabel("Cargando...");
        lblPageInfo.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JButton btnNextPage = new JButton("Siguiente >");
        styleButton(btnNextPage);
        
        JButton btnLastPage = new JButton("Última >>");
        styleButton(btnLastPage);
        
        paginationPanel.add(btnFirstPage);
        paginationPanel.add(btnPrevPage);
        paginationPanel.add(lblPageInfo);
        paginationPanel.add(btnNextPage);
        paginationPanel.add(btnLastPage);
        
        bottomPanel.add(paginationPanel, BorderLayout.NORTH);
        
        // Información
        JLabel lblInfo = new JLabel(
            "<html><body style='padding: 10px;'>" +
            "<b>Nota:</b> Solo se pueden eliminar usuarios que NO tengan ventas registradas.<br>" +
            "Si un trabajador tiene ventas, se conservan para el historial del negocio." +
            "</body></html>"
        );
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        bottomPanel.add(lblInfo, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Pasar la tabla y controles de paginación al handler
        userHandler.setTable(usersTable, usersTableModel);
        userHandler.setPaginationControls(btnFirstPage, btnPrevPage, btnNextPage, btnLastPage, lblPageInfo);
        
        // Cargar datos iniciales
        userHandler.cargarDatos();
        
        return panel;
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
     * Aplica permisos basados en el rol del usuario
     * Delega a los paneles modulares
     */
    private void applyRolePermissions() {
        // Aplicar permisos a paneles modulares
        inventoryPanel.applyRolePermissions();
        alertsPanel.applyRolePermissions();
        
        // Nota: El módulo de ventas (POS) está disponible para todos los roles
        // Los trabajadores pueden vender, pero no pueden modificar productos
    }
    
    // ==================== MÉTODOS DE ACCIÓN ====================
    
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
    
}

