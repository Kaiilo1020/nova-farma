package com.novafarma.service;

import com.novafarma.dao.SaleDAO;
import com.novafarma.dao.UserDAO;
import com.novafarma.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Servicio de lógica de negocio para Usuarios */
public class UserService {
    
    private final UserDAO userDAO;
    private final SaleDAO saleDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
        this.saleDAO = new SaleDAO();
    }
    
    /** Obtiene todos los usuarios */
    public List<User> obtenerTodosLosUsuarios() throws SQLException {
        return userDAO.obtenerTodosLosUsuarios();
    }
    
    /** Obtiene usuarios con paginación */
    public List<User> obtenerUsuariosPaginados(int limit, int offset) throws SQLException {
        return userDAO.obtenerUsuariosPaginados(limit, offset);
    }
    
    /** Cuenta usuarios */
    public int contarUsuarios() throws SQLException {
        return userDAO.contarUsuarios();
    }
    
    /** Busca usuario por ID */
    public User obtenerUsuarioPorId(int id) throws SQLException {
        return userDAO.buscarUsuarioPorId(id);
    }
    
    /** Busca usuario por nombre */
    public User obtenerUsuarioPorNombre(String username) throws SQLException {
        return userDAO.buscarPorNombreUsuario(username);
    }
    
    /** Crea un nuevo usuario (password debe venir hasheado) */
    public boolean crearUsuario(User user) throws SQLException {
        return userDAO.guardarUsuario(user);
    }
    
    /** Verifica si un usuario tiene ventas */
    public boolean tieneVentas(int userId) throws SQLException {
        List<com.novafarma.model.Sale> sales = saleDAO.obtenerVentasPorUsuario(userId);
        return sales != null && !sales.isEmpty();
    }
    
    /** Obtiene número de ventas de un usuario */
    public int obtenerTotalVentasUsuario(int userId) throws SQLException {
        List<com.novafarma.model.Sale> sales = saleDAO.obtenerVentasPorUsuario(userId);
        return sales != null ? sales.size() : 0;
    }
    
    /** Obtiene usuarios con conteo de ventas (evita N+1) */
    public Map<Integer, Integer> obtenerUsuariosConVentas() throws SQLException {
        return userDAO.obtenerUsuariosConConteoVentas();
    }
    
    /** Elimina usuario (valida que no tenga ventas) */
    public DeleteUserResult eliminarUsuario(int userId) throws SQLException {
        DeleteUserResult result = new DeleteUserResult();
        User user = userDAO.buscarUsuarioPorId(userId);
        if (user == null) {
            result.setSuccess(false);
            result.setMessage("El usuario no existe");
            return result;
        }
        
        if (tieneVentas(userId)) {
            int salesCount = obtenerTotalVentasUsuario(userId);
            result.setSuccess(false);
            result.setMessage("No se puede eliminar el usuario '" + user.getUsername() + "' porque tiene " + 
                            salesCount + " venta(s) registrada(s).\n\n" +
                            "Las ventas deben conservarse para el historial del negocio.\n" +
                            "Si el trabajador ya no trabaja, simplemente no le permitas iniciar sesión.");
            result.setSalesCount(salesCount);
            return result;
        }
        
        boolean deleted = userDAO.eliminarUsuario(userId);
        
        if (deleted) {
            result.setSuccess(true);
            result.setMessage("Usuario '" + user.getUsername() + "' eliminado exitosamente");
        } else {
            result.setSuccess(false);
            result.setMessage("No se pudo eliminar el usuario");
        }
        
        return result;
    }
    
    /**
     * Clase que encapsula el resultado de una operación de eliminación de usuario
     */
    public static class DeleteUserResult {
        private boolean success;
        private String message;
        private int salesCount;
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public int getSalesCount() {
            return salesCount;
        }
        
        public void setSalesCount(int salesCount) {
            this.salesCount = salesCount;
        }
    }
}

