# Evidencias APF2 · interfaz JSF

Fecha: 9 de octubre de 2026. Rama `apf2-frontend`. Entorno: JDK 17.0.20.1+1, Maven 3.9.16, Tomcat 9.0.115, MySQL 8.4.9 aislado en `127.0.0.1:3307`, Mojarra 2.3.9.

## Capturas obligatorias

| Archivo | Qué muestra |
|---|---|
| `01-webxml-estructura-paquetes.png` | `web.xml` con `FacesServlet` (`*.xhtml`) resaltado, paquetes Java (`bean`, `controller`, `dao`, `dto`, `exception`, `facade`, `model`, `util`) y vistas Facelets con `faces-config.xml`. Generada a partir de los archivos del repositorio. |
| `02-interfaz-bootstrap-tablero.png`, `02b-interfaz-bootstrap-celular.png` | Interfaz principal con Bootstrap 5 local (1366×768 y 390×844). |
| `03-formulario-jsf-nuevo-insumo.png` | `h:form` con `h:inputText` y `h:selectOneMenu` con datos escritos, antes de `h:commandButton`. |
| `03b-formulario-jsf-guardado.png` | Resultado tras guardar: redirección (Post/Redirect/Get), aviso `h:messages` y la fila nueva en la tabla. |
| `04-datatable-insumos-mysql.png` | `h:dataTable` de insumos alimentado por `InventarioFacade.listarInsumos()` desde MySQL. |
| `04b-mismos-insumos-en-mysql.png` | La misma información consultada directamente en MySQL (`dpao_demo`). |
| `05-historial-commits.png` | Historial de commits de la rama, autor y correo de cada uno. |

## Registros

- `maven-test.log`: `mvn -o clean test` sin MySQL: 89 pruebas, 0 fallas, 14 omitidas (integración JDBC).
- `maven-test-jdbc.log`: `mvn -o test` con `DPAO_JDBC_TEST=true` contra MySQL 8.4.9: 89 pruebas, 0 fallas, 0 omitidas.
- `http-smoke.log`: ingreso por el formulario JSF (302 a `tablero.xhtml`) y carga de cada pantalla y hoja (200).
- `medicion-vistas.log`: 19 estados de las cinco pantallas en celular, tablet y computadora (57 mediciones) con `design/herramientas/medir-jsf.js`: sin scroll, escala tipográfica de cinco tamaños, retícula de 4 px, nada escondido bajo la barra o el botón fijos y sin fallas axe-core WCAG 2 A/AA.
- `git-commit.log`: autor y committer de los últimos commits.

## Datos de la demostración

Base nueva `dpao_demo`: `sql/schema.sql`, `sql/catalogo-dpao.sql` y `sql/demo-insumos-dpao.sql`. Los movimientos del día (entradas de cocina, uso de insumos y ventas del turno) se registraron por la hoja «Nuevo movimiento» de `kardex.xhtml`, es decir, por `KardexBean → InventarioFacade → KardexDAO` en transacción. El insumo «Esencia de vainilla» se registró durante la captura `03`.
