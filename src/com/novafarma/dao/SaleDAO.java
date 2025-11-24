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
    public boolean save(Sale venta) throws SQLException {
        String consultaSQL = "INSERT INTO ventas (producto_id, usuario_id, cantidad, precio_unitario, total) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, venta.getProductoId());
            consultaPreparada.setInt(2, venta.getUsuarioId());
            consultaPreparada.setInt(3, venta.getCantidad());
            consultaPreparada.setDouble(4, venta.getPrecioUnitario());
            consultaPreparada.setDouble(5, venta.getTotal());
            
            return consultaPreparada.executeUpdate() > 0;
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
    public boolean saveAll(List<Sale> ventas) throws SQLException {
        Connection conexion = null;
        PreparedStatement consultaPreparada = null;
        
        try {
            conexion = DatabaseConnection.getConnection();
            conexion.setAutoCommit(false); // Iniciar transacción
            
            String consultaSQL = "INSERT INTO ventas (producto_id, usuario_id, cantidad, precio_unitario, total) " +
                         "VALUES (?, ?, ?, ?, ?)";
            consultaPreparada = conexion.prepareStatement(consultaSQL);
            
            for (Sale venta : ventas) {
                consultaPreparada.setInt(1, venta.getProductoId());
                consultaPreparada.setInt(2, venta.getUsuarioId());
                consultaPreparada.setInt(3, venta.getCantidad());
                consultaPreparada.setDouble(4, venta.getPrecioUnitario());
                consultaPreparada.setDouble(5, venta.getTotal());
                consultaPreparada.addBatch();
            }
            
            consultaPreparada.executeBatch();
            conexion.commit(); // Confirmar transacción
            return true;
            
        } catch (SQLException e) {
            if (conexion != null) {
                try {
                    conexion.rollback(); // Revertir en caso de error
                } catch (SQLException excepcionRollback) {
                    excepcionRollback.printStackTrace();
                }
            }
            throw e; // Relanzar excepción
            
        } finally {
            if (consultaPreparada != null) consultaPreparada.close();
            if (conexion != null) {
                conexion.setAutoCommit(true); // Restaurar auto-commit
                conexion.close();
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
        List<Sale> ventas = new ArrayList<>();
        String consultaSQL = "SELECT id, producto_id, usuario_id, cantidad, precio_unitario, total, fecha_venta " +
                     "FROM ventas ORDER BY fecha_venta DESC";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement();
             ResultSet resultadoConsulta = consulta.executeQuery(consultaSQL)) {
            
            while (resultadoConsulta.next()) {
                Sale venta = mapearResultadoAVenta(resultadoConsulta);
                ventas.add(venta);
            }
        }
        
        return ventas;
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
        List<Sale> ventas = new ArrayList<>();
        String consultaSQL = "SELECT id, producto_id, usuario_id, cantidad, precio_unitario, total, fecha_venta " +
                     "FROM ventas ORDER BY fecha_venta DESC LIMIT ? OFFSET ?";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, limit);
            consultaPreparada.setInt(2, offset);
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                while (resultadoConsulta.next()) {
                    Sale venta = mapearResultadoAVenta(resultadoConsulta);
                    ventas.add(venta);
                }
            }
        }
        
        return ventas;
    }
    
    /**
     * Obtiene ventas de un usuario específico
     * 
     * @param usuarioId ID del usuario
     * @return Lista de ventas del usuario
     * @throws SQLException Si hay error en la consulta
     */
    public List<Sale> findByUserId(int usuarioId) throws SQLException {
        List<Sale> ventas = new ArrayList<>();
        String consultaSQL = "SELECT id, producto_id, usuario_id, cantidad, precio_unitario, total, fecha_venta " +
                     "FROM ventas WHERE usuario_id = ? ORDER BY fecha_venta DESC";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, usuarioId);
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                while (resultadoConsulta.next()) {
                    Sale venta = mapearResultadoAVenta(resultadoConsulta);
                    ventas.add(venta);
                }
            }
        }
        
        return ventas;
    }
    
    /**
     * Obtiene ventas de un producto específico
     * 
     * @param productoId ID del producto
     * @return Lista de ventas del producto
     * @throws SQLException Si hay error en la consulta
     */
    public List<Sale> findByProductId(int productoId) throws SQLException {
        List<Sale> ventas = new ArrayList<>();
        String consultaSQL = "SELECT id, producto_id, usuario_id, cantidad, precio_unitario, total, fecha_venta " +
                     "FROM ventas WHERE producto_id = ? ORDER BY fecha_venta DESC";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setInt(1, productoId);
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                while (resultadoConsulta.next()) {
                    Sale venta = mapearResultadoAVenta(resultadoConsulta);
                    ventas.add(venta);
                }
            }
        }
        
        return ventas;
    }
    
    /**
     * Obtiene ventas en un rango de fechas
     * 
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @return Lista de ventas en el rango
     * @throws SQLException Si hay error en la consulta
     */
    public List<Sale> findByDateRange(Timestamp fechaInicio, Timestamp fechaFin) throws SQLException {
        List<Sale> ventas = new ArrayList<>();
        String consultaSQL = "SELECT id, producto_id, usuario_id, cantidad, precio_unitario, total, fecha_venta " +
                     "FROM ventas WHERE fecha_venta BETWEEN ? AND ? ORDER BY fecha_venta DESC";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             PreparedStatement consultaPreparada = conexion.prepareStatement(consultaSQL)) {
            
            consultaPreparada.setTimestamp(1, fechaInicio);
            consultaPreparada.setTimestamp(2, fechaFin);
            
            try (ResultSet resultadoConsulta = consultaPreparada.executeQuery()) {
                while (resultadoConsulta.next()) {
                    Sale venta = mapearResultadoAVenta(resultadoConsulta);
                    ventas.add(venta);
                }
            }
        }
        
        return ventas;
    }
    
    /**
     * Cuenta el número total de ventas
     * 
     * @return Número de ventas registradas
     * @throws SQLException Si hay error en la consulta
     */
    public int countAll() throws SQLException {
        String consultaSQL = "SELECT COUNT(*) as total FROM ventas";
        
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
     * Calcula el total de ingresos de todas las ventas
     * 
     * @return Suma total de ventas
     * @throws SQLException Si hay error en la consulta
     */
    public double calculateTotalRevenue() throws SQLException {
        String consultaSQL = "SELECT SUM(total) as ingresos FROM ventas";
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement();
             ResultSet resultadoConsulta = consulta.executeQuery(consultaSQL)) {
            
            if (resultadoConsulta.next()) {
                return resultadoConsulta.getDouble("ingresos");
            }
        }
        
        return 0.0;
    }
    
    // ==================== MÉTODO AUXILIAR ====================
    
    /**
     * Mapea un ResultSet a un objeto Sale
     * 
     * @param resultadoConsulta ResultSet con datos de la venta
     * @return Objeto Sale
     * @throws SQLException Si hay error al leer los datos
     */
    private Sale mapearResultadoAVenta(ResultSet resultadoConsulta) throws SQLException {
        int id = resultadoConsulta.getInt("id");
        int productoId = resultadoConsulta.getInt("producto_id");
        int usuarioId = resultadoConsulta.getInt("usuario_id");
        int cantidad = resultadoConsulta.getInt("cantidad");
        double precioUnitario = resultadoConsulta.getDouble("precio_unitario");
        double total = resultadoConsulta.getDouble("total");
        Timestamp fechaVenta = resultadoConsulta.getTimestamp("fecha_venta");
        
        return new Sale(id, productoId, usuarioId, cantidad, precioUnitario, total, fechaVenta);
    }
}

