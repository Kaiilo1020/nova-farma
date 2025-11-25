package com.novafarma.model;

import java.sql.Timestamp;

/**
 * Entidad que representa una Venta en el sistema Nova Farma
 * 
 * Una venta registra la transacción de un producto específico,
 * incluyendo cantidad, precio unitario y total.
 * 
 * IMPORTANTE: El trigger de PostgreSQL actualiza automáticamente
 * el stock del producto al insertar una venta.
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class Sale {
    
    private int id;
    private int productoId;
    private int usuarioId;
    private int cantidad;
    private double precioUnitario;
    private double total;
    private Timestamp fechaVenta;
    
    /**
     * Constructor vacío
     */
    public Sale() {
    }
    
    /**
     * Constructor completo
     */
    public Sale(int id, int productoId, int usuarioId, int cantidad, 
                double precioUnitario, double total, Timestamp fechaVenta) {
        this.id = id;
        this.productoId = productoId;
        this.usuarioId = usuarioId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.total = total;
        this.fechaVenta = fechaVenta;
    }
    
    /**
     * Constructor para nueva venta (sin ID ni fecha)
     * El ID y la fecha se generan automáticamente en la BD
     */
    public Sale(int productoId, int usuarioId, int cantidad, double precioUnitario) {
        this.productoId = productoId;
        this.usuarioId = usuarioId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.total = cantidad * precioUnitario; // Cálculo automático
    }
    
    /**
     * Calcula el total de la venta basado en cantidad y precio unitario
     * 
     * @return Total calculado
     */
    public double calcularTotal() {
        return this.cantidad * this.precioUnitario;
    }
    
    /**
     * Actualiza el total después de cambiar cantidad o precio
     */
    public void actualizarTotal() {
        this.total = calcularTotal();
    }
    
    /**
     * Valida que los datos de la venta sean correctos
     * 
     * @return true si la venta es válida
     */
    public boolean esValida() {
        return cantidad > 0 && 
               precioUnitario > 0 && 
               productoId > 0 && 
               usuarioId > 0;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getProductoId() {
        return productoId;
    }
    
    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }
    
    public int getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public int getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        actualizarTotal(); // Recalcular total automáticamente
    }
    
    public double getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        actualizarTotal(); // Recalcular total automáticamente
    }
    
    public double getTotal() {
        return total;
    }
    
    public void setTotal(double total) {
        this.total = total;
    }
    
    public Timestamp getFechaVenta() {
        return fechaVenta;
    }
    
    public void setFechaVenta(Timestamp fechaVenta) {
        this.fechaVenta = fechaVenta;
    }
    
    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", productoId=" + productoId +
                ", usuarioId=" + usuarioId +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", total=" + total +
                ", fechaVenta=" + fechaVenta +
                '}';
    }
}

