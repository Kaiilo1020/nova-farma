package com.novafarma.ui.panels;

import com.novafarma.dao.SaleDAO;
import com.novafarma.model.User;
import com.novafarma.service.SaleService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Panel que muestra el reporte de ventas del día actual
 * Solo los administradores pueden ver este panel
 */
public class DailySalesReportPanel extends JPanel {
    
    private User currentUser;
    private SaleService saleService;
    
    // Componentes de la interfaz
    private JTable tableSummary;
    private DefaultTableModel modelSummary;
    private JLabel lblTotalTransacciones;
    private JLabel lblTotalProductos;
    private JLabel lblTotalIngresos;
    private JLabel lblFechaReporte;
    
    public DailySalesReportPanel(User currentUser, SaleService saleService) {
        this.currentUser = currentUser;
        this.saleService = saleService;
        
        inicializarInterfaz();
        cargarReporteDelDia();
    }
    
    private void inicializarInterfaz() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Título y botón actualizar
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JLabel lblTitulo = new JLabel("REPORTE DE VENTAS DEL DÍA");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy");
        lblFechaReporte = new JLabel("Fecha: " + dateFormat.format(new Date()));
        lblFechaReporte.setFont(new Font("Arial", Font.PLAIN, 14));
        lblFechaReporte.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(lblTitulo, BorderLayout.CENTER);
        titlePanel.add(lblFechaReporte, BorderLayout.SOUTH);
        
        // Botón para refrescar el reporte
        JButton btnActualizar = new JButton("Actualizar Reporte");
        btnActualizar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.addActionListener(e -> cargarReporteDelDia());
        
        topPanel.add(titlePanel, BorderLayout.CENTER);
        topPanel.add(btnActualizar, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Tabla que muestra ventas por trabajador
        crearTablaSummary();
        
        // Panel con los totales del día
        crearPanelTotales();
    }
    
    private void crearTablaSummary() {
        String[] columnNames = {"Trabajador", "Transacciones", "Productos Vendidos", "Total Recaudado"};
        modelSummary = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableSummary = new JTable(modelSummary);
        tableSummary.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableSummary.setRowHeight(30);
        tableSummary.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tableSummary.setFont(new Font("Arial", Font.PLAIN, 12));
        tableSummary.setFillsViewportHeight(true);
        
        // Configurar ancho de columnas
        tableSummary.getColumnModel().getColumn(0).setPreferredWidth(200); // Trabajador
        tableSummary.getColumnModel().getColumn(1).setPreferredWidth(120); // Transacciones
        tableSummary.getColumnModel().getColumn(2).setPreferredWidth(150); // Productos
        tableSummary.getColumnModel().getColumn(3).setPreferredWidth(150); // Total
        
        // Aplicar estilo a la tabla
        com.novafarma.util.TableStyleHelper.applyTableStyle(tableSummary);
        
        JScrollPane scrollPane = new JScrollPane(tableSummary);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resumen por Trabajador"));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void crearPanelTotales() {
        JPanel panelTotales = new JPanel(new GridLayout(2, 3, 15, 10));
        panelTotales.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("TOTALES DEL DÍA"),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Títulos de los totales
        JLabel lblTituloTransacciones = new JLabel("Total Transacciones:");
        lblTituloTransacciones.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel lblTituloProductos = new JLabel("Productos Vendidos:");
        lblTituloProductos.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel lblTituloIngresos = new JLabel("Ingresos Totales:");
        lblTituloIngresos.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Valores de los totales
        lblTotalTransacciones = new JLabel("0");
        lblTotalTransacciones.setFont(new Font("Arial", Font.PLAIN, 16));
        lblTotalTransacciones.setForeground(new Color(0, 100, 0));
        
        lblTotalProductos = new JLabel("0");
        lblTotalProductos.setFont(new Font("Arial", Font.PLAIN, 16));
        lblTotalProductos.setForeground(new Color(0, 100, 0));
        
        lblTotalIngresos = new JLabel("S/0.00");
        lblTotalIngresos.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotalIngresos.setForeground(new Color(0, 120, 0));
        
        // Agregar títulos y valores al panel
        panelTotales.add(lblTituloTransacciones);
        panelTotales.add(lblTituloProductos);
        panelTotales.add(lblTituloIngresos);
        panelTotales.add(lblTotalTransacciones);
        panelTotales.add(lblTotalProductos);
        panelTotales.add(lblTotalIngresos);
        
        add(panelTotales, BorderLayout.SOUTH);
    }
    
    /** Carga los datos del reporte desde la base de datos */
    public void cargarReporteDelDia() {
        try {
            // Vaciar la tabla antes de cargar nuevos datos
            modelSummary.setRowCount(0);
            
            // Obtener ventas agrupadas por trabajador
            List<SaleDAO.ReporteVentasPorTrabajador> reportePorTrabajador = saleService.obtenerResumenVentasPorTrabajador();
            
            for (SaleDAO.ReporteVentasPorTrabajador reporte : reportePorTrabajador) {
                Object[] fila = {
                    reporte.getUsername(),
                    reporte.getTotalVentas(),
                    reporte.getTotalProductos(),
                    String.format("S/%.2f", reporte.getTotalDinero())
                };
                modelSummary.addRow(fila);
            }
            
            // Obtener totales del día
            SaleDAO.ResumenTotalDelDia totales = saleService.obtenerResumenTotalDelDia();
            
            lblTotalTransacciones.setText(String.valueOf(totales.getTotalTransacciones()));
            lblTotalProductos.setText(String.valueOf(totales.getTotalProductos()));
            lblTotalIngresos.setText(String.format("S/%.2f", totales.getTotalIngresos()));
            
            // Mostrar cuándo se actualizó por última vez
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy - HH:mm:ss");
            lblFechaReporte.setText("Última actualización: " + dateFormat.format(new Date()));
            
            // Si no hay ventas, la tabla queda vacía (sin mostrar mensaje)
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar el reporte de ventas:\n" + e.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /** Oculta el panel si el usuario es trabajador */
    public void aplicarPermisosPorRol() {
        if (currentUser.isTrabajador()) {
            // Solo administradores pueden ver este reporte
            setVisible(false);
        }
    }
}
