<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Inventario | D'PAO COOKIES</title>
    <style>
        :root {
            --brown-900: #3d2418;
            --brown-700: #71452d;
            --cream: #fffaf4;
            --orange: #d97732;
            --green: #177245;
            --red: #b42318;
            --border: #eadfd4;
            --shadow: 0 18px 45px rgba(71, 39, 20, .10);
        }
        * { box-sizing: border-box; }
        body {
            margin: 0;
            background: var(--cream);
            color: var(--brown-900);
            font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
        }
        .hero {
            background: linear-gradient(120deg, #452819, #9a5c34);
            color: white;
            padding: 2.25rem max(1.25rem, calc((100% - 1180px) / 2));
        }
        .eyebrow { margin: 0 0 .5rem; color: #ffd6b0; font-size: .76rem; font-weight: 800; letter-spacing: .16em; text-transform: uppercase; }
        h1 { margin: 0; font-size: clamp(1.8rem, 4vw, 2.75rem); letter-spacing: -.04em; }
        .hero p:last-child { margin: .7rem 0 0; color: #f8e6d8; }
        main { width: min(1180px, calc(100% - 2.5rem)); margin: 2rem auto 4rem; }
        .panel { background: white; border: 1px solid var(--border); border-radius: 18px; box-shadow: var(--shadow); padding: 1.5rem; margin-bottom: 1.5rem; }
        .panel-heading { display: flex; justify-content: space-between; align-items: baseline; gap: 1rem; margin-bottom: 1.25rem; }
        h2 { margin: 0; font-size: 1.2rem; }
        .muted { color: #816f63; font-size: .9rem; }
        .form-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 1rem; }
        .field { display: flex; flex-direction: column; gap: .42rem; }
        .field label { color: var(--brown-700); font-size: .82rem; font-weight: 700; }
        input { width: 100%; border: 1px solid #d9c9bc; border-radius: 9px; padding: .7rem .75rem; color: var(--brown-900); background: #fffdfb; font: inherit; }
        input:focus { outline: 3px solid rgba(217,119,50,.18); border-color: var(--orange); }
        .form-actions { display: flex; justify-content: flex-end; margin-top: 1.25rem; }
        .button { border: 0; border-radius: 9px; padding: .72rem 1.15rem; background: var(--orange); color: white; cursor: pointer; font-weight: 800; }
        .button:hover { background: #b85c22; }
        .alert { border-radius: 10px; padding: .85rem 1rem; margin-bottom: 1.5rem; font-weight: 600; }
        .alert-success { background: #e8f7ee; color: var(--green); border: 1px solid #b8e4c8; }
        .alert-error { background: #fff0ee; color: var(--red); border: 1px solid #f4c3bd; }
        .table-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; min-width: 820px; }
        th { padding: .85rem .75rem; border-bottom: 2px solid var(--border); color: #8a7567; font-size: .72rem; letter-spacing: .08em; text-align: left; text-transform: uppercase; }
        td { padding: 1rem .75rem; border-bottom: 1px solid #f0e8e2; vertical-align: middle; }
        tbody tr:hover { background: #fffaf6; }
        .code { color: var(--orange); font-weight: 800; }
        .insumo-name { font-weight: 750; }
        .badge { display: inline-flex; align-items: center; border-radius: 999px; padding: .38rem .65rem; font-size: .72rem; font-weight: 850; white-space: nowrap; }
        .badge-danger { background: #ffebe8; color: var(--red); }
        .badge-success { background: #e6f6ec; color: var(--green); }
        .stock-form { display: flex; gap: .45rem; align-items: center; }
        .stock-form input { width: 105px; padding: .5rem; }
        .stock-form .button { padding: .5rem .65rem; font-size: .78rem; }
        .empty { padding: 2.5rem 1rem; text-align: center; color: #816f63; }
        @media (max-width: 850px) { .form-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
        @media (max-width: 520px) { main { width: min(100% - 1.25rem, 1180px); margin-top: 1rem; } .panel { padding: 1rem; } .form-grid { grid-template-columns: 1fr; } .panel-heading { display: block; } }
    </style>
</head>
<body>
<header class="hero">
    <p class="eyebrow">Gestión operativa</p>
    <h1>D'PAO COOKIES - Control de Inventario</h1>
    <p>Consulta y actualiza las existencias de tus insumos en un solo lugar.</p>
</header>

<main>
    <c:if test="${not empty mensaje}">
        <div class="alert alert-success" role="status"><c:out value="${mensaje}" /></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error" role="alert"><c:out value="${error}" /></div>
    </c:if>

    <section class="panel">
        <div class="panel-heading">
            <h2>Registrar nuevo insumo</h2>
            <span class="muted">Completa todos los campos requeridos</span>
        </div>
        <form action="${pageContext.request.contextPath}/insumos" method="post">
            <input type="hidden" name="accion" value="registrar">
            <div class="form-grid">
                <div class="field"><label for="codigo">Código</label><input id="codigo" name="codigo" maxlength="20" required></div>
                <div class="field"><label for="nombre">Nombre del insumo</label><input id="nombre" name="nombre" maxlength="100" required></div>
                <div class="field"><label for="unidadMedida">Unidad de medida</label><input id="unidadMedida" name="unidadMedida" maxlength="20" placeholder="kg, unidad, litro" required></div>
                <div class="field"><label for="idCategoria">ID de categoría</label><input id="idCategoria" name="idCategoria" type="number" min="1" required></div>
                <div class="field"><label for="stockActual">Stock actual</label><input id="stockActual" name="stockActual" type="number" min="0" step="0.01" required></div>
                <div class="field"><label for="stockMinimo">Stock mínimo</label><input id="stockMinimo" name="stockMinimo" type="number" min="0" step="0.01" required></div>
                <div class="field"><label for="costoUnitario">Costo unitario (S/)</label><input id="costoUnitario" name="costoUnitario" type="number" min="0" step="0.01" required></div>
            </div>
            <div class="form-actions"><button class="button" type="submit">+ Registrar insumo</button></div>
        </form>
    </section>

    <section class="panel">
        <div class="panel-heading">
            <h2>Inventario actual</h2>
            <span class="muted">${empty listaInsumos ? 0 : listaInsumos.size()} insumos registrados</span>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                <tr><th>Código</th><th>Insumo</th><th>Unidad</th><th>Stock actual</th><th>Stock mínimo</th><th>Costo unitario</th><th>Estado</th><th>Actualizar</th></tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty listaInsumos}">
                        <c:forEach var="insumo" items="${listaInsumos}">
                            <tr>
                                <td class="code"><c:out value="${insumo.codigo}" /></td>
                                <td class="insumo-name"><c:out value="${insumo.nombre}" /></td>
                                <td><c:out value="${insumo.unidadMedida}" /></td>
                                <td><fmt:formatNumber value="${insumo.stockActual}" minFractionDigits="2" maxFractionDigits="2" /></td>
                                <td><fmt:formatNumber value="${insumo.stockMinimo}" minFractionDigits="2" maxFractionDigits="2" /></td>
                                <td>S/ <fmt:formatNumber value="${insumo.costoUnitario}" minFractionDigits="2" maxFractionDigits="2" /></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${insumo.bajoStock}"><span class="badge badge-danger">[REABASTECER]</span></c:when>
                                        <c:otherwise><span class="badge badge-success">[ÓPTIMO]</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <form class="stock-form" action="${pageContext.request.contextPath}/insumos" method="post">
                                        <input type="hidden" name="accion" value="actualizarStock">
                                        <input type="hidden" name="idInsumo" value="${insumo.idInsumo}">
                                        <input aria-label="Nuevo stock" name="nuevoStock" type="number" min="0" step="0.01" value="${insumo.stockActual}" required>
                                        <button class="button" type="submit">Guardar</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise><tr><td class="empty" colspan="8">No hay insumos registrados todavía.</td></tr></c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>
