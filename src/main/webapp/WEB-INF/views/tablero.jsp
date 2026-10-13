<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tablero | D'PAO COOKIES</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css?v=20261008">
</head>
<body>
<header><div class="page-shell">
    <p>D'PAO COOKIES</p>
    <h1>Tablero de operaciones</h1>
    <%@ include file="nav.jspf" %>
</div></header>

<main>
    <%@ include file="messages.jspf" %>

    <c:if test="${not empty resumen}">
        <div class="grid">
            <section class="card">
                <h2>Insumos</h2>
                <p class="metric">${resumen.totalInsumos}</p>
                <a href="${pageContext.request.contextPath}/insumos">Consultar inventario</a>
            </section>
            <section class="card">
                <h2>Reabastecer</h2>
                <p class="metric">${resumen.cantidadAlertas}</p>
                <p>Stock menor o igual al mínimo.</p>
            </section>
            <section class="card">
                <h2>Productos</h2>
                <p class="metric">${resumen.totalProductos}</p>
                <a href="${pageContext.request.contextPath}/productos">Consultar catálogo</a>
            </section>
        </div>

        <section class="panel">
            <h2>Alertas actuales</h2>
            <c:choose>
                <c:when test="${resumen.cantidadAlertas == 0}">
                    <p>No hay insumos por reabastecer.</p>
                </c:when>
                <c:otherwise>
                    <ul class="alertas">
                        <c:forEach var="insumo" items="${resumen.alertas}">
                            <li>
                                <strong><c:out value="${insumo.nombre}"/></strong> (<c:out value="${insumo.codigo}"/>):
                                <fmt:formatNumber value="${insumo.stockActual}" minFractionDigits="2" maxFractionDigits="2"/>&#160;<c:out value="${insumo.unidadMedida}"/> / mínimo
                                <fmt:formatNumber value="${insumo.stockMinimo}" minFractionDigits="2" maxFractionDigits="2"/>&#160;<c:out value="${insumo.unidadMedida}"/>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="panel">
            <div class="panel-heading">
                <h2>Últimos movimientos</h2>
                <a href="${pageContext.request.contextPath}/kardex">Registrar o consultar movimientos</a>
            </div>
            <c:choose>
                <c:when test="${empty resumen.ultimosMovimientos}">
                    <p>Todavía no hay movimientos registrados.</p>
                </c:when>
                <c:otherwise>
                    <ul class="alertas">
                        <c:forEach var="m" items="${resumen.ultimosMovimientos}">
                            <li>
                                <fmt:formatDate value="${m.fechaRegistro}" pattern="dd/MM/yyyy HH:mm"/> ·
                                <span class="badge ${m.entrada ? 'badge-success' : 'badge-danger'}">${m.tipoMovimiento}</span>
                                <c:out value="${m.nombreItem}"/> (<c:out value="${m.codigoItem}"/>):
                                <fmt:formatNumber value="${m.cantidad}" pattern="+#,##0.00;-#,##0.00"/>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </section>
    </c:if>
</main>
</body>
</html>
