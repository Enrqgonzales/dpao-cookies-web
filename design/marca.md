# D'PAO COOKIES — guía de marca e interfaz (APF2)

Fuentes analizadas, entregadas por el dueño el 9 de octubre de 2026 (`../Datos del dueño D'PAO/`):
`03_logo.jpg`, `04_packs-delivery_precios.jpg`, `02_empaque_caja.png`, `05_producto_galletas.jpg`,
`06_campana-redes.jpg`, `01_control-diario_plaza-mayor_agosto.png` y `00_datos_del_dueno.md`.

La estética es **cartoon retro y cálida**: el chef galleta del logo con contornos gruesos chocolate, letras
redondeadas y gruesas, fondos mostaza y crema, y píldoras oscuras para los precios.

La interfaz sigue la filosofía de diseño de Nexus del propietario (`nexus_erp/docs/diseno.md`), sin lo que
no aplica aquí (estilos Vidrio y Tonal, personalización de color, modo oscuro y lo propio de SUNAT):
**intuitiva, minimalista y medida**. Nada sobra, nada falta, y cada regla tiene una prueba.

## Paleta

Valores medidos con muestreo de píxeles sobre las imágenes originales.

| Token | Hex | Medido en | Papel en la interfaz |
|---|---|---|---|
| `--dp-mostaza` | `#F8AE1B` | Fondo del logo: `#F8AE1B` exacto. Afiche: `#FFB423`, `#FFAD42`. | **Solo** la acción principal y lo elegido (sección activa, pestaña, chip, página). Fondo del acceso. |
| `--dp-chocolate` | `#330704` | Letras y contornos del logo: `#330704` exacto. Afiche: `#1F0707`–`#250D01`. | El «grafito» de la casa: texto, cifras, cabecera, barra del celular, éxito con su palabra. |
| `--dp-crema-clara` | `#FFF4DD` | La crema del afiche aclarada para leer textos largos. | Fondo de página. |
| `--dp-crema` | `#F3E0B5` | Paneles del afiche (`#F9DBA9`, `#FDD790`) y campaña (`#F5D29A`). | Fondo de las barras de progreso y esqueletos de carga. |
| `--dp-arena` | `#EFC58B` | Fondo del afiche de packs. | No se usa en la interfaz. |
| `--dp-caramelo` | `#C9741E` | Entre la galleta del logo (`#F89848`) y los bordes horneados (`#521B00`). | No se usa en la interfaz (3.21:1 sobre crema). |
| `--dp-texto-2` | `#6B4A3A` | Derivado del chocolate. | Texto secundario y etiquetas. |
| `--dp-alerta` | `#993536` | Rojo vino apagado. | Solo alertas, siempre con punto **y** palabra («Reponer», «Agotado», «Falta 8.00»). |
| `--dp-alerta-sobre-oscuro` | `#D18789` | Su versión para fondo chocolate. | Estado en la cápsula y en la tarjeta oscura. |

No hay verde: lo que sale bien va en chocolate con un check y su palabra («Cuadra», «Caja cerrada»).

### Contraste (WCAG 2.1)

| Par | Relación |
|---|---|
| Chocolate sobre crema clara | 16.40:1 |
| Crema clara sobre chocolate | 16.40:1 |
| Chocolate sobre mostaza | 9.43:1 |
| Texto secundario sobre blanco | 7.88:1 |
| Alerta sobre blanco / sobre crema clara | 7.20:1 / 6.59:1 |
| Alerta clara sobre chocolate | 6.43:1 |

## Tipografía

| Rol | Familia | Pesos |
|---|---|---|
| Títulos y cifras | **Baloo 2**: redondeada y gruesa, cercana al rotulado de los flyers | 700; 800 solo en cifras de 32 px |
| Cuerpo, formularios y tablas | **Nunito**: terminales redondeados, legible en tamaños chicos | 400, 600, 700 |

Ambas con licencia SIL OFL 1.1 (`assets/fonts/OFL-*.txt`), servidas localmente en WOFF2. No se usa ningún CDN.

**Escala cerrada de cinco tamaños**, cada uno con su papel: cifra 32/40, título 24/32, subtítulo 20/28,
cuerpo 16/24 y etiqueta 12/16. Un tamaño fuera de la escala es un error. Cifras tabulares, alineadas a la derecha.

## Reglas de la interfaz

**Orden**
- **Una pantalla, una acción principal**, en mostaza y siempre en el mismo lugar: arriba a la derecha en tablet y
  computadora, y a lo ancho encima de la barra en el celular, al alcance del pulgar. Una pestaña sin acción deja su
  lugar reservado. Un recorrido (la Caja) mantiene su botón: mismo color, tamaño y lugar en cada paso.
- **Lo avanzado aparece cuando se necesita**: los formularios viven en una hoja que se abre con la acción
  principal; buscar y filtrar son íconos.
- **Perdona los errores**: tras guardar aparece «Deshacer». La confirmación queda solo para lo que no tiene vuelta:
  cerrar la caja.

**Sin scroll y sin huecos**
- Cada pantalla entra entera en celular (390×844), tablet (768×1024) y computadora (1366×768).
- Lo que no entra se divide en **páginas numeradas**; cada fila declara su página por equipo (`data-m`, `data-t`, `data-d`).
- Una tarjeta mide lo que su contenido; nada de media tarjeta vacía.

