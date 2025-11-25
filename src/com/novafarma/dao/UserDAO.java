package com.novafarma.dao;

import com.novafarma.model.User;
import com.novafarma.model.User.UserRole;
import com.novafarma.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** DAO para operaciones CRUD en la tabla usuarios */
public class UserDAO {
    
    /** Autentica usuario con username y password hash SHA-256 */
    public User autenticarUsuario(String username, String passwordHash) throws SQLException {
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
        
            return null;
    }
    
    /** Busca usuario por nombre */
    public User buscarPorNombreUsuario(String username) throws SQLException {
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
    
    /** Busca usuario por ID */
    public User buscarUsuarioPorId(int id) throws SQLException {
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
    
    /** Obtiene todos los usuarios */
    public List<User> obtenerTodosLosUsuarios() throws SQLException {
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
    
    /** Obtiene usuarios con paginación */
    public List<User> obtenerUsuariosPaginados(int limit, int offset) throws SQLException {
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
    
    /** Cuenta usuarios */
    public int contarUsuarios() throws SQLException {
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
    
    /** Obtiene usuarios con conteo de ventas (evita N+1 con LEFT JOIN) */
    public Map<Integer, Integer> obtenerUsuariosConConteoVentas() throws SQLException {
        Map<Integer, Integer> mapaConteoVentas = new HashMap<>();
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
    
    /** Crea un nuevo usuario (password debe venir hasheado SHA-256) */
    public boolean guardarUsuario(User usuario) throws SQLException {
        String consultaSQL = "INSERT INTO usuarios (username, password_hash, rol) VALUES (?, ?, ?::user_role)";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, usuario.getUsername());
            consultaPreparada.setString(2, usuario.getPasswordHash());
            consultaPreparada.setString(3, usuario.getRol().name());
            
            return consultaPreparada.executeUpdate() > 0;
        }
    }
    
    /** Actualiza contraseña de usuario */
    public boolean actualizarContrasena(String username, String newPasswordHash) throws SQLException {
        String consultaSQL = "UPDATE usuarios SET password_hash = ? WHERE username = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, newPasswordHash);
            consultaPreparada.setString(2, username);
            
            return consultaPreparada.executeUpdate() > 0;
        }
    }
    
    /** @deprecated No usado */
    @Deprecated
    public boolean actualizarRol(int userId, UserRole newRole) throws SQLException {
        String consultaSQL = "UPDATE usuarios SET rol = ?::user_role WHERE id = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, newRole.name());
            consultaPreparada.setInt(2, userId);
            
            return consultaPreparada.executeUpdate() > 0;
        }
    }
    
    /** Elimina usuario (solo si no tiene ventas) */
    public boolean eliminarUsuario(int id) throws SQLException {
        String consultaSQL = "DELETE FROM usuarios WHERE id = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, id);
            return consultaPreparada.executeUpdate() > 0;
        }
    }
    
    /** Verifica si existe un usuario por nombre */
    public boolean existeUsuarioPorNombre(String username) throws SQLException {
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
    
    /** @deprecated No usado */
    @Deprecated
    public int contarUsuariosPorRol(UserRole role) throws SQLException {
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
    
    private User mapearResultadoAUsuario(ResultSet resultadoConsulta) throws SQLException {
        int id = resultadoConsulta.getInt("id");
        String username = resultadoConsulta.getString("username");
        String passwordHash = resultadoConsulta.getString("password_hash");
        String rolString = resultadoConsulta.getString("rol");
        UserRole rol = UserRole.valueOf(rolString);
        
        return new User(id, username, passwordHash, rol);
    }
}

