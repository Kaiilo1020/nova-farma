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
        List<Product> productos = new ArrayList<>();
        String consultaSQL = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos WHERE activo = TRUE ORDER BY id ASC";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement();
             ResultSet resultadoConsulta = consulta.executeQuery(consultaSQL)) {
            
            while (resultadoConsulta.next()) {
                Product producto = mapearResultadoAProducto(resultadoConsulta);
                productos.add(producto);
            }
        }
        
        return productos;
    }
    
    /**
     * Obtiene productos activos con paginación
     * OPTIMIZACIÓN: Para manejar grandes volúmenes de datos
     * 
     * @param limit Número máximo de registros a retornar
     * @param offset Número de registros a saltar (para paginación)
     * @return Lista de productos activos ordenados por ID
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> findAllActive(int limit, int offset) throws SQLException {
        List<Product> productos = new ArrayList<>();
        String consultaSQL = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos WHERE activo = TRUE ORDER BY id ASC LIMIT ? OFFSET ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, limit);
            consultaPreparada.setInt(2, offset);
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                while (resultadoConsulta.next()) {
                    Product producto = mapearResultadoAProducto(resultadoConsulta);
                    productos.add(producto);
                }
            }
        }
        
        return productos;
    }
    
    /**
     * Cuenta el número total de productos activos
     * 
     * @return Número total de productos activos
     * @throws SQLException Si hay error en la consulta
     */
    public int countAllActive() throws SQLException {
        String consultaSQL = "SELECT COUNT(*) as total FROM productos WHERE activo = TRUE";
        
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
     * Cuenta el número total de productos activos con stock > 0 (vendibles)
     * 
     * @return Número total de productos vendibles
     * @throws SQLException Si hay error en la consulta
     */
    public int countActiveWithStock() throws SQLException {
        String consultaSQL = "SELECT COUNT(*) as total FROM productos WHERE activo = TRUE AND stock > 0";
        
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
     * Obtiene todos los productos (activos e inactivos)
     * 
     * @return Lista de todos los productos
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> findAll() throws SQLException {
        List<Product> productos = new ArrayList<>();
        String consultaSQL = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos ORDER BY id ASC";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement();
             ResultSet resultadoConsulta = consulta.executeQuery(consultaSQL)) {
            
            while (resultadoConsulta.next()) {
                Product producto = mapearResultadoAProducto(resultadoConsulta);
                productos.add(producto);
            }
        }
        
        return productos;
    }
    
    /**
     * Busca un producto por su ID
     * 
     * @param id ID del producto
     * @return Product si se encuentra, null si no existe
     * @throws SQLException Si hay error en la consulta
     */
    public Product findById(int id) throws SQLException {
        String consultaSQL = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos WHERE id = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, id);
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                if (resultadoConsulta.next()) {
                    return mapearResultadoAProducto(resultadoConsulta);
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
        String consultaSQL = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos " +
                     "WHERE LOWER(nombre) = LOWER(?) " +
                     "ORDER BY id DESC " +  // Ordenar por ID descendente para obtener el más reciente
                     "LIMIT 1";  // Solo tomar el primer resultado (el más reciente)
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, nombre.trim());
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                if (resultadoConsulta.next()) {
                    return mapearResultadoAProducto(resultadoConsulta);
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
    public boolean save(Product producto) throws SQLException {
        String consultaSQL = "INSERT INTO productos (nombre, descripcion, precio, stock, fecha_vencimiento, activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, producto.getNombre());
            consultaPreparada.setString(2, producto.getDescripcion());
            consultaPreparada.setDouble(3, producto.getPrecio());
            consultaPreparada.setInt(4, producto.getStock());
            consultaPreparada.setDate(5, producto.getFechaVencimiento());
            consultaPreparada.setBoolean(6, producto.isActivo());
            
            return consultaPreparada.executeUpdate() > 0;
        }
    }
    
    /**
     * Actualiza un producto existente
     * 
     * @param product Producto con datos actualizados
     * @return true si la actualización fue exitosa
     * @throws SQLException Si hay error en la actualización
     */
    public boolean update(Product producto) throws SQLException {
        String consultaSQL = "UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, " +
                     "stock = ?, fecha_vencimiento = ?, activo = ? WHERE id = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setString(1, producto.getNombre());
            consultaPreparada.setString(2, producto.getDescripcion());
            consultaPreparada.setDouble(3, producto.getPrecio());
            consultaPreparada.setInt(4, producto.getStock());
            consultaPreparada.setDate(5, producto.getFechaVencimiento());
            consultaPreparada.setBoolean(6, producto.isActivo());
            consultaPreparada.setInt(7, producto.getId());
            
            return consultaPreparada.executeUpdate() > 0;
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
        String consultaSQL = "UPDATE productos SET activo = FALSE, stock = 0 WHERE id = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, id);
            return consultaPreparada.executeUpdate() > 0;
        }
    }
    
    /**
     * Elimina físicamente un producto (NO RECOMENDADO - NO USADO)
     * Este método no se usa en la aplicación. Se prefiere soft delete (retireProduct).
     * 
     * @deprecated No se usa. Usar softDelete() en su lugar.
     * @param id ID del producto
     * @return true si la eliminación fue exitosa
     * @throws SQLException Si hay error (ej: violación de FK)
     */
    @Deprecated
    public boolean delete(int id) throws SQLException {
        String consultaSQL = "DELETE FROM productos WHERE id = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, id);
            return consultaPreparada.executeUpdate() > 0;
        }
    }
    
    /**
     * Encuentra productos vencidos o próximos a vencer (≤ 30 días)
     * 
     * @return Lista de productos con alertas de vencimiento
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> findExpiringSoon() throws SQLException {
        List<Product> productos = new ArrayList<>();
        String consultaSQL = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos " +
                     "WHERE fecha_vencimiento IS NOT NULL " +
                     "  AND fecha_vencimiento <= CURRENT_DATE + INTERVAL '30 days' " +
                     "  AND activo = TRUE " +
                     "ORDER BY fecha_vencimiento ASC";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement();
             ResultSet resultadoConsulta = consulta.executeQuery(consultaSQL)) {
            
            while (resultadoConsulta.next()) {
                Product producto = mapearResultadoAProducto(resultadoConsulta);
                productos.add(producto);
            }
        }
        
        return productos;
    }
    
    /**
     * Encuentra todos los productos vencidos (fecha < hoy)
     * 
     * @return Lista de productos vencidos
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> findExpired() throws SQLException {
        List<Product> productos = new ArrayList<>();
        String consultaSQL = "SELECT id, nombre, descripcion, precio, stock, fecha_vencimiento, activo " +
                     "FROM productos " +
                     "WHERE fecha_vencimiento < CURRENT_DATE " +
                     "  AND activo = TRUE " +
                     "ORDER BY fecha_vencimiento ASC";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement();
             ResultSet resultadoConsulta = consulta.executeQuery(consultaSQL)) {
            
            while (resultadoConsulta.next()) {
                Product producto = mapearResultadoAProducto(resultadoConsulta);
                productos.add(producto);
            }
        }
        
        return productos;
    }
    
    /**
     * Desactiva todos los productos vencidos (soft delete masivo)
     * 
     * @return Número de productos desactivados
     * @throws SQLException Si hay error en la operación
     */
    public int softDeleteAllExpired() throws SQLException {
        String consultaSQL = "UPDATE productos " +
                     "SET activo = FALSE, stock = 0 " +
                     "WHERE fecha_vencimiento < CURRENT_DATE AND activo = TRUE";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement()) {
            
            return consulta.executeUpdate(consultaSQL);
        }
    }
    
    // ==================== MÉTODO AUXILIAR ====================
    
    /**
     * Mapea un ResultSet a un objeto Product
     * 
     * @param resultadoConsulta ResultSet con datos del producto
     * @return Objeto Product
     * @throws SQLException Si hay error al leer los datos
     */
    private Product mapearResultadoAProducto(ResultSet resultadoConsulta) throws SQLException {
        int id = resultadoConsulta.getInt("id");
        String nombre = resultadoConsulta.getString("nombre");
        String descripcion = resultadoConsulta.getString("descripcion");
        double precio = resultadoConsulta.getDouble("precio");
        int stock = resultadoConsulta.getInt("stock");
        Date fechaVencimiento = resultadoConsulta.getDate("fecha_vencimiento");
        boolean activo = resultadoConsulta.getBoolean("activo");
        
        return new Product(id, nombre, descripcion, precio, stock, fechaVencimiento, activo);
    }
}