**Sin textos innecesarios**
- La explicación de una sección va solo en la **primera visita**, a pantalla grande, con «Siguiente» y «Entendido».
- Lo que se repite igual en todas las filas no se muestra: «kg» y «S/» van en el encabezado de la columna, y el
  estado aparece solo cuando es un problema.
- Cada cosa aparece una vez: la cápsula de la cabecera dice el local y el estado que importa («Plaza Mayor · Caja por cerrar»).

**Cómo se ve**
- El dinero es el protagonista: en las cifras grandes, «S/» más chico y suave, y los decimales atenuados.
- Las cifras de resumen van todas en chocolate y con la misma forma; el color de alerta vive en la fila del problema.
- Las filas se separan con una línea fina de 1 px al 12 %, sin franjas. Los días se separan con su etiqueta («**Hoy**»).
- Tres capas: fondo, superficies (borde fino) y lo que flota (hojas, ventanas y avisos, con sombra).
- La cápsula (píldora) es la firma: acción principal, pestañas, chips, páginas y estado.
- Un solo juego de íconos, Lucide (ISC), local en `lienzos/css/iconos.css`; sin emojis. Un ícono que se repite
  en todas las filas sobra.

**Medidas**
- Retícula de 4 px; las separaciones grandes, de 8.
- Radios concéntricos: superficie 28 px con relleno 16 (celular y tablet) o 36 con relleno 24 (computadora);
  dentro van campos de 12 px (28 − 16, 36 − 24). Píldoras redondas.
- Toques de 44 px como mínimo; botones y campos de 48 px.

**Celular**: barra de cinco secciones abajo, y solo la activa dice su nombre. Las tablas pasan a filas de dos
renglones: lo principal a la izquierda y la cifra a la derecha, con el detalle debajo.

## Recursos gráficos

- **Logo** (`assets/03_logo.jpg`, copia del original): en la cabecera en círculo y en grande en el acceso.
- **Chef galleta** (`assets/chef-galleta.png`, recorte del logo): primera visita y pantallas vacías.
- **Packs**: tres celdas iguales por pack, cantidad arriba y precio en mostaza sobre chocolate, como las píldoras del afiche.
- No se usan fotos de personas ni el nombre de la trabajadora. La mascota de la campaña en redes no se incorporó.

## Datos usados en los lienzos

- **Catálogo**: los 16 sabores, las 3 categorías y los precios de `00_datos_del_dueno.md`. El afiche escribe
  «Chin Chim», «Bom Bom», «Pie Limón» y «Mashmellow»; se usan los nombres de la lista del dueño (confirmado
  el 9 de octubre de 2026).
- **Packs**: Clásico 2/4/6 por S/ 12/22/30; Edición S/ 16/30/42; Premium S/ 20/40/58.
- **Caja (Plaza Mayor, agosto)**, leída de la hoja de control: 43 galletas por S/ 373.50; recibido S/ 356.50.
  - Efectivo: esperado S/ 49.50 (6 ventas), declarado S/ 41.50 → faltan S/ 8.00.
  - Yape: esperado S/ 315.00 (36 ventas), declarado S/ 315.00 → cuadra.
  - Arándano: 1 venta anotada «R», que no es E, Y, P ni T → faltan S/ 9.00.
  - Diferencia: −S/ 17.00. El stock de los 16 sabores cuadra (inicial 40 + reposición 46 − vendidas 43 = final 43).
- **Insumos**: los 9 insumos críticos del dueño; cantidades, mínimos y costos son **valores de ejemplo**.
- **Formato** (decidido el 9 de octubre de 2026): `S/ 373.50`, con punto decimal como el `es_PE` que ya usa la
  aplicación, y espacio duro tras «S/». Así el diseño y la implementación muestran lo mismo.

## Cómo se ven los estados

Cada lienzo muestra sus estados con la dirección, sin duplicar pantallas (`lienzos/js/lienzo.js`):
`?estado=hoja|error|exito|vacio|cargando|primera|confirmar`, `?paso=1..3`, `?pestana=…`, `?pagina=1..3`.
`lienzos/index.html` enlaza todos.

## Verificación

**Un diseño se aprueba midiéndolo.** `design/herramientas/capturar.js` (Playwright conectado por CDP a Edge,
y axe-core) recorre las 6 pantallas en 31 estados y 3 equipos (93 mediciones), y falla si encuentra:

- scroll vertical u horizontal, o contenido escondido bajo la barra o el botón fijos;
- un tamaño de letra fuera de la escala, o un peso 800 que no sea cifra;
- una separación que no sea múltiplo de 4;
- palabras o montos partidos en dos renglones, o texto recortado;
- violaciones WCAG 2 A/AA (axe-core 4.10).

Última corrida: **sin observaciones**. Tamaños usados: 12, 16, 20, 24 y 32 px. Radios: 8 (casilla, esqueleto),
12, 28, 36 y píldora. Las capturas quedan en `design/capturas/` (`<pantalla>-<estado>-<ancho>.png`).

## Licencias

Bootstrap 5.3.3 (MIT, `vendor/bootstrap/LICENSE`); Baloo 2 y Nunito (OFL 1.1); Lucide (ISC,
`assets/LICENSE-lucide.txt`). El logo y las imágenes de marca pertenecen a D'PAO Cookies; el dueño las entregó
para este proyecto académico.
