<%@ page import="java.util.ArrayList" %>
<%@ page import="model.Utente" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/ParteHTML/navBarAmministratore.jsp" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <title>Riepilogo utenti</title>
    <link rel="stylesheet" type="text/css" href="ParteCSS/VisualizzaUtenti.css">
</head>
<body>
<% ArrayList<Utente> riepilogoUtente= (ArrayList<Utente>) request.getAttribute("riepilogoUtente");
    Utente utente = (Utente) session.getAttribute("Amministratore"); %>
<table>
    <tr>
        <th scope="col">Nome utente</th>
        <th scope="col">Cognome utente</th>
        <th scope="col">Email utente</th>
        <th scope="col">Amministratore</th>
        <th scope="col">Aggiungi</th>
        <th scope="col">Rimuovi</th>
    </tr>
    <% for(Utente ut: riepilogoUtente){ %>
    <tr>
        <td><%=ut.getNome()%></td>
        <td><%=ut.getCognome()%></td>
        <td><%=ut.getEmail()%></td>
        <td><%=ut.isAmministratore()%></td>
        <% if(utente.getEmail().compareTo(ut.getEmail())!=0) {%>
        <td><button class="cart"><a style="text-decoration: none; color: white; text-underline: none" href="RendiAmministratoreServlet?action=amministratore<%=ut.getEmail()%>">Rendi Amministratore</a></button></td>
        <td><button class="cart"><a style="text-decoration: none; color: white; text-underline: none" href="RendiAmministratoreServlet?action=rimuovipermessi<%=ut.getEmail()%>">Rimuovi Amministratore</a></button></td>
        <%}%>
    </tr>
    <%}%>
</table>
</body>
</html>