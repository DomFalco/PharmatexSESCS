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
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/RicercaServlet")
public class RicercaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RicercaServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String nomeProdotto = request.getParameter("search");
            if (nomeProdotto == null) {
                nomeProdotto = "";
            }
            Prodotto p;
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
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la ricerca del prodotto.");
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