<%@ page import="model.Prodotto" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="model.Utente" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="it"> <!-- FIX 1: Aggiunto lang="it" -->
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- FIX 2: Aggiunti integrity e crossorigin per la sicurezza (Snyk/Sonar) -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" integrity="sha512-SfTiTlX6kk+qitfevl/7LibUOeJWlt9rbyDn92a1DqWOw9vWG2MFoays0sgObmWazO5BQPiFucnnEAjpAB+/4Q==" crossorigin="anonymous" referrerpolicy="no-referrer" />

    <link rel="stylesheet" href="ParteCSS/CategorieProdotti.css">
    <link rel="stylesheet" href="ParteCSS/styleSito.css">
    <title>Pharmatex</title>
    <%
        List<Prodotto> prod = (ArrayList<Prodotto>) request.getAttribute("prodotti");
        int n = 0;
        if (request.getAttribute("Valore") != null) {
            n = (int) request.getAttribute("Valore");
        }
    %>
    <script>
        function myFunction() {
            var x = document.getElementById("myTopnav");
            if (x.className === "topnav") {
                x.className += " responsive";
            } else {
                x.className = "topnav";
            }
        }
    </script>
    <style>
        @media screen and (max-width: 1482px) {
            /* FIX 3: Modificato il selettore per il pulsante hamburger */
            .topnav a:not(:first-child), .dropdown .dropbtn {
                display: none;
            }
            .topnav button.icon {
                float: right;
                display: block;
            }
            .topnav .search-container{
                float: right;
                margin-top: 8px;
                margin-right: 10px;
            }
            *{
                zoom: normal;
            }
        }

        @media screen and (max-width: 1482px) {
            .topnav.responsive {position: relative;}
            .topnav.responsive button.icon { /* FIX 3 */
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
<img src="immagini/logosito.png" class="sfondo" alt="Logo Pharmatex">
<div class="topnav" id="myTopnav">
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=Letto'">Letto
            <i class="fa fa-caret-down"></i>
        </button>
        <div class="dropdown-content">
            <a href="InizioServlet?action=LettoSingolo">Letto singolo</a>
            <a href="InizioServlet?action=LettoMatrimoniale">Letto matrimoniale</a>
            <a href="InizioServlet?action=LettoPiazzaEMezzo">Letto piazza e mezzo</a>
            <a href="InizioServlet?action=LettoContenitore">Letto contenitore</a>
        </div>
    </div>

    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=Materasso'">Materasso
            <i class="fa fa-caret-down"></i>
        </button>
        <div class="dropdown-content">
            <a href="InizioServlet?action=MaterassoSingolo">Materasso singolo</a>
            <a href="InizioServlet?action=MaterassoMatrimoniale">Materasso matrimoniale</a>
            <a href="InizioServlet?action=MaterassoPiazzaEMezzo">Materasso piazza e mezzo</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=Rete'">Rete
            <i class="fa fa-caret-down"></i>
        </button>
        <div class="dropdown-content">
            <a href="InizioServlet?action=ReteSingola">Rete singola</a>
            <a href="InizioServlet?action=ReteMatrimoniale">Rete matrimoniale</a>
            <a href="InizioServlet?action=RetePiazzaEMezzo">Rete piazza e mezzo</a>
        </div>
    </div>

    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=Divano'">Divano
            <i class="fa fa-caret-down"></i>
        </button>
        <div class="dropdown-content">
            <a href="InizioServlet?action=DivanoLetto">Divano Letto</a>
            <a href="InizioServlet?action=DivanoContenitore">Divano contenitore</a>
            <a href="InizioServlet?action=DivanoConPenisola">Divano con penisola</a>
            <a href="InizioServlet?action=DivanoPoltrona">Poltrona</a>
        </div>
    </div>

    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=Cuscino'">Cuscino
            <i class="fa fa-caret-down"></i>
        </button>
        <div class="dropdown-content">
            <a href="InizioServlet?action=Cuscinoguanciale">Guanciale</a>
            <a href="InizioServlet?action=Cuscinorex">Relax</a>
            <a href="InizioServlet?action=CuscinoViaggio">Viaggio</a>
        </div>
    </div>

    <!-- FIX 3: Sostituito <a> con <button> per il menu mobile -->
    <button type="button" class="icon" onclick="myFunction()">&#9776;</button>

    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=contatti'">
            <i class="fa fa-phone"></i>
        </button>
    </div>
    <% if(session.getAttribute("Utente")==null) { %>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=login'">
            <i class="fa fa-user-circle"></i>
        </button>
    </div>
    <% } else { %>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='LoginServlet?action=logout'">
            <i class="fa fa-sign-out"></i>
        </button>
    </div>
    <div class="dropdown">
        <%Utente u = (Utente) session.getAttribute("Utente");%>
        <a style="text-decoration: none; pointer-events: none">Bentornato, <c:out value="<%=u.getNome()%>"/></a>
    </div>
    <% } %>

    <% if(session.getAttribute("Utente")!=null) { %>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='LoginServlet?action=carrello'">
            <i class="fa fa-shopping-cart"></i>
        </button>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='LoginServlet?action=riepilogo'">
            <i class="fa fa-truck"></i>
        </button>
    </div>
    <% } %>

    <!-- FIX 4: Aggiunto id all'input e label nascosta per l'accessibilità -->
    <div class="search-container">
        <form action="RicercaServlet">
            <label for="searchInput" style="display: none;">Cerca per nome</label>
            <input type="text" id="searchInput" placeholder="Cerca per nome" name="search">
            <button type="submit" value="Cerca" class="cerca">
                <i class="fa fa-search"></i>
            </button>
        </form>
    </div>
</div>

<!-- CONTENITORE PRODOTTI CON FIX PER LISTE VUOTE -->
<%
    if (prod != null && !prod.isEmpty() && n >= 0) {
        int startIndex = Math.min(n, prod.size() - 1);
        for (int i = 0; i < 5 && startIndex >= 0; i++, startIndex--) {
            String idProd = prod.get(startIndex).getIdProdotto();
            String directory = "immagini/" + idProd + ".jpg";

            if (idProd != null && idProd.length() > 3) {
                try {
                    if (Integer.parseInt(idProd.substring(3)) > 54) {
                        directory = "immagini/fotoNonDisponibile.jpg";
                    }
                } catch (NumberFormatException e) {
                    directory = "immagini/fotoNonDisponibile.jpg";
                }
            } else {
                directory = "immagini/fotoNonDisponibile.jpg";
            }
%>
<div class="box-container">
    <div class="box">
        <div class="image">
            <a href="RicercaServlet?search=<c:out value="<%=prod.get(startIndex).getNomeProd()%>"/>">
                <img src="<c:out value="<%=directory%>"/>" alt="Immagine prodotto">
            </a>
        </div>
        <div class="info">
            <b style="text-align: center;">Modello:<c:out value="<%=prod.get(startIndex).getNomeProd()%>"/></b><br>
            <b style="text-align: center;color: red;"><c:out value='<%=String.format(java.util.Locale.ITALY, "%.2f", prod.get(startIndex).getPrezzo())%>'/>&#8364;</b>
        </div>
    </div>
</div>
<%
    }
} else {
%>
<div style="text-align: center; margin-top: 50px;">
    <h3>Nessun prodotto disponibile al momento.</h3>
</div>
<%
    }
%>
</body>
</html>