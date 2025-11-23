package com.novafarma.dao;

import com.novafarma.model.Sale;
import com.novafarma.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Sale
 * 
 * Responsable de todas las operaciones CRUD en la tabla 'ventas'
 * 
 * IMPORTANTE: El trigger 'trigger_actualizar_stock' se ejecuta automáticamente
 * después de cada INSERT en la tabla ventas, descargando el stock del producto.
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class SaleDAO {
    
    /**
     * Inserta una nueva venta en la base de datos
     * 
     * CRÍTICO: Esta operación NO actualiza el stock manualmente.
     * El trigger de PostgreSQL 'trigger_actualizar_stock' lo hace automáticamente.
     * 
     * @param sale Venta a registrar
     * @return true si la inserción fue exitosa
     * @throws SQLException Si hay error en la inserción o stock insuficiente
     */
    public boolean save(Sale sale) throws SQLException {
        String sql = "INSERT INTO ventas (producto_id, usuario_id, cantidad, precio_unitario, total) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sale.getProductoId());
            pstmt.setInt(2, sale.getUsuarioId());
            pstmt.setInt(3, sale.getCantidad());
            pstmt.setDouble(4, sale.getPrecioUnitario());
            pstmt.setDouble(5, sale.getTotal());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Inserta múltiples ventas en una transacción
     * Si alguna falla, todas se revierten (atomicidad)
     * 
     * @param sales Lista de ventas a registrar
     * @return true si todas las ventas fueron exitosas
     * @throws SQLException Si hay error en alguna inserción
     */
    public boolean saveAll(List<Sale> sales) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción
            
            String sql = "INSERT INTO ventas (producto_id, usuario_id, cantidad, precio_unitario, total) " +
                         "VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            
            for (Sale sale : sales) {
                pstmt.setInt(1, sale.getProductoId());
                pstmt.setInt(2, sale.getUsuarioId());
                pstmt.setInt(3, sale.getCantidad());
                pstmt.setDouble(4, sale.getPrecioUnitario());
                pstmt.setDouble(5, sale.getTotal());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            conn.commit(); // Confirmar transacción
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Revertir en caso de error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e; // Relanzar excepción
            
        } finally {
            if (pstmt != null) pstmt.close();
            if (conn != null) {
                conn.setAutoCommit(true); // Restaurar auto-commit
                conn.close();
            }
        }
    }
    
    /**
     * Obtiene todas las ventas ordenadas por fecha (más reciente primero)
     * 
     * @return Lista de todas las ventas
     * @throws SQLException Si hay error en la consulta
     */
    public List<Sale> findAll() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT id, producto_id, usuario_id, cantidad, precio_unitario, total, fecha_venta " +
                     "FROM ventas ORDER BY fecha_venta DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Sale sale = mapResultSetToSale(rs);
                sales.add(sale);
            }
        }
        
        return sales;
    }
    
    /**
     * Obtiene ventas con paginación
     * OPTIMIZACIÓN: Para manejar grandes volúmenes de datos
     * 
     * @param limit Número máximo de registros a retornar
     * @param offset Número de registros a saltar (para paginación)
     * @return Lista de ventas ordenadas por fecha (más reciente primero)
     * @throws SQLException Si hay error en la consulta
     */
    public List<Sale> findAll(int limit, int offset) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT id, producto_id, usuario_id, cantidad, precio_unitario, total, fecha_venta " +
                     "FROM ventas ORDER BY fecha_venta DESC LIMIT ? OFFSET ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sales.add(sale);
                }
            }
        }
        
        return sales;
    }
    
    /**
     * Obtiene ventas de un usuario específico
     * 
     * @param usuarioId ID del usuario
     * @return Lista de ventas del usuario
     * @throws SQLException Si hay error en la consulta
     */
    public List<Sale> findByUserId(int usuarioId) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT id, producto_id, usuario_id, cantidad, precio_unitario, total, fecha_venta " +
                     "FROM ventas WHERE usuario_id = ? ORDER BY fecha_venta DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, usuarioId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sales.add(sale);
                }
            }
        }
        
        return sales;
    }
    
    /**
     * Obtiene ventas de un producto específico
     * 
     * @param productoId ID del producto
     * @return Lista de ventas del producto
     * @throws SQLException Si hay error en la consulta
     */
    public List<Sale> findByProductId(int productoId) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT id, producto_id, usuario_id, cantidad, precio_unitario, total, fecha_venta " +
                     "FROM ventas WHERE producto_id = ? ORDER BY fecha_venta DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productoId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sales.add(sale);
                }
            }
        }
        
        return sales;
    }
    
    /**
     * Obtiene ventas en un rango de fechas
     * 
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @return Lista de ventas en el rango
     * @throws SQLException Si hay error en la consulta
     */
    public List<Sale> findByDateRange(Timestamp startDate, Timestamp endDate) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT id, producto_id, usuario_id, cantidad, precio_unitario, total, fecha_venta " +
                     "FROM ventas WHERE fecha_venta BETWEEN ? AND ? ORDER BY fecha_venta DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, startDate);
            pstmt.setTimestamp(2, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sales.add(sale);
                }
            }
        }
        
        return sales;
    }
    
    /**
     * Cuenta el número total de ventas
     * 
     * @return Número de ventas registradas
     * @throws SQLException Si hay error en la consulta
     */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM ventas";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        
        return 0;
    }
    
    /**
     * Calcula el total de ingresos de todas las ventas
     * 
     * @return Suma total de ventas
     * @throws SQLException Si hay error en la consulta
     */
    public double calculateTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(total) as ingresos FROM ventas";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("ingresos");
            }
        }
        
        return 0.0;
    }
    
    // ==================== MÉTODO AUXILIAR ====================
    
    /**
     * Mapea un ResultSet a un objeto Sale
     * 
     * @param rs ResultSet con datos de la venta
     * @return Objeto Sale
     * @throws SQLException Si hay error al leer los datos
     */
    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int productoId = rs.getInt("producto_id");
        int usuarioId = rs.getInt("usuario_id");
        int cantidad = rs.getInt("cantidad");
        double precioUnitario = rs.getDouble("precio_unitario");
        double total = rs.getDouble("total");
        Timestamp fechaVenta = rs.getTimestamp("fecha_venta");
        
        return new Sale(id, productoId, usuarioId, cantidad, precioUnitario, total, fechaVenta);
    }
}

