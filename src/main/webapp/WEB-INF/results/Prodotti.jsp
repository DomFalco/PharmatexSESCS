<%@ page import="Controller.JspHelper" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/ParteHTML/navBar.jsp" %>
<%@ include file="/ParteHTML/Filter.html" %>
<html>
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/ParteCSS/CategorieProdotti.css">
    <title><c:out value="<%=JspHelper.estraiTitolo(request)%>"/></title>
</head>
<body>
<% for (Model.Prodotto p : JspHelper.estraiProdotti(request)) {
    String val = p.getIdProdotto().substring(3);
    int y = Integer.parseInt(val);
    String directory = "immagini/" + p.getIdProdotto() + ".jpg";
    if(y>54)
    {
        directory = "immagini/fotoNonDisponibile.jpg";
    }
%>

<div class="box-container">
    <div class="box">
        <div class="image">
            <a href="RicercaServlet?search=<c:out value='<%=p.getNomeProd()%>'/>">
                <img src="<c:out value='<%=directory%>'/>" alt="<c:out value='<%=p.getNomeProd()%>'/>">
            </a>
        </div>
        <div class="info">
            <b style="text-align: center;">Modello:<c:out value="<%=p.getNomeProd()%>"/></b><br>
            <b style="text-align: center;color: red"><%=p.getPrezzo()%> €</b>
        </div>
    </div>
</div>
<%}%>
</body>
</html>