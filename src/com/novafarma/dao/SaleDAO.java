package com.novafarma.dao;

import com.novafarma.model.Sale;
import com.novafarma.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO para operaciones CRUD en la tabla ventas (el trigger actualiza stock automáticamente) */
public class SaleDAO {
    
    /** Inserta una venta (el trigger actualiza stock automáticamente) */
    public boolean guardarVenta(Sale venta) throws SQLException {
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
    
    /** Inserta múltiples ventas en transacción (si falla una, todas se revierten) */
    public boolean guardarVentas(List<Sale> ventas) throws SQLException {
        Connection conexion = null;
        PreparedStatement consultaPreparada = null;
        
        try {
            conexion = DatabaseConnection.getConnection();
            conexion.setAutoCommit(false);
            
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
            conexion.commit();
            return true;
            
        } catch (SQLException e) {
            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException excepcionRollback) {
                    excepcionRollback.printStackTrace();
                }
            }
            throw e;
            
        } finally {
            if (consultaPreparada != null) consultaPreparada.close();
            if (conexion != null) {
                conexion.setAutoCommit(true);
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
    public List<Sale> obtenerTodasLasVentas() throws SQLException {
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
    public List<Sale> obtenerVentasPaginadas(int limit, int offset) throws SQLException {
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
    public List<Sale> obtenerVentasPorUsuario(int usuarioId) throws SQLException {
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
    public List<Sale> obtenerVentasPorProducto(int productoId) throws SQLException {
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
    public List<Sale> obtenerVentasPorRangoFechas(Timestamp fechaInicio, Timestamp fechaFin) throws SQLException {
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
    public int contarVentas() throws SQLException {
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
    public double calcularIngresosTotales() throws SQLException {
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

