package Controller;

import Model.Prodotto;
import Model.ProdottoDAO;
import Model.Utente;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/CercaProdottoPerModificaServlet")
public class CercaProdottoPerModificaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // ===== FIX 1: controllo autorizzazione admin =====
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
        // ===== FINE FIX 1 =====

        // ===== FIX 2: validazione parametro 'search' =====
        String x = request.getParameter("search");
        if (x == null || !x.matches("[a-zA-Z0-9\\s]+")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro 'search' non valido.");
            return;
        }
        // ===== FINE FIX 2 =====

        Prodotto pmod = ProdottoDAO.doRetriveBySearch(x);

        // ===== FIX 3: gestione prodotto non trovato =====
        if (pmod == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Prodotto non trovato.");
            return;
        }
        // ===== FINE FIX 3 =====

        request.setAttribute("prodottoModifica", pmod);
        RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/ModificaProdotto.jsp");
        ds.forward(request, response);
    }
}