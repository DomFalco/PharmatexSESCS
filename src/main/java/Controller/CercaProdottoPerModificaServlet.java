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
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
                return;
            }
            Utente admin = (Utente) session.getAttribute("Amministratore");
            if (admin == null || !admin.isAmministratore()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti.");
                return;
            }

            String x = request.getParameter("search");
            if (x == null || !x.matches("[a-zA-Z0-9\\s]+")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro 'search' non valido.");
                return;
            }

            Prodotto pmod = ProdottoDAO.doRetriveBySearch(x);
            if (pmod == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Prodotto non trovato.");
                return;
            }

            request.setAttribute("prodottoModifica", pmod);
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/ModificaProdotto.jsp");
            ds.forward(request, response);

        } catch (ServletException | IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la visualizzazione del prodotto.");
        }
    }
}