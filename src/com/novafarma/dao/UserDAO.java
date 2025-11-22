package com.novafarma.dao;

import com.novafarma.model.User;
import com.novafarma.model.User.UserRole;
import com.novafarma.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad User
 * 
 * Responsable de todas las operaciones CRUD en la tabla 'usuarios'
 * Incluye autenticación y gestión de roles
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class UserDAO {
    
    /**
     * Autentica un usuario con su nombre de usuario y contraseña (hash SHA-256)
     * 
     * @param username Nombre de usuario
     * @param passwordHash Hash SHA-256 de la contraseña
     * @return User si las credenciales son válidas, null si no
     * @throws SQLException Si hay error en la consulta
     */
    public User authenticate(String username, String passwordHash) throws SQLException {
        String sql = "SELECT id, username, password_hash, rol FROM usuarios " +
                     "WHERE username = ? AND password_hash = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null; // Credenciales inválidas
    }
    
    /**
     * Busca un usuario por su nombre de usuario
     * 
     * @param username Nombre de usuario
     * @return User si existe, null si no
     * @throws SQLException Si hay error en la consulta
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, rol FROM usuarios WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Busca un usuario por su ID
     * 
     * @param id ID del usuario
     * @return User si existe, null si no
     * @throws SQLException Si hay error en la consulta
     */
    public User findById(int id) throws SQLException {
        String sql = "SELECT id, username, password_hash, rol FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Obtiene todos los usuarios del sistema
     * 
     * @return Lista de todos los usuarios
     * @throws SQLException Si hay error en la consulta
     */
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password_hash, rol FROM usuarios ORDER BY id ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
        }
        
        return users;
    }
    
    /**
     * Crea un nuevo usuario
     * 
     * IMPORTANTE: La contraseña debe venir ya hasheada (SHA-256)
     * 
     * @param user Usuario a crear
     * @return true si la creación fue exitosa
     * @throws SQLException Si hay error (ej: username duplicado)
     */
    public boolean save(User user) throws SQLException {
        String sql = "INSERT INTO usuarios (username, password_hash, rol) VALUES (?, ?, ?::user_role)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getRol().name());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Actualiza la contraseña de un usuario
     * 
     * @param username Nombre de usuario
     * @param newPasswordHash Nuevo hash SHA-256 de la contraseña
     * @return true si la actualización fue exitosa
     * @throws SQLException Si hay error en la actualización
     */
    public boolean updatePassword(String username, String newPasswordHash) throws SQLException {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPasswordHash);
            pstmt.setString(2, username);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Actualiza el rol de un usuario
     * 
     * @param userId ID del usuario
     * @param newRole Nuevo rol
     * @return true si la actualización fue exitosa
     * @throws SQLException Si hay error en la actualización
     */
    public boolean updateRole(int userId, UserRole newRole) throws SQLException {
        String sql = "UPDATE usuarios SET rol = ?::user_role WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newRole.name());
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Elimina un usuario (NO RECOMENDADO)
     * Solo usar si no hay ventas asociadas
     * 
     * @param id ID del usuario
     * @return true si la eliminación fue exitosa
     * @throws SQLException Si hay error (ej: violación de FK)
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Verifica si un nombre de usuario ya existe
     * 
     * @param username Nombre de usuario a verificar
     * @return true si el username ya existe
     * @throws SQLException Si hay error en la consulta
     */
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM usuarios WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Cuenta el número de usuarios por rol
     * 
     * @param role Rol a contar
     * @return Número de usuarios con ese rol
     * @throws SQLException Si hay error en la consulta
     */
    public int countByRole(UserRole role) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM usuarios WHERE rol = ?::user_role";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, role.name());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        
        return 0;
    }
    
    // ==================== MÉTODO AUXILIAR ====================
    
    /**
     * Mapea un ResultSet a un objeto User
     * 
     * @param rs ResultSet con datos del usuario
     * @return Objeto User
     * @throws SQLException Si hay error al leer los datos
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String rolString = rs.getString("rol");
        
        // Convertir String a UserRole enum
        UserRole role = UserRole.valueOf(rolString);
        
        return new User(id, username, passwordHash, role);
    }
}

