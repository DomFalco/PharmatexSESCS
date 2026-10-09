<%@ page import="Model.Prodotto" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="Model.AcquistoProdotti" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/ParteHTML/navBar.jsp" %>
<html>
<head>
    <title>Riepilogo ordini</title>
    <%
        ArrayList<AcquistoProdotti> riepilogoProdotti = (ArrayList<AcquistoProdotti>) request.getAttribute("riepiloOrdineUtente");
    %>
    <link rel="stylesheet" href="ParteCSS/CategorieProdotti.css">
    <style>
        table,tr,td{
            border: 3px solid green;
            padding: 5px;
            border-collapse: collapse;
            margin-left: auto;
            margin-right: auto;
            margin-top: 10%;
            width: 35%;
        }
    </style>
</head>
<body>
<% if(riepilogoProdotti!=null && riepilogoProdotti.size()>0)
{
    for (int i =0;i<riepilogoProdotti.size();i++) {
        String val = riepilogoProdotti.get(i).getIdProdotto().substring(3);
        int x = Integer.parseInt(val);
        String directory = "immagini/" + riepilogoProdotti.get(i).getIdProdotto() + ".jpg";
        if(x>54)
        {
            directory = "immagini/fotoNonDisponibile.jpg";
        }%>
<div class="box-container">
    <div class="box">
        <div class="image">
            <img src="<%=directory%>">
        </div>
        <div class="info">
            <p>Nome Prodotto: <%=riepilogoProdotti.get(i).getNomeProd()%></p>
            <p>Prezzo: <%=String.format(java.util.Locale.ITALY, "%.2f", riepilogoProdotti.get(i).getPrezzo())%>€</p>
            <p>Quantià ordinata: <%=riepilogoProdotti.get(i).getQuantitaAcquistata()%></p>
            <p>Via:  <%=riepilogoProdotti.get(i).getVia()%></p>
            <p>Città: <%=riepilogoProdotti.get(i).getCitta()%></p>
            <p>CAP: <%=riepilogoProdotti.get(i).getCap()%></p>
            <p>Provincia: <%=riepilogoProdotti.get(i).getProvincia()%></p>
        </div>
    </div>
</div>
<%}
}
else { %>
<h1>Effettua degli ordini per visualizzarli</h1>
<% } %>
</body>
</html>