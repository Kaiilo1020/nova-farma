package com.novafarma.ui.handlers;

import com.novafarma.model.User;
import com.novafarma.service.UserService;
import com.novafarma.ui.UserCreationDialog;
import com.novafarma.util.Mensajes;
import com.novafarma.util.PaginationHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Maneja las operaciones de usuarios (crear, eliminar, cargar tabla)
 * Separa esta lógica del Dashboard para que sea más simple
 */
public class UserHandler {
    
    private JFrame parent;
    private User currentUser;
    private UserService userService;
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    
    // Paginación
    private static final int PAGE_SIZE = PaginationHelper.DEFAULT_PAGE_SIZE;
    private static final int PAGINATION_THRESHOLD = 100;
    private int currentPage = 1;
    private int totalRecords = 0;
    private boolean paginationEnabled = false;
    
    // Controles de paginación (se pasan desde Dashboard)
    private JButton btnFirstPage;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    private JButton btnLastPage;
    private JLabel lblPageInfo;
    
    public UserHandler(JFrame parent, User currentUser, UserService userService) {
        this.parent = parent;
        this.currentUser = currentUser;
        this.userService = userService;
    }
    
    public void setTable(JTable table, DefaultTableModel model) {
        this.usersTable = table;
        this.usersTableModel = model;
    }
    
    public void setPaginationControls(JButton btnFirst, JButton btnPrev, JButton btnNext, JButton btnLast, JLabel lblInfo) {
        this.btnFirstPage = btnFirst;
        this.btnPrevPage = btnPrev;
        this.btnNextPage = btnNext;
        this.btnLastPage = btnLast;
        this.lblPageInfo = lblInfo;
        
        if (btnFirstPage != null) btnFirstPage.addActionListener(e -> goToFirstPage());
        if (btnPrevPage != null) btnPrevPage.addActionListener(e -> goToPreviousPage());
        if (btnNextPage != null) btnNextPage.addActionListener(e -> goToNextPage());
        if (btnLastPage != null) btnLastPage.addActionListener(e -> goToLastPage());
    }
    
