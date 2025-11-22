package com.novafarma.model;

/**
 * Clase modelo que representa un Usuario del sistema Nova Farma
 * 
 * PROPÓSITO:
 * Esta clase es un POJO (Plain Old Java Object) que encapsula
 * los datos de un usuario y su rol en el sistema.
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class User {
    
    // ==================== ATRIBUTOS ====================
    
    private int id;
    private String username;
    private String passwordHash; // NUNCA almacena la contraseña en texto plano
    private UserRole rol;
    
    // ==================== ENUM DE ROLES ====================
    
    /**
     * Enumeración que define los roles del sistema
     * 
     * ADMINISTRADOR: Puede modificar productos, crear usuarios, etc.
     * TRABAJADOR: Solo puede vender y visualizar inventario
     */
    public enum UserRole {
        ADMINISTRADOR("Administrador"),
        TRABAJADOR("Trabajador");
        
        private final String displayName;
        
        UserRole(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * Convierte un String de la BD a un UserRole
         */
        public static UserRole fromString(String rolString) {
            if (rolString == null) return null;
            
            for (UserRole role : UserRole.values()) {
                if (role.name().equalsIgnoreCase(rolString) || 
                    role.displayName.equalsIgnoreCase(rolString)) {
                    return role;
                }
            }
            return null;
        }
    }
    
    // ==================== CONSTRUCTORES ====================
    
    /**
     * Constructor vacío
     */
    public User() {
    }
    
    /**
     * Constructor con todos los parámetros
     * 
     * @param id ID del usuario
     * @param username Nombre de usuario
     * @param passwordHash Hash SHA-256 de la contraseña
     * @param rol Rol del usuario (ADMINISTRADOR o TRABAJADOR)
     */
    public User(int id, String username, String passwordHash, UserRole rol) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }
    
    /**
     * Constructor sin ID (para crear nuevos usuarios antes de INSERT)
     */
    public User(String username, String passwordHash, UserRole rol) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }
    
    // ==================== GETTERS Y SETTERS ====================
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public UserRole getRol() {
        return rol;
    }
    
    public void setRol(UserRole rol) {
        this.rol = rol;
    }
    
    // ==================== MÉTODOS DE VERIFICACIÓN DE ROL ====================
    
    /**
     * Verifica si el usuario es Administrador
     * 
     * @return true si es administrador, false en caso contrario
     */
    public boolean isAdministrador() {
        return this.rol == UserRole.ADMINISTRADOR;
    }
    
    /**
     * Verifica si el usuario es Trabajador
     * 
     * @return true si es trabajador, false en caso contrario
     */
    public boolean isTrabajador() {
        return this.rol == UserRole.TRABAJADOR;
    }
    
    // ==================== MÉTODO toString ====================
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", rol=" + rol.getDisplayName() +
                '}';
    }
}

