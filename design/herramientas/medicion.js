// Medición compartida por capturar.js (lienzos) y medir-jsf.js (vistas JSF desplegadas en Tomcat).
// Un diseño se aprueba midiéndolo: sin scroll, escala tipográfica cerrada, retícula de 4, sin palabras partidas, AA.
const { chromium } = require('playwright-core');
const path = require('path');
const fs = require('fs');
const os = require('os');
const { spawn, execSync } = require('child_process');

const EQUIPOS = { celular: { width: 390, height: 844 }, tablet: { width: 768, height: 1024 }, computadora: { width: 1366, height: 768 } };
const TAMANOS = [12, 16, 20, 24, 32];
const AXE = fs.readFileSync(require.resolve('axe-core/axe.min.js'), 'utf8');

// Se ejecuta dentro de la página (page.evaluate).
function medir() {
    const vw = document.documentElement.clientWidth, vh = window.innerHeight;
    const r = { scrollV: document.documentElement.scrollHeight - vh, scrollH: document.documentElement.scrollWidth - vw,
        tamanos: {}, pesos: [], fueraRetícula: [], radios: {}, partidas: [], recortados: [] };
    const visible = el => { const cs = getComputedStyle(el); return cs.display !== 'none' && cs.visibility !== 'hidden' && el.getClientRects().length; };
    for (const el of document.querySelectorAll('body *')) {
        if (!visible(el) || el.closest('svg, script, style, .visually-hidden')) continue;
        const cs = getComputedStyle(el);
        const textoPropio = [...el.childNodes].some(n => n.nodeType === 3 && n.textContent.trim());
        if (textoPropio) {
            const t = Math.round(parseFloat(cs.fontSize));
            r.tamanos[t] = (r.tamanos[t] || 0) + 1;
            const w = Number(cs.fontWeight);
            if (w > 800 || (w === 800 && t < 28) || w === 500 || w === 300) r.pesos.push(`${el.tagName}.${el.className} ${t}px/${w}`);
            if (el.children.length === 0 && (cs.overflow === 'hidden' || cs.textOverflow === 'ellipsis') && el.scrollWidth > el.clientWidth + 1)
                r.recortados.push(el.textContent.trim().slice(0, 30));
        }
        for (const p of ['paddingTop', 'paddingRight', 'paddingBottom', 'paddingLeft', 'marginTop', 'marginBottom', 'rowGap', 'columnGap']) {
            const v = parseFloat(cs[p]);
            if (v && Math.abs(v - Math.round(v / 4) * 4) > 0.5 && !el.matches('input, select, textarea, option, html, body'))
                r.fueraRetícula.push(`${el.tagName}.${String(el.className).slice(0, 30)} ${p}=${v}`);
        }
        const rad = cs.borderTopLeftRadius;
        if (rad !== '0px' && !rad.includes('%')) r.radios[rad] = (r.radios[rad] || 0) + 1;
    }
    const recorrido = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
    for (let n = recorrido.nextNode(); n; n = recorrido.nextNode()) {
        const el = n.parentElement;
        if (!el || !visible(el) || el.closest('.visually-hidden, script, style')) continue;
        for (const m of n.textContent.matchAll(/\S{3,}/g)) {
            const rango = document.createRange(); rango.setStart(n, m.index); rango.setEnd(n, m.index + m[0].length);
            if (new Set([...rango.getClientRects()].filter(x => x.width > 0).map(x => Math.round(x.top))).size > 1) r.partidas.push(m[0]);
        }
        // Montos "S/ 12,00" partidos en dos renglones
        for (const m of n.textContent.matchAll(/S\/ ?\s?[\d.,]+/g)) {
            const rango = document.createRange(); rango.setStart(n, m.index); rango.setEnd(n, m.index + m[0].length);
            if (new Set([...rango.getClientRects()].filter(x => x.width > 0).map(x => Math.round(x.top))).size > 1) r.partidas.push(m[0]);
        }
    }
    // Nada escondido: ningún contenido del cuerpo queda debajo de la barra o del botón fijos.
    r.escondidos = [];
    const fijos = [...document.querySelectorAll('.barra-secciones, .accion-principal')]
        .filter(f => visible(f) && getComputedStyle(f).position === 'fixed' && getComputedStyle(f).visibility !== 'hidden')
        .map(f => f.getBoundingClientRect());
    if (fijos.length) {
        const tope = Math.min(...fijos.map(f => f.top));
        for (const el of document.querySelectorAll('main *')) {
            if (!visible(el) || el.closest('.accion-principal, .velo')) continue;
            const b = el.getBoundingClientRect();
            if (b.height && b.bottom > tope + 1 && el.children.length === 0 && el.textContent.trim()) r.escondidos.push(el.textContent.trim().slice(0, 30));
        }
    }
    r.fuentes = [...new Set([...document.fonts].filter(f => f.status === 'loaded').map(f => `${f.family} ${f.weight}`))].sort();
    return r;
}

