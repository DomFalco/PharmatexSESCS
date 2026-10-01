/*package Controller;

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

@WebServlet("/InizioServlet")
public class InizioServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String richiesta = request.getParameter("action");
        if(richiesta==null)
        {
            if(request.getParameter("valore")!=null)
            {
                ArrayList<Prodotto> prodottiCategoriaAmministratore = new ArrayList<Prodotto>();
                prodottiCategoriaAmministratore = ProdottoDAO.doRetriveByCategoria(request.getParameter("valore"));
                request.setAttribute("CategorieProdotti",prodottiCategoriaAmministratore);
                request.setAttribute("Categoria",request.getParameter("valore"));
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/amministratore/CategorieProdotti.jsp");
                dispatcher.forward(request, response);
            }
        }
        if(richiesta.equals("login"))
        {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Login.jsp");
            dispatcher.forward(request, response);
        }
        if(richiesta.equals("contatti")){
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Contatti.jsp");
            dispatcher.forward(request,response);
        }

        HttpSession session = request.getSession();
        session.setAttribute("filtri", richiesta);
        ArrayList<Prodotto> prodottiCategoria = new ArrayList<Prodotto>();
        prodottiCategoria = ProdottoDAO.doRetriveByCategoria(richiesta);
        request.setAttribute(richiesta, prodottiCategoria);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}*/

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

@WebServlet("/InizioServlet")
public class InizioServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String richiesta = request.getParameter("action");

        // 1. GESTIONE DEI PARAMETRI NULLI E VALIDAZIONE (Risolve NPE e Trust Boundary)
        if (richiesta == null) {
            String valore = request.getParameter("valore");
            if (valore != null) {
                // Validazione base anche per 'valore' (solo lettere, numeri e spazi)
                if (!valore.matches("[a-zA-Z0-9\\s]+")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro 'valore' non valido.");
                    return;
                }

                ArrayList<Prodotto> prodottiCategoriaAmministratore = new ArrayList<Prodotto>();
                prodottiCategoriaAmministratore = ProdottoDAO.doRetriveByCategoria(valore);
                request.setAttribute("CategorieProdotti", prodottiCategoriaAmministratore);
                request.setAttribute("Categoria", valore);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/amministratore/CategorieProdotti.jsp");
                dispatcher.forward(request, response);
                return; // FONDAMENTALE: Ferma l'esecuzione qui
            } else {
                // Se mancano entrambi i parametri, blocca la richiesta
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti.");
                return;
            }
        }

        // 2. VALIDAZIONE DI 'RICHIESTA'
        if (!richiesta.matches("[a-zA-Z0-9\\s]+")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro 'action' non valido.");
            return;
        }

        // 3. GESTIONE DELLE PAGINE STATICHE (Login e Contatti)
        if (richiesta.equals("login")) {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Login.jsp");
            dispatcher.forward(request, response);
            return; // FONDAMENTALE: Ferma l'esecuzione per non andare avanti
        }

        if (richiesta.equals("contatti")) {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Contatti.jsp");
            dispatcher.forward(request, response);
            return; // FONDAMENTALE
        }

        // 4. LOGICA PER LA CATEGORIA
        HttpSession session = request.getSession();
        session.setAttribute("filtri", richiesta); // Ora è sicuro salvarlo in sessione

        ArrayList<Prodotto> prodottiCategoria = new ArrayList<Prodotto>();
        prodottiCategoria = ProdottoDAO.doRetriveByCategoria(richiesta);
        request.setAttribute(richiesta, prodottiCategoria);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}