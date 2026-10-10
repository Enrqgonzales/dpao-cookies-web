// Mide y fotografía las vistas JSF desplegadas (Tomcat + MySQL) con las mismas reglas que los lienzos.
// La cuenta se lee del entorno (DPAO_APP_USER / DPAO_APP_PASSWORD), igual que en el servidor.
//
// Uso:
//   node medir-jsf.js medir <carpeta-capturas>            mide todas las pantallas y estados
//   node medir-jsf.js evidencia <carpeta-evidencia>       capturas de la entrega (interfaz, formulario, h:dataTable)
//   node medir-jsf.js pagina <archivo.html|URL> <salida.png>  fotografía una página local o pública (estructura, historial)
// Variable opcional: DPAO_URL (por defecto http://localhost:8080/dpao-cookies-web)
const path = require('path');
const fs = require('fs');
const { pathToFileURL } = require('url');
const { EQUIPOS, medir, abrirEdge, revisarAxe, problemas } = require('./medicion');

const BASE = process.env.DPAO_URL || 'http://localhost:8080/dpao-cookies-web';
const USUARIO = process.env.DPAO_APP_USER;
const CLAVE = process.env.DPAO_APP_PASSWORD;
const PANTALLAS = ['tablero', 'insumos', 'productos', 'kardex'];

// [ruta, nombre, equipos fotografiados, opciones]
//   primera: deja ver la orientación de primera visita · sinSesion: sin ingresar · antes: acción previa a medir
const CASOS = [
    ['/app/acceso.xhtml', 'acceso', ['celular', 'computadora'], { sinSesion: true }],
    ['/app/acceso.xhtml', 'acceso-error', ['celular', 'computadora'], { sinSesion: true, antes: async page => {
        await page.fill('#acceso\\:usuario', 'demo');
        await page.fill('#acceso\\:clave', 'clave-equivocada');
        await Promise.all([page.waitForNavigation(), page.click('input[type=submit]')]);
    } }],
    ['/app/tablero.xhtml', 'tablero', ['celular', 'tablet', 'computadora'], {}],
    ['/app/tablero.xhtml?pestana=atencion', 'tablero-atencion', ['celular'], {}],
    ['/app/tablero.xhtml', 'tablero-primera', ['celular', 'computadora'], { primera: true }],
    ['/app/insumos.xhtml', 'insumos', ['celular', 'tablet', 'computadora'], {}],
    ['/app/insumos.xhtml?hoja=nuevo', 'insumos-hoja', ['celular', 'computadora'], {}],
    ['/app/insumos.xhtml?hoja=nuevo', 'insumos-error', ['celular', 'computadora'], { antes: async page => {
        await page.fill('#formulario\\:stock', '2,50');
        await Promise.all([page.waitForNavigation(), page.click('#formulario input[type=submit]')]);
    } }],
    ['/app/insumos.xhtml?hoja=ajuste&id=4', 'insumos-ajuste', ['celular', 'computadora'], {}],
    ['/app/productos.xhtml', 'productos', ['celular', 'tablet', 'computadora'], {}],
    ['/app/productos.xhtml?categoria=3', 'productos-premium', ['celular'], {}],
    ['/app/productos.xhtml?pestana=packs', 'productos-packs', ['celular', 'computadora'], {}],
    ['/app/productos.xhtml?hoja=editar&id=16', 'productos-hoja', ['celular', 'computadora'], {}],
    ['/app/kardex.xhtml', 'kardex', ['celular', 'tablet', 'computadora'], {}],
    ['/app/kardex.xhtml?hoja=nuevo', 'kardex-hoja', ['celular', 'computadora'], {}],
    ['/app/kardex.xhtml?hoja=nuevo', 'kardex-error', ['celular', 'computadora'], { antes: async page => {
        await page.selectOption('#formulario\\:item', 'PRODUCTO:15');   // Snicker, agotado
        await page.selectOption('#formulario\\:tipo', 'VENTA');
        await page.fill('#formulario\\:cantidad', '2');
        await page.fill('#formulario\\:motivo', 'Venta en vitrina');
        await Promise.all([page.waitForNavigation(), page.click('#formulario input[type=submit]')]);
    } }],
    ['/app/kardex.xhtml?hoja=filtro', 'kardex-filtro', ['celular', 'computadora'], {}],
    ['/app/kardex.xhtml?tipo=VENTA', 'kardex-ventas', ['computadora'], {}],
    ['/app/kardex.xhtml?desde=2020-01-01&hasta=2020-01-31', 'kardex-vacio', ['celular', 'computadora'], {}],
];

async function ingresar(browser) {
    if (!USUARIO || !CLAVE) throw new Error('Defina DPAO_APP_USER y DPAO_APP_PASSWORD en el entorno.');
    const context = await browser.newContext();
    const page = await context.newPage();
    await page.goto(BASE + '/app/acceso.xhtml');
    await page.fill('#acceso\\:usuario', USUARIO);
    await page.fill('#acceso\\:clave', CLAVE);
    await Promise.all([page.waitForURL('**/tablero.xhtml'), page.click('input[type=submit]')]);
    const estado = await context.storageState();
    await context.close();
    return estado;
}

