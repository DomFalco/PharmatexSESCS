<%@ page import="model.Utente" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Pharmatex - Area Amministratore</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="ParteCSS/CategorieProdotti.css">
    <link rel="stylesheet" type="text/css" href="ParteCSS/navBarAmministratore.css">
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
</head>
<body>
<img src="immagini/logosito.png" class="sfondo" alt="Logo Pharmatex">

<div class="topnav" id="myTopnav">
    <!-- 1. Pulsante Home (rimossa la classe 'active' per il colore) -->
    <a href="HomeServletAmministratore"><i class="fa fa-home"></i></a>

    <!-- 2. Categorie -->
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?valore=Letto'">Letto <i class="fa fa-caret-down"></i></button>
        <div class="dropdown-content">
            <a href="InizioServlet?valore=LettoSingolo">Letto singolo</a>
            <a href="InizioServlet?valore=LettoMatrimoniale">Letto matrimoniale</a>
            <a href="InizioServlet?valore=LettoPiazzaEMezzo">Letto piazza e mezzo</a>
            <a href="InizioServlet?valore=LettoContenitore">Letto contenitore</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?valore=Materasso'">Materasso <i class="fa fa-caret-down"></i></button>
        <div class="dropdown-content">
            <a href="InizioServlet?valore=MaterassoSingolo">Materasso singolo</a>
            <a href="InizioServlet?valore=MaterassoMatrimoniale">Materasso matrimoniale</a>
            <a href="InizioServlet?valore=MaterassoPiazzaEMezzo">Materasso piazza e mezzo</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?valore=Rete'">Rete <i class="fa fa-caret-down"></i></button>
        <div class="dropdown-content">
            <a href="InizioServlet?valore=ReteSingola">Rete singola</a>
            <a href="InizioServlet?valore=ReteMatrimoniale">Rete matrimoniale</a>
            <a href="InizioServlet?valore=RetePiazzaEMezzo">Rete piazza e mezzo</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?valore=Divano'">Divano <i class="fa fa-caret-down"></i></button>
        <div class="dropdown-content">
            <a href="InizioServlet?valore=DivanoLetto">Divano Letto</a>
            <a href="InizioServlet?valore=DivanoContenitore">Divano contenitore</a>
            <a href="InizioServlet?valore=DivanoConPenisola">Divano con penisola</a>
            <a href="InizioServlet?valore=DivanoPoltrona">Poltrona</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?valore=Cuscino'">Cuscino <i class="fa fa-caret-down"></i></button>
        <div class="dropdown-content">
            <a href="InizioServlet?valore=Cuscinoguanciale">Guanciale</a>
            <a href="InizioServlet?valore=CuscinoRelax">Relax</a>
            <a href="InizioServlet?valore=CuscinoViaggio">Viaggio</a>
        </div>
    </div>

    <!-- Pulsante Hamburger per Mobile -->
    <button type="button" class="icon" onclick="myFunction()">&#9776;</button>

    <!-- 3, 4, 5, 6, 7, 8. Blocco che ora sta a sinistra, subito dopo le categorie -->
    <div class="right-nav">

        <!-- Logout -->
        <div class="dropdown">
            <button class="dropbtn" onclick="window.location.href='LoginServlet?action=logout'">
                <i class="fa fa-sign-out"></i>
            </button>
        </div>

        <!-- Nome Utente -->
        <div class="dropdown">
            <% Utente u = (Utente) session.getAttribute("Amministratore"); %>
            <span class="user-text">Bentornato Amministratore, <c:out value="<%=u.getNome()%>"/></span>
        </div>

        <!-- Prodotti esauriti -->
        <div class="dropdown">
            <button class="dropbtn" onclick="window.location.href='HomeServletAmministratore?valore=quantita'">
                <i class="fa fa-cart-arrow-down"></i>
            </button>
        </div>

        <!-- Spedizioni/Ordini -->
        <div class="dropdown">
            <button class="dropbtn" onclick="window.location.href='HomeServletAmministratore?valore=ordine'">
                <i class="fa fa-calendar-check-o"></i>
            </button>
        </div>

        <!-- Utenti -->
        <div class="dropdown">
            <button class="dropbtn" onclick="window.location.href='HomeServletAmministratore?valore=clienti'">
                <i class="fa fa-group"></i>
            </button>
        </div>

        <!-- Aggiungi prodotto -->
        <div class="dropdown">
            <button class="dropbtn" onclick="window.location.href='HomeServletAmministratore?valore=aggiungi'">
                <i class="fa fa-cart-plus"></i>
            </button>
        </div>

    </div>
</div>
</body>
</html>