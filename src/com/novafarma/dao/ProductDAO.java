package com.novafarma.dao;

import com.novafarma.model.Product;
import com.novafarma.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO para operaciones CRUD en la tabla productos */
public class ProductDAO {
    
    public List<Product> obtenerProductosActivos() throws SQLException {
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
    
    //** Obtiene productos activos con paginación */
    public List<Product> obtenerProductosActivos(int limit, int offset) throws SQLException {
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
    
    /** Cuenta productos activos */
    public int contarProductosActivos() throws SQLException {
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
    
    /** Cuenta productos activos con stock > 0 */
    public int contarProductosActivosConStock() throws SQLException {
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
    
    /** Busca un producto por ID */
    public Product buscarProductoPorId(int id) throws SQLException {
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
    
    /** Busca producto por nombre (incluye inactivos para detectar duplicados) */
    public Product buscarProductoPorNombre(String nombre) throws SQLException {
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
    
    /** Inserta un nuevo producto */
    public boolean guardarProducto(Product producto) throws SQLException {
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
    
    /** Actualiza un producto existente */
    public boolean actualizarProducto(Product producto) throws SQLException {
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
    
    /** Desactiva producto (soft delete: activo=FALSE, stock=0) */
    public boolean desactivarProducto(int id) throws SQLException {
        String consultaSQL = "UPDATE productos SET activo = FALSE, stock = 0 WHERE id = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, id);
            return consultaPreparada.executeUpdate() > 0;
        }
    }
    
    /** @deprecated No usado. Usar desactivarProducto() */
    @Deprecated
    public boolean eliminarProducto(int id) throws SQLException {
        String consultaSQL = "DELETE FROM productos WHERE id = ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, id);
            return consultaPreparada.executeUpdate() > 0;
        }
    }
    
    /** Obtiene productos que vencen en ≤30 días */
    public List<Product> obtenerProductosPorVencer() throws SQLException {
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
    
    /** Obtiene productos vencidos */
    public List<Product> obtenerProductosVencidos() throws SQLException {
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
    
    /** Desactiva todos los productos vencidos */
    public int desactivarProductosVencidos() throws SQLException {
        String consultaSQL = "UPDATE productos " +
                     "SET activo = FALSE, stock = 0 " +
                     "WHERE fecha_vencimiento < CURRENT_DATE AND activo = TRUE";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement()) {
            
            return consulta.executeUpdate(consultaSQL);
        }
    }
    
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

