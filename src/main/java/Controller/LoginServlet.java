package Controller;

import Model.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    // ===== Costanti =====
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_EMAIL = "Email";
    private static final String PARAM_PASSWORD = "Password";
    private static final String PARAM_PARAMETRI = "parametri";

    private static final String ACTION_LOGOUT = "logout";
    private static final String ACTION_CARRELLO = "carrello";
    private static final String ACTION_RIEPILOGO = "riepilogo";

    private static final String ATTR_UTENTE = "Utente";
    private static final String ATTR_AMMINISTRATORE = "Amministratore";
    private static final String ATTR_RIEPILOGO_ORDINE_UTENTE = "riepiloOrdineUtente";

    private static final String MSG_CREDENZIALI_ERRATE = "Email o password errati!";

    private static final String LOGIN_JSP = "/WEB-INF/results/Login.jsp";
    private static final String CARRELLO_JSP = "/WEB-INF/results/Carrello.jsp";
    private static final String RIEPILOGO_ACQUISTI_JSP = "/WEB-INF/results/RiepilogoAcquisti.jsp";
    private static final String HOME_PAGE = "HomePage";
    private static final String HOME_SERVLET_AMMINISTRATORE = "HomeServletAmministratore";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Utente u = UtenteDAO.doLogin(
                request.getParameter(PARAM_EMAIL),
                request.getParameter(PARAM_PASSWORD));

        String action = request.getParameter(PARAM_ACTION);

        if (action == null) {
            // Login: nessuna action specificata
            if (u == null) {
                request.setAttribute(PARAM_PARAMETRI, MSG_CREDENZIALI_ERRATE);
                RequestDispatcher rs = request.getRequestDispatcher(LOGIN_JSP);
                rs.include(request, response);
            } else if (!u.isAmministratore()) {
                session.setAttribute(ATTR_UTENTE, u);
                RequestDispatcher ds = request.getRequestDispatcher(HOME_PAGE);
                ds.forward(request, response);
            } else {
                session.setAttribute(ATTR_AMMINISTRATORE, u);
                RequestDispatcher ds = request.getRequestDispatcher(HOME_SERVLET_AMMINISTRATORE);
                ds.forward(request, response);
            }
        } else if (action.equals(ACTION_LOGOUT)) {
            session.invalidate();
            RequestDispatcher dispatcher = request.getRequestDispatcher(HOME_PAGE);
            dispatcher.forward(request, response);
        } else if (action.equals(ACTION_CARRELLO)) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(CARRELLO_JSP);
            dispatcher.forward(request, response);
        } else if (action.equals(ACTION_RIEPILOGO)) {
            Utente utente = (Utente) session.getAttribute(ATTR_UTENTE);
            ArrayList<AcquistoProdotti> riepilogoProdotti =
                    AcquistoProdottiDAO.doRetriveAcquistoUtente(utente.getEmail());
            request.setAttribute(ATTR_RIEPILOGO_ORDINE_UTENTE, riepilogoProdotti);
            RequestDispatcher dispatcher = request.getRequestDispatcher(RIEPILOGO_ACQUISTI_JSP);
            dispatcher.forward(request, response);
        }
        // Nota: se action non è null e non matcha nessun valore previsto,
        // la Servlet non fa nulla (silent drop, comportamento documentato).
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}