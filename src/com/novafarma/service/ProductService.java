package com.novafarma.service;

import com.novafarma.dao.ProductDAO;
import com.novafarma.model.Product;

import java.sql.SQLException;
import java.util.List;

/** Servicio de lógica de negocio para Productos */
public class ProductService {
    
    private final ProductDAO productDAO;
    
    public ProductService() {
        this.productDAO = new ProductDAO();
    }
    
    public List<Product> obtenerProductosActivos() throws SQLException {
        return productDAO.obtenerProductosActivos();
    }
    
    public List<Product> obtenerProductosActivosPaginados(int limit, int offset) throws SQLException {
        return productDAO.obtenerProductosActivos(limit, offset);
    }
    
    /** Cuenta productos activos */
    public int contarProductosActivos() throws SQLException {
        return productDAO.contarProductosActivos();
    }
    
    /** Cuenta productos activos con stock > 0 */
    public int contarProductosActivosConStock() throws SQLException {
        return productDAO.contarProductosActivosConStock();
    }
    
    /** Obtiene producto por ID */
    public Product obtenerProductoPorId(int id) throws SQLException {
        return productDAO.buscarProductoPorId(id);
    }
    
    /** Busca producto por nombre (incluye inactivos para detectar duplicados) */
    public Product buscarProductoPorNombre(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }
        return productDAO.buscarProductoPorNombre(nombre);
    }
    
    /** Obtiene productos que vencen en ≤30 días */
    public List<Product> obtenerProductosPorVencer() throws SQLException {
        return productDAO.obtenerProductosPorVencer();
    }
    
    /** Obtiene productos vencidos */
    public List<Product> obtenerProductosVencidos() throws SQLException {
        return productDAO.obtenerProductosVencidos();
    }
    
    /** Crea un nuevo producto con validaciones */
    public boolean crearProducto(Product producto) throws SQLException {
        validarProducto(producto);
        if (producto.getStock() > 0) {
            producto.setActivo(true);
        }
        return productDAO.guardarProducto(producto);
    }
    
    /** Actualiza producto con validaciones */
    public boolean actualizarProducto(Product producto) throws SQLException {
        validarProducto(producto);
        if (producto.getStock() > 0) {
            producto.setActivo(true);
        }
        return productDAO.actualizarProducto(producto);
    }
    
    /** Desactiva producto (soft delete) */
    public boolean desactivarProducto(int productId) throws SQLException {
        return productDAO.desactivarProducto(productId);
    }
    
    /** Desactiva todos los productos vencidos */
    public int desactivarProductosVencidos() throws SQLException {
        return productDAO.desactivarProductosVencidos();
    }
    
    private void validarProducto(Product producto) {
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
    }
    
    /** Valida que un producto sea vendible (activo, no vencido, con stock suficiente) */
    public void validarProductoVendible(Product producto, int cantidadSolicitada) {
        if (producto == null) {
            throw new IllegalStateException("El producto no existe");
        }
        
        if (!producto.isActivo()) {
            throw new IllegalStateException("El producto está inactivo");
        }
        
        if (producto.estaVencido()) {
            throw new IllegalStateException("El producto está vencido: " + producto.getNombre());
        }
        
        if (!producto.tieneStock()) {
            throw new IllegalStateException("El producto no tiene stock disponible");
        }
        
        if (!producto.tieneStockSuficiente(cantidadSolicitada)) {
            throw new IllegalStateException("Stock insuficiente. Disponible: " + producto.getStock() + 
                                          ", Solicitado: " + cantidadSolicitada);
        }
    }
}

