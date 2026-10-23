<%@ page import="java.util.ArrayList" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.math.RoundingMode" %>
<%@ page import="model.Prodotto" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <title>Carrello</title>
    <link rel="stylesheet" href="ParteCSS/Carrello.css">
    <!-- FontAwesome per le icone -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" integrity="sha512-SfTiTlX6kk+qitfevl/7LibUOeJWlt9rbyDn92a1DqWOw9vWG2MFoays0sgObmWazO5BQPiFucnnEAjpAB+/4Q==" crossorigin="anonymous" referrerpolicy="no-referrer" />
</head>
<body>
<%@ include file="/ParteHTML/navBar.jsp" %>

<%
    BigDecimal prezzo = BigDecimal.ZERO;
    ArrayList<Prodotto> carrello = (ArrayList<Prodotto>) session.getAttribute("cart-list");
    ArrayList<Integer> qList = (ArrayList<Integer>) session.getAttribute("quantitaArticoli");
    boolean f = false;
%>

<% if(carrello == null || carrello.isEmpty()) { %>
<h1 style="text-align: center; margin-top: 5%;">Il tuo carrello è vuoto. Effettua degli ordini per visualizzarli.</h1>
<% } else { %>
<div class="box-container1">
    <div class="box1">
        <table>
            <% for (int i = 0; i < carrello.size(); i++) {
                String val = carrello.get(i).getIdProdotto().substring(3);
                int x = Integer.parseInt(val);
                String directory = "immagini/" + carrello.get(i).getIdProdotto() + ".jpg";
                if(x > 54) {
                    directory = "immagini/fotoNonDisponibile.jpg";
                }
                prezzo = prezzo.add(
                        BigDecimal.valueOf(carrello.get(i).getPrezzo())
                                .multiply(BigDecimal.valueOf(qList.get(i)))
                );
            %>
            <tr>
                <td>
                    <b style="text-align: center;">Modello: <c:out value="<%=carrello.get(i).getNomeProd()%>"/></b><br>
                    <div class="image1">
                        <img src="<c:out value="<%=directory%>"/>" alt="Immagine prodotto">
                    </div>
                    <div class="info" style="margin-top: 15px;">
                        <b style="color: red;">Prezzo: <c:out value='<%=String.format(java.util.Locale.ITALY, "%.2f", carrello.get(i).getPrezzo())%>'/> €</b> <br>
                        <b>Quantità articoli: <%=qList.get(i)%></b><br><br>

                        <button type="button" class="cart" onclick="window.location.href='CarrelloServlet?action=rimuovi<%=carrello.get(i).getIdProdotto()%>'">
                            <i class="fa fa-trash-o"></i>
                        </button>

                        <% if(qList.get(i) > carrello.get(i).getQuantita()) { f = true; %>
                        <p style="color: red; font-weight: bold; margin-top: 10px;">Errore: quantità non sufficiente in magazzino</p>
                        <% } %>
                    </div>
                </td>
            </tr>
            <% } %>

            <% if(carrello.size() > 0) { %>
            <tr>
                <th scope="col">
                    <h1>Check-out ordine</h1>
                    <h2>Costo totale: <%=prezzo.setScale(2, RoundingMode.HALF_UP)%>€</h2>
                    <b><p>Le consegne sono previste per un limite dai 7 ai 30 giorni lavorativi</p></b>
                </th>
            </tr>

            <% if(!f) { %>
            <tr>
                <th scope="col">
                    <form method="post" action="PagamentoServlet">
                        <button class="cart" type="submit" style="padding: 15px 30px; font-size: 22px;">
                            <i class="fa fa-credit-card"></i>
                        </button>
                    </form>
                </th>
            </tr>
            <% } %>
            <% } %>
        </table>
    </div>
</div>
<% } %>
</body>
</html>