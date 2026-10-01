<%@ page import="Model.Prodotto" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/ParteHTML/navBar.jsp" %>
<%@ include file="/ParteHTML/Filter.html" %>
<html>
<head>
    <link rel="stylesheet" href="/ParteCSS/CategorieProdotti.css">
    <%
        String x;
        ArrayList<Prodotto> prod = new ArrayList<Prodotto>();
        if (request.getParameter("action") != null) {
            prod = (ArrayList<Prodotto>) request.getAttribute(request.getParameter("action"));
            x = request.getParameter("action");
        } else {
            prod = (ArrayList<Prodotto>) request.getAttribute("filtra");
            x = (String) request.getAttribute("filtraggio");
        }
    %>
    <title><c:out value="<%=x%>"/></title>
</head>
<body>
<% for (Prodotto p : prod) {
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
            <!-- RISOLTO: Usa c:out per l'attributo href (nota le virgolette singole interne) -->
            <a href="RicercaServlet?search=<c:out value='<%=p.getNomeProd()%>'/>">
                <!-- RISOLTO: Usa c:out per l'attributo src -->
                <img src="<c:out value='<%=directory%>'/>">
            </a>
        </div>
        <div class="info">
            <!-- RISOLTO: Usa c:out per il contenuto testuale -->
            <b style="text-align: center;">Modello:<c:out value="<%=p.getNomeProd()%>"/></b><br>
            <b style="text-align: center;color: red"><%=p.getPrezzo()%> €</b>
        </div>
    </div>
</div>
<%}%>
</body>
</html>