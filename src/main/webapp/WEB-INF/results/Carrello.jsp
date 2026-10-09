<%@ page import="java.util.ArrayList" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.math.RoundingMode" %>
<%@ page import="Model.Prodotto" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/ParteHTML/navBar.jsp" %>
<html>
<head>
    <title>Carrello</title>
    <%
        BigDecimal prezzo = BigDecimal.ZERO;
        ArrayList<Prodotto> carrello = (ArrayList<Prodotto>) session.getAttribute("cart-list");
        ArrayList<Integer> qList = (ArrayList<Integer>) session.getAttribute("quantitaArticoli");
        boolean f = false;
    %>
    <link rel="stylesheet" href="ParteCSS/Carrello.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
</head>
<body>
<% if(carrello==null || carrello.size() == 0)
{%>
<h1>Il tuo carrello è vuoto. Effettua degli ordini per visualizzarli</h1>
<%}
else if(carrello!=null || carrello.size()==0)
{%>
<div class="box-container1" style="border: none; margin-top: 3%;border: 0px;">
    <div class="box1">
        <table>
            <% for (int i=0;i<carrello.size();i++) {
                String val = carrello.get(i).getIdProdotto().substring(3);
                int x = Integer.parseInt(val);
                String directory = "immagini/" + carrello.get(i).getIdProdotto() + ".jpg";
                if(x>54)
                {
                    directory = "immagini/fotoNonDisponibile.jpg";
                }
                prezzo = prezzo.add(
                        BigDecimal.valueOf(carrello.get(i).getPrezzo())
                                .multiply(BigDecimal.valueOf(qList.get(i)))
                );
            %>
            <tr>
                <td>
                    <b style="text-align: center;">Modello:<%=carrello.get(i).getNomeProd()%></b><br>
                    <div class="image1">
                        <img src="<%=directory%>" style="width: 250px; height: 250px; filter:drop-shadow(0 3px 5px rgba(0,0,0,.7));">
                    </div>
                    <div class="info">
                        <b style="text-align: center;color: red;"><%=String.format(java.util.Locale.ITALY, "%.2f", carrello.get(i).getPrezzo())%> €</b> <br>
                        <b style="text-align: center;">Quantità articoli:<%=qList.get(i)%></b>
                        <button class="cart"><a style="text-decoration: none; color: white; text-underline: none" href="CarrelloServlet?action=rimuovi<%=carrello.get(i).getIdProdotto()%>"><i class="fa fa-trash-o"></i></a></button>
                        <% if(qList.get(i)>carrello.get(i).getQuantita())
                        { f=true;%>
                        <p style="color: red">Errore quantità non sufficiente in magazzino</p>
                        <%}%>
                    </div>
                </td>
            </tr>
            <%}%>
            <% if(carrello!=null && carrello.size()>0)
            {%>
            <tr>
                <th>
                    <h1>Check-out ordine</h1>
                    <h2>Costo totale: <%=prezzo.setScale(2, RoundingMode.HALF_UP)%>€</h2>
                    <b><p>Le consegne sono previste per un limite dai 7 ai 30 giorni lavorativi</p></b>
                </th>
            </tr>
            <%if(!f)
            {%>
            <tr>
                <th>
                    <form method="post" action="PagamentoServlet">
                        <button class="cart" type="submit">
                            <i class="fa fa-credit-card"></i></button>
                    </form>
                </th>
            </tr>
            <%}%>
            <%}%>
        </table>
    </div>
</div>
<%}%>
</body>
</html>