package com.novafarma.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Renderizador personalizado para la tabla de productos
 * 
 * PROPÓSITO:
 * Implementa el requisito de ALERTAS VISUALES para productos próximos a vencer.
 * Pinta las filas según el estado del producto:
 * 
 * COLORES:
 * - ROJO: Producto vencido (fecha < hoy)
 * - NARANJA: Producto próximo a vencer (dentro de 30 días)
 * - VERDE: Producto en buen estado (más de 30 días restantes)
 * 
 * PROBLEMA DE NEGOCIO QUE RESUELVE:
 * Evita pérdidas económicas por productos vencidos que no se detectan a tiempo.
 * La farmacia actualmente pierde dinero porque no tiene alertas visuales claras.
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class ProductExpirationRenderer extends DefaultTableCellRenderer {
    
    private static final Color COLOR_VENCIDO = new Color(255, 102, 102);      // Rojo suave
    private static final Color COLOR_POR_VENCER = new Color(255, 204, 102);  // Naranja suave
    private static final Color COLOR_NORMAL = new Color(204, 255, 204);      // Verde suave
    private static final Color COLOR_SIN_FECHA = Color.WHITE;                // Blanco (sin fecha)
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    // Columna donde está la fecha de vencimiento (ajustar según tu tabla)
    private int fechaVencimientoColumnIndex = 5;
    
    /**
     * Constructor por defecto
     */
    public ProductExpirationRenderer() {
        super();
    }
    
    /**
     * Constructor con índice de columna personalizado
     * 
     * @param fechaVencimientoColumnIndex Índice de la columna de fecha de vencimiento
     */
    public ProductExpirationRenderer(int fechaVencimientoColumnIndex) {
        super();
        this.fechaVencimientoColumnIndex = fechaVencimientoColumnIndex;
    }
    
    /**
     * Método principal que renderiza cada celda de la tabla
     * 
     * FLUJO DE LÓGICA:
     * 1. Obtiene el valor de la celda de fecha de vencimiento
     * 2. Parsea la fecha a formato Date
     * 3. Calcula los días restantes hasta el vencimiento
     * 4. Aplica el color según la regla de negocio:
     *    - Si está vencido: ROJO
     *    - Si vence en <= 30 días: NARANJA
     *    - Si vence en > 30 días: VERDE
     * 5. Renderiza toda la fila con ese color
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        
        // Llamar al método padre para mantener el comportamiento por defecto
        Component cell = super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column
        );
        
        try {
            // Obtener el valor de la columna de fecha de vencimiento
            Object fechaVencObj = table.getValueAt(row, fechaVencimientoColumnIndex);
            
            if (fechaVencObj == null || fechaVencObj.toString().isEmpty() || 
                fechaVencObj.toString().equalsIgnoreCase("N/A")) {
                // Sin fecha de vencimiento (ej: termómetros, equipos)
                cell.setBackground(isSelected ? table.getSelectionBackground() : COLOR_SIN_FECHA);
                cell.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
                return cell;
            }
            
            // Parsear la fecha
            String fechaStr = fechaVencObj.toString();
            Date fechaVencimiento = DATE_FORMAT.parse(fechaStr);
            
            // Convertir a LocalDate para cálculos más fáciles
            LocalDate fechaVenc = fechaVencimiento.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
            
            LocalDate hoy = LocalDate.now();
            
            // Calcular días restantes
            long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaVenc);
            
            // ==================== LÓGICA DE COLORACIÓN ====================
            
            Color colorFondo;
            Color colorTexto = Color.BLACK;
            
            if (diasRestantes < 0) {
                // PRODUCTO VENCIDO
                colorFondo = COLOR_VENCIDO;
                colorTexto = new Color(139, 0, 0); // Rojo oscuro para el texto
                
            } else if (diasRestantes <= 30) {
                // PRODUCTO PRÓXIMO A VENCER (30 días o menos)
                colorFondo = COLOR_POR_VENCER;
                colorTexto = new Color(139, 90, 0); // Naranja oscuro para el texto
                
            } else {
                // PRODUCTO EN BUEN ESTADO
                colorFondo = COLOR_NORMAL;
                colorTexto = new Color(0, 100, 0); // Verde oscuro para el texto
            }
            
            // Aplicar colores (si no está seleccionada la fila)
            if (isSelected) {
                cell.setBackground(table.getSelectionBackground());
                cell.setForeground(table.getSelectionForeground());
            } else {
                cell.setBackground(colorFondo);
                cell.setForeground(colorTexto);
            }
            
        } catch (Exception e) {
            // Si hay error al parsear la fecha, usar color por defecto
            cell.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            cell.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
        }
        
        return cell;
    }
    
    /**
     * Método de prueba (demo educativa)
     */
    public static void main(String[] args) {
        System.out.println("=== DEMOSTRACIÓN DE COLORACIÓN DE PRODUCTOS ===\n");
        
        // Simular productos con diferentes fechas
        System.out.println("Producto 1: Vencido hace 5 días -> COLOR ROJO");
        System.out.println("Producto 2: Vence en 10 días -> COLOR NARANJA");
        System.out.println("Producto 3: Vence en 60 días -> COLOR VERDE");
        System.out.println("Producto 4: Sin fecha de vencimiento -> COLOR BLANCO");
        
        System.out.println("\nEsto ayuda a la farmacia a:");
        System.out.println("✓ Detectar productos vencidos inmediatamente");
        System.out.println("✓ Planificar promociones para productos próximos a vencer");
        System.out.println("✓ Evitar pérdidas económicas por mercancía caducada");
    }
}

