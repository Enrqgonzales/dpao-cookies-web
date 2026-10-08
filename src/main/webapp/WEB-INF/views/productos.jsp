<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Productos | D'PAO COOKIES</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css?v=20261008">
</head>
<body>
<header><div class="page-shell">
    <p>D'PAO COOKIES</p>
    <h1>Productos terminados</h1>
    <%@ include file="nav.jspf" %>
</div></header>

<main>
    <%@ include file="messages.jspf" %>

    <section class="panel">
        <h2>Registrar producto</h2>
        <p>El stock inicial es cero. Registra la producción como ENTRADA en el Kardex.</p>
        <form method="post" action="${pageContext.request.contextPath}/productos">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="id" value="0">
            <div class="grid">
                <label>Código<input name="codigo" required maxlength="20"></label>
                <label>Nombre<input name="nombre" required maxlength="100"></label>
                <label>Precio (S/)<input name="precio" type="number" min="0" max="99999999.99" step="0.01" required></label>
                <label>Categoría
                    <select name="categoria" required>
                        <c:forEach var="categoria" items="${categorias}">
                            <option value="${categoria.idCategoria}"><c:out value="${categoria.nombre}"/></option>
                        </c:forEach>
                    </select>
                </label>
            </div>
            <button>Registrar producto</button>
        </form>
    </section>

    <section class="panel">
        <h2>Catálogo</h2>
        <div class="scroll">
            <table>
                <thead>
                <tr><th>ID</th><th>Código</th><th>Nombre</th><th>Categoría</th><th class="num">Precio (S/)</th><th class="num">Stock</th><th>Editar</th></tr>
                </thead>
                <tbody>
                <c:forEach var="producto" items="${productos}">
                    <tr>
                        <td>${producto.idProducto}</td>
                        <td class="code"><c:out value="${producto.codigo}"/></td>
                        <td><c:out value="${producto.nombre}"/></td>
                        <td><c:out value="${producto.nombreCategoria}"/></td>
                        <td class="num"><fmt:formatNumber value="${producto.precioVenta}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td class="num"><fmt:formatNumber value="${producto.stockActual}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td>
                            <details>
                                <summary>Editar producto</summary>
                                <form method="post" action="${pageContext.request.contextPath}/productos">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="id" value="${producto.idProducto}">
                                    <label>Código<input name="codigo" value="<c:out value='${producto.codigo}'/>" maxlength="20" required></label>
                                    <label>Nombre<input name="nombre" value="<c:out value='${producto.nombre}'/>" maxlength="100" required></label>
                                    <label>Precio (S/)<input name="precio" type="number" min="0" max="99999999.99" step="0.01" value="${producto.precioVenta}" required></label>
                                    <label>Categoría
                                        <select name="categoria">
                                            <c:forEach var="categoria" items="${categorias}">
                                                <option value="${categoria.idCategoria}" ${categoria.idCategoria == producto.idCategoria ? 'selected' : ''}><c:out value="${categoria.nombre}"/></option>
                                            </c:forEach>
                                        </select>
                                    </label>
                                    <button>Guardar producto</button>
                                </form>
                            </details>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <c:if test="${empty productos}"><p>No hay productos registrados.</p></c:if>
    </section>
</main>
</body>
</html>
