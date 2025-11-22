package com.novafarma.service;

import com.novafarma.dao.ProductDAO;
import com.novafarma.model.Product;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de lógica de negocio para Productos
 * 
 * Responsabilidades:
 * - Validaciones de negocio complejas
 * - Orquestación de operaciones en ProductDAO
 * - Aplicación de reglas de negocio (ej: soft delete)
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class ProductService {
    
    private final ProductDAO productDAO;
    
    /**
     * Constructor
     */
    public ProductService() {
        this.productDAO = new ProductDAO();
    }
    
    // ==================== CONSULTAS ====================
    
    /**
     * Obtiene todos los productos activos
     * 
     * @return Lista de productos activos
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> getAllActiveProducts() throws SQLException {
        return productDAO.findAllActive();
    }
    
    /**
     * Obtiene un producto por su ID
     * 
     * @param id ID del producto
     * @return Product si existe
     * @throws SQLException Si hay error en la consulta
     */
    public Product getProductById(int id) throws SQLException {
        return productDAO.findById(id);
    }
    
    /**
     * Obtiene productos que vencen pronto o ya vencieron
     * 
     * @return Lista de productos con alerta de vencimiento
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> getExpiringSoonProducts() throws SQLException {
        return productDAO.findExpiringSoon();
    }
    
    /**
     * Obtiene productos vencidos
     * 
     * @return Lista de productos vencidos
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> getExpiredProducts() throws SQLException {
        return productDAO.findExpired();
    }
    
    // ==================== OPERACIONES CRUD ====================
    
    /**
     * Crea un nuevo producto con validaciones de negocio
     * 
     * @param product Producto a crear
     * @return true si la creación fue exitosa
     * @throws IllegalArgumentException Si los datos no son válidos
     * @throws SQLException Si hay error en la BD
     */
    public boolean createProduct(Product product) throws SQLException {
        // Validaciones de negocio
        validateProduct(product);
        
        // Si tiene stock > 0, debe estar activo
        if (product.getStock() > 0) {
            product.setActivo(true);
        }
        
        // Delegar al DAO
        return productDAO.save(product);
    }
    
    /**
     * Actualiza un producto existente con validaciones
     * 
     * @param product Producto con datos actualizados
     * @return true si la actualización fue exitosa
     * @throws IllegalArgumentException Si los datos no son válidos
     * @throws SQLException Si hay error en la BD
     */
    public boolean updateProduct(Product product) throws SQLException {
        // Validaciones de negocio
        validateProduct(product);
        
        // REGLA DE NEGOCIO: Si el stock es > 0, activar el producto automáticamente
        if (product.getStock() > 0) {
            product.setActivo(true);
        }
        
        // Delegar al DAO
        return productDAO.update(product);
    }
    
    /**
     * Realiza un "soft delete" del producto
     * Establece activo = FALSE y stock = 0
     * 
     * REGLA DE NEGOCIO: No eliminamos físicamente para mantener integridad referencial
     * 
     * @param productId ID del producto
     * @return true si la operación fue exitosa
     * @throws SQLException Si hay error en la BD
     */
    public boolean retireProduct(int productId) throws SQLException {
        return productDAO.softDelete(productId);
    }
    
    /**
     * Retira todos los productos vencidos (soft delete masivo)
     * 
     * @return Número de productos retirados
     * @throws SQLException Si hay error en la BD
     */
    public int retireAllExpiredProducts() throws SQLException {
        return productDAO.softDeleteAllExpired();
    }
    
    // ==================== VALIDACIONES DE NEGOCIO ====================
    
    /**
     * Valida que un producto cumpla las reglas de negocio
     * 
     * @param product Producto a validar
     * @throws IllegalArgumentException Si alguna validación falla
     */
    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        
        if (product.getNombre() == null || product.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        
        if (product.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        
        if (product.getFechaVencimiento() == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria");
        }
    }
    
    /**
     * Valida que un producto sea vendible
     * Un producto es vendible si:
     * - Está activo
     * - No está vencido
     * - Tiene stock disponible
     * - Tiene suficiente stock para la cantidad solicitada
     * 
     * @param product Producto a validar
     * @param quantityRequested Cantidad solicitada
     * @throws IllegalStateException Si el producto no es vendible
     */
    public void validateSellableProduct(Product product, int quantityRequested) {
        if (product == null) {
            throw new IllegalStateException("El producto no existe");
        }
        
        if (!product.isActivo()) {
            throw new IllegalStateException("El producto está inactivo");
        }
        
        if (product.isExpired()) {
            throw new IllegalStateException("El producto está vencido: " + product.getNombre());
        }
        
        if (!product.hasStock()) {
            throw new IllegalStateException("El producto no tiene stock disponible");
        }
        
        if (!product.hasEnoughStock(quantityRequested)) {
            throw new IllegalStateException("Stock insuficiente. Disponible: " + product.getStock() + 
                                          ", Solicitado: " + quantityRequested);
        }
    }
}

