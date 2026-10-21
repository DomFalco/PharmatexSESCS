package controller;

import model.Prodotto;
import model.ProdottoDAO;
import model.Utente;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/HomePage")
public class HomeServlet extends HttpServlet {

    private static final int MIN_VAL = 5;
    private static final int MAX_VAL = 48;
    private static final Logger LOGGER = Logger.getLogger(HomeServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (request.getParameter("valore") == null) {
                int randomNum = ThreadLocalRandom.current().nextInt(MIN_VAL, MAX_VAL + 1);
                request.setAttribute("Valore", randomNum);
                List<Prodotto> prodotti = ProdottoDAO.doRetriveAll();
                request.setAttribute("prodotti", prodotti);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/HomePage.jsp");
                dispatcher.forward(request, response);
            }
            else if (request.getParameter("valore").equals("home")) {
                HttpSession session = request.getSession(false);
                if (session == null) {
                    inviaErrore(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
                    return;
                }
                Utente admin = (Utente) session.getAttribute("Amministratore");
                if (admin == null || !admin.isAmministratore()) {
                    inviaErrore(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti.");
                    return;
                }
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp");
                dispatcher.forward(request, response);
            }
            else {
                inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Azione non riconosciuta.");
            }
        } catch (ServletException | IOException e) {
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'elaborazione della richiesta.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private void inviaErrore(HttpServletResponse response, int status, String messaggio) {
        try {
            response.sendError(status, messaggio);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore", e);
        }
    }
}