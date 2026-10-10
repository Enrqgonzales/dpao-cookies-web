# D'PAO COOKIES

Desarrollo Web Integrado (100000SI54). Aplicación web transaccional con Servlets, JSP/JSTL y JDBC. Cinco vistas operativas: acceso, tablero, inventario, productos y Kardex. El APF1 entregó la base MVC/DAO; el APF2 (Unidad 2) incorpora DTO, Façade, Kardex transaccional con saldos y vistas sin scriptlets.

## Arquitectura (APF2)

```
Navegador ─► SeguridadFilter ─► Servlet (controller) ─► InventarioFacade (facade) ─► DAO ─► MySQL
                                     │  DTO ▲                 │ valida, mapea entidad↔DTO,
                                     ▼      │                 │ traduce errores SQL
                               JSP (solo JSTL + EL)            ▼
                                                        model (entidades, enums)
```

| Capa | Paquete | Responsabilidad |
|---|---|---|
| Controlador | `controller` | `TableroServlet`, `InsumoServlet`, `ProductoServlet`, `KardexServlet` sobre `BaseServlet`: convierten parámetros HTTP en DTO, llaman a la fachada y aplican Post/Redirect/Get. Ningún servlet importa un DAO. `AccesoServlet` y `SeguridadFilter` gestionan sesión y CSRF. |
| Fachada | `facade` | `InventarioFacade`: único punto de entrada al negocio. Valida reglas, coordina `InsumoDAO`, `ProductoDAO`, `CategoriaDAO` y `KardexDAO`, convierte entidades a DTO (`MapeadorDTO`) y traduce errores MySQL (1062 duplicado, 1452 FK, 3819 CHECK) a mensajes; los demás se registran y se muestran como error genérico. |
| DTO | `dto` | `InsumoDTO`, `ProductoDTO`, `CategoriaDTO`, `MovimientoDTO` (salida del Kardex), `SolicitudMovimientoDTO` (entrada) y `ResumenTableroDTO`. Las vistas solo reciben DTO. |
| Acceso a datos | `dao` | SQL parametrizado; reciben un `ProveedorConexion` para pruebas sin MySQL. El stock solo cambia mediante `KardexDAO`. |
| Modelo | `model` | Entidades de tabla (`Insumo`, `Producto`, `Categoria`, `MovimientoKardex`) y enums `TipoMovimiento` / `ClaseItem` (lista fija de tipos, tablas y columnas). |
| Excepciones | `exception` | `NegocioException` (incluye `StockInsuficienteException`) y `ServicioException`, ambas con mensaje apto para el usuario. |

**Kardex transaccional.** `KardexDAO.registrar` usa `setAutoCommit(false)`, bloquea la fila con `SELECT ... FOR UPDATE`, calcula `stock_posterior = stock_anterior + cantidad`, rechaza todo saldo negativo (`StockInsuficienteException`), actualiza el stock, inserta el movimiento con ambos saldos y hace `commit()`; ante cualquier excepción ejecuta `rollback()`. Un AJUSTE recibe el stock físico contado y registra la diferencia; si no hay diferencia no crea movimiento.

**Vistas.** `web.xml` declara `<scripting-invalid>true</scripting-invalid>`: Jasper rechaza cualquier scriptlet o expresión Java. Listas, condiciones y formatos usan `c:forEach`, `c:if`, `c:choose`, `fmt:formatNumber`, `fmt:formatDate` y EL, con locale `es_PE` y zona `America/Lima`.

## Interfaz JSF (APF2)

La interfaz principal está en JavaServer Faces 2.3 (Mojarra `org.glassfish:javax.faces:2.3.9`, espacio de nombres `javax.faces`) sobre Tomcat 9. Las vistas JSP anteriores siguen disponibles en `/acceso`, `/tablero`, etc.

```
Navegador ─► SeguridadFilter ─► FacesServlet (*.xhtml) ─► Managed Bean (bean) ─► InventarioFacade ─► DAO ─► MySQL
                                       │                       │  DTO ▲
                                       ▼                       ▼      │
                         Facelets: plantilla ui:composition, h:form, h:dataTable, h:messages
```

