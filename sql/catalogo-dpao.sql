-- Catálogo real de D'PAO Cookies (datos entregados por el dueño el 9 de octubre de 2026).
-- Se aplica DESPUÉS de schema.sql. No borra nada y se puede ejecutar más de una vez:
-- categorías y sabores se insertan o actualizan por su clave única; el stock no se toca.
-- Sintaxis compatible con MySQL 8 y con MariaDB (XAMPP).

CREATE TABLE IF NOT EXISTS pack_precio (
 id_categoria INT NOT NULL,
 unidades INT NOT NULL CHECK (unidades > 0),
 precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
 PRIMARY KEY (id_categoria, unidades),
 FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

START TRANSACTION;

INSERT IGNORE INTO categoria(nombre) VALUES ('Clásico'), ('Edición'), ('Premium');

-- Los 16 sabores con el número y el precio unitario de la lista del dueño.
INSERT INTO producto(codigo, nombre, precio_venta, id_categoria)
SELECT s.codigo, s.nombre, s.precio, c.id_categoria
FROM (
  SELECT 'G01' AS codigo, 'Chispas' AS nombre, 7.00 AS precio, 'Clásico' AS categoria
  UNION ALL SELECT 'G02', 'Chunky', 7.00, 'Clásico'
  UNION ALL SELECT 'G03', 'Chin Chin', 7.00, 'Clásico'
  UNION ALL SELECT 'G04', 'Chocolate', 8.00, 'Clásico'
  UNION ALL SELECT 'G05', 'Red Velvet', 8.50, 'Edición'
  UNION ALL SELECT 'G06', 'Monster', 8.50, 'Edición'
  UNION ALL SELECT 'G07', 'Oreo', 8.50, 'Edición'
  UNION ALL SELECT 'G08', 'Marshmallow', 8.50, 'Edición'
  UNION ALL SELECT 'G09', 'Bom o Bom', 8.50, 'Edición'
  UNION ALL SELECT 'G10', 'Cinnamon Roll', 9.00, 'Edición'
  UNION ALL SELECT 'G11', 'Arándano', 9.00, 'Edición'
  UNION ALL SELECT 'G12', 'Limón', 9.00, 'Edición'
  UNION ALL SELECT 'G13', 'Maracuyá', 9.00, 'Edición'
  UNION ALL SELECT 'G14', 'Nutella', 11.00, 'Premium'
  UNION ALL SELECT 'G15', 'Snicker', 11.00, 'Premium'
  UNION ALL SELECT 'G16', 'Pistacho', 15.00, 'Premium'
) s
JOIN categoria c ON c.nombre = s.categoria
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), precio_venta = VALUES(precio_venta), id_categoria = VALUES(id_categoria);

-- Packs por delivery: se combinan sabores de la misma categoría.
INSERT INTO pack_precio(id_categoria, unidades, precio)
SELECT c.id_categoria, p.unidades, p.precio
FROM (
  SELECT 'Clásico' AS categoria, 2 AS unidades, 12.00 AS precio
  UNION ALL SELECT 'Clásico', 4, 22.00
  UNION ALL SELECT 'Clásico', 6, 30.00
  UNION ALL SELECT 'Edición', 2, 16.00
  UNION ALL SELECT 'Edición', 4, 30.00
  UNION ALL SELECT 'Edición', 6, 42.00
  UNION ALL SELECT 'Premium', 2, 20.00
  UNION ALL SELECT 'Premium', 4, 40.00
  UNION ALL SELECT 'Premium', 6, 58.00
) p
JOIN categoria c ON c.nombre = p.categoria
ON DUPLICATE KEY UPDATE precio = VALUES(precio);

COMMIT;
