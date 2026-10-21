<%@ page import="model.Utente" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Pharmatex - Area Amministratore</title>
    <!-- FIX SONARQUBE: Aggiunto integrity e crossorigin -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" integrity="sha512-SfTiTlX6kk+qitfevl/7LibUOeJWlt9rbyDn92a1DqWOw9vWG2MFoays0sgObmWazO5BQPiFucnnEAjpAB+/4Q==" crossorigin="anonymous" referrerpolicy="no-referrer" />
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
    <style>
        @media screen and (max-width: 1100px) {
            .topnav a:not(:first-child), .dropdown .dropbtn { display: none; }
            .topnav a.icon { float: right; display: block; }
            .topnav .search-container{ float: left; }
        }
        @media screen and (max-width: 1100px) {
            .topnav.responsive {position: relative;}
            .topnav.responsive .icon { position: absolute; right: 0; top: 0; }
            .topnav.responsive a { float: none; display: block; text-align: left; }
            .topnav.responsive .dropdown {float: none;}
            .topnav.responsive .dropdown-content {position: relative;}
            .topnav.responsive .dropdown .dropbtn { display: block; width: 100%; text-align: left; }
        }
    </style>
</head>
<body>
<img src="immagini/logosito.png" class="sfondo" alt="Logo Pharmatex">
<div class="topnav" id="myTopnav">
    <a href="HomeServletAmministratore" class="active"><i class="fa fa-home"></i></a>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?valore=Letto'">Letto
            <i class="fa fa-caret-down"></i>
        </button>
        <div class="dropdown-content">
            <a href="InizioServlet?valore=LettoSingolo">Letto singolo</a>
            <a href="InizioServlet?valore=LettoMatrimoniale">Letto matrimoniale</a>
            <a href="InizioServlet?valore=LettoPiazzaEMezzo">Letto piazza e mezzo</a>
            <a href="InizioServlet?valore=LettoContenitore">Letto contenitore</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?valore=Materasso'">Materasso
            <i class="fa fa-caret-down"></i>
        </button>
        <div class="dropdown-content">
            <a href="InizioServlet?valore=MaterassoSingolo">Materasso singolo</a>
            <a href="InizioServlet?valore=MaterassoMatrimoniale">Materasso matrimoniale</a>
            <a href="InizioServlet?valore=MaterassoPiazzaEMezzo">Materasso piazza e mezzo</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?valore=Rete'">Rete
            <i class="fa fa-caret-down"></i>
        </button>
        <div class="dropdown-content">
            <a href="InizioServlet?valore=ReteSingola">Rete singola</a>
            <a href="InizioServlet?valore=ReteMatrimoniale">Rete matrimoniale</a>
            <a href="InizioServlet?valore=RetePiazzaEMezzo">Rete piazza e mezzo</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?valore=Divano'">Divano
            <i class="fa fa-caret-down"></i>
        </button>
        <div class="dropdown-content">
            <a href="InizioServlet?valore=DivanoLetto">Divano Letto</a>
            <a href="InizioServlet?valore=DivanoContenitore">Divano contenitore</a>
            <a href="InizioServlet?valore=DivanoConPenisola">Divano con penisola</a>
            <a href="InizioServlet?valore=DivanoPoltrona">Poltrona</a>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?valore=Cuscino'">Cuscino
            <i class="fa fa-caret-down"></i>
        </button>
        <div class="dropdown-content">
            <a href="InizioServlet?valore=Cuscinoguanciale">Guanciale</a>
            <a href="InizioServlet?valore=CuscinoRelax">Relax</a>
            <a href="InizioServlet?valore=CuscinoViaggio">Viaggio</a>
        </div>
    </div>

    <!-- FIX SONARQUBE: Sostituito <a> con <button> -->
    <button type="button" style="font-size:15px; background:none; border:none; cursor:pointer; padding: 14px 16px; float:right;" class="icon" onclick="myFunction()">&#9776;</button>

    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='LoginServlet?action=logout'">
            <i class="fa fa-sign-out"></i>
        </button>
    </div>
    <div class="dropdown">
        <%Utente u = (Utente) session.getAttribute("Amministratore");%>
        <a style="text-underline: none; pointer-events: none">Bentornato Amministatore, <%=u.getNome()%></a>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='HomeServletAmministratore?valore=quantita'"><i class='fa fa-cart-arrow-down'></i></button>
    </div>
    <div class="dropdown">
        <div class="dropdown">
            <button class="dropbtn" onclick="window.location.href='HomeServletAmministratore?valore=ordine'"><i class="fa fa-calendar-check-o"></i></button>
        </div>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='HomeServletAmministratore?valore=clienti'"><i class="fa fa-group"></i></button>
    </div>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='HomeServletAmministratore?valore=aggiungi'"><i class="fa fa-cart-plus"></i></button>
    </div>
</div>
</body>
</html>