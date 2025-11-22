package com.novafarma.model;

/**
 * Clase que representa un ítem del carrito de ventas
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class CarritoItem {
    
    private int productoId;
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    
    public CarritoItem(int productoId, String nombreProducto, int cantidad, double precioUnitario) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
    }
    
    // Getters y Setters
    
    public int getProductoId() {
        return productoId;
    }
    
    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }
    
    public String getNombreProducto() {
        return nombreProducto;
    }
    
    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
    
    public int getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        recalcularSubtotal();
    }
    
    public double getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        recalcularSubtotal();
    }
    
    public double getSubtotal() {
        return subtotal;
    }
    
    /**
     * Recalcula el subtotal cuando cambia cantidad o precio
     */
    private void recalcularSubtotal() {
        this.subtotal = this.cantidad * this.precioUnitario;
    }
    
    @Override
    public String toString() {
        return "CarritoItem{" +
                "productoId=" + productoId +
                ", nombreProducto='" + nombreProducto + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}

