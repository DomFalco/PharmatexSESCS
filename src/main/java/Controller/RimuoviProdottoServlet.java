package Controller;

import Model.Prodotto;
import Model.ProdottoDAO;
import Model.Utente;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "RimuoviProdottoServlet", value = "/RimuoviProdottoServlet")
public class RimuoviProdottoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessione = request.getSession(false);
        if (sessione == null) {
            try { response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante."); } catch (IOException ex) { throw new ServletException(ex); }
            return;
        }
        Utente admin = (Utente) sessione.getAttribute("Amministratore");
        if (admin == null || !admin.isAmministratore()) {
            try { response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti."); } catch (IOException ex) { throw new ServletException(ex); }
            return;
        }

        Prodotto p = (Prodotto) sessione.getAttribute("idModificaPrezzo");
        if (p == null || p.getIdProdotto() == null) {
            try { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nessun prodotto selezionato per la rimozione."); } catch (IOException ex) { throw new ServletException(ex); }
            return;
        }

        try {
            ProdottoDAO.cancellaProdotto(p.getIdProdotto());
            ArrayList<Prodotto> tuttiProdotti = ProdottoDAO.doRetriveAll();
            request.setAttribute("tuttiProdotti", tuttiProdotti);
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp");
            ds.forward(request, response);
        } catch (ServletException | IOException e) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la rimozione del prodotto.");
            } catch (IOException ex) {
                throw new ServletException("Impossibile inviare la risposta di errore", ex);
            }
        }
    }
}