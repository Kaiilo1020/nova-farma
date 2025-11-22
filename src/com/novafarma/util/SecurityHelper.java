package com.novafarma.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Clase utilitaria para la seguridad de la aplicación Nova Farma
 * 
 * PROPÓSITO EDUCATIVO:
 * Esta clase implementa el requisito CRÍTICO de encriptación de contraseñas
 * usando el algoritmo SHA-256 (Secure Hash Algorithm 256-bit).
 * 
 * ¿POR QUÉ SHA-256?
 * - Es un algoritmo criptográfico de hash unidireccional
 * - Convierte cualquier texto en una cadena de 64 caracteres hexadecimales
 * - Es prácticamente imposible revertir el hash a la contraseña original
 * - Es el estándar de la industria para almacenamiento seguro de contraseñas
 * 
 * @author Nova Farma Development Team
 * @version 1.0
 */
public class SecurityHelper {
    
    /**
     * Encripta una contraseña usando el algoritmo SHA-256
     * 
     * FLUJO DEL MÉTODO:
     * 1. Obtiene la instancia del algoritmo SHA-256 de MessageDigest
     * 2. Convierte la contraseña a bytes (usando UTF-8)
     * 3. Procesa los bytes y genera el hash
     * 4. Convierte el hash a formato hexadecimal legible
     * 
     * EJEMPLO DE USO:
     * String passwordOriginal = "MiPassword123";
     * String hashGenerado = SecurityHelper.encryptPassword(passwordOriginal);
     * // Resultado: "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"
     * 
     * @param password La contraseña en texto plano
     * @return El hash SHA-256 de la contraseña en formato hexadecimal (64 caracteres)
     * @throws RuntimeException si el algoritmo SHA-256 no está disponible
     */
    public static String encryptPassword(String password) {
        try {
            // Paso 1: Obtener la instancia de MessageDigest con algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Paso 2: Convertir la contraseña a bytes y aplicar el hash
            byte[] bytesDelHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Paso 3: Convertir los bytes del hash a formato hexadecimal
            StringBuilder cadenaHex = new StringBuilder();
            for (byte b : bytesDelHash) { // Recorrer cada byte del hash
                // Convertir cada byte a hexadecimal (0-255 -> 00-ff)
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    cadenaHex.append('0'); // Añadir cero inicial si es necesario
                }
                cadenaHex.append(hex);
            }
            
            return cadenaHex.toString();
            
        } catch (NoSuchAlgorithmException e) {
            // Esta excepción es extremadamente rara (SHA-256 está en todas las JVM modernas)
            throw new RuntimeException("Error crítico: Algoritmo SHA-256 no disponible", e);
        }
    }
    
    /**
     * Verifica si una contraseña coincide con un hash almacenado
     * 
     * PROPÓSITO:
     * Este método es útil para el proceso de login. En lugar de desencriptar
     * el hash almacenado (que es imposible), encriptamos la contraseña ingresada
     * y comparamos ambos hashes.
     * 
     * FLUJO DE VALIDACIÓN:
     * 1. Usuario ingresa contraseña en texto plano
     * 2. Sistema la encripta con SHA-256
     * 3. Compara el hash generado con el hash en la base de datos
     * 4. Si coinciden -> Contraseña correcta
     * 
     * @param plainPassword Contraseña en texto plano (ingresada por el usuario)
     * @param hashedPassword Hash SHA-256 almacenado en la base de datos
     * @return true si la contraseña es correcta, false en caso contrario
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        // Encriptar la contraseña ingresada
        String hashDeLaContrasena = encryptPassword(plainPassword);
        
        // Comparar los hashes de forma segura (sin revelar información de tiempo)
        return hashDeLaContrasena.equals(hashedPassword);
    }
    
    /**
     * Método de prueba (solo para desarrollo)
     * Permite ver cómo se encripta una contraseña
     */
    public static void main(String[] args) {
        // DEMOSTRACIÓN EDUCATIVA
        System.out.println("=== DEMOSTRACIÓN DE ENCRIPTACIÓN SHA-256 ===\n");
        
        String[] contrasenas = {"admin123", "trabajador456", "NovaFarma2024"};
        
        for (String contrasena : contrasenas) {
            String hash = encryptPassword(contrasena);
            System.out.println("Contraseña: " + contrasena);
            System.out.println("Hash SHA-256: " + hash);
            System.out.println("Longitud: " + hash.length() + " caracteres");
            System.out.println();
        }
        
        // DEMOSTRACIÓN DE VERIFICACIÓN
        System.out.println("=== DEMOSTRACIÓN DE VERIFICACIÓN ===\n");
        String contrasena = "admin123";
        String hash = encryptPassword(contrasena);
        
        System.out.println("Verificando 'admin123' (correcta): " + 
                          verifyPassword("admin123", hash));
        System.out.println("Verificando 'admin124' (incorrecta): " + 
                          verifyPassword("admin124", hash));
    }
}

