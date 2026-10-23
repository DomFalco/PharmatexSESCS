<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <title>Metodo di pagamento</title>
    <link rel="stylesheet" type="text/css" href="ParteCSS/PagamentoProdotti.css">

    <!-- FIX: Usiamo FontAwesome 6.5.1 da jsDelivr che è più affidabile -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.5.1/css/all.min.css">

    <script>
        function confermaParametri() {
            var nomeRGX = /^[a-zA-Z' ']*$/;
            var nomeCognome = document.getElementById('credenziali').value;

            if ((nomeRGX.test(nomeCognome)) == false) {
                alert("Nome e cognome non validi!");
                return false;
            }

            var numeroCarta = document.getElementById('NCarta').value;
            var numeroCartaRGX = /^[0-9]*$/;

            if ((numeroCartaRGX.test(numeroCarta)) == false) {
                alert("Formato del numero carta non valido!");
                return false;
            }
            if (numeroCarta.length != 16) {
                alert("Dimensione del numero di carta errato!");
                return false;
            }

            var dataRGX = /^[0-9'/']*$/;
            var dataScadenza = document.getElementById('dataScadenza').value;

            const [giorno, mese, anno] = dataScadenza.split("/");
            var dataScadenzaCarta = new Date(anno, mese, giorno);

            if (dataScadenzaCarta < (new Date())) {
                alert("La carta di credito è scaduta");
                return false;
            }

            if ((dataRGX.test(dataScadenza)) == false) {
                alert("Formato data errato!");
                return false;
            }

            var numeroCVV = document.getElementById('cvv').value;
            var numeroCVVRGX = /^[0-9]*$/;

            if ((numeroCVVRGX.test(numeroCVV)) == false) {
                alert("Formato del numero cvv non valido!");
                return false;
            }
            if (numeroCVV.length != 3) {
                alert("Dimensione del numero cvv errato!");
                return false;
            }
            alert("Pagamento effettuato con successo");
            return true;
        }
    </script>
</head>
<body>

<div class="payment-container">
    <form method="post" action="DatiPagamentoServlet" onsubmit="return confermaParametri();">

        <div class="form-group">
            <label for="credenziali">Inserire nome e cognome del titolare</label>
            <input type="text" name="credenziali" id="credenziali" required>
        </div>

        <div class="form-group">
            <label for="NCarta">Inserire il numero di carta</label>
            <input type="number" name="NCarta" id="NCarta" required>
        </div>

        <div class="form-group">
            <label for="dataScadenza">Inserire la data di scadenza</label>
            <input type="text" name="dataScadenza" id="dataScadenza" placeholder="gg/mm/aaaa" required>
        </div>

        <div class="form-group">
            <label for="cvv">Inserisci il CVV</label>
            <input type="number" name="cvv" id="cvv" required>
        </div>

        <div class="button-group">
            <!-- Aggiunto testo "Paga" -->
            <button type="submit" class="cart" aria-label="Paga">
                <i class="fa-solid fa-credit-card"></i>
            </button>

            <!-- Aggiunto testo "Home" -->
            <button type="button" class="cart" onclick="window.location.href='HomePage'" aria-label="Torna alla home">
                <i class="fa-solid fa-house"></i>
            </button>
        </div>

    </form>
</div>

</body>
</html>