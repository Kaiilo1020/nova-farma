package com.novafarma.util;

/**
 * Clase helper para manejar la lógica de paginación
 * 
 * Calcula offsets, total de páginas, y valida rangos de páginas
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class PaginationHelper {
    
    /** Tamaño de página por defecto */
    public static final int DEFAULT_PAGE_SIZE = 50;
    
    /**
     * Calcula el offset basado en el número de página y el tamaño de página
     * 
     * @param pageNumber Número de página (empezando en 1)
     * @param pageSize Tamaño de página
     * @return Offset para usar en la query SQL
     */
    public static int calculateOffset(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        return (pageNumber - 1) * pageSize;
    }
    
    /**
     * Calcula el número total de páginas
     * 
     * @param totalRecords Número total de registros
     * @param pageSize Tamaño de página
     * @return Número total de páginas (mínimo 1)
     */
    public static int calculateTotalPages(int totalRecords, int pageSize) {
        if (totalRecords == 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalRecords / pageSize);
    }
    
    /**
     * Valida y ajusta el número de página si está fuera de rango
     * 
     * @param pageNumber Número de página actual
     * @param totalPages Número total de páginas
     * @return Número de página válido (entre 1 y totalPages)
     */
    public static int validatePageNumber(int pageNumber, int totalPages) {
        if (pageNumber < 1) {
            return 1;
        }
        if (pageNumber > totalPages && totalPages > 0) {
            return totalPages;
        }
        return pageNumber;
    }
    
    /**
     * Obtiene el rango de registros mostrados en la página actual
     * 
     * @param pageNumber Número de página (empezando en 1)
     * @param pageSize Tamaño de página
     * @param totalRecords Número total de registros
     * @return String con el rango (ej: "1-50 de 150")
     */
    public static String getDisplayRange(int pageNumber, int pageSize, int totalRecords) {
        int offset = calculateOffset(pageNumber, pageSize);
        int start = offset + 1;
        int end = Math.min(offset + pageSize, totalRecords);
        
        if (totalRecords == 0) {
            return "0 registros";
        }
        
        return String.format("%d-%d de %d", start, end, totalRecords);
    }
}

