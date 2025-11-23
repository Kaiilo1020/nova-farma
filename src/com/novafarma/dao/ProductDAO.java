package com.novafarma.dao;

import com.novafarma.model.Product;
import com.novafarma.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Product
 * 
 * Responsable de todas las operaciones CRUD en la tabla 'productos'
 * Separa la lógica de acceso a datos de la lógica de presentación
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class ProductDAO {
    
    /**
     * Obtiene todos los productos activos
     * 
     * @return Lista de productos activos ordenados por ID
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> findAllActive() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos WHERE activo = TRUE ORDER BY id ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }
        }
        
        return products;
    }
    
    /**
     * Obtiene todos los productos (activos e inactivos)
     * 
     * @return Lista de todos los productos
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos ORDER BY id ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }
        }
        
        return products;
    }
    
    /**
     * Busca un producto por su ID
     * 
     * @param id ID del producto
     * @return Product si se encuentra, null si no existe
     * @throws SQLException Si hay error en la consulta
     */
    public Product findById(int id) throws SQLException {
        String sql = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Busca un producto por su nombre (case-insensitive)
     * 
     * IMPORTANTE: Busca en TODOS los productos (activos e inactivos)
     * para detectar duplicados incluso si el producto anterior está inactivo.
     * 
     * Si hay múltiples productos con el mismo nombre, retorna el más reciente
     * (el que tiene el ID más alto, asumiendo que los IDs son auto-incrementales).
     * 
     * @param nombre Nombre del producto a buscar
     * @return Product si se encuentra, null si no existe
     * @throws SQLException Si hay error en la consulta
     */
    public Product findByName(String nombre) throws SQLException {
        String sql = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos " +
                     "WHERE LOWER(nombre) = LOWER(?) " +
                     "ORDER BY id DESC " +  // Ordenar por ID descendente para obtener el más reciente
                     "LIMIT 1";  // Solo tomar el primer resultado (el más reciente)
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombre.trim());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Inserta un nuevo producto en la base de datos
     * 
     * @param product Producto a insertar
     * @return true si la inserción fue exitosa
     * @throws SQLException Si hay error en la inserción
     */
    public boolean save(Product product) throws SQLException {
        String sql = "INSERT INTO productos (nombre, descripcion, precio, stock, fecha_vencimiento, activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getNombre());
            pstmt.setString(2, product.getDescripcion());
            pstmt.setDouble(3, product.getPrecio());
            pstmt.setInt(4, product.getStock());
            pstmt.setDate(5, product.getFechaVencimiento());
            pstmt.setBoolean(6, product.isActivo());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Actualiza un producto existente
     * 
     * @param product Producto con datos actualizados
     * @return true si la actualización fue exitosa
     * @throws SQLException Si hay error en la actualización
     */
    public boolean update(Product product) throws SQLException {
        String sql = "UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, " +
                     "stock = ?, fecha_vencimiento = ?, activo = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getNombre());
            pstmt.setString(2, product.getDescripcion());
            pstmt.setDouble(3, product.getPrecio());
            pstmt.setInt(4, product.getStock());
            pstmt.setDate(5, product.getFechaVencimiento());
            pstmt.setBoolean(6, product.isActivo());
            pstmt.setInt(7, product.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Realiza un "soft delete" del producto
     * Establece activo = FALSE y stock = 0
     * 
     * @param id ID del producto
     * @return true si la operación fue exitosa
     * @throws SQLException Si hay error en la operación
     */
    public boolean softDelete(int id) throws SQLException {
        String sql = "UPDATE productos SET activo = FALSE, stock = 0 WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Elimina físicamente un producto (NO RECOMENDADO)
     * Solo usar si no hay restricciones de clave foránea
     * 
     * @param id ID del producto
     * @return true si la eliminación fue exitosa
     * @throws SQLException Si hay error (ej: violación de FK)
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM productos WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Encuentra productos vencidos o próximos a vencer (≤ 30 días)
     * 
     * @return Lista de productos con alertas de vencimiento
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> findExpiringSoon() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos " +
                     "WHERE fecha_vencimiento IS NOT NULL " +
                     "  AND fecha_vencimiento <= CURRENT_DATE + INTERVAL '30 days' " +
                     "  AND activo = TRUE " +
                     "ORDER BY fecha_vencimiento ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }
        }
        
        return products;
    }
    
    /**
     * Encuentra todos los productos vencidos (fecha < hoy)
     * 
     * @return Lista de productos vencidos
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> findExpired() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos " +
                     "WHERE fecha_vencimiento < CURRENT_DATE " +
                     "  AND activo = TRUE " +
                     "ORDER BY fecha_vencimiento ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }
        }
        
        return products;
    }
    
    /**
     * Desactiva todos los productos vencidos (soft delete masivo)
     * 
     * @return Número de productos desactivados
     * @throws SQLException Si hay error en la operación
     */
    public int softDeleteAllExpired() throws SQLException {
        String sql = "UPDATE productos " +
                     "SET activo = FALSE, stock = 0 " +
                     "WHERE fecha_vencimiento < CURRENT_DATE AND activo = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            return stmt.executeUpdate(sql);
        }
    }
    
    // ==================== MÉTODO AUXILIAR ====================
    
    /**
     * Mapea un ResultSet a un objeto Product
     * 
     * @param rs ResultSet con datos del producto
     * @return Objeto Product
     * @throws SQLException Si hay error al leer los datos
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String descripcion = rs.getString("descripcion");
        double precio = rs.getDouble("precio");
        int stock = rs.getInt("stock");
        Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        boolean activo = rs.getBoolean("activo");
        
        return new Product(id, nombre, descripcion, precio, stock, fechaVencimiento, activo);
    }
}

