/* Lienzos D'PAO: muestra un estado de la pantalla según la dirección, sin duplicar el marcado.
   ?estado=vacio|error|exito|hoja|primera|cargando|confirmar   ?paso=1..3   ?pestana=…   ?pagina=1..3
   - [data-ver="a b"]      se ve solo en esos estados ("normal" = sin estado).
   - [data-ocultar="a b"]  se oculta en esos estados.
   - [data-invalido="a"]   el campo se marca inválido en ese estado.
   - [data-paso="n"], [data-pestana="x"]  se ven solo en su paso o pestaña.
   - [data-m|data-t|data-d="n"]  página de la fila en celular, tablet y computadora. */
(function () {
  const q = new URLSearchParams(location.search);
  const cuerpo = document.body;
  const estado = q.get('estado') || 'normal';
  const paso = q.get('paso') || cuerpo.dataset.pasoInicial || '1';
  const pestana = q.get('pestana') || cuerpo.dataset.pestanaInicial || '';
  const pagina = q.get('pagina') || '1';
  cuerpo.dataset.estado = estado;

  const lista = v => (v || '').split(/\s+/).filter(Boolean);
  document.querySelectorAll('[data-ver]').forEach(el => { el.hidden = !lista(el.dataset.ver).includes(estado); });
  document.querySelectorAll('[data-ocultar]').forEach(el => { if (lista(el.dataset.ocultar).includes(estado)) el.hidden = true; });
  document.querySelectorAll('[data-invalido]').forEach(el => {
    if (lista(el.dataset.invalido).includes(estado)) { el.classList.add('is-invalid'); el.setAttribute('aria-invalid', 'true'); }
  });
  document.querySelectorAll('[data-paso]').forEach(el => { if (!lista(el.dataset.paso).includes(paso)) el.hidden = true; });
  document.querySelectorAll('[data-pestana]').forEach(el => { if (!lista(el.dataset.pestana).includes(pestana)) el.hidden = true; });
  // Una pestaña sin acción principal deja su lugar reservado: nada se corre.
  document.querySelectorAll('[data-reservar]').forEach(el => { el.classList.toggle('reservado', lista(el.dataset.reservar).includes(pestana)); });
  document.querySelectorAll('[data-reservar-paso]').forEach(el => { el.classList.toggle('reservado', lista(el.dataset.reservarPaso).includes(paso)); });
  document.querySelectorAll('[data-pestana-enlace]').forEach(el => {
    if (el.dataset.pestanaEnlace === pestana) el.setAttribute('aria-current', 'page'); else el.removeAttribute('aria-current');
  });

  for (const [equipo, clase] of [['m', 'fuera-m'], ['t', 'fuera-t'], ['d', 'fuera-d']]) {
    document.querySelectorAll(`[data-${equipo}]`).forEach(el => { if (el.dataset[equipo] !== pagina) el.classList.add(clase); });
  }
  document.querySelectorAll('.pagina[data-p]').forEach(el => {
    if (el.dataset.p === pagina) el.setAttribute('aria-current', 'page'); else el.removeAttribute('aria-current');
  });
  document.querySelectorAll('[data-rango]').forEach(el => { el.hidden = el.dataset.rango !== pagina; });

  document.querySelectorAll('.pasos li[data-n]').forEach(li => {
    const n = Number(li.dataset.n), actual = Number(paso);
    li.classList.toggle('hecho', n < actual);
    if (n === actual) li.setAttribute('aria-current', 'step'); else li.removeAttribute('aria-current');
  });
})();
