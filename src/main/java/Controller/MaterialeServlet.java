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

@WebServlet("/MaterialeServlet")
public class MaterialeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        // FIX 1: controllo sessione
        if (session == null) {
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp");
            ds.forward(request, response);
            return;
        }

        String mat = (String) session.getAttribute("mat");
        String materiale = (String) session.getAttribute("materiale");

        // FIX 2: controllo parametri null (evita NPE e doppio forward)
        if (mat == null || materiale == null) {
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp");
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

        // FIX 3: se non ci sono prodotti, forward a RicercaErrata e RETURN
        if (prodottiMateriale.isEmpty()) {
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp");
            ds.forward(request, response);
            return;
        }

        request.setAttribute("prodottiMateriale", prodottiMateriale);
        RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/results/ProdottiMateriale.jsp");
        ds.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}