package com.novafarma.ui;

import com.novafarma.model.User;
import com.novafarma.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Panel de Facturación para Nova Farma
 * 
 * Permite crear y gestionar facturas para empresas/clientes
 * con interfaz profesional siguiendo el diseño del sistema.
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class FacturacionPanel extends JPanel {
    
    // ==================== COMPONENTES UI ====================
    
    // Campos de entrada
    private JTextField txtRuc;
    private JTextField txtEmpresa;
    private JTextField txtProducto;
    private JTextField txtUnidades;
    private JTextField txtUnitario;
    private JTextField txtPrecioTotal;
    
    // Tabla de facturas
    private JTable tableFacturas;
    private DefaultTableModel modelFacturas;
    
    // Botones
    private JButton btnLista;
    private JButton btnAdicionar;
    private JButton btnEliminarFactura;
    private JButton btnEliminarTodo;
    
    // Usuario actual
    private User currentUser;
    
    // Formateadores
    private DecimalFormat decimalFormat = new DecimalFormat("$#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    // ==================== CONSTRUCTOR ====================
    
    public FacturacionPanel(User user) {
        this.currentUser = user;
        initializeUI();
        cargarFacturas();
    }
    
    // ==================== INICIALIZACIÓN DE UI ====================
    
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
    
    // ==================== PANEL DE ENTRADA ====================
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        
        // RUC
        gbc.gridx = 0;
        JLabel lblRuc = new JLabel("RUC:");
        lblRuc.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblRuc, gbc);
        
        gbc.gridy = 1;
        txtRuc = new JTextField();
        txtRuc.setPreferredSize(new Dimension(120, 30));
        txtRuc.setFont(new Font("Arial", Font.PLAIN, 12));
        txtRuc.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panel.add(txtRuc, gbc);
        
        // Empresa
        gbc.gridx = 1;
        gbc.gridy = 0;
        JLabel lblEmpresa = new JLabel("Empresa:");
        lblEmpresa.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblEmpresa, gbc);
        
        gbc.gridy = 1;
        txtEmpresa = new JTextField();
        txtEmpresa.setPreferredSize(new Dimension(200, 30));
        txtEmpresa.setFont(new Font("Arial", Font.PLAIN, 12));
        txtEmpresa.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panel.add(txtEmpresa, gbc);
        
        // Producto
        gbc.gridx = 2;
        gbc.gridy = 0;
        JLabel lblProducto = new JLabel("Producto:");
        lblProducto.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblProducto, gbc);
        
        gbc.gridy = 1;
        txtProducto = new JTextField();
        txtProducto.setPreferredSize(new Dimension(150, 30));
        txtProducto.setFont(new Font("Arial", Font.PLAIN, 12));
        txtProducto.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panel.add(txtProducto, gbc);
        
        // Unidades
        gbc.gridx = 3;
        gbc.gridy = 0;
        JLabel lblUnidades = new JLabel("Unidades:");
        lblUnidades.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblUnidades, gbc);
        
        gbc.gridy = 1;
        txtUnidades = new JTextField("1");
        txtUnidades.setPreferredSize(new Dimension(80, 30));
        txtUnidades.setFont(new Font("Arial", Font.PLAIN, 12));
        txtUnidades.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        // Calcular precio total al cambiar unidades
        txtUnidades.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularPrecioTotal();
            }
        });
        panel.add(txtUnidades, gbc);
        
        // Precio Unitario
        gbc.gridx = 4;
        gbc.gridy = 0;
        JLabel lblUnitario = new JLabel("P. Unitario:");
        lblUnitario.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblUnitario, gbc);
        
        gbc.gridy = 1;
        txtUnitario = new JTextField();
        txtUnitario.setPreferredSize(new Dimension(100, 30));
        txtUnitario.setFont(new Font("Arial", Font.PLAIN, 12));
        txtUnitario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        // Calcular precio total al cambiar precio unitario
        txtUnitario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularPrecioTotal();
            }
        });
        panel.add(txtUnitario, gbc);
        
        // Precio Total (calculado automáticamente)
        gbc.gridx = 5;
        gbc.gridy = 0;
        JLabel lblPrecioTotal = new JLabel("Precio Total:");
        lblPrecioTotal.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblPrecioTotal, gbc);
        
        gbc.gridy = 1;
        txtPrecioTotal = new JTextField();
        txtPrecioTotal.setPreferredSize(new Dimension(100, 30));
        txtPrecioTotal.setFont(new Font("Arial", Font.BOLD, 12));
        txtPrecioTotal.setEditable(false);
        panel.add(txtPrecioTotal, gbc);
        
        return panel;
    }
    
    // ==================== PANEL DE TABLA ====================
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "📋 Facturas Registradas",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(52, 152, 219)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Columnas de la tabla
        String[] columns = {"ID", "RUC", "Empresa", "Producto", "Unidades", "P. Unitario", "P. Total", "Fecha"};
        modelFacturas = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No editable directamente
            }
        };
        
        tableFacturas = new JTable(modelFacturas);
        applyTableStyle(tableFacturas); // Aplicar estilo limpio y profesional
        tableFacturas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Ajustar anchos de columnas
        tableFacturas.getColumnModel().getColumn(0).setPreferredWidth(40);   // ID
        tableFacturas.getColumnModel().getColumn(1).setPreferredWidth(100);  // RUC
        tableFacturas.getColumnModel().getColumn(2).setPreferredWidth(200);  // Empresa
        tableFacturas.getColumnModel().getColumn(3).setPreferredWidth(200);  // Producto
        tableFacturas.getColumnModel().getColumn(4).setPreferredWidth(80);   // Unidades
        tableFacturas.getColumnModel().getColumn(5).setPreferredWidth(100);  // P. Unitario
        tableFacturas.getColumnModel().getColumn(6).setPreferredWidth(100);  // P. Total
        tableFacturas.getColumnModel().getColumn(7).setPreferredWidth(130);  // Fecha
        
        JScrollPane scrollPane = new JScrollPane(tableFacturas);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== PANEL DE BOTONES ====================
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        panel.setPreferredSize(new Dimension(160, 0));
        
        // Botón Lista
        btnLista = createStyledButton("🗂️ Lista", new Color(52, 152, 219));
        btnLista.addActionListener(e -> cargarFacturas());
        panel.add(btnLista);
        
        panel.add(Box.createVerticalStrut(15));
        
        // Botón Adicionar
        btnAdicionar = createStyledButton("➕ Adicionar", new Color(46, 204, 113));
        btnAdicionar.addActionListener(e -> adicionarFactura());
        panel.add(btnAdicionar);
        
        panel.add(Box.createVerticalStrut(15));
        
        // Botón Eliminar Factura
        btnEliminarFactura = createStyledButton("🗑️ Eliminar", new Color(243, 156, 18));
        btnEliminarFactura.addActionListener(e -> eliminarFactura());
        panel.add(btnEliminarFactura);
        
        panel.add(Box.createVerticalStrut(15));
        
        // Botón Eliminar Todo
        btnEliminarTodo = createStyledButton("🧹 Limpiar Todo", new Color(231, 76, 60));
        btnEliminarTodo.addActionListener(e -> eliminarTodo());
        panel.add(btnEliminarTodo);
        
        return panel;
    }
    
    // ==================== MÉTODO AUXILIAR PARA BOTONES ====================
    
    /**
     * Crea un botón con estilo estándar de Swing (sin personalizaciones)
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setMaximumSize(new Dimension(140, 40));
        button.setPreferredSize(new Dimension(140, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }
    
    // ==================== MÉTODOS DE LÓGICA ====================
    
    /**
     * Calcula automáticamente el precio total basado en unidades y precio unitario
     */
    private void calcularPrecioTotal() {
        try {
            String unidadesStr = txtUnidades.getText().trim();
            String unitarioStr = txtUnitario.getText().trim();
            
            if (!unidadesStr.isEmpty() && !unitarioStr.isEmpty()) {
                int unidades = Integer.parseInt(unidadesStr);
                double unitario = Double.parseDouble(unitarioStr);
                double total = unidades * unitario;
                
                txtPrecioTotal.setText(decimalFormat.format(total));
            } else {
                txtPrecioTotal.setText("");
            }
        } catch (NumberFormatException e) {
            txtPrecioTotal.setText("Error");
        }
    }
    
    /**
     * Carga todas las facturas desde la base de datos
     */
    private void cargarFacturas() {
        try {
            modelFacturas.setRowCount(0); // Limpiar tabla
            
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT id, ruc, empresa, producto, unidades, precio_unitario, precio_total, fecha_factura " +
                        "FROM facturas ORDER BY fecha_factura DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("ruc"),
                    rs.getString("empresa"),
                    rs.getString("producto"),
                    rs.getInt("unidades"),
                    decimalFormat.format(rs.getDouble("precio_unitario")),
                    decimalFormat.format(rs.getDouble("precio_total")),
                    dateFormat.format(rs.getTimestamp("fecha_factura"))
                };
                modelFacturas.addRow(row);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar facturas: " + e.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Adiciona una nueva factura
     * 
     * VALIDACIONES:
     * 1. Todos los campos deben estar llenos
     * 2. RUC debe tener 8-11 dígitos
     * 3. Unidades y precios deben ser números válidos
     * 4. Precio total se calcula automáticamente
     */
    private void adicionarFactura() {
        // Obtener valores
        String ruc = txtRuc.getText().trim();
        String empresa = txtEmpresa.getText().trim();
        String producto = txtProducto.getText().trim();
        String unidadesStr = txtUnidades.getText().trim();
        String unitarioStr = txtUnitario.getText().trim();
        
        // Validar campos vacíos
        if (ruc.isEmpty() || empresa.isEmpty() || producto.isEmpty() || 
            unidadesStr.isEmpty() || unitarioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Por favor, completa todos los campos",
                "Campos Incompletos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validar RUC (solo números, 8-11 dígitos)
        if (!ruc.matches("\\d{8,11}")) {
            JOptionPane.showMessageDialog(this,
                "El RUC debe contener entre 8 y 11 dígitos numéricos",
                "RUC Inválido",
                JOptionPane.ERROR_MESSAGE);
            txtRuc.requestFocus();
            return;
        }
        
        // Validar empresa (máximo 100 caracteres)
        if (empresa.length() > 100) {
            JOptionPane.showMessageDialog(this,
                "El nombre de la empresa no puede exceder 100 caracteres",
                "Empresa Inválida",
                JOptionPane.ERROR_MESSAGE);
            txtEmpresa.requestFocus();
            return;
        }
        
        try {
            // Validar y parsear números
            int unidades = Integer.parseInt(unidadesStr);
            double unitario = Double.parseDouble(unitarioStr);
            
            if (unidades <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Las unidades deben ser mayor a 0",
                    "Unidades Inválidas",
                    JOptionPane.ERROR_MESSAGE);
                txtUnidades.requestFocus();
                return;
            }
            
            if (unitario <= 0) {
                JOptionPane.showMessageDialog(this,
                    "El precio unitario debe ser mayor a 0",
                    "Precio Inválido",
                    JOptionPane.ERROR_MESSAGE);
                txtUnitario.requestFocus();
                return;
            }
            
            // Calcular precio total
            double precioTotal = unidades * unitario;
            
            // Insertar en la base de datos
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO facturas (ruc, empresa, producto, unidades, precio_unitario, precio_total, usuario_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, ruc);
            stmt.setString(2, empresa);
            stmt.setString(3, producto);
            stmt.setInt(4, unidades);
            stmt.setDouble(5, unitario);
            stmt.setDouble(6, precioTotal);
            stmt.setInt(7, currentUser.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "✅ Factura agregada exitosamente\n\n" +
                    "RUC: " + ruc + "\n" +
                    "Empresa: " + empresa + "\n" +
                    "Total: " + decimalFormat.format(precioTotal),
                    "Factura Registrada",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Limpiar campos
                limpiarCampos();
                
                // Recargar tabla
                cargarFacturas();
            }
            
            stmt.close();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Por favor, ingresa valores numéricos válidos en Unidades y Precio Unitario",
                "Formato Inválido",
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al guardar la factura:\n" + e.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Elimina la factura seleccionada
     */
    private void eliminarFactura() {
        int selectedRow = tableFacturas.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, selecciona una factura de la tabla",
                "Factura No Seleccionada",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int facturaId = (int) modelFacturas.getValueAt(selectedRow, 0);
        String empresa = (String) modelFacturas.getValueAt(selectedRow, 2);
        String total = (String) modelFacturas.getValueAt(selectedRow, 6);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Estás seguro de eliminar esta factura?\n\n" +
            "Empresa: " + empresa + "\n" +
            "Total: " + total,
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                String sql = "DELETE FROM facturas WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, facturaId);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Factura eliminada exitosamente",
                        "Eliminación Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    cargarFacturas();
                }
                
                stmt.close();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar la factura:\n" + e.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Limpia toda la tabla (solo visual, no elimina de BD)
     * Si se desea eliminar de BD también, descomentar el código
     */
    private void eliminarTodo() {
        if (modelFacturas.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "La tabla ya está vacía",
                "Información",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Deseas limpiar toda la tabla?\n" +
            "Esto solo limpiará la vista actual.\n" +
            "Los datos seguirán en la base de datos.",
            "Confirmar Limpieza",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            modelFacturas.setRowCount(0);
            JOptionPane.showMessageDialog(this,
                "Tabla limpiada. Haz clic en 'Lista' para recargar.",
                "Tabla Limpiada",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        /* 
        // DESCOMENTAR ESTO SI SE DESEA ELIMINAR DE LA BASE DE DATOS TAMBIÉN
        
        int confirmDelete = JOptionPane.showConfirmDialog(this,
            "⚠️ ADVERTENCIA ⚠️\n\n" +
            "¿Deseas ELIMINAR PERMANENTEMENTE todas las facturas?\n" +
            "Esta acción NO se puede deshacer.",
            "Eliminar Todo",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmDelete == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                String sql = "DELETE FROM facturas";
                Statement stmt = conn.createStatement();
                int rowsAffected = stmt.executeUpdate(sql);
                
                JOptionPane.showMessageDialog(this,
                    rowsAffected + " facturas eliminadas permanentemente",
                    "Eliminación Completa",
                    JOptionPane.INFORMATION_MESSAGE);
                
                modelFacturas.setRowCount(0);
                stmt.close();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        */
    }
    
    /**
     * Limpia todos los campos de entrada
     */
    private void limpiarCampos() {
        txtRuc.setText("");
        txtEmpresa.setText("");
        txtProducto.setText("");
        txtUnidades.setText("1");
        txtUnitario.setText("");
        txtPrecioTotal.setText("");
        txtRuc.requestFocus();
    }
    
    // ==================== MÉTODO AUXILIAR ====================
    
    /**
     * Aplica un estilo limpio y profesional a las tablas JTable.
     * 
     * ESPECIFICACIONES:
     * - Encabezado: Fondo blanco, texto negro en negrita, sin borde 3D
     * - Cuerpo: Fuente Arial 12pt, altura de fila 28px, grid gris suave
     * - Texto: Negro para buena legibilidad
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

