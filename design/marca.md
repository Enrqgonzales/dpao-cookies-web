# D'PAO COOKIES — guía de marca para la interfaz (APF2)

Fuentes analizadas, entregadas por el dueño el 9 de octubre de 2026 (`../Datos del dueño D'PAO/`):
`03_logo.jpg`, `04_packs-delivery_precios.jpg`, `02_empaque_caja.png`, `05_producto_galletas.jpg`,
`06_campana-redes.jpg`, `01_control-diario_plaza-mayor_agosto.png` y `00_datos_del_dueno.md`.

La estética es **cartoon retro y cálida**: el chef galleta del logo con contornos gruesos chocolate, letras
redondeadas y gruesas, fondos mostaza y crema, píldoras oscuras para los precios y etiquetas tipo
«sticker» ligeramente inclinadas (como «PACK CLÁSICO» en el afiche).

## Paleta

Valores medidos con muestreo de píxeles sobre las imágenes originales.

| Token | Hex | Medido en | Uso en la interfaz |
|---|---|---|---|
| `--dp-mostaza` | `#F8AE1B` | Fondo del logo: `#F8AE1B` exacto. Afiche: `#FFB423`, `#FFAD42`. | Acentos, navegación activa, tarjeta de alerta de insumos, fondo del acceso. |
| `--dp-chocolate` | `#330704` | Letras y contornos del logo: `#330704` exacto (`#310708` en el chef). Afiche: `#1F0707`–`#250D01`. | Texto principal, encabezado, botones primarios, píldoras de precio. |
| `--dp-crema-clara` | `#FFF4DD` | No aparece como superficie en las piezas; es la crema del afiche aclarada para leer textos largos. | Fondo de página. |
| `--dp-crema` | `#F3E0B5` | Paneles del afiche (`#F9DBA9`, `#FDD790`) y campaña (`#F5D29A`); se tomó un tono algo menos saturado para superficies grandes. | Paneles de formulario, encabezados de tabla, chip «Clásico». |
| `--dp-arena` | `#EFC58B` | Fondo del afiche de packs: `#EFC58B`. | Reservado para fondos decorativos. |
| `--dp-caramelo` | `#C9741E` | Entre el cuerpo de la galleta del logo (`#F89848`) y los bordes horneados del afiche (`#521B00`). | Barras de progreso y decoración. **No** para texto pequeño. |
| `--dp-caramelo-osc` | `#8F4A0E` | Derivado del caramelo para cumplir AA. | Enlaces sobre crema. |
| `--dp-texto-2` | `#6B4A3A` | Derivado del chocolate. | Texto secundario. |

Estados (no aparecen en las piezas; se eligieron cálidos para no romper la paleta):
éxito `#1E5C33` sobre `#E2F2E6`, error `#9B1C14` sobre `#FCE4E0`, aviso `#6B4300` sobre `#FFF0C2`.

### Contraste (WCAG 2.1)

| Par | Relación | Nivel |
|---|---|---|
| Chocolate sobre crema clara | 16,40:1 | AA |
| Chocolate sobre crema | 13,76:1 | AA |
| Chocolate sobre mostaza (y al revés) | 9,43:1 | AA |
| Caramelo oscuro sobre crema clara | 6,10:1 | AA |
| Texto secundario sobre crema clara | 7,22:1 | AA |
| Éxito / error / aviso sobre su fondo | 6,86 / 6,74 / 7,62:1 | AA |
| Caramelo sobre crema clara | 3,21:1 | solo texto grande o decoración |
| Blanco sobre caramelo | 3,50:1 | no se usa para texto |

Reglas: nunca texto blanco sobre mostaza; el caramelo no lleva texto menor de 24 px; el foco visible es un
contorno chocolate de 3 px con halo mostaza. Las 24 combinaciones de lienzo × ancho pasaron axe-core
(`wcag2a`, `wcag2aa`) sin violaciones.

## Tipografía

| Rol | Familia | Pesos | Motivo |
|---|---|---|---|
| Títulos, cifras, precios | **Baloo 2** | 700, 800 | Redondeada y gruesa, cercana al rotulado «PACKS D'PAO» y «D'PAO COOKIES» de los flyers. |
| Cuerpo, formularios, tablas | **Nunito** | 400, 600, 700, 800 | Terminales redondeados que armonizan con Baloo; muy legible en tamaños pequeños. |

