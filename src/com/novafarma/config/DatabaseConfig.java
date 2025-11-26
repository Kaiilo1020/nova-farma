package com.novafarma.config;

/**
 * Configuración de la base de datos PostgreSQL
 * 
 * SEPARACIÓN DE RESPONSABILIDADES:
 * - Esta clase SOLO contiene constantes de configuración
 * - La lógica de conexión está en DatabaseConnection.java
 * 
 * IMPORTANTE: Modifica estos valores según tu instalación local de PostgreSQL
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class DatabaseConfig {
    
    // ==================== CONFIGURACIÓN DE CONEXIÓN ====================
    // IMPORTANTE: Modifica estos valores según tu instalación local de PostgreSQL
    
    /** Host de PostgreSQL (por defecto: localhost) */
    public static final String DB_HOST = "localhost";
    
    /** Puerto de PostgreSQL (por defecto: 5432) */
    public static final String DB_PORT = "5432";
    
    /** Nombre de la base de datos */
    public static final String DB_NAME = "nova_farma_db";
    
    /** Usuario de PostgreSQL */
    public static final String DB_USER = "postgres";
    
    /** Contraseña de PostgreSQL */
    public static final String DB_PASSWORD = "postgres";
    
    // ==================== URL DE CONEXIÓN ====================
    
    /**
     * Genera la URL completa de conexión JDBC
     * 
     * @return URL en formato: jdbc:postgresql://host:port/database
     */
    public static String getConnectionUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s", 
            DB_HOST, DB_PORT, DB_NAME);
    }
    
    /**
     * Obtiene el driver JDBC de PostgreSQL
     * 
     * @return Nombre de la clase del driver
     */
    public static String getDriverClass() {
        return "org.postgresql.Driver";
    }
}

