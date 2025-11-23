package com.novafarma.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase utilitaria para la seguridad de la aplicación Nova Farma
 * 
 * ¿QUÉ ES SHA-256?
 * SHA-256 es un algoritmo que convierte cualquier texto (como "admin123")
 * en una cadena de 64 caracteres hexadecimales (como "240be518fabd2724...").
 * 
 * CARACTERÍSTICAS:
 * - Es unidireccional: NO se puede revertir el hash a la contraseña original
 * - Siempre genera 64 caracteres, sin importar la longitud de la contraseña
 * - Es seguro: se usa en bancos, sistemas de seguridad, etc.
 * 
 * EJEMPLO:
 * Contraseña: "admin123"
 * Hash SHA-256: "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"
 * 
 * @author Nova Farma Development Team
 * @version 2.0 (Simplificado para mejor comprensión)
 */
public class SecurityHelper {
    
    /**
     * Encripta una contraseña usando SHA-256
     * 
     * CÓMO FUNCIONA (paso a paso):
     * 
     * 1. Creamos un "motor" de SHA-256
     *    MessageDigest sha = MessageDigest.getInstance("SHA-256");
     * 
     * 2. Le damos la contraseña y nos devuelve un array de bytes (el hash)
     *    byte[] hashBytes = sha.digest(password.getBytes());
     * 
     * 3. Convertimos cada byte a 2 dígitos hexadecimales (00, 01, 02... ff)
     *    Por ejemplo: byte 36 se convierte en "24", byte 190 se convierte en "be"
     * 
     * 4. Unimos todos los dígitos hexadecimales en una cadena de 64 caracteres
     * 
     * EJEMPLO DE USO:
     * String hash = SecurityHelper.encryptPassword("admin123");
     * // Resultado: "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"
     * 
     * @param password La contraseña en texto plano (ej: "admin123")
     * @return El hash SHA-256 en formato hexadecimal (64 caracteres)
     */
    public static String encryptPassword(String password) {
        try {
            // PASO 1: Crear el "motor" de SHA-256
            // MessageDigest es la clase de Java que hace el trabajo de encriptación
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            
            // PASO 2: Convertir la contraseña a bytes y generar el hash
            // getBytes() convierte el texto a números (bytes)
            // digest() aplica SHA-256 y nos devuelve el hash como array de bytes
            byte[] hashBytes = sha.digest(password.getBytes());
            
            // PASO 3: Convertir los bytes a texto hexadecimal
            // Necesitamos convertir cada byte (número 0-255) a 2 dígitos hexadecimales (00-ff)
            StringBuilder resultado = new StringBuilder();
            
            // Recorrer cada byte del hash
            for (byte b : hashBytes) {
                // String.format("%02x", b) convierte el byte a hexadecimal
                // %02x significa: "formato hexadecimal, mínimo 2 dígitos, rellenar con 0 si es necesario"
                // Ejemplo: byte 36 -> "24", byte 5 -> "05"
                resultado.append(String.format("%02x", b));
            }
            
            // PASO 4: Retornar el resultado como texto
            return resultado.toString(); // Retorna el hash como una cadena de texto
            
        } catch (NoSuchAlgorithmException e) {
            // Esto es muy raro que pase (SHA-256 está en todas las versiones modernas de Java)
            throw new RuntimeException("Error: SHA-256 no disponible en este sistema", e);
        }
    }
    
    /**
     * Verifica si una contraseña es correcta comparando con un hash almacenado
     * 
     * ¿CÓMO FUNCIONA?
     * 
     * Como NO podemos "desencriptar" un hash (es imposible), hacemos esto:
     * 
     * 1. Usuario ingresa contraseña: "admin123"
     * 2. Encriptamos esa contraseña: "240be518fabd2724..."
     * 3. Comparamos el hash generado con el hash guardado en la base de datos
     * 4. Si son iguales → Contraseña correcta
     *    Si son diferentes → Contraseña incorrecta
     * 
     * EJEMPLO:
     * String hashEnBD = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
     * 
     * verifyPassword("admin123", hashEnBD) → true (correcta)
     * verifyPassword("admin124", hashEnBD) → false (incorrecta)
     * 
     * @param plainPassword Contraseña en texto plano que el usuario ingresó
     * @param hashedPassword Hash SHA-256 que está guardado en la base de datos
     * @return true si la contraseña es correcta, false si es incorrecta
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        // Encriptar la contraseña que el usuario acaba de ingresar
        String hashGenerado = encryptPassword(plainPassword);
        
        // Comparar: ¿El hash generado es igual al hash guardado?
        return hashGenerado.equals(hashedPassword); // Retorna true si la contraseña es correcta, false si es incorrecta
    }
    
}