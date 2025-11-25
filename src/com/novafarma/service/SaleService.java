package com.novafarma.service;

import com.novafarma.dao.ProductDAO;
import com.novafarma.dao.SaleDAO;
import com.novafarma.model.Product;
import com.novafarma.model.Sale;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Servicio de lógica de negocio para Ventas (el trigger actualiza stock automáticamente) */
public class SaleService {
    
    private final SaleDAO saleDAO;
    private final ProductDAO productDAO;
    private final ProductService productService;
    
    public SaleService() {
        this.saleDAO = new SaleDAO();
        this.productDAO = new ProductDAO();
        this.productService = new ProductService();
    }
    
    /** Obtiene todas las ventas */
    public List<Sale> obtenerTodasLasVentas() throws SQLException {
        return saleDAO.obtenerTodasLasVentas();
    }
    
    /** Obtiene ventas con paginación */
    public List<Sale> obtenerVentasPaginadas(int limit, int offset) throws SQLException {
        return saleDAO.obtenerVentasPaginadas(limit, offset);
    }
    
    /** Obtiene ventas de un usuario */
    public List<Sale> obtenerVentasPorUsuario(int userId) throws SQLException {
        return saleDAO.obtenerVentasPorUsuario(userId);
    }
    
    /** Cuenta ventas */
    public int contarVentas() throws SQLException {
        return saleDAO.contarVentas();
    }
    
    /** Obtiene total de ingresos */
    public double obtenerIngresosTotales() throws SQLException {
        return saleDAO.calcularIngresosTotales();
    }
    
    /**
     * Procesa una venta individual
     * 
     * @param sale Venta a procesar
     * @return true si la venta fue exitosa
     * @throws IllegalStateException Si la validación falla
     * @throws SQLException Si hay error en la BD
     */
    public boolean procesarVenta(Sale venta) throws SQLException {
        // Validar datos de la venta
        if (!venta.esValida()) {
            throw new IllegalStateException("Datos de venta inválidos");
        }
        
        // Obtener producto y validar
        Product producto = productDAO.buscarProductoPorId(venta.getProductoId());
        productService.validarProductoVendible(producto, venta.getCantidad());
        
        // Asegurar que el total esté actualizado
        venta.actualizarTotal();
        
        // Registrar venta (el trigger actualiza el stock automáticamente)
        return saleDAO.guardarVenta(venta);
    }
    
    /**
     * Procesa múltiples ventas en una transacción atómica
     * Si una venta falla, todas se revierten
     * 
     * @param sales Lista de ventas a procesar
     * @return SaleResult con el resultado de la operación
     */
    public SaleResult procesarVentasMultiples(List<Sale> ventas) {
        SaleResult resultado = new SaleResult();
        List<String> errores = new ArrayList<>();
        
        try {
            // Validar todas las ventas ANTES de procesarlas
            for (Sale venta : ventas) {
                try {
                    Product producto = productDAO.buscarProductoPorId(venta.getProductoId());
                    productService.validarProductoVendible(producto, venta.getCantidad());
                    venta.actualizarTotal();
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
            boolean exito = saleDAO.guardarVentas(ventas);
            
            if (exito) {
                resultado.setSuccess(true);
                resultado.setSuccessfulSales(ventas.size());
                resultado.setTotalAmount(calcularMontoTotal(ventas));
                resultado.setTotalUnits(calcularUnidadesTotales(ventas));
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
    public List<String> validarCarrito(List<Sale> ventas) {
        List<String> errores = new ArrayList<>();
        
        if (ventas == null || ventas.isEmpty()) {
            errores.add("El carrito está vacío");
            return errores;
        }
        
        try {
            for (Sale venta : ventas) {
                Product producto = productDAO.buscarProductoPorId(venta.getProductoId());
                
                if (producto == null) {
                    errores.add("Producto ID " + venta.getProductoId() + " no existe");
                    continue;
                }
                
                // Validar vencimiento
                if (producto.estaVencido()) {
                    errores.add(producto.getNombre() + " está VENCIDO. Debe retirarlo del carrito.");
                }
                
                // Validar estado activo
                if (!producto.isActivo()) {
                    errores.add(producto.getNombre() + " está inactivo");
                }
                
                // Validar stock
                if (!producto.tieneStockSuficiente(venta.getCantidad())) {
                    errores.add(producto.getNombre() + " - Stock insuficiente. " +
                             "Disponible: " + producto.getStock() + ", Solicitado: " + venta.getCantidad());
                }
            }
        } catch (SQLException e) {
            errores.add("Error al validar carrito: " + e.getMessage());
        }
        
        return errores;
    }
    
    /**
     * Calcula el monto total de una lista de ventas
     * 
     * @param ventas Lista de ventas
     * @return Suma total
     */
    private double calcularMontoTotal(List<Sale> ventas) {
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
    private int calcularUnidadesTotales(List<Sale> ventas) {
        return ventas.stream()
                    .mapToInt(Sale::getCantidad)
                    .sum();
    }
    
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

