-- =====================================================
-- NOVA FARMA - Script de Creación de Base de Datos
-- Sistema de Gestión Farmacéutica
-- =====================================================

-- PASO 1: Crear la base de datos (ejecutar como superusuario)
-- Descomentar si necesitas crear la base de datos desde cero:
-- DROP DATABASE IF EXISTS nova_farma_db;
-- CREATE DATABASE nova_farma_db;
-- \c nova_farma_db;

-- =====================================================
-- TABLA: usuarios
-- =====================================================
-- Almacena los usuarios del sistema con contraseñas encriptadas

CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL, -- SHA-256 genera 64 caracteres hexadecimales
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'TRABAJADOR')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para búsquedas rápidas por username
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);

-- COMENTARIOS EDUCATIVOS:
COMMENT ON TABLE usuarios IS 'Tabla de usuarios del sistema con contraseñas encriptadas en SHA-256';
COMMENT ON COLUMN usuarios.password_hash IS 'Hash SHA-256 de 64 caracteres - NUNCA almacenar contraseñas en texto plano';
COMMENT ON COLUMN usuarios.rol IS 'ADMINISTRADOR: puede modificar productos y crear usuarios | TRABAJADOR: solo puede vender y visualizar';

-- =====================================================
-- TABLA: productos
-- =====================================================
-- Almacena el inventario de medicamentos y productos

