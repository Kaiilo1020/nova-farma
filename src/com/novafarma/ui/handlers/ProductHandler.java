package com.novafarma.ui.handlers;

import com.novafarma.model.Product;
import com.novafarma.model.User;
import com.novafarma.service.ProductService;
import com.novafarma.ui.ProductDialog;
import com.novafarma.ui.panels.AlertsPanel;
import com.novafarma.ui.panels.InventoryPanel;
import com.novafarma.ui.panels.SalesPanel;
import com.novafarma.util.Mensajes;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Maneja las operaciones de productos (agregar, editar, eliminar)
 * Separa esta lógica del Dashboard para que sea más simple
 */
public class ProductHandler {
    
    private JFrame parent;
    private User currentUser;
    private ProductService productService;
    private InventoryPanel inventoryPanel;
    private AlertsPanel alertsPanel;
    private SalesPanel salesPanel;
    
    public ProductHandler(JFrame parent, User currentUser, ProductService productService, InventoryPanel inventoryPanel) {
        this.parent = parent;
        this.currentUser = currentUser;
        this.productService = productService;
        this.inventoryPanel = inventoryPanel;
    }
    
    public void setAlertsPanel(AlertsPanel alertsPanel) {
        this.alertsPanel = alertsPanel;
    }
    
    public void setSalesPanel(SalesPanel salesPanel) {
        this.salesPanel = salesPanel;
    }
    
