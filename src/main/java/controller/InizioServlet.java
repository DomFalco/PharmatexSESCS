package controller;

import model.Prodotto;
import model.ProdottoDAO;
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

@WebServlet("/InizioServlet")
public class InizioServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(InizioServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String richiesta = request.getParameter("action");

            if (richiesta == null) {
                String valore = request.getParameter("valore");
                if (valore != null) {
                    if (!valore.matches("[a-zA-Z0-9\\s]+")) {
                        inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Parametro 'valore' non valido.");
                        return;
                    }

                    ArrayList<Prodotto> prodottiCategoriaAmministratore = (ArrayList<Prodotto>) ProdottoDAO.doRetriveByCategoria(valore);
                    request.setAttribute("CategorieProdotti", prodottiCategoriaAmministratore);
                    request.setAttribute("Categoria", valore);
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/amministratore/CategorieProdotti.jsp");
                    dispatcher.forward(request, response);
                    return;
                } else {
                    inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti.");
                    return;
                }
            }

            if (!richiesta.matches("[a-zA-Z0-9\\s]+")) {
                inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Parametro 'action' non valido.");
                return;
            }

            if (richiesta.equals("login")) {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Login.jsp");
                dispatcher.forward(request, response);
                return;
            }

            if (richiesta.equals("contatti")) {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Contatti.jsp");
                dispatcher.forward(request, response);
                return;
            }

            HttpSession session = request.getSession();
            session.setAttribute("filtri", richiesta);

            ArrayList<Prodotto> prodottiCategoria = (ArrayList<Prodotto>) ProdottoDAO.doRetriveByCategoria(richiesta);
            request.setAttribute(richiesta, prodottiCategoria);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp");
            dispatcher.forward(request, response);

        } catch (ServletException | IOException e) {
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'inoltro della richiesta.");
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