-- Insumos críticos de D'PAO Cookies para DEMOSTRACIÓN.
-- Los nombres son los que entregó el dueño; cantidades, mínimos y costos son VALORES DE EJEMPLO.
-- Se aplica después de schema.sql y catalogo-dpao.sql sobre una base de demostración. No borra nada.

START TRANSACTION;

INSERT IGNORE INTO categoria(nombre) VALUES ('Insumos de cocina');

INSERT IGNORE INTO insumo(codigo, nombre, unidad_medida, stock_actual, stock_minimo, costo_unitario, id_categoria)
SELECT i.codigo, i.nombre, 'kg', i.stock, i.minimo, i.costo, c.id_categoria
FROM (
  SELECT 'INS-001' AS codigo, 'Harina de trigo' AS nombre, 18.00 AS stock, 10.00 AS minimo, 3.80 AS costo
  UNION ALL SELECT 'INS-002', 'Azúcar rubia', 4.50, 5.00, 4.20
  UNION ALL SELECT 'INS-003', 'Azúcar blanca', 6.00, 4.00, 4.50
  UNION ALL SELECT 'INS-004', 'Mantequilla', 2.50, 3.00, 32.00
  UNION ALL SELECT 'INS-005', 'Chispas de chocolate', 3.20, 2.00, 38.00
  UNION ALL SELECT 'INS-006', 'Cobertura de chocolate', 1.80, 2.00, 42.00
  UNION ALL SELECT 'INS-007', 'Manjar', 4.00, 2.00, 14.00
  UNION ALL SELECT 'INS-008', 'Maicena', 1.20, 1.00, 9.00
  UNION ALL SELECT 'INS-009', 'Queso crema', 0.80, 1.50, 30.00
) i
JOIN categoria c ON c.nombre = 'Insumos de cocina';

COMMIT;
