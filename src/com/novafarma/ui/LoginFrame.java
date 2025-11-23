package com.novafarma.ui;

import com.novafarma.model.User;
import com.novafarma.model.User.UserRole;
import com.novafarma.util.DatabaseConnection;
import com.novafarma.util.SecurityHelper;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Ventana de Login para la aplicación Nova Farma
 * 
 * FUNCIONALIDADES IMPLEMENTADAS:
 * 1. Login con encriptación SHA-256
 * 2. Recuperación de contraseña
 * 3. Validación de usuario y rol
 * 4. Navegación al Dashboard según el rol
 * 
 * REQUISITOS CRÍTICOS CUMPLIDOS:
 * ✓ Contraseñas encriptadas con SHA-256
 * ✓ No se guardan contraseñas en texto plano
 * ✓ Flujo de recuperación de contraseña
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class LoginFrame extends JFrame {
    
    // ==================== COMPONENTES UI ====================
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnForgotPassword;
    private JLabel lblStatus;
    
    // ==================== CONSTRUCTOR ====================
    
    public LoginFrame() {
        initializeUI();
    }
    
    /**
     * Inicializa la interfaz de usuario
     */
    private void initializeUI() {
        // Configuración de la ventana
        setTitle("Nova Farma - Sistema de Gestión");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla
        setResizable(false);
        
        // Panel principal con gradiente (opcional, puedes simplificarlo)
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(41, 128, 185); // Azul profesional
                Color color2 = new Color(109, 213, 250); // Azul claro
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(null);
        
        // ==================== LOGO Y TÍTULO ====================
        
        JLabel lblLogo = new JLabel("🏥 NOVA FARMA", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Arial", Font.BOLD, 28));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setBounds(50, 20, 350, 40);
        mainPanel.add(lblLogo);
        
        JLabel lblSubtitle = new JLabel("Sistema de Gestión Farmacéutica", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(230, 230, 230));
        lblSubtitle.setBounds(50, 60, 350, 20);
        mainPanel.add(lblSubtitle);
        
        // ==================== PANEL DE LOGIN ====================
        
        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        loginPanel.setBounds(50, 100, 350, 220);
        loginPanel.setLayout(null);
        
        // Label: Usuario
        JLabel lblUsername = new JLabel("Usuario:");
        lblUsername.setFont(new Font("Arial", Font.BOLD, 14));
        lblUsername.setBounds(10, 10, 100, 25);
        loginPanel.add(lblUsername);
        
        // Campo de texto: Usuario
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUsername.setBounds(10, 35, 280, 35);
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        loginPanel.add(txtUsername);
        
        // Label: Contraseña
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 14));
        lblPassword.setBounds(10, 80, 100, 25);
        loginPanel.add(lblPassword);
        
        // Campo de contraseña
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setBounds(10, 105, 280, 35);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        loginPanel.add(txtPassword);
        
        // Permitir login con Enter
        txtPassword.addActionListener(e -> performLogin());
        
        // Botón: Iniciar Sesión
        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setBounds(10, 155, 280, 40);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBackground(new Color(46, 204, 113)); // Verde
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> performLogin());
        loginPanel.add(btnLogin);
        
        mainPanel.add(loginPanel);
        
        // ==================== BOTÓN OLVIDÉ CONTRASEÑA ====================
        
        btnForgotPassword = new JButton("¿Olvidaste tu contraseña?");
        btnForgotPassword.setBounds(100, 330, 250, 30);
        btnForgotPassword.setFont(new Font("Arial", Font.PLAIN, 12));
        btnForgotPassword.setForeground(Color.WHITE);
        btnForgotPassword.setContentAreaFilled(false);
        btnForgotPassword.setBorderPainted(false);
        btnForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgotPassword.addActionListener(e -> showPasswordRecovery());
        mainPanel.add(btnForgotPassword);
        
        // Label de estado (mensajes de error/éxito)
        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 12));
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setBounds(50, 85, 350, 15);
        mainPanel.add(lblStatus);
        
        add(mainPanel);
    }
    
    // ==================== MÉTODO DE LOGIN ====================
    
    /**
     * Realiza el proceso de autenticación
     * 
     * FLUJO DE LOGIN CON ENCRIPTACIÓN:
     * 1. Usuario ingresa username y password (texto plano)
     * 2. Java encripta el password con SHA-256
     * 3. Consulta a la BD: WHERE username = ? AND password_hash = ?
     * 4. Si existe coincidencia -> Login exitoso
     * 5. Abre el Dashboard con los permisos del rol del usuario
     */
    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // Validaciones básicas
        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Por favor, completa todos los campos", Color.RED);
            return;
        }
        
        try {
            // PASO CRÍTICO: Encriptar la contraseña con SHA-256
            String passwordHash = SecurityHelper.encryptPassword(password);
            
            // Consulta a la base de datos
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT id, username, password_hash, rol FROM usuarios WHERE username = ? AND password_hash = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, passwordHash); // Enviamos el HASH, no la contraseña plana
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // LOGIN EXITOSO
                int id = rs.getInt("id");
                String rolString = rs.getString("rol");
                UserRole rol = UserRole.fromString(rolString);
                
                User loggedUser = new User(id, username, passwordHash, rol);
                
                // Mostrar mensaje de éxito
                showStatus("¡Bienvenido, " + username + "!", new Color(46, 204, 113));
                
                // Cerrar la ventana de login después de 500ms
                Timer timer = new Timer(500, e -> {
                    openDashboard(loggedUser);
                    dispose();
                });
                timer.setRepeats(false);
                timer.start();
                
            } else {
                // LOGIN FALLIDO
                showStatus("Usuario o contraseña incorrectos", Color.RED);
                txtPassword.setText("");
                txtPassword.requestFocus();
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            showStatus("Error de conexión a la base de datos", Color.RED);
            e.printStackTrace();
        }
    }
    
    // ==================== RECUPERACIÓN DE CONTRASEÑA ====================
    
    /**
     * Muestra el diálogo de recuperación de contraseña
     * 
     * FLUJO DE RECUPERACIÓN:
     * 1. Usuario ingresa su nombre de usuario
     * 2. Sistema valida que el usuario existe
     * 3. Usuario ingresa nueva contraseña
     * 4. Sistema encripta la nueva contraseña con SHA-256
     * 5. Ejecuta UPDATE usuarios SET password_hash = ? WHERE username = ?
     */
    private void showPasswordRecovery() {
        // Paso 1: Solicitar nombre de usuario
        String username = JOptionPane.showInputDialog(
            this,
            "Ingresa tu nombre de usuario:",
            "Recuperación de Contraseña",
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (username == null || username.trim().isEmpty()) {
            return; // Usuario canceló
        }
        
        username = username.trim();
        
        try {
            // Paso 2: Verificar que el usuario existe
            Connection conn = DatabaseConnection.getConnection();
            String checkSql = "SELECT id FROM usuarios WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                JOptionPane.showMessageDialog(
                    this,
                    "El usuario '" + username + "' no existe en el sistema.",
                    "Usuario No Encontrado",
                    JOptionPane.ERROR_MESSAGE
                );
                rs.close();
                checkStmt.close();
                return;
            }
            
            rs.close();
            checkStmt.close();
            
            // Paso 3: Solicitar nueva contraseña
            JPasswordField newPasswordField = new JPasswordField();
            JPasswordField confirmPasswordField = new JPasswordField();
            
            Object[] message = {
                "Nueva contraseña:", newPasswordField,
                "Confirmar contraseña:", confirmPasswordField
            };
            
            int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Establecer Nueva Contraseña",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (option == JOptionPane.OK_OPTION) {
                String newPassword = new String(newPasswordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                
                // Validar que las contraseñas coincidan
                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Las contraseñas no coinciden. Intenta nuevamente.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                
                // Validar longitud mínima
                if (newPassword.length() < 4) {
                    JOptionPane.showMessageDialog(
                        this,
                        "La contraseña debe tener al menos 4 caracteres.",
                        "Contraseña Débil",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                
                // Paso 4: Encriptar la nueva contraseña con SHA-256
                String newPasswordHash = SecurityHelper.encryptPassword(newPassword);
                
                // Paso 5: Actualizar en la base de datos
                String updateSql = "UPDATE usuarios SET password_hash = ? WHERE username = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, newPasswordHash);
                updateStmt.setString(2, username);
                
                int rowsAffected = updateStmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(
                        this,
                        "¡Contraseña actualizada exitosamente!\nYa puedes iniciar sesión con tu nueva contraseña.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
                
                updateStmt.close();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al conectar con la base de datos.\n" + e.getMessage(),
                "Error de Conexión",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Muestra un mensaje de estado temporal
     */
    private void showStatus(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setForeground(color);
        
        // Limpiar el mensaje después de 3 segundos
        Timer timer = new Timer(3000, e -> lblStatus.setText(""));
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Abre el Dashboard según el rol del usuario
     */
    private void openDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            Dashboard dashboard = new Dashboard(user);
            dashboard.setVisible(true);
        });
    }
    
    // ==================== MÉTODO MAIN (PUNTO DE ENTRADA) ====================
    
    /**
     * Punto de entrada de la aplicación
     */
    public static void main(String[] args) {
        // Configurar el Look and Feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Ejecutar en el Event Dispatch Thread (EDT) de Swing
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

