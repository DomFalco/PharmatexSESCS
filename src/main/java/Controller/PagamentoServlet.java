package Controller;

import Model.Utente;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet("/PagamentoServlet")
public class PagamentoServlet extends HttpServlet {
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
        Utente utente = (Utente) session.getAttribute("Utente");
        if (utente == null) {
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: utente non autenticato.");
            return;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Pagamento.jsp");
        dispatcher.forward(request, response);
    }
}