<%@ page import="Model.Prodotto" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link rel="stylesheet" type="text/css" href="ParteCSS/paginaProdotto.css">
    <link rel="stylesheet" type="text/css" href="ParteCSS/styleSito.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <% Prodotto p = (Prodotto) request.getAttribute("prodotto");
        String val = p.getIdProdotto().substring(3);
        int y = Integer.parseInt(val);
        String directory = "immagini/" + p.getIdProdotto() + ".jpg";
        if(y>54)
        {
            directory = "immagini/fotoNonDisponibile.jpg";
        }
    %>
    <title><%=p.getNomeProd()%></title>
    <style>
        .box-container .box .image img{
            width: 500px;height: 500px;
            border-style: solid;
            border-width: 2px;
            filter:drop-shadow(0 3px 5px rgba(0,0,0,.7));
        }

        .box-container{
            border: none;
            margin-top: 3%;
            margin-left: 40%;
            margin-right: 50%;
            float: left;
            border: 0px;
        }
    </style>
</head>
<body>

<div class="box-container" >
    <div class="box">
        <div class="image">
            <img src="<%=directory%>">
        </div>
        <div class="info">
            <p><%= p.getDescrizione() %>
            </p>
            <p>Lunghezza:<%= p.getLunghezza() %> cm Larghezza: <%= p.getLarghezza() %> cm </p>
            <p>Quantità disponibile:<%=p.getQuantita()%>
            </p>
            <%
                String x = p.getIdProdotto();
                if (x.charAt(0) == 'M') {%>
            <p>Materiale:<%=p.getTipologiaMaterasso()%>
            </p>
            <%} else if (x.charAt(0) == 'L') {%>
            <p>Colore:<%=p.getColoreLetto()%>
            </p>
            <%} else if (x.charAt(0) == 'R') {%>
            <p>Materiale:<%=p.getMaterialeRete()%>
            </p>
            <%} else if (x.charAt(0) == 'D' || x.charAt(0) == 'P') {%>
            <p>Rivestimento:<%=p.getRivestimentoDivano()%>
            </p>
            <p>Colore:<%=p.getColoreDivano()%>
            </p>
            <%} else {%>
            <p>Tipo stoffa:<%=p.getTipoStoffaCuscino()%>
            </p>
            <p>Forma:<%=p.getFormaCuscino()%>
            </p>
            <p>Materiale:<%=p.getMaterialeCuscino()%>
            </p>
            <%}%>
            <b><p>Le consegne sono previste per un limite dai 7 ai 30 giorni lavorativi</p></b>
        </div>
    </div>
    <table>
        <tr>
            <th>
                <h1>Modello:<%=p.getNomeProd()%>
                </h1>
                <h2><%=String.format(java.util.Locale.ITALY, "%.2f", p.getPrezzo())%>€</h2>
            </th>
        </tr>
        <% if (p.getQuantita() == 0) {%>
        <th>
            <p style="color: red">Prodotto non disponibile</p>
        </th>
        <%} else {%>
        <th>
            <form action="CarrelloServlet">
                <label>Seleziona quantità:</label>
                <select name="quantita" id="quantita">
                    <% for(int i=1;i<=p.getQuantita();i++)
                    {
                        if(p.getQuantita()!=0)
                        {%>
                    <option value="<%=i%>"><%=i%></option>
                    <%}%>
                    <%}%>
                </select>
                <button class="cart" type="submit"><i class="fa fa-shopping-cart"></i></button>
            </form>
        </th>
        <%}%>
        <tr>
            <td>
                <button class="cart"><a style="text-decoration: none; color: white; text-underline: none" href="HomePage"><i class="fa fa-home"></i></a></button>
            </td>
        </tr>
    </table>
</div>
</body>
</html>