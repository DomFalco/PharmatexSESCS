package Controller;

import Model.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet("/HomeServletAmministratore")
public class HomeServletAmministratore extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // ===== FIX: controllo autorizzazione amministratore =====
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
            return;
        }
        Utente admin = (Utente) session.getAttribute("Amministratore");
        if (admin == null || !admin.isAmministratore()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti.");
            return;
        }
        // ===== FINE FIX =====

        if(request.getParameter("valore")==null)
        {
            ArrayList<Prodotto> tuttiProdotti = ProdottoDAO.doRetriveAll();
            request.setAttribute("tuttiProdotti", tuttiProdotti);
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp");
            ds.forward(request, response);
        }
        else if(request.getParameter("valore").equals("quantita"))
        {
            ArrayList<Prodotto> prodottiEsauriti=ProdottoDAO.doRetriveQuantitaEsaurita();
            request.setAttribute("prodottiEsauriti",prodottiEsauriti);
            RequestDispatcher ds=request.getRequestDispatcher("/WEB-INF/amministratore/QuantitaEsaurita.jsp");
            ds.forward(request,response);
        } else if(request.getParameter("valore").equals("ordine"))
        {
            ArrayList<AcquistoProdotti> riepilogoProdotti= AcquistoProdottiDAO.doRetriveAcquisto();
            request.setAttribute("riepilogoProdotti",riepilogoProdotti);
            RequestDispatcher ds=request.getRequestDispatcher("/WEB-INF/amministratore/RiepilogoOrdini.jsp");
            ds.forward(request,response);
        }
        else if(request.getParameter("valore").equals("clienti"))
        {
            ArrayList<Utente> riepilogoUtente= UtenteDAO.doRetriveUtente();
            request.setAttribute("riepilogoUtente",riepilogoUtente);
            RequestDispatcher ds=request.getRequestDispatcher("/WEB-INF/amministratore/VisualizzaUtenti.jsp");
            ds.forward(request,response);
        }
        else if(request.getParameter("valore").equals("aggiungi"))
        {
            RequestDispatcher ds=request.getRequestDispatcher("/WEB-INF/amministratore/AggiungiNuovoProdotto.jsp");
            ds.forward(request,response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
}