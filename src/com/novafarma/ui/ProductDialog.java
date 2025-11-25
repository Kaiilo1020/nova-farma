package com.novafarma.ui;

import com.novafarma.model.Product;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Diálogo para crear o editar productos
 * 
 * Reemplaza los formularios con JOptionPane por un diálogo dedicado
 * con mejor UX y validación
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class ProductDialog extends JDialog {
    
    // Campos del formulario
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JTextField txtPrecio;
    private JTextField txtStock;
    private JTextField txtFechaVenc;
    
    // Botones
    private JButton btnOk;
    private JButton btnCancel;
    
    // Resultado
    private Product resultProduct;
    private boolean cancelled = true;
    
    /**
     * Constructor para crear un nuevo producto
     */
    public ProductDialog(Frame parent) {
        super(parent, "Agregar Producto", true);
        inicializarInterfaz(null);
    }
    
    /**
     * Constructor para editar un producto existente
     */
    public ProductDialog(Frame parent, Product product) {
        super(parent, "Editar Producto ID: " + product.getId(), true);
        inicializarInterfaz(product);
    }
    
    private void inicializarInterfaz(Product product) {
        setSize(500, 480);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
        
        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Título centrado
        JLabel lblTitle = new JLabel(product == null ? "Nuevo Producto" : "Editar Producto");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Campo: Nombre
        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setFont(new Font("Arial", Font.BOLD, 12));
        txtNombre = new JTextField(product != null ? product.getNombre() : "");
        txtNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        mainPanel.add(lblNombre);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtNombre);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Campo: Descripción
        JLabel lblDescripcion = new JLabel("Descripción:");
        lblDescripcion.setFont(new Font("Arial", Font.BOLD, 12));
        txtDescripcion = new JTextField(product != null ? product.getDescripcion() : "");
        txtDescripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        mainPanel.add(lblDescripcion);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtDescripcion);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Campo: Precio
        JLabel lblPrecio = new JLabel("Precio:");
        lblPrecio.setFont(new Font("Arial", Font.BOLD, 12));
        txtPrecio = new JTextField(product != null ? String.valueOf(product.getPrecio()) : "");
        txtPrecio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        mainPanel.add(lblPrecio);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtPrecio);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Campo: Stock
        JLabel lblStock = new JLabel("Stock:");
        lblStock.setFont(new Font("Arial", Font.BOLD, 12));
        txtStock = new JTextField(product != null ? String.valueOf(product.getStock()) : "");
        txtStock.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        mainPanel.add(lblStock);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtStock);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Campo: Fecha Vencimiento
        JLabel lblFechaVenc = new JLabel("Fecha Vencimiento (YYYY-MM-DD):");
        lblFechaVenc.setFont(new Font("Arial", Font.BOLD, 12));
        String fechaStr = "";
        if (product != null && product.getFechaVencimiento() != null) {
            fechaStr = product.getFechaVencimiento().toString();
        } else if (product == null) {
            fechaStr = ""; // Dejar vacío por defecto para nuevo producto
        }
        txtFechaVenc = new JTextField(fechaStr);
        txtFechaVenc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        txtFechaVenc.setToolTipText("Formato: YYYY-MM-DD (ejemplo: 2025-12-31). Dejar vacío si no tiene fecha de vencimiento.");
        
        mainPanel.add(lblFechaVenc);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtFechaVenc);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Agregar scroll por si el contenido es muy largo
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnOk = new JButton("Aceptar");
        btnOk.setFont(new Font("Arial", Font.PLAIN, 12));
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.addActionListener(e -> handleOk());
        
        btnCancel = new JButton("Cancelar");
        btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> handleCancel());
        
        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Enter en cualquier campo ejecuta OK
        getRootPane().setDefaultButton(btnOk);
        
        // ESC cierra el diálogo
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                handleCancel();
            }
        });
    }
    
    private void handleOk() {
        // Validar campos
        if (!validateFields()) {
            return;
        }
        
        try {
            String nombre = txtNombre.getText().trim();
            String descripcion = txtDescripcion.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());
            
            // Validar precio y stock
            if (precio <= 0) {
                JOptionPane.showMessageDialog(this,
                    "El precio debe ser mayor a 0",
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE);
                txtPrecio.requestFocus();
                return;
            }
            
            if (stock < 0) {
                JOptionPane.showMessageDialog(this,
                    "El stock no puede ser negativo",
                    "Error de Validación",
                    JOptionPane.ERROR_MESSAGE);
                txtStock.requestFocus();
                return;
            }
            
            // Manejar fecha de vencimiento
            Date fechaVenc = null;
            String fechaStr = txtFechaVenc.getText().trim();
            if (!fechaStr.isEmpty()) {
                try {
                    // Validar formato YYYY-MM-DD
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setLenient(false);
                    sdf.parse(fechaStr);
                    fechaVenc = Date.valueOf(fechaStr);
                } catch (ParseException | IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(this,
                        "Formato de fecha inválido. Use YYYY-MM-DD (ejemplo: 2025-12-31)",
                        "Error de Validación",
                        JOptionPane.ERROR_MESSAGE);
                    txtFechaVenc.requestFocus();
                    return;
                }
            }
            
            // Crear objeto Product
            if (resultProduct == null) {
                resultProduct = new Product(nombre, descripcion, precio, stock, fechaVenc);
            } else {
                resultProduct.setNombre(nombre);
                resultProduct.setDescripcion(descripcion);
                resultProduct.setPrecio(precio);
                resultProduct.setStock(stock);
                resultProduct.setFechaVencimiento(fechaVenc);
            }
            
            cancelled = false;
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Error: Precio y Stock deben ser números válidos",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleCancel() {
        cancelled = true;
        dispose();
    }
    
    private boolean validateFields() {
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El nombre es obligatorio",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
            txtNombre.requestFocus();
            return false;
        }
        
        if (txtPrecio.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El precio es obligatorio",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
            txtPrecio.requestFocus();
            return false;
        }
        
        if (txtStock.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El stock es obligatorio",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
            txtStock.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Muestra el diálogo y retorna el producto creado/editado
     * 
     * @return Product si el usuario hizo clic en Aceptar, null si canceló
     */
    public Product showDialog() {
        setVisible(true);
        return cancelled ? null : resultProduct;
    }
    
    /**
     * Método estático para crear un nuevo producto
     */
    public static Product mostrarDialogoCreacion(Frame parent) {
        ProductDialog dialog = new ProductDialog(parent);
        return dialog.showDialog();
    }
    
    /**
     * Método estático para editar un producto existente
     */
    public static Product mostrarDialogoEdicion(Frame parent, Product product) {
        ProductDialog dialog = new ProductDialog(parent, product);
        Product edited = dialog.showDialog();
        if (edited != null && product != null) {
            edited.setId(product.getId()); // Mantener el ID original
        }
        return edited;
    }
}

