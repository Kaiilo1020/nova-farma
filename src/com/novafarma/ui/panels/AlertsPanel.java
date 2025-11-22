package com.novafarma.ui.panels;

import com.novafarma.model.Product;
import com.novafarma.model.User;
import com.novafarma.service.ProductService;
import com.novafarma.ui.ProductExpirationRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Panel de Alertas de Vencimiento
 * 
 * RESPONSABILIDADES:
 * - Mostrar productos vencidos o próximos a vencer (≤ 30 días)
 * - Renderizado con colores (Rojo/Naranja/Verde)
 * - Botón para retirar productos vencidos (solo ADMINISTRADOR)
 * 
 * ARQUITECTURA:
 * - Usa ProductService.getExpiringSoonProducts()
 * - UI separada de lógica de negocio
 * - Callback para acción de retirar vencidos
 * 
 * @author Nova Farma Development Team
 * @version 2.0
 */
public class AlertsPanel extends JPanel {
    
    // Servicios
    private ProductService productService;
    
    // Usuario actual (para permisos)
    private User currentUser;
    
    // Componentes UI
    private JTable tableAlertas;
    private DefaultTableModel modelAlertas;
    private JButton btnEliminarVencidos;
    
    // Callback para acción de retirar vencidos
    private Runnable onEliminarVencidos;
    
    // ==================== CONSTRUCTOR ====================
    
    public AlertsPanel(User currentUser, ProductService productService) {
        this.currentUser = currentUser;
        this.productService = productService;
        
        initializeUI();
    }
    
    // ==================== INICIALIZACIÓN DE UI ====================
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel superior: Título y botones
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JLabel lblTitulo = new JLabel("⚠️ ALERTAS DE VENCIMIENTO");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Panel de botones
        JPanel btnPanelAlertas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        btnEliminarVencidos = new JButton("❌ Retirar Vencidos");
        btnEliminarVencidos.setFont(new Font("Arial", Font.PLAIN, 12));
        btnEliminarVencidos.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminarVencidos.setToolTipText("Desactiva productos vencidos (NO los elimina de la BD)");
        btnEliminarVencidos.addActionListener(e -> {
            if (onEliminarVencidos != null) onEliminarVencidos.run();
        });
        
        JButton btnRefreshAlertas = new JButton("🔄 Actualizar");
        btnRefreshAlertas.setFont(new Font("Arial", Font.PLAIN, 12));
        btnRefreshAlertas.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefreshAlertas.addActionListener(e -> cargarAlertas());
        
        btnPanelAlertas.add(btnEliminarVencidos);
        btnPanelAlertas.add(btnRefreshAlertas);
        
        topPanel.add(lblTitulo, BorderLayout.WEST);
        topPanel.add(btnPanelAlertas, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // Tabla de alertas
        String[] columnsAlertas = {"ID", "Producto", "Stock", "Fecha Venc.", "Días Restantes", "Estado"};
        modelAlertas = new DefaultTableModel(columnsAlertas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableAlertas = new JTable(modelAlertas);
        applyTableStyle(tableAlertas);
        
        // Renderer de alertas visuales
        ProductExpirationRenderer alertRenderer = new ProductExpirationRenderer(3);
        for (int i = 0; i < tableAlertas.getColumnCount(); i++) {
            tableAlertas.getColumnModel().getColumn(i).setCellRenderer(alertRenderer);
        }
        
        // Ajustar anchos de columnas
        tableAlertas.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableAlertas.getColumnModel().getColumn(1).setPreferredWidth(250);
        tableAlertas.getColumnModel().getColumn(2).setPreferredWidth(80);
        tableAlertas.getColumnModel().getColumn(3).setPreferredWidth(120);
        tableAlertas.getColumnModel().getColumn(4).setPreferredWidth(120);
        tableAlertas.getColumnModel().getColumn(5).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(tableAlertas);
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior: Información
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblInfo = new JLabel("🔴 Vencido  |  🟠 Vence en ≤ 30 días  |  Ambos roles pueden ver estas alertas");
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        infoPanel.add(lblInfo);
        
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    /**
     * Carga productos con alertas de vencimiento
     */
    public void cargarAlertas() {
        try {
            modelAlertas.setRowCount(0);
            
            List<Product> products = productService.getExpiringSoonProducts();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            
            for (Product product : products) {
                long diasRestantes = product.getDaysUntilExpiration();
                
                String estado;
                if (diasRestantes < 0) {
                    estado = "❌ VENCIDO (" + Math.abs(diasRestantes) + " días atrás)";
                } else if (diasRestantes == 0) {
                    estado = "⚠️ VENCE HOY";
                } else {
                    estado = "⚠️ Por vencer";
                }
                
                Object[] row = {
                    product.getId(),
                    product.getNombre(),
                    product.getStock(),
                    dateFormat.format(product.getFechaVencimiento()),
                    diasRestantes + " días",
                    estado
                };
                
                modelAlertas.addRow(row);
            }
            
            // Mostrar mensaje si no hay alertas
            if (products.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "✅ ¡Excelente!\n\n" +
                    "No hay productos vencidos ni próximos a vencer\n" +
                    "en los próximos 30 días.",
                    "Sin Alertas",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar alertas: " + e.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Aplica permisos basados en el rol del usuario
     */
    public void applyRolePermissions() {
        if (currentUser.isTrabajador()) {
            // TRABAJADOR: No puede retirar productos vencidos
            btnEliminarVencidos.setEnabled(false);
        } else {
            // ADMINISTRADOR: Puede retirar vencidos
            btnEliminarVencidos.setEnabled(true);
        }
    }
    
    /**
     * Obtiene la fila seleccionada en la tabla de alertas
     */
    public int getSelectedAlertRow() {
        int selectedRow = tableAlertas.getSelectedRow();
        if (selectedRow != -1) {
            return tableAlertas.convertRowIndexToModel(selectedRow);
        }
        return -1;
    }
    
    /**
     * Obtiene el ID del producto seleccionado en la tabla de alertas
     */
    public Integer getSelectedProductId() {
        int row = getSelectedAlertRow();
        if (row != -1) {
            return (Integer) modelAlertas.getValueAt(row, 0);
        }
        return null;
    }
    
    /**
     * Obtiene el modelo de la tabla
     */
    public DefaultTableModel getTableModel() {
        return modelAlertas;
    }
    
    // ==================== SETTER PARA CALLBACK ====================
    
    public void setOnEliminarVencidos(Runnable callback) {
        this.onEliminarVencidos = callback;
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

