package com.novafarma.util;

/**
 * Mensajes comunes de la aplicación
 * Centraliza los textos para evitar repetición
 */
public class Mensajes {
    
    // Mensajes de éxito
    public static final String PRODUCTO_AGREGADO = "Producto agregado correctamente";
    public static final String PRODUCTO_ACTUALIZADO = "Producto actualizado correctamente";
    public static final String PRODUCTO_ELIMINADO = "Producto eliminado correctamente";
    public static final String USUARIO_CREADO = "Usuario creado correctamente";
    public static final String USUARIO_ELIMINADO = "Usuario eliminado correctamente";
    public static final String VENTA_COMPLETADA = "Venta completada correctamente";
    
    // Mensajes de error
    public static final String ERROR_BD = "Error de base de datos";
    public static final String ERROR_CARGAR = "Error al cargar datos";
    public static final String ERROR_GUARDAR = "No se pudo guardar";
    public static final String ERROR_ELIMINAR = "No se pudo eliminar";
    
    // Validaciones
    public static final String CAMPOS_VACIOS = "Por favor completa todos los campos";
    public static final String PRECIO_INVALIDO = "El precio debe ser mayor a 0";
    public static final String STOCK_INVALIDO = "El stock no puede ser negativo";
    public static final String NOMBRE_VACIO = "El nombre no puede estar vacío";
    
    // Permisos
    public static final String SIN_PERMISOS = "No tienes permisos para realizar esta acción";
    public static final String SOLO_ADMIN = "Solo los administradores pueden hacer esto";
    
    // Selección
    public static final String SELECCIONAR_PRODUCTO = "Por favor selecciona un producto";
    public static final String SELECCIONAR_USUARIO = "Por favor selecciona un usuario";
    
    // Confirmaciones
    public static final String CONFIRMAR_ELIMINAR = "¿Estás seguro de eliminar esto?";
    public static final String CONFIRMAR_VENTA = "¿Confirmar esta venta?";
    
    // Títulos de diálogos
    public static final String TITULO_EXITO = "Éxito";
    public static final String TITULO_ERROR = "Error";
    public static final String TITULO_ADVERTENCIA = "Advertencia";
    public static final String TITULO_INFO = "Información";
}

