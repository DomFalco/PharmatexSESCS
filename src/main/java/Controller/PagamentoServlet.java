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
            try { response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante."); } catch (IOException ex) { throw new ServletException(ex); }
            return;
        }
        Utente utente = (Utente) session.getAttribute("Utente");
        if (utente == null) {
            try { response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: utente non autenticato."); } catch (IOException ex) { throw new ServletException(ex); }
            return;
        }

        try {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Pagamento.jsp");
            dispatcher.forward(request, response);
        } catch (ServletException | IOException e) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il caricamento della pagina di pagamento.");
            } catch (IOException ex) {
                throw new ServletException("Impossibile inviare la risposta di errore", ex);
            }
        }
    }
}