| Pieza | Dónde | Qué hace |
|---|---|---|
| Configuración | `WEB-INF/web.xml`, `WEB-INF/faces-config.xml` | `FacesServlet` para `*.xhtml`, `PROJECT_STAGE=Production`, estado de vista en el servidor, mensajes en castellano (`mensajes.properties`), página de error sin trazas. |
| Plantilla | `WEB-INF/plantillas/app.xhtml` | `ui:composition` común: cabecera, secciones, cápsula «local · por atender», salir (POST), barra del celular, avisos `h:messages`. |
| Vistas | `app/acceso.xhtml`, `tablero`, `insumos`, `productos`, `kardex` | `h:form`, `h:inputText`, `h:selectOneMenu`, `h:commandButton`, `h:message` por campo y `h:dataTable` de insumos con datos de MySQL. Hojas laterales abiertas por `f:viewParam` (`?hoja=nuevo`). |
| Managed beans | `pe.com.dpaocookies.bean` | `AccesoBean` (request), `SesionBean` (session: usuario y local, inyectado con `@ManagedProperty`), `TableroBean`, `InsumosBean`, `ProductosBean`, `KardexBean` (view) y `FormatoBean` (application). Llaman **solo** a `InventarioFacade` y trabajan con DTO; no importan DAO, JDBC ni entidades. Las reglas (validación, saldos, rango de fechas) las decide la fachada: el bean muestra su mensaje junto al campo o en la hoja. |
| Navegación | `faces-config.xml` y acciones del bean | Reglas explícitas para el acceso: `ingreso` lleva de `acceso.xhtml` al tablero y `salida` vuelve al acceso desde cualquier pantalla, ambas con redirección. Las demás acciones usan navegación implícita (`pagina?faces-redirect=true`, Post/Redirect/Get); el éxito viaja en el Flash. `NavegacionTest` comprueba que las reglas coincidan con lo que devuelve el bean. |
| Recursos | `assets/` | Bootstrap 5.3.3, Baloo 2 y Nunito, íconos Lucide y el tema de los lienzos aprobados (`design/`), todo local. `assets/js/dpao.js` pagina tablas y mosaicos según el equipo y muestra la orientación de primera visita. |

**Por qué `@ManagedBean` y esquemas 3.1/2.2.** Tomcat 9 no trae CDI. Mojarra 2.3 activa su modo CDI si `web.xml` declara 4.0 o `faces-config.xml` declara 2.3, y entonces exige un `BeanManager`. Por eso `web.xml` usa el esquema 3.1 y `faces-config.xml` el 2.2: la implementación sigue siendo JSF 2.3. El POM incluye solo la API de CDI (la necesita `h:dataTable`) y la API de JAXB (el Flash cifra su cookie con ella y Java 11+ ya no la trae).

**Seguridad.** Igual que en las vistas JSP: sesión obligatoria para `/app/*` salvo `/app/acceso.xhtml`, cambio de id de sesión al ingresar, sesión de 30 minutos, cabeceras `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Referrer-Policy` y `Cache-Control: no-store`, UTF-8 y salida escapada por Facelets. Cada `h:form` envía el token CSRF de la sesión además del `ViewState` de JSF; un POST sin token correcto recibe 403. La cuenta se lee de `DPAO_APP_USER`/`DPAO_APP_PASSWORD`; no hay credenciales en el código.

**Catálogo real.** `sql/catalogo-dpao.sql` carga los 16 sabores del dueño (Clásico, Edición y Premium) y la tabla `pack_precio` con los packs de delivery de 2, 4 y 6 galletas; se puede ejecutar más de una vez. `sql/demo-insumos-dpao.sql` agrega nueve insumos de cocina de ejemplo. Ninguno borra `schema.sql` ni `seed.sql`.

**Pendiente.** La pantalla de cierre de caja de los lienzos no está implementada: el modelo actual no guarda el medio de pago de cada venta ni los cierres. Tampoco hay «Deshacer», porque el Kardex no anula movimientos.

## Acceso para revisión del docente

Cuenta pública de demostración local:

- **Usuario:** `demo`
- **Contraseña:** `Demo-local-APF1`
- **Dirección (JSF):** http://localhost:8080/dpao-cookies-web/ (lleva a `/app/acceso.xhtml`)
- **Dirección (JSP del APF1):** http://localhost:8080/dpao-cookies-web/acceso

Antes de arrancar Tomcat, configurar esta cuenta en la misma terminal:

```powershell
$env:DPAO_APP_USER='demo'
$env:DPAO_APP_PASSWORD='Demo-local-APF1'
```

Después, seguir la instalación de MySQL, configuración de las variables `DPAO_DB_*`, compilación y despliegue descritos abajo. Para revisar con datos de ejemplo, crear una base nueva y cargar `sql/schema.sql` seguido de `sql/demo.sql`, en lugar de `sql/seed.sql`. Si Tomcat ya estaba iniciado, reiniciarlo desde la terminal configurada para que reciba las variables.

