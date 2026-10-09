// Genera ../lienzos/css/iconos.css a partir de lucide-static: un solo juego de íconos, servido localmente.
// Cada ícono es una máscara CSS, así toma el color del texto (currentColor) y funciona en file://.
const fs = require('fs');
const path = require('path');

const ICONOS = ['layout-dashboard', 'wheat', 'cookie', 'arrow-left-right', 'wallet', 'plus', 'search',
    'sliders-horizontal', 'check', 'x', 'chevron-right', 'chevron-left', 'undo-2', 'log-out', 'store',
    'circle-user', 'package', 'triangle-alert'];

const carpeta = path.join(path.dirname(require.resolve('lucide-static/package.json')), 'icons');
const reglas = ICONOS.map(nombre => {
    const svg = fs.readFileSync(path.join(carpeta, `${nombre}.svg`), 'utf8')
        .replace(/<!--.*?-->/gs, '')
        .replace(/\s+class="[^"]*"/, '')
        .replace(/\s+/g, ' ')
        .replace(/> </g, '><')
        .trim();
    const uri = 'data:image/svg+xml,' + encodeURIComponent(svg).replace(/'/g, '%27');
    return `.ico-${nombre} { --ico: url("${uri}"); }`;
});

const css = `/* Íconos Lucide (lucide-static 0.460.0, licencia ISC). Generado por design/herramientas/generar_iconos.js; no editar a mano. */
.ico { display: inline-block; flex: none; width: 24px; height: 24px; background-color: currentColor;
  -webkit-mask: var(--ico) center / contain no-repeat; mask: var(--ico) center / contain no-repeat; vertical-align: middle; }
.ico-16 { width: 16px; height: 16px; }
${reglas.join('\n')}
`;
const destino = path.join(__dirname, '..', 'lienzos', 'css', 'iconos.css');
fs.writeFileSync(destino, css);
fs.copyFileSync(path.join(carpeta, '..', 'LICENSE'), path.join(__dirname, '..', 'assets', 'LICENSE-lucide.txt'));
console.log(`iconos.css: ${ICONOS.length} íconos`);
