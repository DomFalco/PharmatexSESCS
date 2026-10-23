<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page import="model.Prodotto" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <link rel="stylesheet" type="text/css" href="ParteCSS/paginaProdotto.css">
    <link rel="stylesheet" type="text/css" href="ParteCSS/navBarAmministratore.css">
    <link rel="stylesheet" type="text/css" href="ParteCSS/CategorieProdotti.css">

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css">

    <%
        Prodotto prodottoModifica = (Prodotto) request.getAttribute("prodottoModifica");
        String val = prodottoModifica.getIdProdotto().substring(3);
        int y = Integer.parseInt(val);
        String directory = "immagini/" + prodottoModifica.getIdProdotto() + ".jpg";
        if(y > 54) {
            directory = "immagini/fotoNonDisponibile.jpg";
        }
    %>
    <title><%=prodottoModifica.getNomeProd()%></title>

    <script>
        function validatePQ() {
            var quantita = document.getElementById("quantitaTotale").value;
            var prezzo = document.getElementById("nuovoPrezzo").value;

            if(quantita === "" && prezzo === "") {
                alert("Errore quantità e prezzo devono avere un valore!");
                return false;
            }
            else if(quantita != null && prezzo == null) {
                var quantitaRGX = /^[0-9]*$/;
            }
            else if(prezzo != null && quantita == null) {
                var prezzoRGX = /^[0-9.]*$/;
            }
            else if(quantita != null && prezzo != null) {
                var quantitaRGX = /^[0-9]*$/;
                var prezzoRGX = /^[0-9.]*$/;
            }

            if(quantitaRGX.test(quantita) == false) {
                alert("Errore nel definire la nuova quantità");
                return false;
            }
            if(prezzoRGX.test(prezzo) == false) {
                alert("Errore nel definire il nuovo valore del prezzo");
                return false;
            }
            return true;
        }
    </script>
    <style>
        @media screen and (max-width: 1100px) {
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
    </style>
</head>
<body>

<div class="box-container">
    <div class="box">
        <div class="image" style="text-align: center; margin-top: 20px;">
            <img style="border-style: solid; border-width: 2px; width: 300px; height: 300px;" src="<%=directory%>" alt="Immagine prodotto">
        </div>

        <div style="text-align: center; margin-top: 15px;">
            <p><b>Quantità disponibile: <%=prodottoModifica.getQuantita()%></b></p>
            <p><b>Prezzo Attuale: <%=prodottoModifica.getPrezzo()%>€</b></p>
        </div>

        <!-- FIX: Rimosso position fixed e width auto per renderla centrata e più piccola -->
        <table id="tabella" style="margin: 20px auto; width: 60%; max-width: 700px;">
            <tr>
                <th scope="col">
                    <% HttpSession sessionPrezzo = request.getSession();
                        sessionPrezzo.setAttribute("idModificaPrezzo", prodottoModifica);
                    %>
                    <h2 style="margin: 0;">Modello: <%=prodottoModifica.getNomeProd()%></h2>
                </th>
            </tr>
            <tr>
                <th scope="col">
                    <!-- FIX: Form strutturato in modo compatto -->
                    <form action="ModificaProdottiServletAmministratore" class="form-edit">
                        <div class="form-row">
                            <label for="nuovoPrezzo">Nuovo prezzo:</label>
                            <input type="text" id="nuovoPrezzo" name="nuovoPrezzo" placeholder="Es. 45.50">
                        </div>
                        <div class="form-row">
                            <label for="quantitaTotale">Nuova quantità:</label>
                            <input type="text" id="quantitaTotale" name="quantitaTotale" placeholder="Es. 10">
                        </div>
                        <button type="submit" onclick="return(validatePQ())" class="cart" style="margin-top: 10px;">Premi</button>
                    </form>
                </th>
            </tr>
            <tr>
                <th scope="col" style="padding: 15px;">
                    <button type="button" class="cart" onclick="window.location.href='RimuoviProdottoServlet'" aria-label="Rimuovi prodotto">
                        <i class="fa fa-trash-o"></i>
                    </button>
                    <button type="button" class="cart" onclick="window.location.href='HomeServletAmministratore'" aria-label="Torna alla Home">
                        <i class="fa fa-home"></i>
                    </button>
                </th>
            </tr>
        </table>
    </div>
</div>
</body>
</html>