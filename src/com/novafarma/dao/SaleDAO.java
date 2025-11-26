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
     * @param start Date Fecha inicial
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
    
    /** Obtiene todas las ventas del día actual */
    public List<Sale> obtenerVentasDelDiaActual() throws SQLException {
        List<Sale> ventas = new ArrayList<>();
        String consultaSQL = "SELECT * FROM ventas WHERE DATE(fecha_venta) = CURRENT_DATE ORDER BY fecha_venta DESC";
        
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
    
    /** Obtiene resumen de ventas del día agrupado por trabajador */
    public List<ReporteVentasPorTrabajador> obtenerResumenVentasPorTrabajador() throws SQLException {
        List<ReporteVentasPorTrabajador> reporte = new ArrayList<>();
        String consultaSQL = """
            SELECT 
                u.id as usuario_id,
                u.username,
                COUNT(v.id) as total_ventas,
                SUM(v.cantidad) as total_productos,
                SUM(v.total) as total_dinero
            FROM usuarios u
            LEFT JOIN ventas v ON u.id = v.usuario_id AND DATE(v.fecha_venta) = CURRENT_DATE
            GROUP BY u.id, u.username
            HAVING COUNT(v.id) > 0
            ORDER BY total_dinero DESC
            """;
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement();
             ResultSet resultadoConsulta = consulta.executeQuery(consultaSQL)) {
            
            while (resultadoConsulta.next()) {
                int usuarioId = resultadoConsulta.getInt("usuario_id");
                String username = resultadoConsulta.getString("username");
                int totalVentas = resultadoConsulta.getInt("total_ventas");
                int totalProductos = resultadoConsulta.getInt("total_productos");
                double totalDinero = resultadoConsulta.getDouble("total_dinero");
                
                ReporteVentasPorTrabajador reporteUsuario = new ReporteVentasPorTrabajador(
                    usuarioId, username, totalVentas, totalProductos, totalDinero
                );
                reporte.add(reporteUsuario);
            }
        }
        
        return reporte;
    }
    
    /** Obtiene el resumen total de ventas del día */
    public ResumenTotalDelDia obtenerResumenTotalDelDia() throws SQLException {
        String consultaSQL = """
            SELECT 
                COUNT(*) as total_transacciones,
                SUM(cantidad) as total_productos_vendidos,
                SUM(total) as total_ingresos
            FROM ventas 
            WHERE DATE(fecha_venta) = CURRENT_DATE
            """;
        
        try (Connection conexion = DatabaseConnection.getConnection();
             Statement consulta = conexion.createStatement();
             ResultSet resultadoConsulta = consulta.executeQuery(consultaSQL)) {
            
            if (resultadoConsulta.next()) {
                int totalTransacciones = resultadoConsulta.getInt("total_transacciones");
                int totalProductos = resultadoConsulta.getInt("total_productos_vendidos");
                double totalIngresos = resultadoConsulta.getDouble("total_ingresos");
                
                return new ResumenTotalDelDia(totalTransacciones, totalProductos, totalIngresos);
            }
        }
        
        return new ResumenTotalDelDia(0, 0, 0.0);
    }
    
    /** Clase para el reporte de ventas por trabajador */
    public static class ReporteVentasPorTrabajador {
        private int usuarioId;
        private String username;
        private int totalVentas;
        private int totalProductos;
        private double totalDinero;
        
        public ReporteVentasPorTrabajador(int usuarioId, String username, int totalVentas, int totalProductos, double totalDinero) {
            this.usuarioId = usuarioId;
            this.username = username;
            this.totalVentas = totalVentas;
            this.totalProductos = totalProductos;
            this.totalDinero = totalDinero;
        }
        
        // Getters
        public int getUsuarioId() { return usuarioId; }
        public String getUsername() { return username; }
        public int getTotalVentas() { return totalVentas; }
        public int getTotalProductos() { return totalProductos; }
        public double getTotalDinero() { return totalDinero; }
    }
    
    /** Clase para el resumen total del día */
    public static class ResumenTotalDelDia {
        private int totalTransacciones;
        private int totalProductos;
        private double totalIngresos;
        
        public ResumenTotalDelDia(int totalTransacciones, int totalProductos, double totalIngresos) {
            this.totalTransacciones = totalTransacciones;
            this.totalProductos = totalProductos;
            this.totalIngresos = totalIngresos;
        }
        
        // Getters
        public int getTotalTransacciones() { return totalTransacciones; }
        public int getTotalProductos() { return totalProductos; }
        public double getTotalIngresos() { return totalIngresos; }
    }
}

