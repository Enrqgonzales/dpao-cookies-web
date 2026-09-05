# D'PAO COOKIES

APF1 de Desarrollo Web Integrado (100000SI54). Aplicación MVC/DAO con Servlets, JSP y JDBC. Cinco vistas operativas: acceso, tablero, inventario, productos y Kardex.

## Entorno comprobado

Windows 11, Eclipse Temurin JDK **17.0.20.1+1**, Maven **3.9.16**, Tomcat **9.0.115**, MySQL Community **8.4.9**, Connector/J **8.2.0**. `javax.servlet-api:4.0.1`, `javax.servlet.jsp-api:2.3.3` (provided) y JSTL 1.2. Tomcat 10 requiere migración a `jakarta` y no es el destino de este WAR.

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
Invoke-WebRequest http://localhost:8080/dpao-cookies-web/acceso
```

Abrir `http://localhost:8080/dpao-cookies-web/acceso`. El filtro establece UTF-8 antes de leer parámetros. JSP y fragmentos usan UTF-8; las tablas usan utf8mb4. Las vistas están bajo `WEB-INF/views`. Las operaciones POST usan token CSRF, sesión y redirección GET; los mensajes se consumen una vez. Los detalles SQL se registran en el servidor, no en la página.

## Funciones demostrables

| Ruta | Comportamiento |
|---|---|
| `/acceso` | Valida credenciales externas; renueva ID de sesión. `/salir` invalida sesión mediante POST. |
| `/tablero` | Cuenta insumos y productos de MySQL y presenta alertas actuales. |
| `/insumos` | Registra y lista insumos; ajusta stock y registra el cambio en Kardex. Reabastecer cuando actual ≤ mínimo. |
| `/productos` | Registra y edita código, nombre, precio y categoría. Stock inicial cero; movimientos desde Kardex. |
| `/kardex` | Entradas, salidas, ventas y mermas para insumos/productos; filtros por tipo y fechas inclusivas. |

Cantidades y precios conservan `BigDecimal` hasta `setBigDecimal`. El servidor rechaza valores ausentes, negativos, con escala mayor de dos o mayores de 99999999.99; textos obligatorios y longitudes 20/100/20, códigos duplicados y FK inexistentes. El stock inicial del insumo es su saldo de apertura; el historial registra cambios posteriores. Un ajuste sin diferencia no crea un movimiento vacío.

Kardex utiliza dos FK anulables (`id_insumo`, `id_producto`) y un CHECK XOR. Bloquea el ítem con `SELECT ... FOR UPDATE`, actualiza el stock e inserta el movimiento en una sola transacción. Hace rollback si cualquier paso falla; no admite saldo negativo. Las cantidades de salida se guardan con signo negativo. Las tablas y columnas dinámicas se limitan a una lista fija, los datos van parametrizados.

## Pruebas

Unitarias, sin MySQL: `mvn -B test`. Los tests JDBC aparecen **omitidos** si no se habilitan; esto no demuestra integración.

Para la suite completa, iniciar una instancia MySQL **exclusiva de pruebas** en `127.0.0.1:3307`, con un directorio de datos nuevo separado de cualquier instalación real. Crear `dpao_test`. La cuenta de pruebas necesita CREATE DATABASE, tablas y triggers, además de DML; no usar estos permisos para la aplicación real.

```powershell
$env:DPAO_JDBC_TEST='true'
$env:DPAO_DB_URL='jdbc:mysql://127.0.0.1:3307/dpao_test?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8'
$env:DPAO_DB_USER='<cuenta de pruebas>'
$env:DPAO_DB_PASSWORD='<clave de pruebas>'
mvn -B test
```

`JdbcTest` exige exactamente ese host, puerto y base inicial. Cada ejecución crea un esquema `dpao_test_<marca temporal>`, importa SQL y conserva sus datos para inspección. No borra datos ni usa mocks: prueba conexión y error de conexión reales, alta, consulta UTF-8, actualización decimal, duplicados, FK, productos, movimientos, stock insuficiente, restricciones, rollback ante fallo de INSERT y diez movimientos concurrentes sin pérdida de stock.

Pruebas HTTP (Python 3, solo biblioteca estándar): desplegar el WAR conectado a la base **de demostración aislada**, con `schema.sql` y `seed.sql` cargados, y usar las mismas credenciales externas de acceso:

```powershell
$env:DPAO_HTTP_TEST='true'
$env:DPAO_APP_USER='<usuario configurado en Tomcat>'
$env:DPAO_APP_PASSWORD='<clave configurada en Tomcat>'
python scripts/http_smoke.py
```

Este script agrega registros ficticios identificados por fecha de ejecución. Verifica cinco vistas, credenciales inválidas/válidas, navegación, UTF-8, PRG, errores, productos, movimientos, filtros, CSRF y salida. No debe ejecutarse contra un despliegue real. Los tiempos registrados corresponden a solicitudes locales secuenciales, no a una prueba de carga ni garantía del RNF de rendimiento.

## Evidencias y límites

Informe final: [APF1 en Word](docs/APF1_DesarrolloWebIntegrado_Casas_Anderson.docx), 21 páginas, índice actualizado con Word y revisión visual de cada página. La sección 18 contiene la matriz de los seis criterios y sus límites.

- `artifacts/evidence/jdbc-tests.log`: suite completa con MySQL real.
- `artifacts/evidence/http-tests.log`: estados HTTP y aserciones del flujo.
- `artifacts/evidence/versions.log`, `package.log`: versiones y empaquetado.
- `artifacts/evidence/screenshots/`: capturas reales de cinco vistas y móvil.

Pendientes de evolución: cuentas múltiples y roles, responsable individual en Kardex, edición de otros datos del insumo, recetas y consumo automático por producción, lotes/caducidad, paginación, despliegue público con HTTPS y prueba de carga. Las ventas registran movimiento de stock; no emiten comprobantes ni procesan pagos. El proyecto no presenta estas capacidades como terminadas.
