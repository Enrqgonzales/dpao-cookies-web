// Verifica y captura los lienzos con Playwright (Edge instalado).
// Uso: node capturar.js <carpeta-lienzos> <carpeta-capturas>
const { chromium } = require('playwright-core');
const path = require('path');
const fs = require('fs');
const { pathToFileURL } = require('url');

const [dirLienzos, dirCapturas] = process.argv.slice(2);
const lienzos = ['acceso', 'tablero', 'inventario', 'productos', 'kardex', 'cierre-caja'];
const axe = fs.readFileSync(require.resolve('axe-core/axe.min.js'), 'utf8');
fs.mkdirSync(dirCapturas, { recursive: true });

(async () => {
    // Edge actual ya no trae el modo headless antiguo que pide esta versión de Playwright.
    const browser = await chromium.launch({ channel: 'msedge', headless: false, args: ['--headless=new'] });
    let problemas = 0;
    for (const ancho of [360, 390, 768, 1366]) {
        const context = await browser.newContext({ viewport: { width: ancho, height: 900 }, deviceScaleFactor: 1 });
        const page = await context.newPage();
        const errores = [];
        page.on('console', m => { if (m.type() === 'error') errores.push(m.text()); });
        page.on('requestfailed', r => errores.push('recurso no cargado: ' + r.url()));
        for (const nombre of lienzos) {
            await page.goto(pathToFileURL(path.join(dirLienzos, nombre + '.html')).href);
            await page.evaluate(() => document.fonts.ready);
            const r = await page.evaluate(() => {
                const vw = document.documentElement.clientWidth;
                const desborde = document.documentElement.scrollWidth - vw;
                const fuera = [];
                const cortados = [];
                for (const el of document.querySelectorAll('body *')) {
                    if (el.closest('.table-responsive') || el.closest('.tabla-apilada thead') || el.closest('.visually-hidden') || el.closest('.collapse:not(.show)')) continue;
                    const cs = getComputedStyle(el);
                    if (cs.display === 'none' || cs.visibility === 'hidden') continue;
                    const b = el.getBoundingClientRect();
                    if (b.width && b.right > vw + 1) fuera.push(el.tagName + '.' + el.className + ' «' + el.textContent.trim().slice(0, 30) + '»');
                    if (el.children.length === 0 && el.textContent.trim() && (cs.overflow === 'hidden' || cs.textOverflow === 'ellipsis') && el.scrollWidth > el.clientWidth + 1)
                        cortados.push(el.tagName + ' «' + el.textContent.trim().slice(0, 30) + '»');
                }
                // Palabras partidas: una palabra (sin espacios) cuyo texto ocupa más de una línea.
                const recorrido = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
                for (let n = recorrido.nextNode(); n; n = recorrido.nextNode()) {
                    const el = n.parentElement;
                    if (!el || el.closest('.tabla-apilada thead, .visually-hidden, script, style, .collapse:not(.show)')) continue;
                    for (const m of n.textContent.matchAll(/\S{3,}/g)) {
                        const rango = document.createRange();
                        rango.setStart(n, m.index); rango.setEnd(n, m.index + m[0].length);
                        const lineas = new Set([...rango.getClientRects()].filter(r => r.width > 0).map(r => Math.round(r.top)));
                        if (lineas.size > 1) cortados.push('palabra partida «' + m[0] + '»');
                    }
                }
                const fuentes =[...new Set([...document.fonts].filter(f => f.status === 'loaded').map(f => f.family + ' ' + f.weight))];
                return { desborde, fuera: fuera.slice(0, 5), cortados: cortados.slice(0, 5), fuentes };
            });
            let contraste = [];
            if (ancho === 1366 || ancho === 360) {
                await page.addScriptTag({ content: axe });
                contraste = await page.evaluate(async () => {
                    const res = await axe.run(document, { preload: false, runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa'] } });
                    return res.violations.map(v => `${v.id} (${v.nodes.length}): ${v.nodes.slice(0, 2).map(n => n.target.join(' ')).join(' | ')}`);
                });
            }
            const ok = r.desborde <= 0 && !r.fuera.length && !r.cortados.length && !contraste.length;
            if (!ok) problemas++;
            console.log(`${ok ? 'OK ' : 'REV'} ${String(ancho).padStart(4)}px ${nombre.padEnd(12)} desborde=${r.desborde}` +
                (r.fuera.length ? ` fuera=${JSON.stringify(r.fuera)}` : '') + (r.cortados.length ? ` cortados=${JSON.stringify(r.cortados)}` : '') +
                (contraste.length ? ` axe=${JSON.stringify(contraste)}` : '') + (ancho === 1366 && nombre === 'acceso' ? ` fuentes=${r.fuentes.join(', ')}` : ''));
            if (ancho === 1366 || ancho === 390) {
                await page.screenshot({ path: path.join(dirCapturas, `${nombre}-${ancho}.png`), fullPage: true });
            }
        }
        if (errores.length) { problemas++; console.log('Errores de carga:', [...new Set(errores)]); }
        await context.close();
    }
    await browser.close();
    console.log(problemas ? `Con observaciones: ${problemas}` : 'Sin observaciones');
})().catch(e => { console.error(e); process.exit(1); });
