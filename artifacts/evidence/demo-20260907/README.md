# Corrección de la demostración

Se creó `dpao_demo` con `sql/schema.sql` y `sql/demo.sql`, conservando `dpao_test` y los esquemas de integración. No se borraron registros anteriores.

- Compilación y 11 pruebas Java aprobadas: `demo-build.log`.
- Flujo HTTP aprobado sobre `dpao_test`: `demo-http-tests.log`.
- El mismo script se detuvo antes de enviar formularios al apuntar a `dpao_demo`: `demo-protection.log`. El AssertionError es el rechazo esperado de la protección, no un fallo de la aplicación.
- La base de demostración conservó tres insumos, tres productos y cuatro movimientos después de ese intento.
- Chrome mostró una única alerta: Azúcar rubia (AZU-001), 2.00 kg frente a mínimo 3.00 kg; catálogo sin nombres HTTP y fechas formateadas en Kardex.

Las capturas e informe de la entrega del 5 de septiembre conservan los resultados históricos auténticos. Los nombres de prueba de aquellas evidencias no representan el catálogo de presentación actual.
