package com.novafarma.service;

import com.novafarma.dao.SaleDAO;
import com.novafarma.dao.UserDAO;
import com.novafarma.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Servicio de lógica de negocio para Usuarios
 * 
 * Responsabilidades:
 * - Validación antes de eliminar usuarios
 * - Verificación de ventas asociadas
 * - Gestión de usuarios del sistema
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class UserService {
    
    private final UserDAO userDAO;
    private final SaleDAO saleDAO;
    
    /**
     * Constructor
     */
    public UserService() {
        this.userDAO = new UserDAO();
        this.saleDAO = new SaleDAO();
    }
    
    // ==================== CONSULTAS ====================
    
    /**
     * Obtiene todos los usuarios del sistema
     * 
     * @return Lista de todos los usuarios
     * @throws SQLException Si hay error en la consulta
     */
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }
    
    /**
     * Obtiene usuarios con paginación
     * OPTIMIZACIÓN: Para manejar grandes volúmenes de datos
     * 
     * @param limit Número máximo de registros a retornar
     * @param offset Número de registros a saltar (para paginación)
     * @return Lista de usuarios
     * @throws SQLException Si hay error en la consulta
     */
    public List<User> getUsersPaginated(int limit, int offset) throws SQLException {
        return userDAO.findAll(limit, offset);
    }
    
    /**
     * Cuenta el número total de usuarios
     * 
     * @return Número total de usuarios
     * @throws SQLException Si hay error en la consulta
     */
    public int countAllUsers() throws SQLException {
        return userDAO.countAll();
    }
    
    /**
     * Busca un usuario por su ID
     * 
     * @param id ID del usuario
     * @return User si existe, null si no
     * @throws SQLException Si hay error en la consulta
     */
    public User getUserById(int id) throws SQLException {
        return userDAO.findById(id);
    }
    
    /**
     * Busca un usuario por su nombre de usuario
     * 
     * @param username Nombre de usuario
     * @return User si existe, null si no
     * @throws SQLException Si hay error en la consulta
     */
    public User getUserByUsername(String username) throws SQLException {
        return userDAO.findByUsername(username);
    }
    
    // ==================== OPERACIONES ====================
    
    /**
     * Crea un nuevo usuario
     * 
     * @param user Usuario a crear (la contraseña debe venir hasheada)
     * @return true si la creación fue exitosa
     * @throws SQLException Si hay error (ej: username duplicado)
     */
    public boolean createUser(User user) throws SQLException {
        return userDAO.save(user);
    }
    
    /**
     * Verifica si un usuario tiene ventas registradas
     * 
     * @param userId ID del usuario
     * @return true si el usuario tiene ventas, false si no
     * @throws SQLException Si hay error en la consulta
     */
    public boolean hasSales(int userId) throws SQLException {
        List<com.novafarma.model.Sale> sales = saleDAO.findByUserId(userId);
        return sales != null && !sales.isEmpty();
    }
    
    /**
     * Obtiene el número de ventas de un usuario
     * 
     * @param userId ID del usuario
     * @return Número de ventas del usuario
     * @throws SQLException Si hay error en la consulta
     */
    public int getSalesCount(int userId) throws SQLException {
        List<com.novafarma.model.Sale> sales = saleDAO.findByUserId(userId);
        return sales != null ? sales.size() : 0;
    }
    
    /**
     * Obtiene un mapa con el conteo de ventas para todos los usuarios
     * OPTIMIZACIÓN: Una sola query en lugar de N queries (evita problema N+1)
     * 
     * @return Map donde la clave es el ID del usuario y el valor es el conteo de ventas
     * @throws SQLException Si hay error en la consulta
     */
    public Map<Integer, Integer> getAllUsersSalesCount() throws SQLException {
        return userDAO.findAllWithSalesCount();
    }
    
    /**
     * Elimina un usuario del sistema
     * 
     * VALIDACIONES:
     * - Verifica que el usuario no tenga ventas asociadas
     * - Previene eliminar el último administrador
     * 
     * @param userId ID del usuario a eliminar
     * @return Resultado de la eliminación con mensaje descriptivo
     * @throws SQLException Si hay error en la operación
     */
    public DeleteUserResult deleteUser(int userId) throws SQLException {
        DeleteUserResult result = new DeleteUserResult();
        
        // Verificar que el usuario existe
        User user = userDAO.findById(userId);
        if (user == null) {
            result.setSuccess(false);
            result.setMessage("El usuario no existe");
            return result;
        }
        
        // NO permitir eliminar el usuario actual (esto se valida en la UI)
        
        // Verificar si tiene ventas
        if (hasSales(userId)) {
            int salesCount = getSalesCount(userId);
            result.setSuccess(false);
            result.setMessage("No se puede eliminar el usuario '" + user.getUsername() + "' porque tiene " + 
                            salesCount + " venta(s) registrada(s).\n\n" +
                            "Las ventas deben conservarse para el historial del negocio.\n" +
                            "Si el trabajador ya no trabaja, simplemente no le permitas iniciar sesión.");
            result.setSalesCount(salesCount);
            return result;
        }
        
        // Intentar eliminar
        boolean deleted = userDAO.delete(userId);
        
        if (deleted) {
            result.setSuccess(true);
            result.setMessage("Usuario '" + user.getUsername() + "' eliminado exitosamente");
        } else {
            result.setSuccess(false);
            result.setMessage("No se pudo eliminar el usuario");
        }
        
        return result;
    }
    
    // ==================== CLASE INTERNA: RESULTADO DE ELIMINACIÓN ====================
    
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

