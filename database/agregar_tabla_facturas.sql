-- =====================================================
-- Script para Agregar Tabla de Facturas
-- Ejecutar en pgAdmin Query Tool o psql
-- =====================================================

-- Conectar a la base de datos
\c nova_farma_db

-- Crear tabla de facturas
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

-- Crear índices
CREATE INDEX IF NOT EXISTS idx_facturas_ruc ON facturas(ruc);
CREATE INDEX IF NOT EXISTS idx_facturas_fecha ON facturas(fecha_factura);
CREATE INDEX IF NOT EXISTS idx_facturas_empresa ON facturas(empresa);

-- Comentarios
COMMENT ON TABLE facturas IS 'Facturas emitidas a empresas y clientes';
COMMENT ON COLUMN facturas.ruc IS 'Registro Único de Contribuyente (8-11 dígitos)';
COMMENT ON COLUMN facturas.precio_total IS 'Calculado: unidades × precio_unitario';

-- Datos de prueba (opcional)
INSERT INTO facturas (ruc, empresa, producto, unidades, precio_unitario, precio_total, usuario_id) VALUES
('20123456789', 'Farmacia Central', 'Paracetamol 500mg', 100, 5.50, 550.00, 1),
('10987654321', 'Clínica San José', 'Ibuprofeno 400mg', 50, 7.80, 390.00, 1),
('20555666777', 'Hospital Regional', 'Amoxicilina 500mg', 200, 12.50, 2500.00, 1)
ON CONFLICT DO NOTHING;

-- Verificar
SELECT * FROM facturas;

-- Mensaje de éxito
SELECT 'Tabla de facturas creada exitosamente' AS mensaje;

