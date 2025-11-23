package com.novafarma.util;

import com.novafarma.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase para gestionar la conexión a la base de datos PostgreSQL
 * 
 * PATRÓN DE DISEÑO: Singleton (una única instancia de conexión)
 * 
 * CONFIGURACIÓN:
 * - La configuración de la base de datos está en DatabaseConfig.java
 * - Modifica los valores en DatabaseConfig.java según tu instalación local
 * 
 * @author Nova Farma Development Team
 * @version 2.0 (Refactorizado: configuración separada)
 */
public class DatabaseConnection {
    
    // ==================== PATRÓN SINGLETON ====================
    
    private static Connection connection = null;
    
    /**
     * Constructor privado para evitar instanciación externa
     * (parte del patrón Singleton)
     */
    private DatabaseConnection() {
        // Constructor privado
    }
    
    /**
     * Obtiene la conexión a la base de datos
     * 
     * PATRÓN SINGLETON:
     * - Si no existe conexión, la crea
     * - Si existe pero está cerrada, la recrea
     * - Si existe y está abierta, la reutiliza
     * 
     * @return Connection objeto de conexión JDBC
     * @throws SQLException si hay error de conexión
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Verificar si la conexión está cerrada o es nula
            if (connection == null || connection.isClosed()) {
                // Cargar el driver de PostgreSQL (necesario en algunas versiones de Java)
                Class.forName(DatabaseConfig.getDriverClass());
                
                // Establecer la conexión usando configuración desde DatabaseConfig
                connection = DriverManager.getConnection(
                    DatabaseConfig.getConnectionUrl(),
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD
                );
                
                System.out.println("✓ Conexión establecida con PostgreSQL");
            }
            
            return connection;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "Error: Driver de PostgreSQL no encontrado. " +
                "Asegúrate de tener postgresql-XX.X.jar en el classpath", e
            );
        }
    }
    
    /**
     * Cierra la conexión a la base de datos
     * Debe llamarse al cerrar la aplicación
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si la conexión está activa
     * 
     * @return true si hay conexión activa, false en caso contrario
     */
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Método de prueba para verificar la conexión
     */
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE CONEXIÓN A POSTGRESQL ===\n");
        
        try {
            getConnection();
            System.out.println("Estado: Conectado exitosamente");
            System.out.println("Base de datos: " + DatabaseConfig.DB_NAME);
            System.out.println("URL: " + DatabaseConfig.getConnectionUrl());
            
            // Cerrar la conexión
            closeConnection();
            
        } catch (SQLException e) {
            System.err.println("ERROR de conexión:");
            System.err.println("- Mensaje: " + e.getMessage());
            System.err.println("\nVERIFICA:");
            System.err.println("1. PostgreSQL está ejecutándose");
            System.err.println("2. La base de datos '" + DatabaseConfig.DB_NAME + "' existe");
            System.err.println("3. Usuario/contraseña son correctos");
            System.err.println("4. El driver postgresql-XX.X.jar está en el classpath");
            System.err.println("\nNOTA: Modifica la configuración en DatabaseConfig.java");
        }
    }
}

