package controller;

import model.Utente;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/PagamentoServlet")
public class PagamentoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PagamentoServlet.class.getName());

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
            Utente utente = (Utente) session.getAttribute("Utente");
            if (utente == null) {
                inviaErrore(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: utente non autenticato.");
                return;
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Pagamento.jsp");
            dispatcher.forward(request, response);
        } catch (ServletException | IOException e) {
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il caricamento della pagina di pagamento.");
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