async function nuevaPagina(browser, viewport, sesion, primera) {
    const context = await browser.newContext({ viewport, deviceScaleFactor: 1, storageState: sesion || undefined });
    if (!primera) {
        await context.addInitScript(claves => claves.forEach(k => localStorage.setItem('dpao-orientacion-' + k, '1')), PANTALLAS);
    }
    const page = await context.newPage();
    const errores = [];
    page.on('pageerror', e => errores.push(String(e)));
    page.on('requestfailed', q => errores.push('no cargó ' + q.url()));
    page.on('response', q => { if (q.status() >= 400) errores.push(`${q.status()} ${q.url()}`); });
    return { context, page, errores };
}

async function medirTodo(dir) {
    fs.mkdirSync(dir, { recursive: true });
    const { browser, cerrar } = await abrirEdge();
    const sesion = await ingresar(browser);
    let observaciones = 0;
    for (const [ruta, nombre, fotos, op] of CASOS) {
        for (const [equipo, viewport] of Object.entries(EQUIPOS)) {
            const { context, page, errores } = await nuevaPagina(browser, viewport, op.sinSesion ? null : sesion, op.primera);
            await page.goto(BASE + ruta);
            if (op.antes) await op.antes(page);
            await page.evaluate(() => document.fonts.ready);
            const r = await page.evaluate(medir);
            const lista = problemas(r, await revisarAxe(page), errores);
            if (lista.length) observaciones++;
            console.log(`${lista.length ? 'REV' : 'OK '} ${equipo.padEnd(11)} ${nombre.padEnd(18)} ${lista.join(' · ')}`);
            if (fotos.includes(equipo)) await page.screenshot({ path: path.join(dir, `${nombre}-${viewport.width}.png`) });
            await context.close();
        }
    }
    await cerrar();
    console.log(observaciones ? `Con observaciones: ${observaciones}` : 'Sin observaciones');
}

// Capturas de la entrega: interfaz con Bootstrap, formulario JSF con datos y h:dataTable con datos de MySQL.
async function evidencia(dir) {
    fs.mkdirSync(dir, { recursive: true });
    const { browser, cerrar } = await abrirEdge();
    const sesion = await ingresar(browser);
    const foto = (page, nombre) => page.screenshot({ path: path.join(dir, nombre) });

    let { context, page } = await nuevaPagina(browser, EQUIPOS.computadora, sesion);
    await page.goto(BASE + '/app/tablero.xhtml');
    await foto(page, '02-interfaz-bootstrap-tablero.png');
    await context.close();

    ({ context, page } = await nuevaPagina(browser, EQUIPOS.celular, sesion));
    await page.goto(BASE + '/app/tablero.xhtml');
    await foto(page, '02b-interfaz-bootstrap-celular.png');
    await context.close();

    // Formulario h:form con los datos escritos, y el resultado tras guardarlo (Post/Redirect/Get).
    ({ context, page } = await nuevaPagina(browser, EQUIPOS.computadora, sesion));
    await page.goto(BASE + '/app/insumos.xhtml?hoja=nuevo');
    await page.fill('#formulario\\:codigo', 'INS-010');
    await page.fill('#formulario\\:nombre', 'Esencia de vainilla');
    await page.selectOption('#formulario\\:unidad', 'litro');
    await page.fill('#formulario\\:stock', '1.50');
    await page.fill('#formulario\\:minimo', '0.50');
    await page.fill('#formulario\\:costo', '28.00');
    await foto(page, '03-formulario-jsf-nuevo-insumo.png');
    await Promise.all([page.waitForURL('**/insumos.xhtml'), page.click('#formulario input[type=submit]')]);
    await foto(page, '03b-formulario-jsf-guardado.png');
    await context.close();

    // h:dataTable alimentado desde MySQL (InventarioFacade.listarInsumos).
    ({ context, page } = await nuevaPagina(browser, EQUIPOS.computadora, sesion));
    await page.goto(BASE + '/app/insumos.xhtml');
    await foto(page, '04-datatable-insumos-mysql.png');
    await context.close();

    await cerrar();
}

async function fotografiarPagina(archivo, salida) {
    const { browser, cerrar } = await abrirEdge();
    const context = await browser.newContext({ viewport: { width: 1366, height: 768 }, deviceScaleFactor: 1 });
    const page = await context.newPage();
    await page.goto(/^https?:/.test(archivo) ? archivo : pathToFileURL(path.resolve(archivo)).href, { waitUntil: 'networkidle' });
    if (/^https?:/.test(archivo)) await page.waitForTimeout(4000);   // GitHub completa fechas y avatares después de cargar
    await page.screenshot({ path: salida, fullPage: true });
    await cerrar();
}

const [modo, a, b] = process.argv.slice(2);
const tareas = { medir: () => medirTodo(a), evidencia: () => evidencia(a), pagina: () => fotografiarPagina(a, b) };
if (!tareas[modo]) {
    console.error('Uso: node medir-jsf.js medir|evidencia <carpeta>  ·  node medir-jsf.js pagina <archivo.html|URL> <salida.png>');
    process.exit(2);
}
tareas[modo]().catch(e => { console.error(e); process.exit(1); });