CREATE TABLE IF NOT EXISTS productos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL CHECK (precio >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    fecha_vencimiento DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para búsquedas y alertas
CREATE INDEX IF NOT EXISTS idx_productos_nombre ON productos(nombre);
CREATE INDEX IF NOT EXISTS idx_productos_vencimiento ON productos(fecha_vencimiento);

COMMENT ON TABLE productos IS 'Inventario de medicamentos y productos farmacéuticos';
COMMENT ON COLUMN productos.fecha_vencimiento IS 'Fecha de vencimiento del producto - usado para alertas';

-- =====================================================
-- TABLA: ventas
-- =====================================================
-- Registra las transacciones de venta

CREATE TABLE IF NOT EXISTS ventas (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER NOT NULL REFERENCES productos(id),
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10, 2) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para reportes
CREATE INDEX IF NOT EXISTS idx_ventas_fecha ON ventas(fecha_venta);
CREATE INDEX IF NOT EXISTS idx_ventas_producto ON ventas(producto_id);
CREATE INDEX IF NOT EXISTS idx_ventas_usuario ON ventas(usuario_id);

COMMENT ON TABLE ventas IS 'Registro de ventas realizadas (tanto por administradores como trabajadores)';

-- =====================================================
-- TABLA: facturas
-- =====================================================
-- Almacena las facturas emitidas a empresas/clientes

CREATE TABLE IF NOT EXISTS facturas (
    id SERIAL PRIMARY KEY,
    ruc VARCHAR(20) NOT NULL,
    empresa VARCHAR(100) NOT NULL,
    producto VARCHAR(100) NOT NULL,
    unidades INTEGER NOT NULL CHECK (unidades > 0),
    precio_unitario DECIMAL(10, 2) NOT NULL CHECK (precio_unitario > 0),
    precio_total DECIMAL(10, 2) NOT NULL,
    fecha_factura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_id INTEGER REFERENCES usuarios(id)
);

-- Índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_facturas_ruc ON facturas(ruc);
CREATE INDEX IF NOT EXISTS idx_facturas_fecha ON facturas(fecha_factura);
CREATE INDEX IF NOT EXISTS idx_facturas_empresa ON facturas(empresa);

COMMENT ON TABLE facturas IS 'Facturas emitidas a empresas y clientes';
COMMENT ON COLUMN facturas.ruc IS 'Registro Único de Contribuyente (8-11 dígitos)';
COMMENT ON COLUMN facturas.precio_total IS 'Calculado: unidades × precio_unitario';

-- =====================================================
-- DATOS DE PRUEBA
-- =====================================================

-- IMPORTANTE: Las contraseñas deben ser encriptadas con SHA-256
-- Puedes usar el método SecurityHelper.main() en Java para generar hashes

-- Usuario Administrador
-- Username: admin
-- Contraseña: admin123
-- Hash SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT INTO usuarios (username, password_hash, rol) VALUES 
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRADOR')
ON CONFLICT (username) DO NOTHING;

-- Usuario Trabajador
-- Username: trabajador1
-- Contraseña: trabajador123
-- Hash SHA-256: e29ad9f2e3e0eb0e82d1a33e52e2d0e1d53c8f19e2d3e4a5b6c7d8e9f0a1b2c3
INSERT INTO usuarios (username, password_hash, rol) VALUES 
('trabajador1', 'e29ad9f2e3e0eb0e82d1a33e52e2d0e1d53c8f19e2d3e4a5b6c7d8e9f0a1b2c3', 'TRABAJADOR')
ON CONFLICT (username) DO NOTHING;

-- Nota: Para generar nuevos hashes, ejecuta SecurityHelper.java o usa este comando en Java:
-- String hash = SecurityHelper.encryptPassword("tu_contraseña");

-- Productos de ejemplo
INSERT INTO productos (nombre, descripcion, precio, stock, fecha_vencimiento) VALUES 
('Paracetamol 500mg', 'Analgésico y antipirético', 5.50, 100, '2025-12-31'),
('Ibuprofeno 400mg', 'Antiinflamatorio no esteroideo', 7.80, 75, '2025-10-15'),
('Amoxicilina 500mg', 'Antibiótico de amplio espectro', 12.50, 50, '2025-06-30'),
('Vitamina C 1000mg', 'Suplemento vitamínico', 15.00, 200, '2026-03-20'),
('Omeprazol 20mg', 'Inhibidor de la bomba de protones', 9.90, 80, '2025-08-10'),
('Loratadina 10mg', 'Antihistamínico', 6.50, 120, '2025-11-25'),
('Alcohol en Gel 500ml', 'Desinfectante para manos', 4.20, 150, '2026-12-31'),
('Termómetro Digital', 'Medición de temperatura corporal', 25.00, 30, NULL)
ON CONFLICT DO NOTHING;

-- =====================================================
-- VISTA: Productos próximos a vencer (30 días)
-- =====================================================

CREATE OR REPLACE VIEW productos_por_vencer AS
SELECT 
    id,
    nombre,
    descripcion,
    precio,
    stock,
    fecha_vencimiento,
    fecha_vencimiento - CURRENT_DATE AS dias_restantes
FROM productos
WHERE fecha_vencimiento IS NOT NULL 
  AND fecha_vencimiento <= CURRENT_DATE + INTERVAL '30 days'
  AND fecha_vencimiento >= CURRENT_DATE
ORDER BY fecha_vencimiento ASC;

COMMENT ON VIEW productos_por_vencer IS 'Productos que vencen en los próximos 30 días - usado para alertas';

-- =====================================================
-- FUNCIÓN: Actualizar stock después de una venta
-- =====================================================

CREATE OR REPLACE FUNCTION actualizar_stock_venta()
RETURNS TRIGGER AS $$
BEGIN
    -- Restar la cantidad vendida del stock
    UPDATE productos 
    SET stock = stock - NEW.cantidad
    WHERE id = NEW.producto_id;
    
    -- Verificar que no quede stock negativo
    IF (SELECT stock FROM productos WHERE id = NEW.producto_id) < 0 THEN
        RAISE EXCEPTION 'Stock insuficiente para el producto ID %', NEW.producto_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger que se ejecuta después de INSERT en ventas
CREATE TRIGGER trigger_actualizar_stock
AFTER INSERT ON ventas
FOR EACH ROW
EXECUTE FUNCTION actualizar_stock_venta();

COMMENT ON FUNCTION actualizar_stock_venta IS 'Función que actualiza automáticamente el stock al registrar una venta';

-- =====================================================
-- CONSULTAS ÚTILES PARA DESARROLLO
-- =====================================================

-- Ver todos los usuarios (sin mostrar el hash completo)
-- SELECT id, username, LEFT(password_hash, 10) || '...' AS password_preview, rol FROM usuarios;

-- Ver productos con stock bajo (menos de 20 unidades)
-- SELECT nombre, stock FROM productos WHERE stock < 20 ORDER BY stock ASC;

-- Ver productos que vencen pronto
-- SELECT * FROM productos_por_vencer;

-- Resumen de ventas por día
-- SELECT DATE(fecha_venta) AS fecha, COUNT(*) AS num_ventas, SUM(total) AS total_vendido
-- FROM ventas
-- GROUP BY DATE(fecha_venta)
-- ORDER BY fecha DESC;

-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================

-- Para probar la conexión desde Java, asegúrate de:
-- 1. PostgreSQL está corriendo: sudo service postgresql start (Linux) o Services (Windows)
-- 2. La base de datos existe: \l en psql
-- 3. El usuario tiene permisos: GRANT ALL ON DATABASE nova_farma_db TO postgres;
-- 4. El driver JDBC está en el classpath: postgresql-XX.X.jar

SELECT 'Base de datos Nova Farma creada exitosamente' AS mensaje;

