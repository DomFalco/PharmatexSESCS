<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Model.Prodotto" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <link rel="stylesheet" type="text/css" href="ParteCSS/paginaProdotto.css">
    <link rel="stylesheet" type="text/css" href="ParteCSS/navBarAmministratore.css">
    <link rel="stylesheet" type="text/css" href="ParteCSS/CategorieProdotti.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <% Prodotto prodottoModifica = (Prodotto) request.getAttribute("prodottoModifica");
        String val = prodottoModifica.getIdProdotto().substring(3);
        int y = Integer.parseInt(val);
        String directory = "immagini/" + prodottoModifica.getIdProdotto() + ".jpg";
        if(y>54)
        {
            directory = "immagini/fotoNonDisponibile.jpg";
        }
    %>
    <title><%=prodottoModifica.getNomeProd()%></title>

    <script>
        function validatePQ(){
            var quantita=document.getElementById("quantitaTotale").value;
            var prezzo=document.getElementById("nuovoPrezzo").value;

            if(quantita==="" && prezzo==="")
            {
                console.log("ciao");
                alert("Errore quantità e prezzo devono avere un valore!");
                return false;
            }
            else if(quantita!=null && prezzo==null)
            {
                var quantitaRGX=/^[0-9]*$/;
            }
            else if(prezzo!=null && quantita==null)
            {
                var prezzoRGX=/^[0-9.]*$/;
            }
            else if(quantita!=null && prezzo!=null)
            {
                var quantitaRGX=/^[0-9]*$/;
                var prezzoRGX=/^[0-9.]*$/;
            }

            if(quantitaRGX.test(quantita)==false){
                alert("Errore nel definire la nuova quantità");
                return false;
            }
            if(prezzoRGX.test(prezzo)==false){
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
        }

        @media screen and (max-width: 1100px) {
            #tabella{
                left: 7%;
                top: 80%;
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

        @media screen and (max-width: 500px) {
            #tabella{
                left: 7%;
                top: 70%;
            }
        }
    </style>
</head>
<body>
<div class="box-container">
    <div class="box">
        <div class="image">
            <img style="border-style: solid;border-width: 2px;" src="<%=directory%>" alt="Immagine prodotto">
        </div>
        <p><b>Quantità disponibile:<%=prodottoModifica.getQuantita()%></b></p>
        <p><b>Prezzo Attuale: <%=prodottoModifica.getPrezzo()%>€</b></p>
        <table style="position: fixed; left: 7%; top: 70%; width: auto;" id="tabella">
            <tr>
                <th scope="col">
                    <% HttpSession sessionPrezzo= request.getSession();
                        sessionPrezzo.setAttribute("idModificaPrezzo",prodottoModifica);
                    %>
                    <h1>Modello:<%=prodottoModifica.getNomeProd()%></h1>
                </th>
            </tr>
            <tr>
                <th scope="col">
                    <form action="ModificaProdottiServletAmministratore">
                        <label for="nuovoPrezzo">Inserisci nuovo prezzo:</label>
                        <input type="text" id="nuovoPrezzo" name="nuovoPrezzo">
                        <label for="quantitaTotale">Inserisci la quantità:</label>
                        <input type="text" id="quantitaTotale" name="quantitaTotale">
                        <button type="submit" onclick="return(validatePQ())" class="cart">Premi</button>
                    </form>
                </th>
            </tr>
            <tr>
                <th scope="col">
                    <button class="cart" onclick="window.location.href='RimuoviProdottoServlet'"><i class="fa fa-trash-o"></i></button>
                </th>
            </tr>
            <tr>
                <td>
                    <button class="cart" onclick="window.location.href='HomeServletAmministratore'"><i class="fa fa-home"></i></button>
                </td>
            </tr>
        </table>
    </div>
</div>
</body>
</html>