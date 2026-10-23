<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <link rel="stylesheet" type="text/css" href="ParteCSS/styleSito.css">
    <link rel="stylesheet" type="text/css" href="ParteCSS/Registrazione.css">
    <title>Registrazione Utente</title>
    <% String x = " ";
        if(request.getAttribute("controllo") != null) {
            x = "Email già presente!";
        }
    %>
    <script>
        function validateRegistrazione() {
            var email = document.getElementById('email').value;
            var emailRGX = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
            if((emailRGX.test(email) == false)) {
                alert("Formato email non valido!");
                return false;
            }

            var passwordRGX = /^[a-zA-Z0-9!@?]*$/;
            var password = document.getElementById('passwordEmail').value;
            if((passwordRGX.test(password)) == false) {
                alert("Caratteri password non validi!");
                return false;
            }
            if((password.length < 8 || password.length > 30)) {
                alert("Dimensione password non valida!");
                return false;
            }

            var nomeRGX = /^[a-zA-Z' ']*$/;
            var nome = document.getElementById('nome').value;
            if((nomeRGX.test(nome)) == false) {
                alert("Nome non valido!");
                return false;
            }

            var cognomeRGX = /^[a-zA-Z' ']*$/;
            var cognome = document.getElementById('cognome').value;
            if((cognomeRGX.test(cognome)) == false) {
                alert("Cognome non valido!");
                return false;
            }

            var dataRGX = /^[0-9'/']*$/;
            var data = document.getElementById('datadiNascita').value;
            if((dataRGX.test(data)) == false) {
                alert("Formato data errato!");
                return false;
            }

            var numeroTelefono = document.getElementById('numeroTelefono').value;
            var numeroTelefonoRGX = /^[0-9]*$/;
            if((numeroTelefonoRGX.test(numeroTelefono)) == false) {
                alert("Formato del numero di telefono errato!");
                return false;
            }
            if(numeroTelefono.length != 10) {
                alert("Dimensione del numero di telefono errata!");
                return false;
            }

            var codiceFiscale = document.getElementById('codiceFiscale').value;
            var codiceFiscaleRGX = /^[A-Z0-9]*$/;
            if((codiceFiscaleRGX.test(codiceFiscale)) == false) {
                alert("Codice fiscale non valido!");
                return false;
            }
            if(codiceFiscale.length != 16) {
                alert("Dimensione del codice fiscale errata!");
                return false;
            }

            var citta = document.getElementById('citta').value;
            var cittaRGX = /^[A-Za-zìòàèéù' ']*$/;
            if((cittaRGX.test(citta)) == false) {
                alert("Nome citta' errato!");
                return false;
            }

            var cap = document.getElementById('cap').value;
            var capRGX = /^[0-9]*$/;
            if((capRGX.test(cap)) == false) {
                alert("Formato del cap errato!");
                return false
            }
            if(cap.length != 5) {
                alert("Dimensione cap errato");
                return false;
            }

            var provincia = document.getElementById('provincia').value;
            var provinciaRGX = /^[A-Z]*$/;
            if((provinciaRGX.test(provincia)) == false) {
                alert("La provincia inserita non e' corretta");
                return false;
            }
            if(provincia.length != 2) {
                alert("La provincia non rispetta la lunghezza richiesta");
                return false;
            }

            var nazione = document.getElementById('nazione').value;
            var nazioneRGX = /^[A-Za-z]*$/;
            if((nazioneRGX.test(nazione )) == false) {
                alert("Nome nazione errato!");
                return false;
            }

            return true;
        }
    </script>
</head>
<body>
<img src="immagini/logosito.png" class="sfondo" alt="Logo Pharmatex">

<!-- FIX: Aggiunto onsubmit al form per bloccare il tasto Invio se i dati sono errati -->
<form method="post" action="RegistrazioneServlet" onsubmit="return validateRegistrazione();">
    <table>
        <tr>
            <th scope="col">
                <label for="nome">Inserisci il nome</label>
                <input type="text" placeholder="Mario" name="nome" id="nome" required>

                <label for="cognome">Inserisci il cognome</label>
                <input type="text" placeholder="Rossi" name="cognome" id="cognome" required>

                <label for="codiceFiscale">Inserisci il codice fiscale</label>
                <input type="text" name="codiceFiscale" id="codiceFiscale" pattern="[A-Z0-9]{16}" required title="Il codice fiscale deve avere lunghezza pari a 16 e deve contenere numeri e lettere maiuscole">

                <label for="datadiNascita">Inserisci la data di nascita</label>
                <input type="text" placeholder="GG/MM/AAAA" id="datadiNascita" name="datadiNascita" required>

                <label for="email">Inserisci l'email</label>
                <input type="email" placeholder="prova@esempio.it" name="email" id="email" required>

                <label for="passwordEmail">Inserisci la password</label>
                <input type="password" placeholder="Esempio2!" name="passwordEmail" id="passwordEmail" required >
            </th>

            <th scope="col">
                <label for="numeroTelefono">Inserisci il numero di cellulare</label>
                <input type="tel" placeholder="3331234567" id="numeroTelefono" name="numeroTelefono" required>

                <label for="via">Inserisci la via</label>
                <input type="text" placeholder="Via Esempio 10" id="via" name="via" required>

                <label for="citta">Inserisci la citta'</label>
                <input type="text" placeholder="Roma" id="citta" name="citta" required>

                <label for="cap">Inserisci il cap</label>
                <input type="text" placeholder="00100" id="cap" name="cap">

                <label for="provincia">Inserisci la provincia</label>
                <input type="text" placeholder="RM" id="provincia" name="provincia" pattern="[A-Z]{2}" title="Inserire due caratteri in maiuscolo per la rappresentazione della provincia">

                <label for="nazione">Inserisci la nazione</label>
                <input type="text" placeholder="Italia" id="nazione" name="nazione">
            </th>
        </tr>
    </table>

    <!-- FIX: Rimosso onclick, ora è gestito dall'onsubmit del form -->
    <button type="submit" class="cart">Registrati</button>

    <p style="color: red; text-align: center; font-weight: bold; margin-top: 15px;">
        <c:out value="<%=x%>"/>
    </p>
</form>
<br>
</body>
</html>