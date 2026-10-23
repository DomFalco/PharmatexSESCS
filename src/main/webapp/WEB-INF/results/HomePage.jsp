<%@ page import="model.Prodotto" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="model.Utente" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
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
            *{
                zoom: normal;
            }
        }

        @media screen and (max-width: 1482px) {
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

        /* FIX: Stile pulito per la barra di ricerca e per il logo */
        .search-container {
            float: right;
            margin-top: 8px;
            margin-right: 15px;
        }
        .search-container form {
            display: flex;
            align-items: center;
            margin: 0;
        }
        .search-container input[type="text"] {
            padding: 0 10px;
            font-size: 16px;
            border: none;
            border-radius: 4px 0 0 4px;
            width: 15em;
            height: 36px; /* Altezza fissa */
            box-sizing: border-box; /* Include i bordi nell'altezza */
            margin: 0;
            outline: none;
        }
        .search-container .cerca {
            width: 40px; /* Larghezza proporzionata */
            height: 36px; /* STESSA ALTEZZA DELL'INPUT */
            border: none;
            background-color: white;
            border-radius: 0 4px 4px 0;
            cursor: pointer;
            color: #006600;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0;
            box-sizing: border-box; /* Include i bordi nell'altezza */
        }
        .sfondo {
            max-width: 100%;
            height: auto;
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
            <a href="InizioServlet?action=CuscinoRelax">Relax</a>
            <a href="InizioServlet?action=CuscinoViaggio">Viaggio</a>
        </div>
    </div>

    <a href="javascript:void(0);" style="font-size:15px;" class="icon" onclick="myFunction()">&#9776;</a>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=contatti'">
            <i class="fa fa-phone"></i>
        </button>
    </div>
    <%
        if(session.getAttribute("Utente")==null)
        {%>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='InizioServlet?action=login'">
            <i class="fa fa-user-circle"></i>
        </button>
    </div>
    <%}
    else
    {%>
    <div class="dropdown">
        <button class="dropbtn" onclick="window.location.href='LoginServlet?action=logout'">
            <i class="fa fa-sign-out"></i>
        </button>
    </div>
    <div class="dropdown">
        <%Utente u = (Utente) session.getAttribute("Utente");%>
        <a style="text-decoration: none; pointer-events: none">Bentornato, <c:out value="<%=u.getNome()%>"/></a>
    </div>
    <%}%>
    <%if(session.getAttribute("Utente")!=null)
    {%>
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
    <%}%>

    <!-- FIX RICERCA: Rimossi gli stili inline, ora è gestita dal CSS pulito sopra -->
    <div class="search-container">
        <form action="RicercaServlet">
            <input type="text" placeholder="Cerca per nome" name="search">
            <button type="submit" value="Cerca" class="cerca" aria-label="Cerca">
                <i class="fa fa-search"></i>
            </button>
        </form>
    </div>
</div>

<!-- HO RIPRISTINATO ESATTAMENTE LA TUA STRUTTURA ORIGINALE -->
<%
    for (int i = 0; i < 5; i++,n--) {
        String directory = "immagini/" + prod.get(n).getIdProdotto() + ".jpg";
        if(Integer.parseInt(prod.get(n).getIdProdotto().substring(3))>54)
        {
            directory = "immagini/fotoNonDisponibile.jpg";
        }
%>
<div class="box-container">
    <div class="box">
        <div class="image">
            <a href="RicercaServlet?search=<c:out value="<%=prod.get(n).getNomeProd()%>"/>">
                <img src="<c:out value="<%=directory%>"/>">
            </a>
        </div>
        <div class="info">
            <b style="text-align: center;">Modello:<c:out value="<%=prod.get(n).getNomeProd()%>"/></b><br>
            <b style="text-align: center;color: red;"><c:out value="<%=prod.get(n).getPrezzo()%>"/>&#8364;</b>
        </div>
    </div>
</div>
<% } %>
</body>
</html>