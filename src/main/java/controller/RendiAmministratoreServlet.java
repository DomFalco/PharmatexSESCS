package controller;

import model.Utente;
import model.UtenteDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "RendiAmministratoreServlet", value = "/RendiAmministratoreServlet")
public class RendiAmministratoreServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RendiAmministratoreServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                inviaErrore(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
                return;
            }
            Utente admin = (Utente) session.getAttribute("Amministratore");
            if (admin == null || !admin.isAmministratore()) {
                inviaErrore(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti.");
                return;
            }

            String action = request.getParameter("action");
            if (action == null) {
                inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Parametro 'action' mancante.");
                return;
            }

            if (action.startsWith("amministratore")) {
                String val = action.substring("amministratore".length());
                if (val.isEmpty()) {
                    inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Email utente mancante.");
                    return;
                }
                UtenteDAO.rendiAmministratore(val);
                ArrayList<Utente> u = (ArrayList<Utente>) UtenteDAO.doRetriveUtente();
                request.setAttribute("riepilogoUtente", u);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/amministratore/VisualizzaUtenti.jsp");
                dispatcher.forward(request, response);
            }
            else if (action.startsWith("rimuovipermessi")) {
                String val = action.substring("rimuovipermessi".length());
                if (val.isEmpty()) {
                    inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Email utente mancante.");
                    return;
                }
                UtenteDAO.rimuoviAmministratore(val);
                ArrayList<Utente> u = (ArrayList<Utente>) UtenteDAO.doRetriveUtente();
                request.setAttribute("riepilogoUtente", u);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/amministratore/VisualizzaUtenti.jsp");
                dispatcher.forward(request, response);
            }
            else {
                inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Azione non riconosciuta.");
            }
        } catch (ServletException | IOException e) {
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'aggiornamento dei permessi.");
        }
    }

    private void inviaErrore(HttpServletResponse response, int status, String messaggio) {
        try {
            response.sendError(status, messaggio);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore", e);
        }
    }
}