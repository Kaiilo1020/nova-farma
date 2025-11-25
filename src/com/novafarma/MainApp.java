package com.novafarma;

import com.novafarma.ui.LoginFrame;

import javax.swing.*;

/**
 * Clase principal de la aplicación Nova Farma
 * 
 * PUNTO DE ENTRADA:
 * Esta clase contiene el método main() que inicia la aplicación.
 * 
 * ARQUITECTURA DE LA APLICACIÓN:
 * 
 * com.novafarma/
 * ├── MainApp.java (Punto de entrada)
 * ├── model/
 * │   └── User.java (Modelo de datos de usuario)
 * ├── util/
 * │   ├── SecurityHelper.java (Encriptación SHA-256)
 * │   └── DatabaseConnection.java (Conexión JDBC)
 * └── ui/
 *     ├── LoginFrame.java (Ventana de login)
 *     ├── Dashboard.java (Panel principal)
 *     └── UserCreationDialog.java (Diálogo de creación)
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class MainApp {
    
    /**
     * Método principal - Punto de entrada de la aplicación
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Configurar el Look and Feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo establecer el Look and Feel del sistema");
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            } catch (Exception e) {
                System.err.println("ERROR al iniciar la aplicación:");
                e.printStackTrace();
                
                JOptionPane.showMessageDialog(null,
                    "Error al iniciar la aplicación:\n" + e.getMessage(),
                    "Error Fatal",
                    JOptionPane.ERROR_MESSAGE);
                
                System.exit(1);
            }
        });
    }
}

