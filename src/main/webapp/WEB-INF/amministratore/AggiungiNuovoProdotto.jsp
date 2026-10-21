<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <title>Aggiunta nuovo prodotto</title>
    <link rel="stylesheet" type="text/css" href="ParteCSS/AggiuntaProdotto.css">
    <!-- FIX SONARQUBE: Aggiunto integrity e crossorigin -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" integrity="sha512-SfTiTlX6kk+qitfevl/7LibUOeJWlt9rbyDn92a1DqWOw9vWG2MFoays0sgObmWazO5BQPiFucnnEAjpAB+/4Q==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <script>
        function validateNewProduct(){
            var idProdotto=document.getElementById('idProdotto').value;
            var idProdottoRGX=/^[A-Z0-9]*$/;
            var nomeProdotto=document.getElementById('nomeProdotto').value;
            var nomeProdottoRGX=/^[a-zA-Z' ']*$/;
            var lunghezza=document.getElementById('lunghezza').value;
            var lunghezzaRGX=/^[0-9]*$/;
            var larghezza=document.getElementById('larghezza').value;
            var larghezzaRGX=/^[0-9]*$/;
            var prezzo=document.getElementById('prezzo').value;
            var prezzoRGX=/^[0-9'.']*$/;
            var quantita=document.getElementById('quantita').value;
            var quantitaRGX=/^[0-9]*$/;
            var tipoMaterialeMaterasso=document.getElementById('tipoMaterialeMaterasso').value;
            var tipoMaterialeMaterassoRGX=/^[a-zA-Z]*$/;
            var rivestimentoDivano=document.getElementById('rivestimentoDivano').value;
            var rivestimentoDivanoRGX=/^[a-zA-Z]*$/;
            var coloreLetto=document.getElementById('coloreLetto').value;
            var coloreLettoRGX=/^[a-zA-Z]*$/;
            var tipoStoffaCuscino=document.getElementById('tipoStoffaCuscino').value;
            var tipoStoffaCuscinoRGX=/^[a-zA-Z]*$/;
            var formaCuscino=document.getElementById('formaCuscino').value;
            var formaCuscinoRGX=/^[a-zA-Z]*$/;
            var materialeRete=document.getElementById('materialeRete').value;
            var materialeReteRGX=/^[a-zA-Z]*$/;
            var numProdotto=idProdotto.substring(3);
            console.log(numProdotto)
            if(numProdotto<=54){
                alert("Errore nella definizione del valore dell'ID");
                return false;
            }

            if(idProdottoRGX.test(idProdotto)==false){
                alert("Errore nella definizione dell'ID");
                return false;
            }

            if(nomeProdottoRGX.test(nomeProdotto)==false){
                alert("Errore nella definizione del nome");
                return false;
            }

            if(lunghezzaRGX.test(lunghezza)==false){
                alert("Errore nella definizione della lunghezza");
                return false;
            }

            if(larghezzaRGX.test(larghezza)==false){
                alert("Errore nella definizione della larghezza");
                return false;
            }

            if(prezzoRGX.test(prezzo)==false){
                alert("Errore nella definizione del prezzo");
                return false;
            }

            if(quantitaRGX.test(quantita)==false){
                alert("Errore nella definizione della quantita");
                return false;
            }

            if(tipoMaterialeMaterassoRGX.test(tipoMaterialeMaterasso)==false){
                alert("Errore nella definizione del materiale del materasso");
                return false;
            }

            if(coloreLettoRGX.test(coloreLetto)==false){
                alert("Errore nella definizione del colore del letto");
                return false;
            }

            if(rivestimentoDivanoRGX.test(rivestimentoDivano)==false){
                alert("Errore nella definizione del rivestimento del divano");
                return false;
            }

            if(tipoStoffaCuscinoRGX.test(tipoStoffaCuscino)==false){
                alert("Errore nella definizione della stoffa del cuscino");
                return false;
            }

            if(formaCuscinoRGX.test(formaCuscino)==false){
                alert("Errore nella definizione della forma del cuscino");
                return false;
            }
            if(materialeReteRGX.test(materialeRete)==false){
                alert("Errore nella definizione del materiale della rete");
                return false;
            }

            return true;
        }
    </script>
    <style>
        @media screen and (max-width: 1920px) {
            textarea{
                width: 80%;
            }
        }
        table {
            border: 3px solid green;
            padding: 5px;
            border-collapse: collapse;
            margin-left: auto;
            margin-right: auto;
            margin-top: 5%;
            width: 50%;
        }
    </style>
