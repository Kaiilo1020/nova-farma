package com.novafarma.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Utilidad para encriptación SHA-256 de contraseñas */
public class SecurityHelper {
    
    /** Encripta una contraseña usando SHA-256 */
    public static String encryptPassword(String password) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = sha.digest(password.getBytes());
            
            StringBuilder resultado = new StringBuilder();
            for (byte b : hashBytes) {
                resultado.append(String.format("%02x", b));
            }
            
            return resultado.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: SHA-256 no disponible en este sistema", e);
        }
    }
    
}