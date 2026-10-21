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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/FiltraggioServletPrezzo")
public class FiltraggioServletPrezzo extends HttpServlet {

    private static final String PARAM_PREZZO_MIN = "prezzomin";
    private static final String PARAM_PREZZO_MAX = "prezzomax";
    private static final String ATTR_FILTRI = "filtri";
    private static final String ATTR_FILTRA = "filtra";
    private static final String ATTR_FILTRAGGIO = "filtraggio";
    private static final String RICERCA_ERRATA_JSP = "/WEB-INF/results/RicercaErrata.jsp";
    private static final String PRODOTTI_JSP = "/WEB-INF/results/Prodotti.jsp";
    private static final double PREZZO_MAX_DEFAULT = 5000.0;
    private static final double PREZZO_MIN_DEFAULT = 0.0;

    private static final Logger LOGGER = Logger.getLogger(FiltraggioServletPrezzo.class.getName());

    @Override
    @SuppressWarnings("java:S1989")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession();
            String richiesta = (String) session.getAttribute(ATTR_FILTRI);
            List<Prodotto> filterList = null;

            String prezzoMin = request.getParameter(PARAM_PREZZO_MIN);
            String prezzoMax = request.getParameter(PARAM_PREZZO_MAX);

            if (prezzoMin == null) prezzoMin = "";
            if (prezzoMax == null) prezzoMax = "";

            if (prezzoMin.equals("") && prezzoMax.equals("")) {
                RequestDispatcher dispatcher = request.getRequestDispatcher(RICERCA_ERRATA_JSP);
                dispatcher.forward(request, response);
                return;
            } else if (prezzoMin.equals("")) {
                double valoreMax = Double.parseDouble(prezzoMax);
                filterList = ProdottoDAO.doRetriveByFilter(richiesta, valoreMax, PREZZO_MIN_DEFAULT);
            } else if (prezzoMax.equals("")) {
                double valoreMin = Double.parseDouble(prezzoMin);
                filterList = ProdottoDAO.doRetriveByFilter(richiesta, PREZZO_MAX_DEFAULT, valoreMin);
            } else {
                double valoreMin = Double.parseDouble(prezzoMin);
                double valoreMax = Double.parseDouble(prezzoMax);
                filterList = ProdottoDAO.doRetriveByFilter(richiesta, valoreMax, valoreMin);
            }

            request.setAttribute(ATTR_FILTRA, filterList);
            request.setAttribute(ATTR_FILTRAGGIO, richiesta);

            RequestDispatcher dispatcher = request.getRequestDispatcher(PRODOTTI_JSP);
            dispatcher.forward(request, response);

        } catch (NumberFormatException e) {
            inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Il prezzo deve essere un valore numerico valido.");
        } catch (ServletException | IOException e) {
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il filtraggio per prezzo.");
        }
    }

    private void inviaErrore(HttpServletResponse response, int status, String messaggio) {
        try {
            response.sendError(status, messaggio);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore", e);
        }
    }
}