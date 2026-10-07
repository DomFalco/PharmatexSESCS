package Controller;

import Model.Prodotto;
import Model.ProdottoDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/FiltraggioServletPrezzo")
public class FiltraggioServletPrezzo extends HttpServlet {

    // ===== Costanti =====
    private static final String PARAM_PREZZO_MIN = "prezzomin";
    private static final String PARAM_PREZZO_MAX = "prezzomax";

    private static final String ATTR_FILTRI = "filtri";
    private static final String ATTR_FILTRA = "filtra";
    private static final String ATTR_FILTRAGGIO = "filtraggio";

    private static final String RICERCA_ERRATA_JSP = "/WEB-INF/results/RicercaErrata.jsp";
    private static final String PRODOTTI_JSP = "/WEB-INF/results/Prodotti.jsp";

    private static final double PREZZO_MAX_DEFAULT = 5000.0;
    private static final double PREZZO_MIN_DEFAULT = 0.0;

    @Override
    @SuppressWarnings("java:S1989")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String richiesta = (String) session.getAttribute(ATTR_FILTRI);
        ArrayList<Prodotto> filterList = null;

        String prezzoMin = request.getParameter(PARAM_PREZZO_MIN);
        String prezzoMax = request.getParameter(PARAM_PREZZO_MAX);

        if (prezzoMin.equals("") && prezzoMax.equals("")) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(RICERCA_ERRATA_JSP);
            dispatcher.forward(request, response);
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
    }
}