Esta cuenta es exclusivamente de demostración; no es la cuenta de MySQL ni de GitHub. El repositorio contiene el código, pero no aloja la aplicación: `localhost` funciona en el equipo donde se instalen MySQL y Tomcat. La carpeta `.runtime` es local y no se incluye al clonar; el docente debe utilizar su propia instalación del entorno indicado abajo.

## Entorno comprobado

Windows 11, Eclipse Temurin JDK **17.0.20.1+1**, Maven **3.9.16**, Tomcat **9.0.115**, MySQL Community **8.4.9**, Connector/J **8.2.0**. `javax.servlet-api:4.0.1`, `javax.servlet.jsp-api:2.3.3` (provided), JSTL 1.2, Mojarra **2.3.9** (`javax.faces`), `cdi-api` 2.0.SP1 y `jakarta.xml.bind-api` 2.3.3 (solo API). Tomcat 10 requiere migración a `jakarta` y no es el destino de este WAR.

Las salidas auténticas están en [artifacts/evidence](artifacts/evidence). Las fechas de ejecución y hashes se obtienen de los registros y de `git log`, no de un historial reconstruido.

## Instalación y configuración

1. Clonar este repositorio. Instalar JDK 17, Maven, Tomcat 9 y MySQL 8.0.16 o posterior (se requieren CHECK aplicados).
2. Establecer `JAVA_HOME` al JDK 17 y añadir Maven a PATH. Confirmar `java -version` y `mvn -version`.
3. Crear una base **nueva** en MySQL. No ejecutar sobre una base con datos reales ni reutilizar a ciegas el esquema anterior:

```sql
CREATE DATABASE db_dpao_cookies CHARACTER SET utf8mb4;
USE db_dpao_cookies;
SOURCE C:/ruta/dpao-cookies-web/sql/schema.sql;
SOURCE C:/ruta/dpao-cookies-web/sql/seed.sql;
-- Catálogo real de D'PAO (16 sabores y packs de delivery), opcional:
SOURCE C:/ruta/dpao-cookies-web/sql/catalogo-dpao.sql;
```

`schema.sql` crea tablas, claves, CHECK, índices y FK sin DROP. `seed.sql` contiene datos ficticios y se aplica una sola vez sobre tablas nuevas. Para una instalación real, omitir los datos de demostración y cargar categorías autorizadas. Crear una cuenta de aplicación con permisos SELECT, INSERT y UPDATE sobre esta base; no necesita DROP, CREATE ni DELETE. La migración de datos de un esquema anterior requiere un plan separado y respaldo.

4. Definir las variables en el proceso que arrancará Tomcat (o en su servicio). No guardar secretos en Git:

```powershell
$env:DPAO_DB_URL='jdbc:mysql://127.0.0.1:3306/db_dpao_cookies?characterEncoding=UTF-8&serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000'
$env:DPAO_DB_USER='dpao_app'
$env:DPAO_DB_PASSWORD='<clave de la cuenta MySQL>'
$env:DPAO_APP_USER='<usuario de acceso>'
$env:DPAO_APP_PASSWORD='<clave de acceso>'
```

La conexión no contiene credenciales predeterminadas. Las tres variables DB también admiten propiedades JVM del mismo nombre para pruebas. La cuenta de acceso es única y se configura externamente; no hay administración de usuarios, roles ni auditoría individual. Para acceso fuera del equipo local, configurar HTTPS y certificados de MySQL conforme al entorno.

## Compilación y despliegue

```powershell
mvn -B package
Copy-Item target/dpao-cookies-web.war '<Tomcat9>/webapps/dpao-cookies-web.war'
& '<Tomcat9>/bin/catalina.bat' run
```

Configurar el conector de `conf/server.xml` y verificar que 8080 esté libre antes de arrancar otra instancia:

