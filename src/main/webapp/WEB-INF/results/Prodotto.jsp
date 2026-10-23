<%@ page import="model.Prodotto" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <link rel="stylesheet" type="text/css" href="ParteCSS/paginaProdotto.css">
    <link rel="stylesheet" type="text/css" href="ParteCSS/styleSito.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <%
        Prodotto p = (Prodotto) request.getAttribute("prodotto");
        String val = p.getIdProdotto().substring(3);
        int y = Integer.parseInt(val);
        String directory = "immagini/" + p.getIdProdotto() + ".jpg";
        if(y > 54) {
            directory = "immagini/fotoNonDisponibile.jpg";
        }
    %>
    <title><%=p.getNomeProd()%></title>
    <style>
        /* Stili specifici per l'immagine e il contenitore di questa pagina */
        .box-container .box .image img {
            width: 500px;
            height: 500px;
            border-style: solid;
            border-width: 2px;
            filter: drop-shadow(0 3px 5px rgba(0,0,0,.7));
        }

        .box-container {
            margin-top: 3%;
            margin-left: 40%;
            margin-right: 50%;
            float: left;
            border: 0px;
        }
    </style>
</head>
<body>

<div class="box-container">
    <div class="box">
        <div class="image">
            <img src="<%=directory%>" alt="Immagine prodotto">
        </div>
        <div class="info">
            <p><c:out value="<%= p.getDescrizione() %>"/></p>
            <p>Lunghezza: <%= p.getLunghezza() %> cm Larghezza: <%= p.getLarghezza() %> cm</p>
            <p>Quantità disponibile: <%=p.getQuantita()%></p>
            <%
                String x = p.getIdProdotto();
                if (x.charAt(0) == 'M') {%>
            <p>Materiale: <c:out value="<%=p.getTipologiaMaterasso()%>"/></p>
            <%} else if (x.charAt(0) == 'L') {%>
            <p>Colore: <c:out value="<%=p.getColoreLetto()%>"/></p>
            <%} else if (x.charAt(0) == 'R') {%>
            <p>Materiale: <c:out value="<%=p.getMaterialeRete()%>"/></p>
            <%} else if (x.charAt(0) == 'D' || x.charAt(0) == 'P') {%>
            <p>Rivestimento: <c:out value="<%=p.getRivestimentoDivano()%>"/></p>
            <p>Colore: <c:out value="<%=p.getColoreDivano()%>"/></p>
            <%} else {%>
            <p>Tipo stoffa: <c:out value="<%=p.getTipoStoffaCuscino()%>"/></p>
            <p>Forma: <c:out value="<%=p.getFormaCuscino()%>"/></p>
            <p>Materiale: <c:out value="<%=p.getMaterialeCuscino()%>"/></p>
            <%}%>
            <b><p>Le consegne sono previste per un limite dai 7 ai 30 giorni lavorativi</p></b>
        </div>
    </div>

    <table>
        <tr>
            <th>
                <h1>Modello: <c:out value="<%=p.getNomeProd()%>"/></h1>
                <h2><c:out value='<%=String.format(java.util.Locale.ITALY, "%.2f", p.getPrezzo())%>'/>€</h2>
            </th>
        </tr>

        <% if (p.getQuantita() == 0) { %>
        <tr>
            <th>
                <p style="color: red; font-weight: bold;">Prodotto non disponibile</p>
            </th>
        </tr>
        <% } else { %>
        <tr>
            <th>
                <!-- Form allineato con CSS (form-acquisto) -->
                <form action="CarrelloServlet" class="form-acquisto">
                    <label for="quantita">Seleziona quantità:</label>
                    <select name="quantita" id="quantita">
                        <% for(int i=1; i<=p.getQuantita(); i++) { %>
                        <option value="<%=i%>"><%=i%></option>
                        <% } %>
                    </select>
                    <button class="cart" type="submit" aria-label="Aggiungi al carrello">
                        <i class="fa fa-shopping-cart"></i>
                    </button>
                </form>
            </th>
        </tr>
        <% } %>

        <tr>
            <td>
                <button class="cart" onclick="window.location.href='HomePage'" aria-label="Torna alla Home">
                    <i class="fa fa-home"></i>
                </button>
            </td>
        </tr>
    </table>
</div>
</body>
</html>