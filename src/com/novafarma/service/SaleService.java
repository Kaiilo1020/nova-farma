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
     * Obtiene ventas con paginación
     * OPTIMIZACIÓN: Para manejar grandes volúmenes de datos
     * 
     * @param limit Número máximo de registros a retornar
     * @param offset Número de registros a saltar (para paginación)
     * @return Lista de ventas
     * @throws SQLException Si hay error en la consulta
     */
    public List<Sale> getSalesPaginated(int limit, int offset) throws SQLException {
        return saleDAO.findAll(limit, offset);
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
     * Cuenta el número total de ventas
     * 
     * @return Número total de ventas
     * @throws SQLException Si hay error en la consulta
     */
    public int countAllSales() throws SQLException {
        return saleDAO.countAll();
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
    public boolean processSale(Sale venta) throws SQLException {
        // Validar datos de la venta
        if (!venta.isValid()) {
            throw new IllegalStateException("Datos de venta inválidos");
        }
        
        // Obtener producto y validar
        Product producto = productDAO.findById(venta.getProductoId());
        productService.validateSellableProduct(producto, venta.getCantidad());
        
        // Asegurar que el total esté actualizado
        venta.updateTotal();
        
        // Registrar venta (el trigger actualiza el stock automáticamente)
        return saleDAO.save(venta);
    }
    
    /**
     * Procesa múltiples ventas en una transacción atómica
     * Si una venta falla, todas se revierten
     * 
     * @param sales Lista de ventas a procesar
     * @return SaleResult con el resultado de la operación
     */
    public SaleResult processMultipleSales(List<Sale> ventas) {
        SaleResult resultado = new SaleResult();
        List<String> errores = new ArrayList<>();
        
        try {
            // Validar todas las ventas ANTES de procesarlas
            for (Sale venta : ventas) {
                try {
                    Product producto = productDAO.findById(venta.getProductoId());
                    productService.validateSellableProduct(producto, venta.getCantidad());
                    venta.updateTotal();
                } catch (IllegalStateException | SQLException e) {
                    errores.add("Producto ID " + venta.getProductoId() + ": " + e.getMessage());
                }
            }
            
            // Si hay errores de validación, no procesar ninguna venta
            if (!errores.isEmpty()) {
                resultado.setSuccess(false);
                resultado.setErrors(errores);
                resultado.setMessage("Validación fallida. No se procesó ninguna venta.");
                return resultado;
            }
            
            // Todas las validaciones pasaron, procesar ventas en transacción
            boolean exito = saleDAO.saveAll(ventas);
            
            if (exito) {
                resultado.setSuccess(true);
                resultado.setSuccessfulSales(ventas.size());
                resultado.setTotalAmount(calculateTotalAmount(ventas));
                resultado.setTotalUnits(calculateTotalUnits(ventas));
                resultado.setMessage("Venta completada exitosamente");
            } else {
                resultado.setSuccess(false);
                resultado.setMessage("Error al procesar las ventas en la base de datos");
            }
            
        } catch (SQLException e) {
            resultado.setSuccess(false);
            resultado.setMessage("Error de base de datos: " + e.getMessage());
            errores.add(e.getMessage());
            resultado.setErrors(errores);
        }
        
        return resultado;
    }
    
    /**
     * Valida que un carrito de compras sea procesable
     * Verifica:
     * - Que no haya productos vencidos
     * - Que haya stock suficiente
     * - Que los productos estén activos
     * 
     * @param ventas Lista de ventas (carrito)
     * @return Lista de errores (vacía si todo está OK)
     */
    public List<String> validateCart(List<Sale> ventas) {
        List<String> errores = new ArrayList<>();
        
        if (ventas == null || ventas.isEmpty()) {
            errores.add("El carrito está vacío");
            return errores;
        }
        
        try {
            for (Sale venta : ventas) {
                Product producto = productDAO.findById(venta.getProductoId());
                
                if (producto == null) {
                    errores.add("Producto ID " + venta.getProductoId() + " no existe");
                    continue;
                }
                
                // Validar vencimiento
                if (producto.isExpired()) {
                    errores.add(producto.getNombre() + " está VENCIDO. Debe retirarlo del carrito.");
                }
                
                // Validar estado activo
                if (!producto.isActivo()) {
                    errores.add(producto.getNombre() + " está inactivo");
                }
                
                // Validar stock
                if (!producto.hasEnoughStock(venta.getCantidad())) {
                    errores.add(producto.getNombre() + " - Stock insuficiente. " +
                             "Disponible: " + producto.getStock() + ", Solicitado: " + venta.getCantidad());
                }
            }
        } catch (SQLException e) {
            errores.add("Error al validar carrito: " + e.getMessage());
        }
        
        return errores;
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Calcula el monto total de una lista de ventas
     * 
     * @param ventas Lista de ventas
     * @return Suma total
     */
    private double calculateTotalAmount(List<Sale> ventas) {
        return ventas.stream()
                    .mapToDouble(Sale::getTotal)
                    .sum();
    }
    
    /**
     * Calcula el total de unidades vendidas
     * 
     * @param ventas Lista de ventas
     * @return Suma de unidades
     */
    private int calculateTotalUnits(List<Sale> ventas) {
        return ventas.stream()
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

