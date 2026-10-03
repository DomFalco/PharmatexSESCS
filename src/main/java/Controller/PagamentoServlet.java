package Controller;

import Model.Utente;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/PagamentoServlet")
public class PagamentoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // ===== FIX: controllo autenticazione utente =====
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
            return;
        }
        Utente utente = (Utente) session.getAttribute("Utente");
        if (utente == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: utente non autenticato.");
            return;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Pagamento.jsp");
        dispatcher.forward(request, response);
    }
}