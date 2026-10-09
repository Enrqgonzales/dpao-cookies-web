# Herramientas de los lienzos

`capturar.js` abre cada lienzo en Microsoft Edge (Playwright, sin interfaz) en 360, 390, 768 y 1366 px y comprueba:

- que no haya desbordamiento horizontal, texto recortado ni palabras partidas;
- que carguen las fuentes locales;
- que axe-core no reporte violaciones WCAG 2 A/AA (en 360 y 1366 px).

Guarda capturas de página completa en `../capturas/` (1366 y 390 px).

```powershell
cd design/herramientas
npm install
npm run capturar
```

Requiere Node.js 18+ y Microsoft Edge instalado (se usa `channel: 'msedge'`, sin descargar navegadores).
