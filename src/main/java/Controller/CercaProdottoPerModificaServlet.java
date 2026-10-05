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
        HttpSession session = request.getSession(false);
        if (session == null) {
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
            return;
        }
        Utente admin = (Utente) session.getAttribute("Amministratore");
        if (admin == null || !admin.isAmministratore()) {
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti.");
            return;
        }

        String x = request.getParameter("search");
        if (x == null || !x.matches("[a-zA-Z0-9\\s]+")) {
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Parametro 'search' non valido.");
            return;
        }

        Prodotto pmod = ProdottoDAO.doRetriveBySearch(x);
        if (pmod == null) {
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Prodotto non trovato.");
            return;
        }

        request.setAttribute("prodottoModifica", pmod);
        RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/ModificaProdotto.jsp");
        ds.forward(request, response);
    }
}