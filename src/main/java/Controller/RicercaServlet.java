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

@WebServlet("/RicercaServlet")
public class RicercaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String nomeProdotto = request.getParameter("search");
            if (nomeProdotto == null) {
                nomeProdotto = "";
            }
            Prodotto p = null;
            String nProd = nomeProdotto.toUpperCase();
            HttpSession session = request.getSession();
            String address;

            p = ProdottoDAO.doRetriveBySearch(nProd);
            session.setAttribute("prod", p);

            if (nomeProdotto.equals("") || p == null || p.getNomeProd() == null) {
                address = "/WEB-INF/results/RicercaErrata.jsp";
            } else {
                address = "/WEB-INF/results/Prodotto.jsp";
            }
            request.setAttribute("prodotto", p);
            RequestDispatcher ds = request.getRequestDispatcher(address);
            ds.forward(request, response);

        } catch (ServletException | IOException e) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la ricerca del prodotto.");
            } catch (IOException ex) {
                throw new ServletException("Impossibile inviare la risposta di errore", ex);
            }
        }
    }
}