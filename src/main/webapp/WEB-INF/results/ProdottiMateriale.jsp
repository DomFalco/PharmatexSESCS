<%@ page import="Model.Prodotto" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/ParteHTML/navBar.jsp" %>
<%@ include file="/ParteHTML/Filter.html" %>
<html>
<head>
    <%
        ArrayList<Prodotto> prod = (ArrayList<Prodotto>) request.getAttribute("prodottiMateriale");
    %>
    <%if(prod.get(0).getIdProdotto().charAt(0)=='M')
    { %>
    <title>Materassi</title>
    <% }
    else if(prod.get(0).getIdProdotto().charAt(0)=='R'){ %>
    <title>Reti</title>
    <% }
    else { %>
    <title>Cuscini</title>
    <% }%>
    <style>
        @media screen and (max-width: 1100px) {
            .topnav a:not(:first-child), .dropdown .dropbtn {
                display: none;
            }
            .topnav a.icon {
                float: right;
                display: block;
            }
            .topnav .search-container{
                float: left;
            }
        }

        @media screen and (max-width: 1100px) {
            .topnav.responsive {position: relative;}
            .topnav.responsive .icon {
                position: absolute;
                right: 0;
                top: 0;
            }
            .topnav.responsive a {
                float: none;
                display: block;
                text-align: left;
            }
            .topnav.responsive .dropdown {float: none;}
            .topnav.responsive .dropdown-content {position: relative;}
            .topnav.responsive .dropdown .dropbtn {
                display: block;
                width: 100%;
                text-align: left;
            }
        }
    </style>
</head>
<body>
<% for (Prodotto p : prod) {
    String val = p.getIdProdotto().substring(3);
    int x = Integer.parseInt(val);
    String directory = "immagini/" + p.getIdProdotto() + ".jpg";
    if(x>54)
    {
        directory = "immagini/fotoNonDisponibile.jpg";
    }
%>
<div class="box-container">
    <div class="box">
        <div class="image">
            <a href="RicercaServlet?search=<%=p.getNomeProd()%>">
                <img src="<%=directory%>">
            </a>
        </div>
        <div class="info">
            <b style="text-align: center;">Modello:<%=p.getNomeProd()%>
            </b><br>
            <b style="text-align: center;color: red"><%=String.format(java.util.Locale.ITALY, "%.2f", p.getPrezzo())%> €</b>
        </div>
    </div>
</div>
<%}%>
</body>
</html>