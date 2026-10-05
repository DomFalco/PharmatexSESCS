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
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
            return;
        }
        Utente admin = (Utente) session.getAttribute("Amministratore");
        if (admin == null || !admin.isAmministratore()) {
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Parametro 'action' mancante.");
            return;
        }

        if (action.startsWith("amministratore")) {
            String val = action.substring("amministratore".length());
            if (val.isEmpty()) {
                ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email utente mancante.");
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
                ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email utente mancante.");
                return;
            }
            UtenteDAO.rimuoviAmministratore(val);
            ArrayList<Utente> u = UtenteDAO.doRetriveUtente();
            request.setAttribute("riepilogoUtente", u);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/amministratore/VisualizzaUtenti.jsp");
            dispatcher.forward(request, response);
        }
        else {
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Azione non riconosciuta.");
        }
    }
}