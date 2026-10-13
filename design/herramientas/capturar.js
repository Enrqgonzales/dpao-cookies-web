// Mide y fotografía los lienzos con Playwright (Edge instalado) y axe-core.
// Un diseño se aprueba midiéndolo: sin scroll, escala tipográfica cerrada, retícula de 4, sin palabras partidas, AA.
// Uso: node capturar.js <carpeta-lienzos> <carpeta-capturas>
const { EQUIPOS, medir, abrirEdge, revisarAxe, problemas: revisar } = require('./medicion');
const path = require('path');
const fs = require('fs');
const { pathToFileURL } = require('url');

const [dirLienzos, dirCapturas] = process.argv.slice(2);
// [lienzo, consulta, nombre de la captura, equipos fotografiados]
const CASOS = [
    ['acceso', '', 'acceso', ['celular', 'tablet', 'computadora']],
    ['acceso', '?estado=error', 'acceso-error', ['celular', 'computadora']],
    ['tablero', '', 'tablero', ['celular', 'tablet', 'computadora']],
    ['tablero', '?pestana=atencion', 'tablero-atencion', ['celular']],
    ['tablero', '?estado=cargando', 'tablero-cargando', ['celular', 'computadora']],
    ['tablero', '?estado=primera', 'tablero-primera', ['celular', 'computadora']],
    ['inventario', '', 'insumos', ['celular', 'tablet', 'computadora']],
    ['inventario', '?pagina=2', 'insumos-pagina2', ['celular']],
    ['inventario', '?estado=hoja', 'insumos-hoja', ['celular', 'computadora']],
    ['inventario', '?estado=error', 'insumos-error', ['celular', 'computadora']],
    ['inventario', '?estado=exito', 'insumos-exito', ['celular', 'computadora']],
    ['inventario', '?estado=vacio', 'insumos-vacio', ['celular', 'computadora']],
    ['productos', '', 'productos', ['celular', 'tablet', 'computadora']],
    ['productos', '?pagina=2', 'productos-pagina2', ['celular']],
    ['productos', '?pestana=packs', 'productos-packs', ['celular', 'computadora']],
    ['productos', '?estado=hoja', 'productos-hoja', ['celular', 'computadora']],
    ['productos', '?estado=vacio', 'productos-vacio', ['celular', 'computadora']],
    ['kardex', '', 'kardex', ['celular', 'tablet', 'computadora']],
    ['kardex', '?pagina=2', 'kardex-pagina2', ['celular', 'computadora']],
    ['kardex', '?estado=hoja', 'kardex-hoja', ['celular', 'computadora']],
    ['kardex', '?estado=error', 'kardex-error', ['celular', 'computadora']],
    ['kardex', '?estado=exito', 'kardex-exito', ['celular', 'computadora']],
    ['kardex', '?estado=vacio', 'kardex-vacio', ['celular', 'computadora']],
    ['cierre-caja', '?paso=1', 'caja-paso1-stock', ['celular', 'tablet', 'computadora']],
    ['cierre-caja', '?paso=1&pagina=2', 'caja-paso1-pagina2', ['celular', 'computadora']],
    ['cierre-caja', '?paso=2', 'caja-paso2-dinero', ['celular', 'tablet', 'computadora']],
    ['cierre-caja', '', 'caja-paso3-diferencia', ['celular', 'tablet', 'computadora']],
    ['cierre-caja', '?estado=error', 'caja-error', ['celular', 'computadora']],
    ['cierre-caja', '?estado=confirmar', 'caja-confirmar', ['celular', 'computadora']],
    ['cierre-caja', '?estado=exito', 'caja-cerrada', ['celular', 'computadora']],
    ['cierre-caja', '?estado=primera', 'caja-primera', ['celular', 'computadora']],
];
fs.mkdirSync(dirCapturas, { recursive: true });

(async () => {
    const { browser, cerrar } = await abrirEdge();
    let observaciones = 0;
    const tamanosGlobal = {}, radiosGlobal = {};
    const solo = process.env.SOLO ? process.env.SOLO.split(',') : null;   // SOLO=tablero,kardex para medir algunos
    for (const [lienzo, consulta, nombre, fotos] of CASOS.filter(c => !solo || solo.includes(c[0]))) {
        for (const [equipo, viewport] of Object.entries(EQUIPOS)) {
            const context = await browser.newContext({ viewport, deviceScaleFactor: 1 });
            const page = await context.newPage();
            const errores = [];
            page.on('pageerror', e => errores.push(String(e)));
            page.on('requestfailed', q => errores.push('no cargó ' + q.url()));
            await page.goto(pathToFileURL(path.join(dirLienzos, lienzo + '.html')).href + consulta);
            await page.evaluate(() => document.fonts.ready);
            const r = await page.evaluate(medir);
            const axeR = await revisarAxe(page);
            Object.entries(r.tamanos).forEach(([t, n]) => tamanosGlobal[t] = (tamanosGlobal[t] || 0) + n);
            Object.entries(r.radios).forEach(([t, n]) => radiosGlobal[t] = (radiosGlobal[t] || 0) + n);
            const problemas = revisar(r, axeR, errores);
            if (problemas.length) observaciones++;
            console.log(`${problemas.length ? 'REV' : 'OK '} ${equipo.padEnd(11)} ${nombre.padEnd(22)} ${problemas.join(' · ')}`);
            if (fotos.includes(equipo)) await page.screenshot({ path: path.join(dirCapturas, `${nombre}-${viewport.width}.png`) });
            await context.close();
        }
    }
    await cerrar();
    console.log('Tamaños de letra usados (px: elementos):', JSON.stringify(tamanosGlobal));
    console.log('Radios usados:', JSON.stringify(radiosGlobal));
    console.log(observaciones ? `Con observaciones: ${observaciones}` : 'Sin observaciones');
})().catch(e => { console.error(e); process.exit(1); });
