package controller;

import model.Prodotto;
import model.ProdottoDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/MaterialeServlet")
public class MaterialeServlet extends HttpServlet {

    private static final String RICERCA_ERRATA_JSP = "/WEB-INF/results/RicercaErrata.jsp";
    private static final String PRODOTTI_MATERIALE_JSP = "/WEB-INF/results/ProdottiMateriale.jsp";
    private static final Logger LOGGER = Logger.getLogger(MaterialeServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession(false);

            if (session == null) {
                RequestDispatcher ds = request.getRequestDispatcher(RICERCA_ERRATA_JSP);
                ds.forward(request, response);
                return;
            }

            String mat = (String) session.getAttribute("mat");
            String materiale = (String) session.getAttribute("materiale");

            if (mat == null || materiale == null) {
                RequestDispatcher ds = request.getRequestDispatcher(RICERCA_ERRATA_JSP);
                ds.forward(request, response);
                return;
            }

            ArrayList<Prodotto> prodottiMateriale = new ArrayList<>();

            if (mat.equalsIgnoreCase("materasso")) {
                prodottiMateriale = (ArrayList<Prodotto>) ProdottoDAO.doRetriveMaterialeMaterasso(materiale);
            } else if (mat.equalsIgnoreCase("rete")) {
                prodottiMateriale = (ArrayList<Prodotto>) ProdottoDAO.doRetriveMaterialeRete(materiale);
            } else if (mat.equalsIgnoreCase("cuscino")) {
                prodottiMateriale = (ArrayList<Prodotto>) ProdottoDAO.doRetriveMaterialeCuscino(materiale);
            }

            if (prodottiMateriale.isEmpty()) {
                RequestDispatcher ds = request.getRequestDispatcher(RICERCA_ERRATA_JSP);
                ds.forward(request, response);
                return;
            }

            request.setAttribute("prodottiMateriale", prodottiMateriale);
            RequestDispatcher ds = request.getRequestDispatcher(PRODOTTI_MATERIALE_JSP);
            ds.forward(request, response);

        } catch (ServletException | IOException e) {
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il caricamento dei materiali.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request, response);
    }

    private void inviaErrore(HttpServletResponse response, int status, String messaggio) {
        try {
            response.sendError(status, messaggio);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore", e);
        }
    }
}