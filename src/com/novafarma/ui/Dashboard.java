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
import java.sql.SQLException;
import java.util.List;

/** Dashboard principal con control de roles (Administrador/Trabajador) */
public class Dashboard extends JFrame {
    
    private User currentUser;
    private ProductService productService;
    private SaleService saleService;
    private UserService userService;
    private InventoryPanel inventoryPanel;
    private AlertsPanel alertsPanel;
    private SalesPanel salesPanel;
    private ProductHandler productHandler;
    private UserHandler userHandler;
    private JLabel lblWelcome;
    private JLabel lblRole;
    private JTabbedPane tabbedPane;
    
    public Dashboard(User user) {
        this.currentUser = user;
        this.productService = new ProductService();
        this.saleService = new SaleService();
        this.userService = new UserService();
        
        inicializarPaneles();
        inicializarManejadores();
        
        inicializarInterfaz();
        aplicarPermisosPorRol();
        
        inventoryPanel.cargarProductos();
        salesPanel.cargarCatalogo();
        alertsPanel.cargarAlertas();
    }
    
    private void inicializarPaneles() {
        inventoryPanel = new InventoryPanel(currentUser, productService);
        alertsPanel = new AlertsPanel(currentUser, productService);
        salesPanel = new SalesPanel(currentUser, productService, saleService);
    }
    
    private void inicializarManejadores() {
        productHandler = new ProductHandler(this, currentUser, productService, inventoryPanel);
        productHandler.setAlertsPanel(alertsPanel);
        productHandler.setSalesPanel(salesPanel);
        
        userHandler = new UserHandler(this, currentUser, userService);
        
        inventoryPanel.setAccionAgregarProducto(() -> productHandler.agregar());
        inventoryPanel.setAccionEditarProducto(() -> productHandler.editar());
        inventoryPanel.setAccionEliminarProducto(() -> productHandler.eliminar());
        inventoryPanel.setAccionRefrescar(() -> inventoryPanel.cargarProductos());
        
        alertsPanel.setAccionEliminarVencidos(() -> productHandler.eliminarVencidos());
        
        salesPanel.setOnVentaFinalizada(() -> inventoryPanel.cargarProductos());
    }
    
    private void inicializarInterfaz() {
        setTitle("Nova Farma - Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel headerPanel = crearPanelEncabezado();
        add(headerPanel, BorderLayout.NORTH);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        tabbedPane.addTab("Inventario", inventoryPanel);
        tabbedPane.addTab("Ventas / Facturación", salesPanel);
        
        if (currentUser.isAdministrador()) {
            JPanel usersPanel = crearPanelUsuarios();
            tabbedPane.addTab("Usuarios", usersPanel);
            
            JPanel salesHistoryPanel = crearPanelHistorialVentas();
            tabbedPane.addTab("Historial de Ventas", salesHistoryPanel);
        }
        
        tabbedPane.addTab("Alertas", alertsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        JPanel footerPanel = crearPanelPie();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(1000, 80));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
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
        
        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setFont(new Font("Arial", Font.PLAIN, 12));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> cerrarSesion());
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(btnLogout, BorderLayout.EAST);
        
        return panel;
    }
    
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    
private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
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
        
        String[] columnNames = {"ID", "Usuario", "Rol", "Ventas Registradas"};
        usersTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        usersTable = new JTable(usersTableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.setRowHeight(25);
        usersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        usersTable.setFont(new Font("Arial", Font.PLAIN, 12));
        usersTable.setFillsViewportHeight(true);
        
        usersTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        usersTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        usersTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        usersTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Usuarios"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
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
        
        userHandler.setTable(usersTable, usersTableModel);
        userHandler.setPaginationControls(btnFirstPage, btnPrevPage, btnNextPage, btnLastPage, lblPageInfo);
        userHandler.cargarDatos();
        
        return panel;
    }
    
    private JPanel crearPanelHistorialVentas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        JButton btnRefresh = new JButton("Actualizar");
        styleButton(btnRefresh);
        btnRefresh.addActionListener(e -> cargarHistorialVentas());
        
        btnPanel.add(btnRefresh);
        panel.add(btnPanel, BorderLayout.NORTH);
        
        String[] columnNames = {"ID", "Producto ID", "Usuario ID", "Cantidad", "Precio Unit.", "Total", "Fecha Venta"};
        DefaultTableModel         salesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable salesTable = new JTable(salesTableModel);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        salesTable.setRowHeight(25);
        salesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        salesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        salesTable.setFillsViewportHeight(true);
        
        salesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        salesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        salesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        salesTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        salesTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        salesTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        salesTable.getColumnModel().getColumn(6).setPreferredWidth(180);
        
        com.novafarma.util.TableStyleHelper.applyTableStyle(salesTable);
        
        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Historial de Ventas"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JLabel lblInfo = new JLabel(
            "<html><body style='padding: 10px;'>" +
            "<b>Nota:</b> Este historial muestra todas las ventas realizadas en el negocio.<br>" +
            "Las ventas se ordenan por fecha (más recientes primero)." +
            "</body></html>"
        );
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panel.add(lblInfo, BorderLayout.SOUTH);
        
        panel.putClientProperty("salesTable", salesTable);
        panel.putClientProperty("salesTableModel", salesTableModel);
        
        cargarHistorialVentas(panel);
        
        return panel;
    }
    
    private void cargarHistorialVentas() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component comp = tabbedPane.getComponentAt(i);
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                JTable table = (JTable) panel.getClientProperty("salesTable");
                if (table != null) {
                    cargarHistorialVentas(panel);
                    break;
                }
            }
        }
    }
    
    private void cargarHistorialVentas(JPanel panel) {
        JTable salesTable = (JTable) panel.getClientProperty("salesTable");
        DefaultTableModel salesTableModel = (DefaultTableModel) panel.getClientProperty("salesTableModel");
        
        if (salesTable == null || salesTableModel == null) return;
        
        try {
            salesTableModel.setRowCount(0);
            
            List<com.novafarma.model.Sale> ventas = saleService.obtenerTodasLasVentas();
            
            for (com.novafarma.model.Sale venta : ventas) {
                Object[] fila = {
                    venta.getId(),
                    venta.getProductoId(),
                    venta.getUsuarioId(),
                    venta.getCantidad(),
                    String.format("S/%.2f", venta.getPrecioUnitario()),
                    String.format("S/%.2f", venta.getTotal()),
                    formatFecha(venta.getFechaVenta())
                };
                salesTableModel.addRow(fila);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar el historial de ventas:\n" + e.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String formatFecha(java.sql.Timestamp fecha) {
        if (fecha == null) return "N/A";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(fecha);
    }
    
    private JPanel crearPanelPie() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(1000, 30));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        
        JLabel lblFooter = new JLabel("Nova Farma © 2024 - Sistema de Gestión Farmacéutica");
        lblFooter.setFont(new Font("Arial", Font.PLAIN, 10));
        
        panel.add(lblFooter);
        
        return panel;
    }
    
    private void aplicarPermisosPorRol() {
        inventoryPanel.aplicarPermisosPorRol();
        alertsPanel.aplicarPermisosPorRol();
    }
    
    private void cerrarSesion() {
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
    
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
}

