package com.novafarma.util;

import javax.swing.*;
import java.awt.*;

/**
 * Clase utilitaria para aplicar estilos consistentes a las tablas JTable
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class TableStyleHelper {
    
    /**
     * Aplica un estilo limpio y profesional a las tablas JTable
     * 
     * @param table La tabla a la que se aplicará el estilo
     */
    public static void applyTableStyle(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setGridColor(new Color(200, 200, 200));
        table.setForeground(Color.BLACK);
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setSelectionForeground(Color.BLACK);
        
        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
            table.getTableHeader().setBackground(Color.WHITE);
            table.getTableHeader().setForeground(Color.BLACK);
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        }
    }
}