Ambas tienen licencia SIL Open Font License 1.1 (`assets/fonts/OFL-*.txt`) y se sirven **localmente** en
WOFF2 (subconjunto latino, que cubre á, é, í, ó, ú, ñ, ¿ y ¡). No se usa ningún CDN.

Escala: h1 `clamp(1.75rem, …, 2.6rem)`, h2 `clamp(1.3rem, …, 1.65rem)`, cuerpo 16 px con interlineado 1,55;
cifras con `font-variant-numeric: tabular-nums` y alineadas a la derecha.

## Recursos gráficos

- **Logo** (`assets/03_logo.jpg`, copia del original): en el encabezado, recortado en círculo con borde mostaza,
  y en grande en la pantalla de acceso.
- **Chef galleta** (`assets/chef-galleta.png`, recorte del logo): estados vacíos y notas de ayuda.
- **Bordes redondeados**: tarjetas de 20 px con borde chocolate translúcido y sombra sólida inferior, que
  imita el volumen del logo; botones y chips en forma de píldora.
- **Píldoras de precio**: fondo chocolate, «S/» e importe en mostaza, como «2 x s/ 12» del afiche.
- **Etiqueta sticker**: mostaza con texto chocolate, rotada −1,5°, para destacar el lugar o la sección.
- **Categorías** con los colores de las columnas del afiche: Clásico (crema), Edición (mostaza) y Premium (chocolate con texto mostaza).
- No se usan fotos de personas ni el nombre de la trabajadora. La mascota de la campaña en redes no se incorporó.

## Datos usados en los lienzos

- **Catálogo**: los 16 sabores, las 3 categorías y los precios de `00_datos_del_dueno.md`. El afiche escribe
  «Chin Chim», «Bom Bom», «Pie Limón» y «Mashmellow»; se usó la lista del dueño (Chin Chin, Bom o Bom, Limón,
  Marshmallow). **Por confirmar con el dueño.**
- **Packs**: Clásico 2/4/6 por S/ 12/22/30; Edición S/ 16/30/42; Premium S/ 20/40/58.
- **Cierre de caja (Plaza Mayor, agosto)**: leído de la hoja de control. Se vendieron 43 galletas por S/ 373,50 y se recibieron S/ 356,50.
  - Efectivo: esperado S/ 49,50 (6 ventas), declarado S/ 41,50 → faltan S/ 8,00.
  - Yape: esperado S/ 315,00 (36 ventas), declarado S/ 315,00 → cuadra.
  - Arándano: 1 venta marcada «R», que no es E, Y, P ni T → faltan S/ 9,00.
  - Diferencia total: −S/ 17,00. El stock de los 16 sabores cuadra (inicial 40 + reposición 46 − vendidas 43 = final 43).
- **Insumos**: son los 9 insumos críticos del dueño, pero las cantidades, los mínimos y los costos son **valores de ejemplo**.
- **Formato**: moneda `S/ 373,50` con coma decimal y espacio duro entre «S/» y el monto. La aplicación actual usa
  `fmt` con `es_PE`, que muestra punto decimal; hay que decidirlo antes de implementar.

## Responsividad y verificación

Breakpoints de Bootstrap 5.3. Las tablas largas se apilan como tarjetas por debajo de 576 px
(`.tabla-apilada`). La tabla de stock por sabor se desplaza en horizontal, con foco por teclado y un aviso.

Verificación con `design/herramientas/capturar.js` (Playwright + Edge instalado + axe-core); ver
`design/herramientas/README.md`:

- 360, 390, 768 y 1366 px: sin desbordamiento horizontal, sin texto recortado y sin palabras partidas.
- Fuentes locales cargadas (Baloo 2 700/800, Nunito 400–800).
- axe-core 4.10: 0 violaciones WCAG 2 A/AA en 360 y 1366 px.

## Licencias

Bootstrap 5.3.3 (MIT, `vendor/bootstrap/LICENSE`); Baloo 2 y Nunito (OFL 1.1). El logo y las imágenes de
marca pertenecen a D'PAO Cookies; el dueño las entregó para este proyecto académico.