```xml
<Connector port="8080" address="127.0.0.1" protocol="HTTP/1.1"
           connectionTimeout="20000" URIEncoding="UTF-8" />
```

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
Invoke-WebRequest http://localhost:8080/dpao-cookies-web/app/acceso.xhtml
```

Para la demostración JSF con el catálogo real, crear una base nueva y cargar, en este orden, `sql/schema.sql`, `sql/catalogo-dpao.sql` y `sql/demo-insumos-dpao.sql`. Los sabores empiezan con cero en vitrina: las entradas, ventas y mermas se registran desde **Kardex → Nuevo movimiento** (o desde el tablero, **Registrar movimiento**). En Windows PowerShell una variable de entorno no puede quedar vacía; si la cuenta MySQL de pruebas no tiene clave, se pasa como propiedad JVM: `$env:CATALINA_OPTS='-DDPAO_DB_PASSWORD='`.

Abrir `http://localhost:8080/dpao-cookies-web/` (interfaz JSF) o `http://localhost:8080/dpao-cookies-web/acceso` (vistas JSP). El filtro establece UTF-8 antes de leer parámetros. JSP y fragmentos usan UTF-8; las tablas usan utf8mb4. Las vistas están bajo `WEB-INF/views`. Las operaciones POST usan token CSRF, sesión y redirección GET; los mensajes se consumen una vez. Los detalles SQL se registran en el servidor, no en la página.

## Funciones demostrables

| Ruta | Comportamiento |
|---|---|
| `/acceso` | Valida credenciales externas; renueva ID de sesión. `/salir` invalida sesión mediante POST. |
| `/tablero` | Cuenta insumos y productos, presenta alertas actuales y los cinco últimos movimientos. |
| `/insumos` | Registra (categoría desde lista) y lista insumos; el ajuste de stock se registra como AJUSTE en Kardex. Reabastecer cuando actual ≤ mínimo. |
| `/productos` | Registra y edita código, nombre, precio y categoría. Stock inicial cero; movimientos desde Kardex. |
| `/kardex` | Entradas, salidas, ventas, mermas y ajustes por conteo para insumos/productos elegidos por nombre; saldo anterior/posterior; filtros por tipo y fechas inclusivas. |

Cantidades y precios conservan `BigDecimal` hasta `setBigDecimal`. El servidor rechaza valores ausentes, negativos, con escala mayor de dos o mayores de 99999999.99; textos obligatorios y longitudes 20/100/20, códigos duplicados y FK inexistentes. El stock inicial del insumo es su saldo de apertura; el historial registra cambios posteriores. Un ajuste sin diferencia no crea un movimiento vacío.

Kardex utiliza dos FK anulables (`id_insumo`, `id_producto`) y un CHECK XOR. Bloquea el ítem con `SELECT ... FOR UPDATE`, actualiza el stock e inserta el movimiento en una sola transacción. Hace rollback si cualquier paso falla; no admite saldo negativo. Las cantidades de salida se guardan con signo negativo. Las tablas y columnas dinámicas se limitan a una lista fija, los datos van parametrizados.

## Pruebas

### Separar presentación y pruebas

Para presentar la aplicación, crear una base nueva `dpao_demo` y ejecutar `schema.sql` seguido de `demo.sql` (no `seed.sql`). Contiene tres insumos, tres productos y cuatro movimientos ficticios coherentes. Conectar Tomcat a esa base. Las alertas muestran código y unidad, y el historial identifica el código del ítem y explica el signo de las cantidades.

En este equipo, con MySQL disponible en 3307 y el Tomcat anterior detenido, `scripts/start-demo.ps1` arranca la instalación preparada en `.runtime` usando siempre `dpao_demo`; requiere las variables de credenciales configuradas. No reinicializa ni borra datos. La antigua `dpao_test` conserva las pruebas y sus evidencias.

`http_smoke.py` ahora comprueba que el servidor declare conexión a la base aislada `dpao_test` antes de enviar cualquier formulario. Se detiene si se ejecuta contra la demostración, aunque se haya definido `DPAO_HTTP_TEST=true`.

Unitarias, sin MySQL: `mvn -B clean test` (92 pruebas; las 14 de integración JDBC aparecen omitidas). Incluyen los managed beans (`bean/*Test`), probados con una fachada falsa y una `Vista` falsa, sin servidor ni `FacesContext`: ingreso válido e inválido, resumen del tablero, alta y ajuste de insumos, alta y edición de sabores, filtro y registro de movimientos, y cada mensaje de error en su campo o en su hoja. `KardexDAOTransaccionTest` usa un JDBC simulado con `java.lang.reflect.Proxy` (`support/JdbcFalso`) para comprobar `setAutoCommit(false)`, el orden bloqueo → UPDATE → INSERT, `commit()` y `rollback()` ante fallo del INSERT, stock insuficiente e ítem inexistente. `InventarioFacadeTest` verifica validación, mapeo a DTO, traducción de errores y una llamada de la fachada con rollback. Los tests JDBC aparecen **omitidos** si no se habilitan; esto no demuestra integración.

