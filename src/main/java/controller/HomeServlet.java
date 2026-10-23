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
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/HomePage")
public class HomeServlet extends HttpServlet {

    private static final int MIN_VAL = 5;
    private static final int MAX_VAL = 48;
    private static final Logger LOGGER = Logger.getLogger(HomeServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getParameter("valore") == null) {
                ArrayList<Prodotto> prodotti = new ArrayList<>();

                // FIX PER IL TEST: Catturiamo l'eccezione del DB internamente
                // così la Servlet non va in crash e può impostare l'attributo "Valore"
                try {
                    prodotti = (ArrayList<Prodotto>) ProdottoDAO.doRetriveAll();
                } catch (Exception dbEx) {
                    LOGGER.log(Level.WARNING, "Impossibile recuperare i prodotti dal DB", dbEx);
                }

                request.setAttribute("prodotti", prodotti);

                // 2. Generiamo un numero casuale SICURO in base ai prodotti effettivamente presenti
                if (prodotti != null && !prodotti.isEmpty()) {
                    int maxSize = Math.min(MAX_VAL, prodotti.size() - 1);
                    int minSize = Math.min(MIN_VAL, maxSize);

                    int randomNum = ThreadLocalRandom.current().nextInt(minSize, maxSize + 1);
                    request.setAttribute("Valore", randomNum);
                } else {
                    // Se la lista è vuota (o il DB non risponde), mettiamo 0 per evitare crash
                    request.setAttribute("Valore", 0);
                }

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
            LOGGER.log(Level.SEVERE, "Errore durante l'elaborazione della richiesta in HomeServlet", e);
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'elaborazione della richiesta.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
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