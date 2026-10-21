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

        try {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Pagamento.jsp");
            dispatcher.forward(request, response);
        } catch (ServletException | IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il caricamento della pagina di pagamento.");
        }
    }
}