<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kardex | D'PAO COOKIES</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css?v=20261008">
</head>
<body>
<header><div class="page-shell">
    <p>D'PAO COOKIES</p>
    <h1>Kardex de movimientos</h1>
    <%@ include file="nav.jspf" %>
</div></header>

<main>
    <%@ include file="messages.jspf" %>

    <section class="panel">
        <h2>Registrar movimiento</h2>
        <form method="post" action="${pageContext.request.contextPath}/kardex">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <div class="grid">
                <label>Ítem
                    <select name="item" required>
                        <option value="">Seleccione un ítem</option>
                        <optgroup label="Insumos">
                            <c:forEach var="insumo" items="${insumos}">
                                <option value="INSUMO:${insumo.idInsumo}"><c:out value="${insumo.codigo} · ${insumo.nombre}"/> (stock <fmt:formatNumber value="${insumo.stockActual}" minFractionDigits="2" maxFractionDigits="2"/>&#160;<c:out value="${insumo.unidadMedida}"/>)</option>
                            </c:forEach>
                        </optgroup>
                        <optgroup label="Productos">
                            <c:forEach var="producto" items="${productos}">
                                <option value="PRODUCTO:${producto.idProducto}"><c:out value="${producto.codigo} · ${producto.nombre}"/> (stock <fmt:formatNumber value="${producto.stockActual}" minFractionDigits="2" maxFractionDigits="2"/>)</option>
                            </c:forEach>
                        </optgroup>
                    </select>
                </label>
                <label>Tipo
                    <select name="tipo">
                        <c:forEach var="tipo" items="${tiposMovimiento}">
                            <option value="${tipo}">${tipo.etiqueta}</option>
                        </c:forEach>
                    </select>
                </label>
                <label>Cantidad<input name="cantidad" type="number" min="0" max="99999999.99" step="0.01" required></label>
            </div>
            <p class="muted">En un ajuste por conteo, la cantidad es el stock físico contado. Ninguna salida, venta, merma o ajuste puede dejar existencias negativas.</p>
            <label>Motivo<input name="motivo" maxlength="255" required></label>
            <button>Registrar movimiento</button>
        </form>
    </section>

    <section class="panel">
        <h2>Historial</h2>
        <p>Las cantidades negativas indican salidas; las positivas, entradas. Los saldos muestran las existencias antes y después del movimiento.</p>
        <form method="get" action="${pageContext.request.contextPath}/kardex">
            <div class="grid">
                <label>Tipo
                    <select name="tipo">
                        <option value="">Todos</option>
                        <c:forEach var="tipo" items="${tiposMovimiento}">
                            <option value="${tipo}" ${param.tipo == tipo.name() ? 'selected' : ''}>${tipo.etiqueta}</option>
                        </c:forEach>
                    </select>
                </label>
                <label>Desde<input name="desde" type="date" value="<c:out value='${param.desde}'/>"></label>
                <label>Hasta<input name="hasta" type="date" value="<c:out value='${param.hasta}'/>"></label>
            </div>
            <button>Filtrar</button>
        </form>
        <div class="scroll">
            <table>
                <thead>
                <tr><th>Fecha</th><th>Ítem</th><th>Tipo</th><th class="num">Cantidad</th><th class="num">Anterior</th><th class="num">Posterior</th><th>Motivo</th></tr>
                </thead>
                <tbody>
                <c:forEach var="m" items="${movimientos}">
                    <tr>
                        <td><fmt:formatDate value="${m.fechaRegistro}" pattern="dd/MM/yyyy HH:mm:ss"/></td>
                        <td><c:out value="${m.nombreItem}"/> (<c:out value="${m.codigoItem}"/>) <span class="muted etiqueta">${m.claseItem}</span></td>
                        <td><span class="badge ${m.entrada ? 'badge-success' : 'badge-danger'}">${m.tipoMovimiento}</span></td>
                        <td class="num"><fmt:formatNumber value="${m.cantidad}" pattern="+#,##0.00;-#,##0.00"/></td>
                        <td class="num"><fmt:formatNumber value="${m.stockAnterior}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td class="num"><fmt:formatNumber value="${m.stockPosterior}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td><c:out value="${m.observacion}"/></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <c:if test="${empty movimientos}"><p>Sin movimientos para esta consulta.</p></c:if>
    </section>
</main>
</body>
</html>
