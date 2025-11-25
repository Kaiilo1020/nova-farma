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

/** Ventana de Login con autenticación SHA-256 y recuperación de contraseña */
public class LoginFrame extends JFrame {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnForgotPassword;
    private JLabel lblStatus;
    
    public LoginFrame() {
        inicializarInterfaz();
    }
    
    private void inicializarInterfaz() {
        setTitle("Nova Farma - Sistema de Gestión");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(41, 128, 185);
                Color color2 = new Color(109, 213, 250);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(null);
        
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
        
        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        loginPanel.setBounds(50, 100, 350, 220);
        loginPanel.setLayout(null);
        
        JLabel lblUsername = new JLabel("Usuario:");
        lblUsername.setFont(new Font("Arial", Font.BOLD, 14));
        lblUsername.setBounds(10, 10, 100, 25);
        loginPanel.add(lblUsername);
        
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUsername.setBounds(10, 35, 280, 35);
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        loginPanel.add(txtUsername);
        
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 14));
        lblPassword.setBounds(10, 80, 100, 25);
        loginPanel.add(lblPassword);
        
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setBounds(10, 105, 280, 35);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        loginPanel.add(txtPassword);
        
        txtPassword.addActionListener(e -> realizarLogin());
        
        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setBounds(10, 155, 280, 40);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBackground(new Color(46, 204, 113));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> realizarLogin());
        loginPanel.add(btnLogin);
        
        mainPanel.add(loginPanel);
        
        btnForgotPassword = new JButton("¿Olvidaste tu contraseña?");
        btnForgotPassword.setBounds(100, 330, 250, 30);
        btnForgotPassword.setFont(new Font("Arial", Font.PLAIN, 12));
        btnForgotPassword.setForeground(Color.WHITE);
        btnForgotPassword.setContentAreaFilled(false);
        btnForgotPassword.setBorderPainted(false);
        btnForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgotPassword.addActionListener(e -> mostrarRecuperacionContrasena());
        mainPanel.add(btnForgotPassword);
        
        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 12));
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setBounds(50, 85, 350, 15);
        mainPanel.add(lblStatus);
        
        add(mainPanel);
    }
    
    private void realizarLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            mostrarEstado("Por favor, completa todos los campos", Color.RED);
            return;
        }
        
        try {
            String passwordHash = SecurityHelper.encryptPassword(password);
            Connection conexion = DatabaseConnection.getConnection();
            String consultaSQL = "SELECT id, username, password_hash, rol FROM usuarios WHERE username = ? AND password_hash = ?";
            
            PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL);
            consultaPreparada.setString(1, username);
            consultaPreparada.setString(2, passwordHash);
            
            ResultSet resultadoConsulta = consultaPreparada.executeQuery();
            
            if (resultadoConsulta.next()) {
                int id = resultadoConsulta.getInt("id");
                String rolString = resultadoConsulta.getString("rol");
                UserRole rol = UserRole.fromString(rolString);
                
                User usuarioLogueado = new User(id, username, passwordHash, rol);
                mostrarEstado("¡Bienvenido, " + username + "!", new Color(46, 204, 113));
                
                Timer temporizador = new Timer(500, e -> {
                    abrirDashboard(usuarioLogueado);
                    dispose();
                });
                temporizador.setRepeats(false);
                temporizador.start();
                
            } else {
                mostrarEstado("Usuario o contraseña incorrectos", Color.RED);
                txtPassword.setText("");
                txtPassword.requestFocus();
            }
            
            resultadoConsulta.close();
            consultaPreparada.close();
            
        } catch (SQLException e) {
            mostrarEstado("Error de conexión a la base de datos", Color.RED);
            e.printStackTrace();
        }
    }
    
    private void mostrarRecuperacionContrasena() {
        String username = JOptionPane.showInputDialog(
            this,
            "Ingresa tu nombre de usuario:",
            "Recuperación de Contraseña",
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        username = username.trim();
        
        try {
            Connection conexion = DatabaseConnection.getConnection();
            String consultaVerificacion = "SELECT id FROM usuarios WHERE username = ?";
            PreparedStatement consultaVerificar = conexion.prepareStatement(consultaVerificacion);
            consultaVerificar.setString(1, username);
            ResultSet resultadoVerificacion = consultaVerificar.executeQuery();
            
            if (!resultadoVerificacion.next()) {
                JOptionPane.showMessageDialog(
                    this,
                    "El usuario '" + username + "' no existe en el sistema.",
                    "Usuario No Encontrado",
                    JOptionPane.ERROR_MESSAGE
                );
                resultadoVerificacion.close();
                consultaVerificar.close();
                return;
            }
            
            resultadoVerificacion.close();
            consultaVerificar.close();
            
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
                
                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Las contraseñas no coinciden. Intenta nuevamente.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                
                if (newPassword.length() < 4) {
                    JOptionPane.showMessageDialog(
                        this,
                        "La contraseña debe tener al menos 4 caracteres.",
                        "Contraseña Débil",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                
                String newPasswordHash = SecurityHelper.encryptPassword(newPassword);
                String consultaActualizacion = "UPDATE usuarios SET password_hash = ? WHERE username = ?";
                PreparedStatement consultaActualizar = conexion.prepareStatement(consultaActualizacion);
                consultaActualizar.setString(1, newPasswordHash);
                consultaActualizar.setString(2, username);
                
                int filasAfectadas = consultaActualizar.executeUpdate();
                
                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(
                        this,
                        "¡Contraseña actualizada exitosamente!\nYa puedes iniciar sesión con tu nueva contraseña.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
                
                consultaActualizar.close();
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
    
    private void mostrarEstado(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setForeground(color);
        
        Timer timer = new Timer(3000, e -> lblStatus.setText(""));
        timer.setRepeats(false);
        timer.start();
    }
    
    private void abrirDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            Dashboard dashboard = new Dashboard(user);
            dashboard.setVisible(true);
        });
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

