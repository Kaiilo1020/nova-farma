package com.novafarma.model;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Entidad que representa un Producto en el sistema Nova Farma
 * 
 * Contiene la lógica de dominio relacionada con productos:
 * - Validación de vencimiento
 * - Cálculo de días restantes
 * - Estado de activación
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class Product {
    
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;
    private Date fechaVencimiento;
    private boolean activo;
    
    // Constructores
    
    /**
     * Constructor vacío
     */
    public Product() {
        this.activo = true; // Por defecto, productos nuevos están activos
    }
    
    /**
     * Constructor completo
     */
    public Product(int id, String nombre, String descripcion, double precio, 
                   int stock, Date fechaVencimiento, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.fechaVencimiento = fechaVencimiento;
        this.activo = activo;
    }
    
    /**
     * Constructor sin ID (para inserciones)
     */
    public Product(String nombre, String descripcion, double precio, 
                   int stock, Date fechaVencimiento) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.fechaVencimiento = fechaVencimiento;
        this.activo = true;
    }
    
    // Lógica de negocio
    
    /**
     * Verifica si el producto está vencido
     * 
     * @return true si la fecha de vencimiento es anterior a hoy
     */
    public boolean estaVencido() {
        if (fechaVencimiento == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate expirationDate = fechaVencimiento.toLocalDate();
        return expirationDate.isBefore(today);
    }
    
    /**
     * Verifica si el producto vence pronto (≤ 30 días)
     * 
     * @return true si vence en los próximos 30 días (pero no está vencido)
     */
    public boolean vencePronto() {
        if (fechaVencimiento == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate expirationDate = fechaVencimiento.toLocalDate();
        
        long daysRemaining = ChronoUnit.DAYS.between(today, expirationDate);
        return daysRemaining >= 0 && daysRemaining <= 30;
    }
    
    /**
     * Calcula los días restantes hasta el vencimiento
     * 
     * @return Número de días (positivo si no vencido, negativo si vencido)
     */
    public long obtenerDiasHastaVencimiento() {
        if (fechaVencimiento == null) {
            return Long.MAX_VALUE;
        }
        LocalDate today = LocalDate.now();
        LocalDate expirationDate = fechaVencimiento.toLocalDate();
        return ChronoUnit.DAYS.between(today, expirationDate);
    }
    
    /**
     * Verifica si hay stock disponible
     * 
     * @return true si el stock es mayor a 0
     */
    public boolean tieneStock() {
        return this.stock > 0;
    }
    
    /**
     * Verifica si hay suficiente stock para una cantidad solicitada
     * 
     * @param quantityRequested Cantidad solicitada
     * @return true si hay suficiente stock
     */
    public boolean tieneStockSuficiente(int cantidadSolicitada) {
        return this.stock >= cantidadSolicitada;
    }
    
    /**
     * Valida que el producto sea vendible
     * Un producto es vendible si:
     * - Está activo
     * - No está vencido
     * - Tiene stock disponible
     * 
     * @return true si el producto puede venderse
     */
    public boolean esVendible() {
        return this.activo && !estaVencido() && tieneStock();
    }
    
    // Getters y Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public double getPrecio() {
        return precio;
    }
    
    public void setPrecio(double precio) {
        this.precio = precio;
    }
    
    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }
    
    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }
    
    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    // Métodos de objeto
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                ", activo=" + activo +
                '}';
    }
}

