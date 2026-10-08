# Evidencias APF2 — 8 de octubre de 2026

Entorno: Windows 11, Temurin JDK 17.0.20.1+1, Maven 3.9.16, Tomcat 9.0.115, MySQL Community 8.4.9.

- `maven-test.log`: `mvn -B test` con `DPAO_JDBC_TEST=true` contra una instancia MySQL desechable en
  `127.0.0.1:3307/dpao_test` (datos nuevos, sin información real). 41 pruebas, 0 fallos, 0 omitidas:
  `KardexDAOTransaccionTest` (8), `InventarioFacadeTest` (15), `InsumoTest` (6), `JdbcTest` (6) e
  `InventarioFacadeIntegracionTest` (6). Las líneas `GRAVE` y las trazas `SQLException` corresponden a
  los casos de error provocados a propósito (fallo de conexión, trigger que hace fallar el INSERT).
- `http-smoke.log`: `scripts/http_smoke.py` sobre el WAR desplegado en Tomcat 9 conectado a la misma
  instancia de pruebas. Todas las JSP compilaron con `scripting-invalid=true`.
