<%@ page import="model.Prodotto" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <link rel="stylesheet" type="text/css" href="ParteCSS/styleSito.css">
    <link rel="stylesheet" type="text/css" href="ParteCSS/Login.css">
    <title>Login Utente</title>
    <% String x=" ";
        if(request.getAttribute("parametri")!=null) {
            x="Email o password errati!";
        }%>
</head>
<body>
<img src="immagini/logosito.png" class="sfondo" alt="Logo Pharmatex">

<form action="LoginServlet" method="post" onsubmit="return confermaParametri();">
    <table>
        <tr>
            <th scope="col">
                <!-- Le label ora prendono il grassetto direttamente dal CSS -->
                <label for="email">Inserisci l'email</label>
                <input type="email" placeholder="Inserisci l'email" name="Email" id="email" required>
                <br>

                <label for="password">Inserisci la password</label>
                <input type="password" placeholder="Inserisci la password" name="Password" id="password" required>
                <br>

                <!-- Pulsante Login -->
                <button type="submit" id="submit" class="cart" aria-label="Effettua il login">Login</button>

                <!-- Messaggio di errore sicuro (Fix Snyk) -->
                <p style="color: red; text-align: center; margin-top: 15px; font-weight: bold;">
                    <c:out value="<%=x%>"/>
                </p>
            </th>
        </tr>
        <tr>
            <th scope="col">
                <a href="Registrazione" style="text-decoration: none;">
                    <button type="button" class="cart" aria-label="Vai alla registrazione">Registrati</button>
                </a>
            </th>
        </tr>
    </table>
</form>

<script>
    function confermaParametri() {
        var email = document.getElementById('email').value;
        var password = document.getElementById('password').value;
        var emailRGX = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
        var resultEmail = emailRGX.test(email);
        var passwordRGX = /^[a-zA-Z0-9!@#$%^&*]*$/;
        var resultPassword = passwordRGX.test(password);

        if (password.length < 8) {
            alert("La password non rispetta il numero minimo di caratteri");
            return false;
        }
        if (resultEmail == false) {
            alert("L'email non rispecchia il formato corretto. Riprovare");
            return false;
        }
        if (resultPassword == false) {
            alert("La password non rispecchia il formato corretto. Riprovare");
            return false;
        }
        return true;
    }
</script>
</body>
</html>