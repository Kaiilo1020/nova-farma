package com.novafarma.dao;

import com.novafarma.model.User;
import com.novafarma.model.User.UserRole;
import com.novafarma.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String consultaSQL = "SELECT id, username, password_hash, rol FROM usuarios " +
                     "WHERE username = ? AND password_hash = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, username);
            consultaPreparada.setString(2, passwordHash);
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                if (resultadoConsulta.next()) {
                    return mapearResultadoAUsuario(resultadoConsulta);
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
        String consultaSQL = "SELECT id, username, password_hash, rol FROM usuarios WHERE username = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, username);
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                if (resultadoConsulta.next()) {
                    return mapearResultadoAUsuario(resultadoConsulta);
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
        String consultaSQL = "SELECT id, username, password_hash, rol FROM usuarios WHERE id = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, id);
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                if (resultadoConsulta.next()) {
                    return mapearResultadoAUsuario(resultadoConsulta);
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
        List<User> usuarios = new ArrayList<>();
        String consultaSQL = "SELECT id, username, password_hash, rol FROM usuarios ORDER BY id ASC";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement();
             ResultSet resultadoConsulta = consulta.executeQuery(consultaSQL)) {
            
            while (resultadoConsulta.next()) {
                User usuario = mapearResultadoAUsuario(resultadoConsulta);
                usuarios.add(usuario);
            }
        }
        
        return usuarios;
    }
    
    /**
     * Obtiene usuarios con paginación
     * OPTIMIZACIÓN: Para manejar grandes volúmenes de datos
     * 
     * @param limit Número máximo de registros a retornar
     * @param offset Número de registros a saltar (para paginación)
     * @return Lista de usuarios ordenados por ID
     * @throws SQLException Si hay error en la consulta
     */
    public List<User> findAll(int limit, int offset) throws SQLException {
        List<User> usuarios = new ArrayList<>();
        String consultaSQL = "SELECT id, username, password_hash, rol FROM usuarios ORDER BY id ASC LIMIT ? OFFSET ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, limit);
            consultaPreparada.setInt(2, offset);
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                while (resultadoConsulta.next()) {
                    User usuario = mapearResultadoAUsuario(resultadoConsulta);
                    usuarios.add(usuario);
                }
            }
        }
        
        return usuarios;
    }
    
    /**
     * Cuenta el número total de usuarios
     * 
     * @return Número total de usuarios
     * @throws SQLException Si hay error en la consulta
     */
    public int countAll() throws SQLException {
        String consultaSQL = "SELECT COUNT(*) as total FROM usuarios";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement();
             ResultSet resultadoConsulta = consulta.executeQuery(consultaSQL)) {
            
            if (resultadoConsulta.next()) {
                return resultadoConsulta.getInt("total");
            }
        }
        
        return 0;
    }
    
    /**
     * Obtiene todos los usuarios con el conteo de ventas en una sola query
     * OPTIMIZACIÓN: Evita el problema N+1 usando LEFT JOIN
     * 
     * @return Map donde la clave es el ID del usuario y el valor es el conteo de ventas
     * @throws SQLException Si hay error en la consulta
     */
    public Map<Integer, Integer> findAllWithSalesCount() throws SQLException {
        Map<Integer, Integer> mapaConteoVentas = new HashMap<>();
        
        // Query optimizada: LEFT JOIN para traer usuarios y conteo de ventas en una sola consulta
        String consultaSQL = "SELECT u.id, u.username, u.password_hash, u.rol, " +
                     "COALESCE(COUNT(v.id), 0) as ventas_count " +
                     "FROM usuarios u " +
                     "LEFT JOIN ventas v ON u.id = v.usuario_id " +
                     "GROUP BY u.id, u.username, u.password_hash, u.rol " +
                     "ORDER BY u.id ASC";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement();
             ResultSet resultadoConsulta = consulta.executeQuery(consultaSQL)) {
            
            while (resultadoConsulta.next()) {
                int idUsuario = resultadoConsulta.getInt("id");
                int conteoVentas = resultadoConsulta.getInt("ventas_count");
                mapaConteoVentas.put(idUsuario, conteoVentas);
            }
        }
        
        return mapaConteoVentas;
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
    public boolean save(User usuario) throws SQLException {
        String consultaSQL = "INSERT INTO usuarios (username, password_hash, rol) VALUES (?, ?, ?::user_role)";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, usuario.getUsername());
            consultaPreparada.setString(2, usuario.getPasswordHash());
            consultaPreparada.setString(3, usuario.getRol().name());
            
            return consultaPreparada.executeUpdate() > 0;
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
        String consultaSQL = "UPDATE usuarios SET password_hash = ? WHERE username = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, newPasswordHash);
            consultaPreparada.setString(2, username);
            
            return consultaPreparada.executeUpdate() > 0;
        }
    }
    
    /**
     * Actualiza el rol de un usuario (NO USADO)
     * 
     * @deprecated No se usa en la aplicación actual
     * @param userId ID del usuario
     * @param newRole Nuevo rol
     * @return true si la actualización fue exitosa
     * @throws SQLException Si hay error en la actualización
     */
    @Deprecated
    public boolean updateRole(int userId, UserRole newRole) throws SQLException {
        String consultaSQL = "UPDATE usuarios SET rol = ?::user_role WHERE id = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, newRole.name());
            consultaPreparada.setInt(2, userId);
            
            return consultaPreparada.executeUpdate() > 0;
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
        String consultaSQL = "DELETE FROM usuarios WHERE id = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, id);
            return consultaPreparada.executeUpdate() > 0;
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
        String consultaSQL = "SELECT COUNT(*) as total FROM usuarios WHERE username = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, username);
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                if (resultadoConsulta.next()) {
                    return resultadoConsulta.getInt("total") > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Cuenta el número de usuarios por rol (NO USADO)
     * 
     * @deprecated No se usa en la aplicación actual
     * @param role Rol a contar
     * @return Número de usuarios con ese rol
     * @throws SQLException Si hay error en la consulta
     */
    @Deprecated
    public int countByRole(UserRole role) throws SQLException {
        String consultaSQL = "SELECT COUNT(*) as total FROM usuarios WHERE rol = ?::user_role";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, role.name());
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                if (resultadoConsulta.next()) {
                    return resultadoConsulta.getInt("total");
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
    private User mapearResultadoAUsuario(ResultSet resultadoConsulta) throws SQLException {
        int id = resultadoConsulta.getInt("id");
        String username = resultadoConsulta.getString("username");
        String passwordHash = resultadoConsulta.getString("password_hash");
        String rolString = resultadoConsulta.getString("rol");
        
        // Convertir String a UserRole enum
        UserRole rol = UserRole.valueOf(rolString);
        
        return new User(id, username, passwordHash, rol);
    }
}

