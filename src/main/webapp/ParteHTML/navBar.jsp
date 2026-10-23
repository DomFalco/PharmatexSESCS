<%@ page import="model.Utente" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" type="text/css" href="ParteCSS/styleSito.css">
<!-- FontAwesome per le icone -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">

<div class="topnav" id="myTopnav">
    <!-- 1. Pulsante Home -->
    <a href="HomePage" style="font-size: 18px;"><i class="fa fa-home"></i></a>

    <!-- 2. Categorie -->
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=Letto'">Letto <i class="fa fa-caret-down"></i></button>
        <div class="dropdown-content">
            <a href="InizioServlet?action=LettoSingolo">Letto singolo</a>
            <a href="InizioServlet?action=LettoMatrimoniale">Letto matrimoniale</a>
            <a href="InizioServlet?action=LettoPiazzaEMezzo">Letto piazza e mezzo</a>
            <a href="InizioServlet?action=LettoContenitore">Letto contenitore</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=Materasso'">Materasso <i class="fa fa-caret-down"></i></button>
        <div class="dropdown-content">
            <a href="InizioServlet?action=MaterassoSingolo">Materasso singolo</a>
            <a href="InizioServlet?action=MaterassoMatrimoniale">Materasso matrimoniale</a>
            <a href="InizioServlet?action=MaterassoPiazzaEMezzo">Materasso piazza e mezzo</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=Rete'">Rete <i class="fa fa-caret-down"></i></button>
        <div class="dropdown-content">
            <a href="InizioServlet?action=ReteSingola">Rete singola</a>
            <a href="InizioServlet?action=ReteMatrimoniale">Rete matrimoniale</a>
            <a href="InizioServlet?action=RetePiazzaEMezzo">Rete piazza e mezzo</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=Divano'">Divano <i class="fa fa-caret-down"></i></button>
        <div class="dropdown-content">
            <a href="InizioServlet?action=DivanoLetto">Divano Letto</a>
            <a href="InizioServlet?action=DivanoContenitore">Divano contenitore</a>
            <a href="InizioServlet?action=DivanoConPenisola">Divano con penisola</a>
            <a href="InizioServlet?action=DivanoPoltrona">Poltrona</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=Cuscino'">Cuscino <i class="fa fa-caret-down"></i></button>
        <div class="dropdown-content">
            <a href="InizioServlet?action=Cuscinoguanciale">Guanciale</a>
            <a href="InizioServlet?action=CuscinoRelax">Relax</a>
            <a href="InizioServlet?action=CuscinoViaggio">Viaggio</a>
        </div>
    </div>

    <!-- Icona Hamburger per mobile -->
    <a href="javascript:void(0);" style="font-size:15px;" class="icon" onclick="myFunction()">&#9776;</a>

    <!-- 3. Contatti e 4. Utente (Login/Logout) -->
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
    <!-- Carrello e Riepilogo spostati qui per l'utente loggato -->
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
    <div class="dropdown">
        <% Utente u = (Utente) session.getAttribute("Utente"); %>
        <a style="text-decoration: none; pointer-events: none;">Bentornato, <c:out value="<%=u.getNome()%>"/></a>
    </div>
    <% } %>

    <!-- 5. Ricerca (spinta a destra dal CSS) -->
    <div class="search-container">
        <form action="RicercaServlet">
            <input type="text" placeholder="Cerca per nome" name="search">
            <button type="submit" value="Cerca" class="cerca">
                <i class="fa fa-search"></i>
            </button>
        </form>
    </div>
</div>

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