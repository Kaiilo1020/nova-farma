package com.novafarma.ui;

import com.novafarma.util.DatabaseConnection;
import com.novafarma.util.SecurityHelper;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Diálogo para crear nuevos usuarios
 * 
 * REQUISITO CUMPLIDO:
 * - Solo accesible por ADMINISTRADORES
 * - Encripta la contraseña con SHA-256 antes de guardar
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class UserCreationDialog extends JDialog {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JComboBox<String> comboRole;
    private JButton btnCreate;
    private JButton btnCancel;
    
    public UserCreationDialog(Frame parent) {
        super(parent, "Crear Nuevo Usuario", true);
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(400, 350);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
        
        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Título
        JLabel lblTitle = new JLabel("Crear Nuevo Usuario");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Campo: Username
        JLabel lblUsername = new JLabel("Nombre de Usuario:");
        txtUsername = new JTextField();
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        mainPanel.add(lblUsername);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtUsername);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Campo: Contraseña
        JLabel lblPassword = new JLabel("Contraseña:");
        txtPassword = new JPasswordField();
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        mainPanel.add(lblPassword);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtPassword);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Campo: Confirmar Contraseña
        JLabel lblConfirmPassword = new JLabel("Confirmar Contraseña:");
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        mainPanel.add(lblConfirmPassword);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(txtConfirmPassword);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Campo: Rol
        JLabel lblRole = new JLabel("Rol:");
        comboRole = new JComboBox<>(new String[]{"ADMINISTRADOR", "TRABAJADOR"});
        comboRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        mainPanel.add(lblRole);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(comboRole);
        mainPanel.add(Box.createVerticalStrut(20));
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnCreate = new JButton("Crear Usuario");
        btnCreate.addActionListener(e -> createUser());
        
        btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnCreate);
        buttonPanel.add(btnCancel);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Crea un nuevo usuario en la base de datos
     * 
     * FLUJO:
     * 1. Validar campos
     * 2. Encriptar contraseña con SHA-256
     * 3. Insertar en la tabla usuarios
     */
    private void createUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        String rolString = (String) comboRole.getSelectedItem();
        
        // Validaciones
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El nombre de usuario no puede estar vacío",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "La contraseña no puede estar vacía",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this,
                "La contraseña debe tener al menos 4 caracteres",
                "Contraseña Débil",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "Las contraseñas no coinciden",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // PASO CRÍTICO: Encriptar la contraseña con SHA-256
            String passwordHash = SecurityHelper.encryptPassword(password);
            
            // Insertar en la base de datos
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO usuarios (username, password_hash, rol) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, passwordHash); // Guardamos el HASH, no la contraseña plana
            stmt.setString(3, rolString);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                    "Usuario '" + username + "' creado exitosamente\n" +
                    "Rol: " + rolString,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                
                stmt.close();
                dispose();
            }
            
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key") || e.getMessage().contains("unique constraint")) {
                JOptionPane.showMessageDialog(this,
                    "El nombre de usuario '" + username + "' ya existe.\n" +
                    "Por favor, elige otro nombre de usuario.",
                    "Usuario Duplicado",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al crear usuario:\n" + e.getMessage(),
                    "Error de Base de Datos",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}

