# Revisión visual y funcional — 9 de septiembre de 2026

Alcance: correcciones de presentación del Avance 1, sin funciones adicionales.

- Se unificaron los contenedores del encabezado y contenido y se eliminó el CSS duplicado de inventario.
- Formularios de una columna en móvil, controles ajustados al espacio y tablas con desplazamiento interno.
- La edición desplegada del catálogo cabe en su columna (220 px de contenido dentro de una celda de 240 px).
- Textos y etiquetas consistentes; límites de DECIMAL(10,2) también en los campos de inventario.
- Chrome: navegación de tablero, inventario, productos y Kardex con tamaños solicitados de 320, 360, 768, 1280 y 1920. El zoom existente de Chrome dio anchos CSS reales de 355, 400, 853, 1422 y 2133, registrados en responsive.json. No se detectó desbordamiento horizontal de la página ni caracteres de sustitución en esos recorridos. Las tablas conservan desplazamiento interno en móvil. Acceso se comprobó en los mismos tamaños, en access-responsive.json.
- Captura real: kardex-mobile.jpg. La captura final de escritorio no se obtuvo porque la conexión de automatización con Chrome agotó su tiempo de espera; no se presenta como evidencia.

## Pruebas reproducidas

JDK local 17.0.20.1, Maven del entorno NetBeans, Tomcat 9.0.115, MySQL real 8.4. Resultado completo en maven.log y http.log.

Compilación y pruebas: JAVA_HOME apuntando al JDK 17, DPAO_JDBC_TEST=true, URL de dpao_test en 127.0.0.1:3307, credenciales locales mediante variables de entorno.

    mvn -B -o -Dmaven.repo.local=.runtime/m2 package

Resultado: BUILD SUCCESS; 11 pruebas, 0 fallos, 0 errores, 0 omitidas. JDBC crea su propio esquema aislado.

Para HTTP se desplegó el WAR en Tomcat conectado a dpao_test, con DPAO_HTTP_TEST=true y credenciales de acceso por entorno:

    python scripts/http_smoke.py

Resultado: PASS de navegación, autenticación, PRG, UTF-8, registro, edición, validaciones, Kardex, filtros, CSRF y cierre de sesión. Se restauró después Tomcat conectado a dpao_demo. No se modificaron los datos de demostración con estas pruebas.

Estas comprobaciones cubren los recorridos indicados; no representan una garantía universal para cualquier navegador o dispositivo.
