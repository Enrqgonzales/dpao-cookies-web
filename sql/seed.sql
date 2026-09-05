-- Datos ficticios para una base nueva de demostración o pruebas.
INSERT INTO categoria(nombre) VALUES ('Materias primas'), ('Galletas');
INSERT INTO insumo(codigo,nombre,unidad_medida,stock_actual,stock_minimo,costo_unitario,id_categoria)
 VALUES ('HAR-001','Harina de trigo','kg',5.00,5.00,4.50,1);
INSERT INTO producto(codigo,nombre,precio_venta,stock_actual,id_categoria)
 VALUES ('GAL-001','Galleta de chocolate',6.50,0,2);
