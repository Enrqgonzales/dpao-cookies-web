<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Acceso | D'PAO COOKIES</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css?v=20261008"></head>
<body><header><div class="page-shell"><p>Gestión operativa</p><h1>D'PAO COOKIES</h1></div></header><main class="login"><section class="panel"><h2>Acceso al sistema</h2><p>Ingresa con la cuenta configurada por el administrador.</p>
<c:if test="${not empty sessionScope.error}"><p class="error" role="alert"><c:out value="${sessionScope.error}"/></p><c:remove var="error" scope="session"/></c:if>
<form action="${pageContext.request.contextPath}/acceso" method="post"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><label>Usuario<input name="usuario" required autocomplete="username" maxlength="100"></label><label>Contraseña<input name="clave" type="password" required autocomplete="current-password"></label><button>Ingresar</button></form></section></main></body></html>
