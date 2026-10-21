package Controller;

import Model.Utente;
import Model.UtenteDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "RendiAmministratoreServlet", value = "/RendiAmministratoreServlet")
public class RendiAmministratoreServlet extends HttpServlet {
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
        Utente admin = (Utente) session.getAttribute("Amministratore");
        if (admin == null || !admin.isAmministratore()) {
            try { response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti."); } catch (IOException ex) { throw new ServletException(ex); }
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            try { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro 'action' mancante."); } catch (IOException ex) { throw new ServletException(ex); }
            return;
        }

        try {
            if (action.startsWith("amministratore")) {
                String val = action.substring("amministratore".length());
                if (val.isEmpty()) {
                    try { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email utente mancante."); } catch (IOException ex) { throw new ServletException(ex); }
                    return;
                }
                UtenteDAO.rendiAmministratore(val);
                ArrayList<Utente> u = UtenteDAO.doRetriveUtente();
                request.setAttribute("riepilogoUtente", u);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/amministratore/VisualizzaUtenti.jsp");
                dispatcher.forward(request, response);
            }
            else if (action.startsWith("rimuovipermessi")) {
                String val = action.substring("rimuovipermessi".length());
                if (val.isEmpty()) {
                    try { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email utente mancante."); } catch (IOException ex) { throw new ServletException(ex); }
                    return;
                }
                UtenteDAO.rimuoviAmministratore(val);
                ArrayList<Utente> u = UtenteDAO.doRetriveUtente();
                request.setAttribute("riepilogoUtente", u);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/amministratore/VisualizzaUtenti.jsp");
                dispatcher.forward(request, response);
            }
            else {
                try { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione non riconosciuta."); } catch (IOException ex) { throw new ServletException(ex); }
            }
        } catch (ServletException | IOException e) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'aggiornamento dei permessi.");
            } catch (IOException ex) {
                throw new ServletException("Impossibile inviare la risposta di errore", ex);
            }
        }
    }
}