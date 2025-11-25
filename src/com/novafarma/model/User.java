package com.novafarma.model;

/** Modelo de Usuario con rol y autenticación */
public class User {
    
    private int id;
    private String username;
    private String passwordHash;
    private UserRole rol;
    
    /** Roles del sistema */
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
    
    public User() {
    }
    
    public User(int id, String username, String passwordHash, UserRole rol) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }
    
    public User(String username, String passwordHash, UserRole rol) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }
    
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
    
    public boolean isAdministrador() {
        return this.rol == UserRole.ADMINISTRADOR;
    }
    
    public boolean isTrabajador() {
        return this.rol == UserRole.TRABAJADOR;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", rol=" + rol.getDisplayName() +
                '}';
    }
}

