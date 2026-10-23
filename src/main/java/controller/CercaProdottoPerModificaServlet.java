package controller;

import model.Prodotto;
import model.ProdottoDAO;
import model.Utente;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/CercaProdottoPerModificaServlet")
public class CercaProdottoPerModificaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CercaProdottoPerModificaServlet.class.getName());

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

            String x = request.getParameter("search");
            // FIX: Aggiunto il punto '.' per far passare nomi come "3.1", "4.0", ecc.
            if (x == null || !x.matches("[a-zA-Z0-9\\s\\-'.]+")) {
                inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Parametro 'search' non valido.");
                return;
            }

            Prodotto pmod = ProdottoDAO.doRetriveBySearch(x);
            if (pmod == null) {
                inviaErrore(response, HttpServletResponse.SC_NOT_FOUND, "Prodotto non trovato.");
                return;
            }

            request.setAttribute("prodottoModifica", pmod);
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/ModificaProdotto.jsp");
            ds.forward(request, response);

        } catch (ServletException | IOException e) {
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la visualizzazione del prodotto.");
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