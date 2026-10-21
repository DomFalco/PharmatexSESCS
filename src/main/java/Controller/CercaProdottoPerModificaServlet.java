package Controller;

import Model.Prodotto;
import Model.ProdottoDAO;
import Model.Utente;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet("/CercaProdottoPerModificaServlet")
public class CercaProdottoPerModificaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                try {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
                } catch (IOException ex) {
                    throw new ServletException("Impossibile inviare la risposta di errore", ex);
                }
                return;
            }
            Utente admin = (Utente) session.getAttribute("Amministratore");
            if (admin == null || !admin.isAmministratore()) {
                try {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti.");
                } catch (IOException ex) {
                    throw new ServletException("Impossibile inviare la risposta di errore", ex);
                }
                return;
            }

            String x = request.getParameter("search");
            if (x == null || !x.matches("[a-zA-Z0-9\\s]+")) {
                try {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro 'search' non valido.");
                } catch (IOException ex) {
                    throw new ServletException("Impossibile inviare la risposta di errore", ex);
                }
                return;
            }

            Prodotto pmod = ProdottoDAO.doRetriveBySearch(x);
            if (pmod == null) {
                try {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Prodotto non trovato.");
                } catch (IOException ex) {
                    throw new ServletException("Impossibile inviare la risposta di errore", ex);
                }
                return;
            }

            request.setAttribute("prodottoModifica", pmod);
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/ModificaProdotto.jsp");
            ds.forward(request, response);

        } catch (ServletException | IOException e) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la visualizzazione del prodotto.");
            } catch (IOException ex) {
                throw new ServletException("Impossibile inviare la risposta di errore", ex);
            }
        }
    }
}