-- MySQL 8.0.16+; ejecutar sobre una base NUEVA seleccionada por el operador.
-- No elimina tablas ni datos existentes. InnoDB aplica FK, CHECK y transacciones.
CREATE TABLE categoria (
 id_categoria INT PRIMARY KEY AUTO_INCREMENT,
 nombre VARCHAR(100) NOT NULL UNIQUE,
 CHECK (CHAR_LENGTH(TRIM(nombre)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE insumo (
 id_insumo INT PRIMARY KEY AUTO_INCREMENT,
 codigo VARCHAR(20) NOT NULL UNIQUE,
 nombre VARCHAR(100) NOT NULL,
 unidad_medida VARCHAR(20) NOT NULL,
 stock_actual DECIMAL(10,2) NOT NULL CHECK (stock_actual >= 0),
 stock_minimo DECIMAL(10,2) NOT NULL CHECK (stock_minimo >= 0),
 costo_unitario DECIMAL(10,2) NOT NULL CHECK (costo_unitario >= 0),
 id_categoria INT NOT NULL,
 FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON DELETE RESTRICT,
 CHECK (CHAR_LENGTH(TRIM(codigo)) > 0),
 CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
 CHECK (CHAR_LENGTH(TRIM(unidad_medida)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE producto (
 id_producto INT PRIMARY KEY AUTO_INCREMENT,
 codigo VARCHAR(20) NOT NULL UNIQUE,
 nombre VARCHAR(100) NOT NULL,
 precio_venta DECIMAL(10,2) NOT NULL CHECK (precio_venta >= 0),
 stock_actual DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
 id_categoria INT NOT NULL,
 FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON DELETE RESTRICT,
 CHECK (CHAR_LENGTH(TRIM(codigo)) > 0),
 CHECK (CHAR_LENGTH(TRIM(nombre)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE movimiento_kardex (
 id_movimiento BIGINT PRIMARY KEY AUTO_INCREMENT,
 id_insumo INT NULL,
 id_producto INT NULL,
 tipo_movimiento ENUM('ENTRADA','SALIDA','VENTA','MERMA','AJUSTE') NOT NULL,
 cantidad DECIMAL(10,2) NOT NULL CHECK (cantidad <> 0),
 stock_anterior DECIMAL(10,2) NOT NULL CHECK (stock_anterior >= 0),
 stock_posterior DECIMAL(10,2) NOT NULL CHECK (stock_posterior >= 0),
 observacion VARCHAR(255) NOT NULL,
 fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (id_insumo) REFERENCES insumo(id_insumo) ON DELETE RESTRICT,
 FOREIGN KEY (id_producto) REFERENCES producto(id_producto) ON DELETE RESTRICT,
 CHECK ((id_insumo IS NOT NULL) <> (id_producto IS NOT NULL)),
 CHECK (stock_posterior = stock_anterior + cantidad),
 CHECK ((tipo_movimiento IN ('SALIDA','VENTA','MERMA') AND cantidad < 0)
     OR (tipo_movimiento = 'ENTRADA' AND cantidad > 0) OR tipo_movimiento = 'AJUSTE'),
 INDEX idx_movimiento_fecha (fecha_registro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
