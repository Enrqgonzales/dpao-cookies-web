<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Inventario | D'PAO COOKIES</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css?v=20260909">
</head>
<body>
<header class="hero"><div class="page-shell">
    <p class="eyebrow">D'PAO COOKIES</p>
    <h1>Inventario de insumos</h1>
    <p>Consulta y actualiza las existencias de tus insumos en un solo lugar.</p>
    <%@ include file="nav.jspf" %>
</div></header>

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
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="registrar">
            <div class="form-grid">
                <div class="field"><label for="codigo">Código</label><input id="codigo" name="codigo" maxlength="20" required></div>
                <div class="field"><label for="nombre">Nombre del insumo</label><input id="nombre" name="nombre" maxlength="100" required></div>
                <div class="field"><label for="unidadMedida">Unidad de medida</label><input id="unidadMedida" name="unidadMedida" maxlength="20" placeholder="kg, unidad, litro" required></div>
                <div class="field"><label for="idCategoria">ID de categoría</label><input id="idCategoria" name="idCategoria" type="number" min="1" required></div>
                <div class="field"><label for="stockActual">Stock actual</label><input id="stockActual" name="stockActual" type="number" min="0" max="99999999.99" step="0.01" required></div>
                <div class="field"><label for="stockMinimo">Stock mínimo</label><input id="stockMinimo" name="stockMinimo" type="number" min="0" max="99999999.99" step="0.01" required></div>
                <div class="field"><label for="costoUnitario">Costo unitario (S/)</label><input id="costoUnitario" name="costoUnitario" type="number" min="0" max="99999999.99" step="0.01" required></div>
            </div>
            <div class="form-actions"><button class="button" type="submit">Registrar insumo</button></div>
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
                                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                        <input type="hidden" name="accion" value="actualizarStock">
                                        <input type="hidden" name="idInsumo" value="${insumo.idInsumo}">
                                        <input aria-label="Nuevo stock" name="nuevoStock" type="number" min="0" max="99999999.99" step="0.01" value="${insumo.stockActual}" required>
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
