package Controller;

import Model.Prodotto;
import Model.ProdottoDAO;
import Model.Utente;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

@WebServlet("/HomePage")
public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter("valore") == null) {
            int min_val = 5;
            int max_val = 48;
            Random rand = new Random();
            int randomNum = min_val + rand.nextInt((max_val - min_val) + 1);
            request.setAttribute("Valore", randomNum);
            ArrayList<Prodotto> prodotti = ProdottoDAO.doRetriveAll();
            request.setAttribute("prodotti", prodotti);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/HomePage.jsp");
            dispatcher.forward(request, response);
        }
        else if (request.getParameter("valore").equals("home")) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
                return;
            }
            Utente admin = (Utente) session.getAttribute("Amministratore");
            if (admin == null || !admin.isAmministratore()) {
                ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti.");
                return;
            }
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp");
            dispatcher.forward(request, response);
        }
        else {
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Azione non riconosciuta.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}