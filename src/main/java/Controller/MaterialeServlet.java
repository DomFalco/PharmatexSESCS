package Controller;

import Model.Prodotto;
import Model.ProdottoDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/MaterialeServlet")
public class MaterialeServlet extends HttpServlet {

    private static final String RICERCA_ERRATA_JSP = "/WEB-INF/results/RicercaErrata.jsp";
    private static final String PRODOTTI_MATERIALE_JSP = "/WEB-INF/results/ProdottiMateriale.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            prodottiMateriale = ProdottoDAO.doRetriveMaterialeMaterasso(materiale);
        } else if (mat.equalsIgnoreCase("rete")) {
            prodottiMateriale = ProdottoDAO.doRetriveMaterialeRete(materiale);
        } else if (mat.equalsIgnoreCase("cuscino")) {
            prodottiMateriale = ProdottoDAO.doRetriveMaterialeCuscino(materiale);
        }

        if (prodottiMateriale.isEmpty()) {
            RequestDispatcher ds = request.getRequestDispatcher(RICERCA_ERRATA_JSP);
            ds.forward(request, response);
            return;
        }

        request.setAttribute("prodottiMateriale", prodottiMateriale);
        RequestDispatcher ds = request.getRequestDispatcher(PRODOTTI_MATERIALE_JSP);
        ds.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}