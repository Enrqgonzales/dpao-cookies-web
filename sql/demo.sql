-- Datos ficticios de presentación; aplicar una vez sobre schema.sql vacío.
START TRANSACTION;
INSERT INTO categoria(nombre) VALUES ('Materias primas'), ('Galletas');
INSERT INTO insumo(codigo,nombre,unidad_medida,stock_actual,stock_minimo,costo_unitario,id_categoria) VALUES
('HAR-001','Harina de trigo','kg',12.00,5.00,4.50,1),
('AZU-001','Azúcar rubia','kg',2.00,3.00,4.00,1),
('MAN-001','Mantequilla','kg',6.00,2.00,18.00,1);
INSERT INTO producto(codigo,nombre,precio_venta,stock_actual,id_categoria) VALUES
('GAL-001','Galleta de chocolate',6.50,18.00,2),
('GAL-002','Galleta de avena',6.00,12.00,2),
('GAL-003','Galleta de vainilla',6.00,16.00,2);
INSERT INTO movimiento_kardex(id_producto,tipo_movimiento,cantidad,stock_anterior,stock_posterior,observacion) VALUES
(1,'ENTRADA',24.00,0.00,24.00,'Producción de demostración'),
(1,'VENTA',-6.00,24.00,18.00,'Venta de demostración'),
(2,'ENTRADA',12.00,0.00,12.00,'Producción de demostración'),
(3,'ENTRADA',16.00,0.00,16.00,'Producción de demostración');
COMMIT;
