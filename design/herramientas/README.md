# Herramientas de los lienzos

- `capturar.js` abre cada lienzo y cada estado en Microsoft Edge (sin interfaz, Playwright conectado por CDP) en
  celular 390×844, tablet 768×1024 y computadora 1366×768, mide y fotografía. Falla si encuentra scroll, contenido
  escondido bajo lo fijo, tamaños de letra fuera de la escala (12, 16, 20, 24, 32), pesos indebidos, separaciones
  fuera de la retícula de 4, palabras o montos partidos, texto recortado o violaciones WCAG 2 A/AA (axe-core).
- `generar_iconos.js` produce `../lienzos/css/iconos.css` con los íconos Lucide como máscaras CSS.

```powershell
cd design/herramientas
npm install
npm run iconos      # solo si cambia la lista de íconos
npm run capturar    # mide los 31 estados y guarda las capturas en ../capturas
$env:SOLO="kardex"; npm run capturar   # mide un solo lienzo
```

Requiere Node.js 18+ y Microsoft Edge instalado. No descarga navegadores: arranca un Edge propio con puerto de
depuración (el lanzamiento por tubería de Playwright falla con Edge 135, que entrega el control a otro proceso)
y lo cierra al terminar.