</head>
<body>
<form method="post" action="AggiuntaProdottoServlet">
    <table>
        <tr>
            <th class="inserimento1" scope="col">
                <p>Per i prodotti la prima lettera rappresenta la categoria e la seconda il tipo: MM000 Materasso Matrimoniale</p>
                <label for="idProdotto">Inserisci l'id del prodotto:</label>
                <input type="text" name="idProdotto" id="idProdotto" required> <br>

                <label for="nomeCategoria">Scegli la categoria del prodotto:</label>
                <select name="nomeCategoria" id="nomeCategoria">
                    <option id="MaterassoMatrimoniale"> MaterassoMatrimoniale </option>
                    <option id="MaterassoSingolo"> MaterassoSingolo </option>
                    <option id="MaterassoPiazzaEMezzo"> MaterassoPiazzaEMezzo </option>
                    <option id="LettoContenitoreMatrimoniale"> LettoContenitoreMatrimoniale </option>
                    <option id="LettoContenitorePiazzaEMezzo"> LettoContenitorePiazzaEMezzo </option>
                    <option id="LettoContenitoreSingolo"> LettoContenitoreSingolo </option>
                    <option id="LettoSingolo"> LettoSingolo </option>
                    <option id="LettoMatrimoniale"> LettoMatrimoniale </option>
                    <option id="ReteSingolaElettrica"> ReteSingolaElettrica </option>
                    <option id="ReteMatrimonialeElettrica"> ReteMatrimonialeElettrica </option>
                    <option id="RetePiazzaEMezzoDoghe"> RetePiazzaEMezzoDoghe </option>
                    <option id="DivanoLetto"> DivanoLetto </option>
                    <option id="CuscinoViaggio"> CuscinoViaggio </option>
                    <option id="CuscinoRelax"> CuscinoRelax </option>
                    <option id="CuscinoGuanciale"> CuscinoGuanciale </option>
                    <option id="DivanoPoltrona"> DivanoPoltrona </option>
                    <option id="DivanoConPenisola"> DivanoConPenisola </option>
                </select><br>
                <label for="nomeProdotto">Inserisci il nome del prodotto</label>
                <input type="text" name="nomeProdotto" id="nomeProdotto" required><br>
                <label for="descrizione">Inserisci la descrizione del prodotto:</label>
                <textarea name="descrizione" id="descrizione" rows="4" cols="80"></textarea><br>
                <label for="larghezza">Inserisci la larghezza</label>
                <input type="text" name="larghezza" id="larghezza"><br>
                <label for="lunghezza">Inserisci la lunghezza</label>
                <input type="text" name="lunghezza" id="lunghezza"><br>
                <label for="prezzo">Inserisci il prezzo</label>
                <input  type="text" name="prezzo" id="prezzo"><br>
                <label for="quantita">Inserisci la quantità</label>
                <input type="text" name="quantita" id="quantita"><br>
            </th>
            <th class="inserimento" scope="col">
                <p>Inserire i parametri solo per i prodotti di appartenenza:</p>
                <label for="tipoMaterialeMaterasso">Inserire il tipo di materiale del materasso</label>
                <input type="text" name="tipoMaterialeMaterasso" id="tipoMaterialeMaterasso"><br>
                <label for="coloreLetto">Inserire il colore del letto</label>
                <input type="text" name="coloreLetto" id="coloreLetto"><br>
                <label for="materialeRete">Inserire il materiale della rete</label>
                <input type="text" name="materialeRete" id="materialeRete"><br>
                <label for="rivestimentoDivano">Inserire il materiale del divano</label>
                <input type="text" name="rivestimentoDivano" id="rivestimentoDivano"><br>
                <label for="coloreDivano">Inserire colore divano</label>
                <input type="text" name="coloreDivano" id="coloreDivano"><br>
                <label for="tipoStoffaCuscino">Inserire la stoffa del cuscino</label>
                <input type="text" name="tipoStoffaCuscino" id="tipoStoffaCuscino"><br>
                <label for="materialeCuscino">Inserire il materiale del cuscino</label>
                <input type="text" name="materialeCuscino" id="materialeCuscino"><br>
                <label for="formaCuscino">Inserire la forma del cuscino</label>
                <input type="text" name="formaCuscino" id="formaCuscino"><br>
            </th>
        </tr>
    </table>
    <button onclick="return(validateNewProduct())" type="submit" class="cart"><i class="fa fa-cart-plus"></i></button>
    <button onclick="window.location.href='HomeServletAmministratore'" type="submit" class="cart"><i class="fa fa-home"></i></button>
</form>
</body>
</html>