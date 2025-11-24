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
     * Obtiene productos activos con paginación
     * OPTIMIZACIÓN: Para manejar grandes volúmenes de datos
     * 
     * @param limit Número máximo de registros a retornar
     * @param offset Número de registros a saltar (para paginación)
     * @return Lista de productos activos
     * @throws SQLException Si hay error en la consulta
     */
    public List<Product> getActiveProductsPaginated(int limit, int offset) throws SQLException {
        return productDAO.findAllActive(limit, offset);
    }
    
    /**
     * Cuenta el número total de productos activos
     * 
     * @return Número total de productos activos
     * @throws SQLException Si hay error en la consulta
     */
    public int countActiveProducts() throws SQLException {
        return productDAO.countAllActive();
    }
    
    /**
     * Cuenta el número total de productos activos con stock > 0 (vendibles)
     * 
     * @return Número total de productos vendibles
     * @throws SQLException Si hay error en la consulta
     */
    public int countActiveProductsWithStock() throws SQLException {
        return productDAO.countActiveWithStock();
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
     * Busca un producto por su nombre (case-insensitive)
     * 
     * PROPÓSITO: Detectar duplicados antes de crear un nuevo producto.
     * Busca en TODOS los productos (activos e inactivos) para encontrar
     * productos que fueron desactivados pero que pueden reactivarse con un nuevo lote.
     * 
     * EJEMPLO DE USO:
     * - Producto "pseudoefedrina" existe con ID=20, activo=FALSE, stock=0
     * - Usuario intenta agregar "pseudoefedrina" nuevamente
     * - Este método encuentra el producto existente (ID=20)
     * - El sistema pregunta si quiere actualizar el existente o crear uno nuevo
     * 
     * @param nombre Nombre del producto a buscar
     * @return Product si se encuentra, null si no existe
     * @throws SQLException Si hay error en la consulta
     */
    public Product findProductByName(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }
        return productDAO.findByName(nombre);
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
    
    // Operaciones CRUD
    
    /**
     * Crea un nuevo producto con validaciones de negocio
     * 
     * @param producto Producto a crear
     * @return true si la creación fue exitosa
     * @throws IllegalArgumentException Si los datos no son válidos
     * @throws SQLException Si hay error en la BD
     */
    public boolean createProduct(Product producto) throws SQLException {
        // Validaciones de negocio
        validateProduct(producto);
        
        // Si tiene stock > 0, debe estar activo
        if (producto.getStock() > 0) {
            producto.setActivo(true);
        }
        
        // Delegar al DAO
        return productDAO.save(producto);
    }
    
    /**
     * Actualiza un producto existente con validaciones
     * 
     * @param producto Producto con datos actualizados
     * @return true si la actualización fue exitosa
     * @throws IllegalArgumentException Si los datos no son válidos
     * @throws SQLException Si hay error en la BD
     */
    public boolean updateProduct(Product producto) throws SQLException {
        // Validaciones de negocio
        validateProduct(producto);
        
        // REGLA DE NEGOCIO: Si el stock es > 0, activar el producto automáticamente
        if (producto.getStock() > 0) {
            producto.setActivo(true);
        }
        
        // Delegar al DAO
        return productDAO.update(producto);
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
    
    // Validaciones de negocio
    
    /**
     * Valida que un producto cumpla las reglas de negocio
     * 
     * @param producto Producto a validar
     * @throws IllegalArgumentException Si alguna validación falla
     */
    private void validateProduct(Product producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        
        if (producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        
        // La fecha de vencimiento es opcional (algunos productos no tienen fecha de vencimiento)
    }
    
    /**
     * Valida que un producto sea vendible
     * Un producto es vendible si:
     * - Está activo
     * - No está vencido
     * - Tiene stock disponible
     * - Tiene suficiente stock para la cantidad solicitada
     * 
     * @param producto Producto a validar
     * @param cantidadSolicitada Cantidad solicitada
     * @throws IllegalStateException Si el producto no es vendible
     */
    public void validateSellableProduct(Product producto, int cantidadSolicitada) {
        if (producto == null) {
            throw new IllegalStateException("El producto no existe");
        }
        
        if (!producto.isActivo()) {
            throw new IllegalStateException("El producto está inactivo");
        }
        
        if (producto.isExpired()) {
            throw new IllegalStateException("El producto está vencido: " + producto.getNombre());
        }
        
        if (!producto.hasStock()) {
            throw new IllegalStateException("El producto no tiene stock disponible");
        }
        
        if (!producto.hasEnoughStock(cantidadSolicitada)) {
            throw new IllegalStateException("Stock insuficiente. Disponible: " + producto.getStock() + 
                                          ", Solicitado: " + cantidadSolicitada);
        }
    }
}