Para la suite completa, iniciar una instancia MySQL **exclusiva de pruebas** en `127.0.0.1:3307`, con un directorio de datos nuevo separado de cualquier instalación real. Crear `dpao_test`. La cuenta de pruebas necesita CREATE DATABASE, tablas y triggers, además de DML; no usar estos permisos para la aplicación real.

```powershell
$env:DPAO_JDBC_TEST='true'
$env:DPAO_DB_URL='jdbc:mysql://127.0.0.1:3307/dpao_test?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8'
$env:DPAO_DB_USER='<cuenta de pruebas>'
$env:DPAO_DB_PASSWORD='<clave de pruebas>'
mvn -B test
```

`JdbcTest` exige exactamente ese host, puerto y base inicial. Cada ejecución crea un esquema `dpao_test_<marca temporal>`, importa SQL y conserva sus datos para inspección. No borra datos ni usa mocks: prueba conexión y error de conexión reales, alta, consulta UTF-8, actualización decimal, duplicados, FK, productos, movimientos, stock insuficiente, restricciones, rollback ante fallo de INSERT y diez movimientos concurrentes sin pérdida de stock. `InventarioFacadeIntegracionTest` recorre la fachada contra el mismo esquema: saldos encadenados ENTRADA → VENTA → MERMA → AJUSTE, venta sin stock que no altera stock ni historial, rollback por trigger que hace fallar el INSERT, ajuste sin diferencia, código duplicado y diez ventas concurrentes sobre cinco unidades (cinco aceptadas, cinco rechazadas, stock final cero).

Pruebas HTTP (Python 3, solo biblioteca estándar): desplegar el WAR conectado a la base **de pruebas aislada `dpao_test` en 127.0.0.1:3307**, con `schema.sql` y `seed.sql` cargados, y usar las mismas credenciales externas de acceso:

```powershell
$env:DPAO_HTTP_TEST='true'
$env:DPAO_APP_USER='<usuario configurado en Tomcat>'
$env:DPAO_APP_PASSWORD='<clave configurada en Tomcat>'
python scripts/http_smoke.py
```

Este script agrega registros ficticios identificados por fecha de ejecución. Verifica cinco vistas, credenciales inválidas/válidas, navegación, UTF-8, PRG, errores, productos, movimientos, filtros, CSRF y salida. No debe ejecutarse contra un despliegue real. Los tiempos registrados corresponden a solicitudes locales secuenciales, no a una prueba de carga ni garantía del RNF de rendimiento.

## Evidencias y límites

Informe final: [APF1 en Word](docs/APF1_DesarrolloWebIntegrado_Casas_Anderson.docx), 21 páginas, índice actualizado con Word y revisión visual de cada página. La sección 18 contiene la matriz de los seis criterios y sus límites.

- `artifacts/evidence/apf2-jsf/`: interfaz JSF. Capturas `01` (web.xml y paquetes), `02` (interfaz con Bootstrap, computadora y celular), `03` (formulario JSF con datos y el resultado tras guardar), `04` (`h:dataTable` con datos de MySQL y la misma consulta en MySQL) y `05` (historial de commits); `maven-test.log` (92 pruebas, 14 omitidas sin MySQL), `maven-test-jdbc.log` (92 pruebas, 0 omitidas, MySQL 8.4.9), `http-smoke.log` y `medicion-vistas.log` (19 estados × 3 equipos: sin scroll, en la retícula de 4 y sin fallas axe AA, medidos con `design/herramientas/medir-jsf.js`).
- `artifacts/evidence/apf2-20261008/`: APF2, `mvn test` completo con MySQL 8.4.9 real (41 pruebas, 0 omitidas) y prueba HTTP sobre Tomcat 9.0.115.
- `artifacts/evidence/jdbc-tests.log`: suite completa con MySQL real (APF1).
- `artifacts/evidence/http-tests.log`: estados HTTP y aserciones del flujo.
- `artifacts/evidence/versions.log`, `package.log`: versiones y empaquetado.
- `artifacts/evidence/screenshots/`: capturas reales de cinco vistas y móvil.

Pendientes de evolución: cuentas múltiples y roles, responsable individual en Kardex, edición de otros datos del insumo, recetas y consumo automático por producción, lotes/caducidad, paginación, despliegue público con HTTPS y prueba de carga. Las ventas registran movimiento de stock; no emiten comprobantes ni procesan pagos. El proyecto no presenta estas capacidades como terminadas.