// Edge actual entrega el control a otro proceso al arrancar, y el lanzamiento por tubería de Playwright
// lo da por cerrado. Se arranca un Edge propio con puerto de depuración y Playwright se conecta por CDP.
const EDGE = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
async function abrirEdge() {
    const puerto = 9400 + Math.floor(Math.random() * 400);
    const perfil = fs.mkdtempSync(path.join(os.tmpdir(), 'lienzos-edge-'));
    spawn(EDGE, ['--headless=new', `--remote-debugging-port=${puerto}`, `--user-data-dir=${perfil}`, '--no-first-run',
        '--disable-extensions', '--hide-scrollbars', 'about:blank'], { detached: true, stdio: 'ignore' }).unref();
    for (let i = 0; i < 60; i++) {
        try { await (await fetch(`http://127.0.0.1:${puerto}/json/version`)).json(); break; }
        catch { await new Promise(r => setTimeout(r, 250)); }
    }
    const browser = await chromium.connectOverCDP(`http://127.0.0.1:${puerto}`);
    const cerrar = async () => {
        await browser.close().catch(() => {});
        try { execSync(`powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \\"Name='msedge.exe'\\" | Where-Object CommandLine -like '*${path.basename(perfil)}*' | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }"`, { stdio: 'ignore' }); } catch {}
    };
    return { browser, cerrar };
}

/** axe-core con las reglas WCAG 2 A y AA. */
async function revisarAxe(page) {
    await page.addScriptTag({ content: AXE });
    return page.evaluate(async () => (await axe.run(document, { preload: false, runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa'] } }))
        .violations.map(v => `${v.id}: ${v.nodes.slice(0, 2).map(n => n.target.join(' ')).join(' | ')}`));
}

/** Problemas de una medición según las reglas de diseño (lista vacía = aprobada). */
function problemas(r, axeR, errores) {
    const p = [];
    const tamanosRaros = Object.keys(r.tamanos).map(Number).filter(t => !TAMANOS.includes(t));
    if (r.scrollV > 0) p.push(`scroll vertical ${r.scrollV}px`);
    if (r.scrollH > 0) p.push(`scroll horizontal ${r.scrollH}px`);
    if (tamanosRaros.length) p.push(`tamaños fuera de escala ${tamanosRaros}`);
    if (r.pesos.length) p.push(`pesos ${r.pesos.slice(0, 3)}`);
    if (r.fueraRetícula.length) p.push(`fuera de la retícula ${r.fueraRetícula.slice(0, 4)}`);
    if (r.partidas.length) p.push(`partidas ${[...new Set(r.partidas)].slice(0, 4)}`);
    if (r.recortados.length) p.push(`recortados ${r.recortados.slice(0, 3)}`);
    if (r.escondidos.length) p.push(`escondido bajo lo fijo ${r.escondidos.slice(0, 3)}`);
    if (axeR.length) p.push(`axe ${axeR.slice(0, 3)}`);
    if (errores.length) p.push(`errores ${[...new Set(errores)].slice(0, 2)}`);
    return p;
}

module.exports = { EQUIPOS, TAMANOS, medir, abrirEdge, revisarAxe, problemas };
