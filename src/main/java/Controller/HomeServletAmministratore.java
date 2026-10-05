package Controller;

import Model.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/HomeServletAmministratore")
public class HomeServletAmministratore extends HttpServlet {

    private static final String PARAM_VALORE = "valore";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

        if (request.getParameter(PARAM_VALORE) == null) {
            ArrayList<Prodotto> tuttiProdotti = ProdottoDAO.doRetriveAll();
            request.setAttribute("tuttiProdotti", tuttiProdotti);
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp");
            ds.forward(request, response);
        }
        else if (request.getParameter(PARAM_VALORE).equals("quantita")) {
            ArrayList<Prodotto> prodottiEsauriti = ProdottoDAO.doRetriveQuantitaEsaurita();
            request.setAttribute("prodottiEsauriti", prodottiEsauriti);
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/QuantitaEsaurita.jsp");
            ds.forward(request, response);
        }
        else if (request.getParameter(PARAM_VALORE).equals("ordine")) {
            ArrayList<AcquistoProdotti> riepilogoProdotti = AcquistoProdottiDAO.doRetriveAcquisto();
            request.setAttribute("riepilogoProdotti", riepilogoProdotti);
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/RiepilogoOrdini.jsp");
            ds.forward(request, response);
        }
        else if (request.getParameter(PARAM_VALORE).equals("clienti")) {
            ArrayList<Utente> riepilogoUtente = UtenteDAO.doRetriveUtente();
            request.setAttribute("riepilogoUtente", riepilogoUtente);
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/VisualizzaUtenti.jsp");
            ds.forward(request, response);
        }
        else if (request.getParameter(PARAM_VALORE).equals("aggiungi")) {
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/AggiungiNuovoProdotto.jsp");
            ds.forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}