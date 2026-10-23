package controller;

import model.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

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

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession();
            String email = request.getParameter(PARAM_EMAIL);
            String password = request.getParameter(PARAM_PASSWORD);

            Utente u = UtenteDAO.doLogin(email, password);

            String action = request.getParameter(PARAM_ACTION);

            if (action == null) {
                if (u == null) {
                    request.setAttribute(PARAM_PARAMETRI, MSG_CREDENZIALI_ERRATE);
                    RequestDispatcher rs = request.getRequestDispatcher(LOGIN_JSP);
                    rs.forward(request, response); // Meglio forward che include
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

                // PROTEZIONE: Se non sei loggato, non cercare l'email (evita NullPointerException)
                if (utente == null) {
                    request.setAttribute(PARAM_PARAMETRI, "Devi effettuare il login per vedere il riepilogo.");
                    RequestDispatcher dispatcher = request.getRequestDispatcher(LOGIN_JSP);
                    dispatcher.forward(request, response);
                    return;
                }

                ArrayList<AcquistoProdotti> riepilogoProdotti = (ArrayList<AcquistoProdotti>) AcquistoProdottiDAO.doRetriveAcquistoUtente(utente.getEmail());
                request.setAttribute(ATTR_RIEPILOGO_ORDINE_UTENTE, riepilogoProdotti);
                RequestDispatcher dispatcher = request.getRequestDispatcher(RIEPILOGO_ACQUISTI_JSP);
                dispatcher.forward(request, response);
            }
        } catch (ServletException | IOException | RuntimeException e) {
            e.printStackTrace(); // FONDAMENTALE: Stampa l'errore vero nella console di Tomcat
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'elaborazione del login.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }

    private void inviaErrore(HttpServletResponse response, int status, String messaggio) {
        try {
            response.sendError(status, messaggio);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore", e);
        }
    }
}