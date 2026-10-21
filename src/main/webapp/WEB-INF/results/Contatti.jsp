<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <title>Contatti</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <link rel="stylesheet" type="text/css" href="ParteCSS/Contatti.css">
    <script>
        $(document).ready(function(){
            $("#1").mouseenter(function () { $("#1").css("color","#058c42"); });
            $("#1").mouseleave(function(){ $("#1").css("color", "black"); });
        });
        $(document).ready(function(){
            $("#2").mouseenter(function () { $("#2").css("color","#058c42"); });
            $("#2").mouseleave(function(){ $("#2").css("color", "black"); });
        });
        $(document).ready(function(){
            $("#3").mouseenter(function () { $("#3").css("color","#058c42"); });
            $("#3").mouseleave(function(){ $("#3").css("color", "black"); });
        });
        $(document).ready(function(){
            $("#4").mouseenter(function () { $("#4").css("color","#058c42"); });
            $("#4").mouseleave(function(){ $("#4").css("color", "black"); });
        });
        $(document).ready(function(){
            $("#5").mouseenter(function () { $("#5").css("color","#058c42"); });
            $("#5").mouseleave(function(){ $("#5").css("color", "black"); });
        });
        $(document).ready(function(){
            $("#5").css("display","none");
            $("#5").css("color","transparent");
        });
        $(document).ready(function(){
            $("#chiSiamo").click(function(){
                $("#5").slideToggle("slow");
                $("#5").css("color","black");
                $("#5").css("font-weight","bold");
            });
        });
    </script>
</head>
<body>
<img src="immagini/pharmatex.jpg" class="contatti" alt="Immagine contatti Pharmatex">
<table>
    <tr>
        <th scope="col" style="font-size: 2vw">Indirizzo:</th>
        <th scope="col" style="font-size: 2vw">Contatti Telefonici:</th>
        <th scope="col" style="font-size: 2vw">E-mail:</th>
        <th scope="col" style="font-size: 2vw">Orario:</th>
    </tr>
    <tr>
        <!-- FIX SONARQUBE: I dati non devono usare <th>, ma <td> -->
        <td><p id="1">Via casa dell'acqua n.20 <br> 80013 Casalnuovo di Napoli (NA) <br> loc. Tavernanova</p></td>
        <td><p id="2">Cellulare:3477386676<br>Fisso:0813184751</p></td>
        <td><p id="3">pharmatex.materassi@libero.it</p></td>
        <td><p id="4">Lun - Ven 09:30 - 18:00 <br> Sabato 09:00 - 13:00 <br> Domenica chiuso</p></td>
        <td><button id="chiSiamo" class="cart">Chi siamo</button></td>
        <td><button class="cart" onclick="window.location.href='HomePage'">HomePage</button></td>
    </tr>
</table>
<p id="5">Ci occupiamo della vendita di materassi Pharmatex a Casalnuovo, in provincia di Napoli.<br> Si tratta di un prodotto artigianale che può vantare oltre 50 anni di esperienza nel settore per offrire a chiunque l'opportunità di avere un riposo notturno adeguato, per recuperare le energie fisiche e mentali per una nuova giornata di lavoro.<br>
    Nel nostro servizio di vendita materassi offriamo al cliente un'ampia gamma di soluzioni realizzate con specifiche caratteristiche e con un ottimo rapporto qualità-prezzo.<br>
    Potrete valutare i nostri materassi in memory foam che si adattano alla perfezione alle forme del corpo o scegliere il materasso con molle ortopedico. <br> In funzione delle vostre caratteristiche fisiche e della vostra abitudine nel dormire, sarete opportunamente consigliati sulla tipologia più adatta.<br>
    Per maggiori informazioni sul nostro servizio di vendita materassi e per gli altri accessori indispensabili per il riposo notturno, come toghe e reti rinforzate, contattateci utilizzando il numero di telefono indicato sul sito.
</p>
</body>
</html>