/* D'PAO COOKIES — comportamiento de las vistas JSF (sin dependencias).
   - [data-paginar="m t d"]: reparte filas o mosaicos en páginas según el equipo
     (celular < 768 px, tablet < 992 px, computadora); 0 = sin páginas.
   - .orientacion[data-orientacion]: primera visita, se muestra una sola vez por pantalla.
   - Hoja abierta: foco en el primer campo con error (o el primero) y Escape para cerrar.
   - Aviso flotante: se retira solo a los 8 segundos. */
(function () {
  'use strict';

  const celular = matchMedia('(max-width: 767.98px)');
  const tablet = matchMedia('(min-width: 768px) and (max-width: 991.98px)');

  function guardar(clave, valor) { try { localStorage.setItem(clave, valor); } catch (e) { /* sin almacenamiento */ } }
  function leer(clave) { try { return localStorage.getItem(clave); } catch (e) { return null; } }

  // ---------------------------------------------------------------- Páginas
  document.querySelectorAll('[data-paginar]').forEach(function (contenedor) {
    const tamanos = contenedor.dataset.paginar.split(/\s+/).map(Number);
    // Una tabla (o un envoltorio con una tabla, como el de h:dataTable) pagina sus filas; lo demás, sus hijos.
    const tabla = contenedor.tagName === 'TABLE' ? contenedor
      : (contenedor.children.length === 1 && contenedor.firstElementChild.tagName === 'TABLE' ? contenedor.firstElementChild : null);
    const filas = tabla
      ? Array.from(tabla.tBodies).flatMap(function (tb) { return Array.from(tb.rows); })
      : Array.from(contenedor.children);
    const elementos = filas.filter(function (f) { return !f.classList.contains('fila-dia'); });
    const dias = filas.filter(function (f) { return f.classList.contains('fila-dia'); });
    const nav = document.createElement('nav');
    nav.className = 'paginas';
    nav.setAttribute('aria-label', 'Páginas');
    contenedor.insertAdjacentElement('afterend', nav);
    let pagina = 1;

    function tamano() { return celular.matches ? tamanos[0] : tablet.matches ? tamanos[1] : tamanos[2]; }

    function pintar() {
      const n = tamano() || 0;
      const total = elementos.length;
      const paginas = n > 0 ? Math.max(1, Math.ceil(total / n)) : 1;
      if (pagina > paginas) pagina = paginas;
      elementos.forEach(function (el, i) { el.hidden = n > 0 && (i < (pagina - 1) * n || i >= pagina * n); });
      // Un día se ve si alguna de sus filas está en la página.
      dias.forEach(function (dia) {
        let siguiente = dia.nextElementSibling, alguna = false;
        while (siguiente && !siguiente.classList.contains('fila-dia')) { if (!siguiente.hidden) alguna = true; siguiente = siguiente.nextElementSibling; }
        dia.hidden = !alguna;
      });
      nav.hidden = paginas <= 1;
      nav.textContent = '';
      if (paginas <= 1) return;
      const rango = document.createElement('span');
      rango.className = 'rango t-2';
      const desde = (pagina - 1) * n + 1, hasta = Math.min(pagina * n, total);
      rango.textContent = (desde === hasta ? desde : desde + '–' + hasta) + ' de ' + total;
      nav.appendChild(rango);
      // Hasta cuatro páginas, un botón por página; con más, anterior y siguiente (no se parte la fila).
      if (paginas <= 4) {
        for (let p = 1; p <= paginas; p++) {
          const boton = botonPagina(String(p), 'Página ' + p, function () { pagina = p; });
          if (p === pagina) boton.setAttribute('aria-current', 'page');
          nav.appendChild(boton);
        }
      } else {
        nav.appendChild(botonPagina(null, 'Página anterior', function () { pagina--; }, 'ico-chevron-left', pagina === 1));
        nav.appendChild(botonPagina(null, 'Página siguiente', function () { pagina++; }, 'ico-chevron-right', pagina === paginas));
      }
    }

    function botonPagina(texto, etiqueta, mover, icono, desactivado) {
      const boton = document.createElement('button');
      boton.type = 'button';
      boton.className = 'pagina';
      boton.setAttribute('aria-label', etiqueta);
      if (texto) boton.textContent = texto;
      if (icono) {
        const ico = document.createElement('span');
        ico.className = 'ico ' + icono;
        ico.setAttribute('aria-hidden', 'true');
        boton.appendChild(ico);
      }
      boton.disabled = !!desactivado;
      boton.addEventListener('click', function () {
        mover();
        pintar();
        const foco = nav.querySelector('[aria-current]') || nav.querySelector('[aria-label="' + etiqueta + '"]');
        if (foco && !foco.disabled) foco.focus();
      });
      return boton;
    }
    pintar();
    celular.addEventListener('change', pintar);
    tablet.addEventListener('change', pintar);
  });

  // ------------------------------------------------------ Primera visita
  document.querySelectorAll('.orientacion[data-orientacion]').forEach(function (velo) {
    const clave = 'dpao-orientacion-' + velo.dataset.orientacion;
    if (leer(clave)) return;
    const pasos = Array.from(velo.querySelectorAll('.paso'));
    const puntos = Array.from(velo.querySelectorAll('.puntos-paso span'));
    const contador = velo.querySelector('.contador-paso');
    const boton = velo.querySelector('.siguiente');
    let actual = 0;
    function mostrar() {
      pasos.forEach(function (p, i) { p.hidden = i !== actual; });
      puntos.forEach(function (s, i) { if (i === actual) s.setAttribute('aria-current', 'step'); else s.removeAttribute('aria-current'); });
      if (contador) contador.textContent = 'Paso ' + (actual + 1) + ' de ' + pasos.length;
      boton.textContent = actual === pasos.length - 1 ? 'Entendido' : 'Siguiente';
    }
    function cerrar() { guardar(clave, '1'); velo.hidden = true; }
    boton.addEventListener('click', function () { if (actual < pasos.length - 1) { actual++; mostrar(); } else { cerrar(); } });
    velo.addEventListener('keydown', function (e) { if (e.key === 'Escape') cerrar(); });
    mostrar();
    velo.hidden = false;
    boton.focus();
  });

  // ---------------------------------------------------------------- Hoja
  const hoja = document.querySelector('.velo-hoja');
  if (hoja && !hoja.hidden) {
    const campo = hoja.querySelector('[aria-invalid="true"]') || hoja.querySelector('input:not([type="hidden"]):not([type="submit"]), select');
    if (campo) campo.focus();
    hoja.addEventListener('keydown', function (e) {
      if (e.key !== 'Escape') return;
      const cerrar = hoja.querySelector('.hoja-cerrar');
      if (cerrar) cerrar.click();
    });
  }

  // ------------------------------------------------------- Aviso flotante
  document.querySelectorAll('.aviso-flotante').forEach(function (aviso) {
    setTimeout(function () { aviso.hidden = true; }, 8000);
  });
})();