    public void cargarDatos() {
        try {
            // Contar total de usuarios
            int totalUsuarios = userService.countAllUsers();
            
            // Activar paginación si hay muchos registros
            if (totalUsuarios > PAGINATION_THRESHOLD) {
                paginationEnabled = true;
                totalRecords = totalUsuarios;
                cargarDatosPaginated();
            } else {
                paginationEnabled = false;
                cargarDatosCompleto();
            }
            
            updatePaginationControls();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent,
                Mensajes.ERROR_CARGAR + ":\n" + e.getMessage(),
                Mensajes.ERROR_BD,
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarDatosCompleto() {
        try {
            usersTableModel.setRowCount(0);
            
            List<User> usuarios = userService.getAllUsers();
            Map<Integer, Integer> ventasPorUsuario = userService.getAllUsersSalesCount();
            
            for (User usuario : usuarios) {
                int ventas = ventasPorUsuario.getOrDefault(usuario.getId(), 0);
                Object[] fila = {
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getRol().getDisplayName(),
                    ventas + " venta(s)"
                };
                usersTableModel.addRow(fila);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void cargarDatosPaginated() {
        try {
            usersTableModel.setRowCount(0);
            
            int offset = PaginationHelper.calculateOffset(currentPage, PAGE_SIZE);
            List<User> usuarios = userService.getUsersPaginated(PAGE_SIZE, offset);
            Map<Integer, Integer> ventasPorUsuario = userService.getAllUsersSalesCount();
            
            for (User usuario : usuarios) {
                int ventas = ventasPorUsuario.getOrDefault(usuario.getId(), 0);
                Object[] fila = {
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getRol().getDisplayName(),
                    ventas + " venta(s)"
                };
                usersTableModel.addRow(fila);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updatePaginationControls() {
        if (lblPageInfo == null) return;
        
        if (!paginationEnabled) {
            lblPageInfo.setText("Total: " + totalRecords + " usuarios");
            if (btnFirstPage != null) btnFirstPage.setEnabled(false);
            if (btnPrevPage != null) btnPrevPage.setEnabled(false);
            if (btnNextPage != null) btnNextPage.setEnabled(false);
            if (btnLastPage != null) btnLastPage.setEnabled(false);
            return;
        }
        
        int totalPaginas = PaginationHelper.calculateTotalPages(totalRecords, PAGE_SIZE);
        currentPage = PaginationHelper.validatePageNumber(currentPage, totalPaginas);
        
        String rango = PaginationHelper.getDisplayRange(currentPage, PAGE_SIZE, totalRecords);
        lblPageInfo.setText(String.format("Página %d de %d (%s)", currentPage, totalPaginas, rango));
        
        if (btnFirstPage != null) btnFirstPage.setEnabled(currentPage > 1);
        if (btnPrevPage != null) btnPrevPage.setEnabled(currentPage > 1);
        if (btnNextPage != null) btnNextPage.setEnabled(currentPage < totalPaginas);
        if (btnLastPage != null) btnLastPage.setEnabled(currentPage < totalPaginas);
    }
    
    private void goToFirstPage() {
        currentPage = 1;
        cargarDatosPaginated();
        updatePaginationControls();
    }
    
    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            cargarDatosPaginated();
            updatePaginationControls();
        }
    }
    
    private void goToNextPage() {
        int totalPaginas = PaginationHelper.calculateTotalPages(totalRecords, PAGE_SIZE);
        if (currentPage < totalPaginas) {
            currentPage++;
            cargarDatosPaginated();
            updatePaginationControls();
        }
    }
    
    private void goToLastPage() {
        int totalPaginas = PaginationHelper.calculateTotalPages(totalRecords, PAGE_SIZE);
        currentPage = totalPaginas;
        cargarDatosPaginated();
        updatePaginationControls();
    }
    
    public void crear() {
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(parent, Mensajes.SOLO_ADMIN, Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        UserCreationDialog dialog = new UserCreationDialog(parent);
        dialog.setVisible(true);
        cargarDatos();
    }
    
    public void eliminar() {
        if (currentUser.isTrabajador()) {
            JOptionPane.showMessageDialog(parent, Mensajes.SOLO_ADMIN, Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int filaSeleccionada = usersTable.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(parent, Mensajes.SELECCIONAR_USUARIO, Mensajes.TITULO_ADVERTENCIA, JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int userId = (Integer) usersTableModel.getValueAt(filaSeleccionada, 0);
        String username = (String) usersTableModel.getValueAt(filaSeleccionada, 1);
        
        if (userId == currentUser.getId()) {
            JOptionPane.showMessageDialog(parent,
                "No puedes eliminar tu propio usuario mientras estás conectado.",
                Mensajes.TITULO_ADVERTENCIA,
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirmar = JOptionPane.showConfirmDialog(parent,
            "¿Estás seguro de eliminar al usuario '" + username + "'?\n\nEsta acción no se puede deshacer.",
            "Confirmar",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                UserService.DeleteUserResult resultado = userService.deleteUser(userId);
                
                if (resultado.isSuccess()) {
                    eliminarFila(userId);
                    JOptionPane.showMessageDialog(parent, resultado.getMessage(), Mensajes.TITULO_EXITO, JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(parent, resultado.getMessage(), Mensajes.TITULO_ADVERTENCIA, JOptionPane.WARNING_MESSAGE);
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), Mensajes.TITULO_ERROR, JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void eliminarFila(int userId) {
        int totalFilas = usersTableModel.getRowCount();
        for (int i = 0; i < totalFilas; i++) {
            int id = (Integer) usersTableModel.getValueAt(i, 0);
            if (id == userId) {
                usersTableModel.removeRow(i);
                return;
            }
        }
    }
}

