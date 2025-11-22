package com.novafarma.service;

import com.novafarma.dao.ProductDAO;
import com.novafarma.dao.SaleDAO;
import com.novafarma.model.Product;
import com.novafarma.model.Sale;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de lógica de negocio para Ventas
 * 
 * Responsabilidades:
 * - Validación de ventas (stock, vencimiento)
 * - Orquestación de transacciones de venta
 * - Cálculo de totales
 * - Coordinación entre ProductDAO y SaleDAO
 * 
 * IMPORTANTE: El trigger de PostgreSQL actualiza automáticamente
 * el stock al insertar una venta. Java NO debe hacerlo.
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class SaleService {
    
    private final SaleDAO saleDAO;
    private final ProductDAO productDAO;
    private final ProductService productService;
    
    /**
     * Constructor
     */
    public SaleService() {
        this.saleDAO = new SaleDAO();
        this.productDAO = new ProductDAO();
        this.productService = new ProductService();
    }
    
    // ==================== CONSULTAS ====================
    
    /**
     * Obtiene todas las ventas
     * 
     * @return Lista de ventas
     * @throws SQLException Si hay error en la consulta
     */
    public List<Sale> getAllSales() throws SQLException {
        return saleDAO.findAll();
    }
    
    /**
     * Obtiene ventas de un usuario específico
     * 
     * @param userId ID del usuario
     * @return Lista de ventas del usuario
     * @throws SQLException Si hay error en la consulta
     */
    public List<Sale> getSalesByUser(int userId) throws SQLException {
        return saleDAO.findByUserId(userId);
    }
    
    /**
     * Obtiene el total de ingresos
     * 
     * @return Suma total de todas las ventas
     * @throws SQLException Si hay error en la consulta
     */
    public double getTotalRevenue() throws SQLException {
        return saleDAO.calculateTotalRevenue();
    }
    
    // ==================== OPERACIONES DE VENTA ====================
    
    /**
     * Procesa una venta individual
     * 
     * @param sale Venta a procesar
     * @return true si la venta fue exitosa
     * @throws IllegalStateException Si la validación falla
     * @throws SQLException Si hay error en la BD
     */
    public boolean processSale(Sale sale) throws SQLException {
        // Validar datos de la venta
        if (!sale.isValid()) {
            throw new IllegalStateException("Datos de venta inválidos");
        }
        
        // Obtener producto y validar
        Product product = productDAO.findById(sale.getProductoId());
        productService.validateSellableProduct(product, sale.getCantidad());
        
        // Asegurar que el total esté actualizado
        sale.updateTotal();
        
        // Registrar venta (el trigger actualiza el stock automáticamente)
        return saleDAO.save(sale);
    }
    
    /**
     * Procesa múltiples ventas en una transacción atómica
     * Si una venta falla, todas se revierten
     * 
     * @param sales Lista de ventas a procesar
     * @return SaleResult con el resultado de la operación
     */
    public SaleResult processMultipleSales(List<Sale> sales) {
        SaleResult result = new SaleResult();
        List<String> errors = new ArrayList<>();
        
        try {
            // Validar todas las ventas ANTES de procesarlas
            for (Sale sale : sales) {
                try {
                    Product product = productDAO.findById(sale.getProductoId());
                    productService.validateSellableProduct(product, sale.getCantidad());
                    sale.updateTotal();
                } catch (IllegalStateException | SQLException e) {
                    errors.add("Producto ID " + sale.getProductoId() + ": " + e.getMessage());
                }
            }
            
            // Si hay errores de validación, no procesar ninguna venta
            if (!errors.isEmpty()) {
                result.setSuccess(false);
                result.setErrors(errors);
                result.setMessage("Validación fallida. No se procesó ninguna venta.");
                return result;
            }
            
            // Todas las validaciones pasaron, procesar ventas en transacción
            boolean success = saleDAO.saveAll(sales);
            
            if (success) {
                result.setSuccess(true);
                result.setSuccessfulSales(sales.size());
                result.setTotalAmount(calculateTotalAmount(sales));
                result.setTotalUnits(calculateTotalUnits(sales));
                result.setMessage("Venta completada exitosamente");
            } else {
                result.setSuccess(false);
                result.setMessage("Error al procesar las ventas en la base de datos");
            }
            
        } catch (SQLException e) {
            result.setSuccess(false);
            result.setMessage("Error de base de datos: " + e.getMessage());
            errors.add(e.getMessage());
            result.setErrors(errors);
        }
        
        return result;
    }
    
    /**
     * Valida que un carrito de compras sea procesable
     * Verifica:
     * - Que no haya productos vencidos
     * - Que haya stock suficiente
     * - Que los productos estén activos
     * 
     * @param sales Lista de ventas (carrito)
     * @return Lista de errores (vacía si todo está OK)
     */
    public List<String> validateCart(List<Sale> sales) {
        List<String> errors = new ArrayList<>();
        
        if (sales == null || sales.isEmpty()) {
            errors.add("El carrito está vacío");
            return errors;
        }
        
        try {
            for (Sale sale : sales) {
                Product product = productDAO.findById(sale.getProductoId());
                
                if (product == null) {
                    errors.add("Producto ID " + sale.getProductoId() + " no existe");
                    continue;
                }
                
                // Validar vencimiento
                if (product.isExpired()) {
                    errors.add("❌ " + product.getNombre() + " está VENCIDO. Debe retirarlo del carrito.");
                }
                
                // Validar estado activo
                if (!product.isActivo()) {
                    errors.add("⚠️ " + product.getNombre() + " está inactivo");
                }
                
                // Validar stock
                if (!product.hasEnoughStock(sale.getCantidad())) {
                    errors.add("⚠️ " + product.getNombre() + " - Stock insuficiente. " +
                             "Disponible: " + product.getStock() + ", Solicitado: " + sale.getCantidad());
                }
            }
        } catch (SQLException e) {
            errors.add("Error al validar carrito: " + e.getMessage());
        }
        
        return errors;
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Calcula el monto total de una lista de ventas
     * 
     * @param sales Lista de ventas
     * @return Suma total
     */
    private double calculateTotalAmount(List<Sale> sales) {
        return sales.stream()
                    .mapToDouble(Sale::getTotal)
                    .sum();
    }
    
    /**
     * Calcula el total de unidades vendidas
     * 
     * @param sales Lista de ventas
     * @return Suma de unidades
     */
    private int calculateTotalUnits(List<Sale> sales) {
        return sales.stream()
                    .mapToInt(Sale::getCantidad)
                    .sum();
    }
    
    // ==================== CLASE INTERNA: RESULTADO DE VENTA ====================
    
    /**
     * Clase que encapsula el resultado de una operación de venta
     */
    public static class SaleResult {
        private boolean success;
        private int successfulSales;
        private int failedSales;
        private double totalAmount;
        private int totalUnits;
        private String message;
        private List<String> errors;
        
        public SaleResult() {
            this.errors = new ArrayList<>();
        }
        
        // Getters y Setters
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public int getSuccessfulSales() {
            return successfulSales;
        }
        
        public void setSuccessfulSales(int successfulSales) {
            this.successfulSales = successfulSales;
        }
        
        public int getFailedSales() {
            return failedSales;
        }
        
        public void setFailedSales(int failedSales) {
            this.failedSales = failedSales;
        }
        
        public double getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
        }
        
        public int getTotalUnits() {
            return totalUnits;
        }
        
        public void setTotalUnits(int totalUnits) {
            this.totalUnits = totalUnits;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }
}

