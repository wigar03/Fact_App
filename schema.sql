-- =============================================================================
-- Base de Datos: tienda_javafx
-- Sistema de Facturación e Inventario - JavaFX
-- =============================================================================

-- 1. Tabla: Categoria
CREATE TABLE IF NOT EXISTS categoria (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
);

-- 2. Tabla: Producto
CREATE TABLE IF NOT EXISTS producto (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    categoria_id INTEGER NOT NULL,
    precio_venta NUMERIC(12, 2) NOT NULL,
    existencia INTEGER NOT NULL DEFAULT 0,
    ruta_imagen VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT producto_codigo_key UNIQUE (codigo),
    CONSTRAINT uq_producto_nombre UNIQUE (nombre),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) 
        REFERENCES categoria(id) ON UPDATE CASCADE ON DELETE RESTRICT
);