    public void agregar() {
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(parent, Mensajes.SOLO_ADMIN, Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Usar diálogo dedicado en lugar de JOptionPane
        Product nuevo = ProductDialog.showCreateDialog(parent);
        
        if (nuevo == null) {
            return; // Usuario canceló
        }
        
        try {
            // Verificar si ya existe
            Product existente = productService.findProductByName(nuevo.getNombre());
            if (existente != null) {
                int respuesta = preguntarActualizarOcrear(nuevo.getNombre(), existente);
                if (respuesta == 0) {
                    // Actualizar existente con los datos del nuevo
                    existente.setNombre(nuevo.getNombre());
                    existente.setDescripcion(nuevo.getDescripcion());
                    existente.setPrecio(nuevo.getPrecio());
                    existente.setStock(nuevo.getStock());
                    existente.setFechaVencimiento(nuevo.getFechaVencimiento());
                    actualizarExistente(existente);
                    return;
                } else if (respuesta == 2) {
                    return; // Cancelar
                }
            }
            
            // Crear nuevo
            boolean exito = productService.createProduct(nuevo);
            
            if (exito) {
                Product creado = productService.findProductByName(nuevo.getNombre());
                if (creado != null && creado.isActivo()) {
                    inventoryPanel.addProductRow(creado);
                }
                JOptionPane.showMessageDialog(parent, Mensajes.PRODUCTO_AGREGADO, Mensajes.TITULO_EXITO, JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parent, Mensajes.ERROR_GUARDAR, Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void editar() {
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(parent, Mensajes.SOLO_ADMIN, Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int fila = inventoryPanel.getSelectedProductRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(parent, Mensajes.SELECCIONAR_PRODUCTO, Mensajes.TITULO_ADVERTENCIA, JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Integer productId = inventoryPanel.getSelectedProductId();
            if (productId == null) return;
            
            Product producto = productService.getProductById(productId);
            if (producto == null) return;
            
            // Usar diálogo dedicado en lugar de JOptionPane
            Product editado = ProductDialog.showEditDialog(parent, producto);
            
            if (editado == null) {
                return; // Usuario canceló
            }
            
            boolean exito = productService.updateProduct(editado);
            if (exito) {
                Product actualizado = productService.getProductById(productId);
                if (actualizado != null && actualizado.isActivo()) {
                    inventoryPanel.updateProductRow(actualizado);
                } else {
                    inventoryPanel.removeProductRow(productId);
                }
                JOptionPane.showMessageDialog(parent, Mensajes.PRODUCTO_ACTUALIZADO, Mensajes.TITULO_EXITO, JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parent, Mensajes.ERROR_GUARDAR, Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void eliminar() {
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(parent, Mensajes.SOLO_ADMIN, Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int fila = inventoryPanel.getSelectedProductRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(parent, Mensajes.SELECCIONAR_PRODUCTO, Mensajes.TITULO_ADVERTENCIA, JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Integer productId = inventoryPanel.getSelectedProductId();
            if (productId == null) return;
            
            int confirmar = JOptionPane.showConfirmDialog(parent,
                Mensajes.CONFIRMAR_ELIMINAR,
                "Confirmar",
                JOptionPane.YES_NO_OPTION);
            
            if (confirmar == JOptionPane.YES_OPTION) {
                boolean exito = productService.retireProduct(productId);
                if (exito) {
                    inventoryPanel.removeProductRow(productId);
                    JOptionPane.showMessageDialog(parent, Mensajes.PRODUCTO_ELIMINADO, Mensajes.TITULO_EXITO, JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parent, Mensajes.ERROR_ELIMINAR, Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
                }
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private int preguntarActualizarOcrear(String nombre, Product existente) {
        String estado = existente.isActivo() ? "ACTIVO" : "INACTIVO";
        String mensaje = String.format(
            "Ya existe un producto llamado '%s':\n\n" +
            "ID: %d\n" +
            "Estado: %s\n" +
            "Stock: %d\n" +
            "Precio: $%.2f\n\n" +
            "¿Qué deseas hacer?",
            nombre, existente.getId(), estado, existente.getStock(), existente.getPrecio()
        );
        
        Object[] opciones = {"Actualizar Existente", "Crear Nuevo", "Cancelar"};
        return JOptionPane.showOptionDialog(parent, mensaje, "Producto Duplicado",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opciones, opciones[0]);
    }
    
    private void actualizarExistente(Product existente) {
        try {
            boolean exito = productService.updateProduct(existente);
            if (exito) {
                Product actualizado = productService.getProductById(existente.getId());
                if (actualizado != null && actualizado.isActivo()) {
                    inventoryPanel.updateProductRow(actualizado);
                } else {
                    inventoryPanel.removeProductRow(existente.getId());
                }
                JOptionPane.showMessageDialog(parent, Mensajes.PRODUCTO_ACTUALIZADO, Mensajes.TITULO_EXITO, JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parent, Mensajes.ERROR_GUARDAR, Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Maneja la eliminación de productos vencidos desde el panel de alertas
     */
    public void eliminarVencidos() {
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(parent, Mensajes.SOLO_ADMIN, Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (alertsPanel == null) return;
        
        Integer productId = alertsPanel.getSelectedProductId();
        
        if (productId != null) {
            eliminarProductoVencidoSeleccionado(productId);
        } else {
            eliminarTodosLosVencidos();
        }
    }
    
    private void eliminarProductoVencidoSeleccionado(int productoId) {
        try {
            Product product = productService.getProductById(productoId);
            if (product == null) {
                JOptionPane.showMessageDialog(parent, "Producto no encontrado", Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String nombreProducto = product.getNombre();
            long diasRestantes = product.getDaysUntilExpiration();
            
            int confirm = JOptionPane.showConfirmDialog(parent,
                "¿Retirar este producto del inventario?\n\n" +
                "Producto: " + nombreProducto + "\n" +
                "Estado: " + diasRestantes + "\n\n" +
                "El producto se marcará como INACTIVO y con stock 0.\n" +
                "NO se eliminará de la base de datos (se conserva el historial).\n\n" +
                "Cuando llegue un nuevo lote, podrás editarlo y reactivarlo.",
                "Confirmar Desactivación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = productService.retireProduct(productoId);
                
                if (success) {
                    JOptionPane.showMessageDialog(parent,
                        "Producto desactivado exitosamente\n\n" +
                        "Producto: " + nombreProducto + "\n" +
                        "Stock: 0\n" +
                        "Activo: NO\n\n" +
                        "Cuando llegue un nuevo lote:\n" +
                        "1. Ve a Inventario\n" +
                        "2. Busca el producto\n" +
                        "3. Edítalo con el nuevo stock y fecha\n" +
                        "4. Se reactivará automáticamente",
                        "Producto Desactivado",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    actualizarPaneles();
                }
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminarTodosLosVencidos() {
        try {
            List<Product> productsExpired = productService.getExpiredProducts();
            int totalVencidos = productsExpired.size();
            
            if (totalVencidos == 0) {
                JOptionPane.showMessageDialog(parent,
                    "No hay productos vencidos activos para retirar.",
                    "Sin Productos Vencidos",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(parent,
                "RETIRAR PRODUCTOS VENCIDOS\n\n" +
                "Se encontraron " + totalVencidos + " productos vencidos.\n\n" +
                "Se marcarán como INACTIVOS (stock = 0, activo = FALSE)\n" +
                "NO se eliminarán de la base de datos.\n\n" +
                "¿Continuar?",
                "Confirmar Desactivación Masiva",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                int rowsUpdated = productService.retireAllExpiredProducts();
                
                JOptionPane.showMessageDialog(parent,
                    "Operación completada\n\n" +
                    "Productos desactivados: " + rowsUpdated + "\n\n" +
                    "Estos productos se conservan en la base de datos\n" +
                    "y pueden reactivarse cuando llegue un nuevo lote.\n\n" +
                    "Para reactivar:\n" +
                    "1. Inventario → Buscar el producto\n" +
                    "2. Editar → Nuevo stock y fecha\n" +
                    "3. Se reactiva automáticamente",
                    "Productos Desactivados",
                    JOptionPane.INFORMATION_MESSAGE);
                
                actualizarPaneles();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarPaneles() {
        if (alertsPanel != null) alertsPanel.cargarAlertas();
        if (inventoryPanel != null) inventoryPanel.loadProductsData();
        if (salesPanel != null) salesPanel.cargarCatalogo();
    }
}

