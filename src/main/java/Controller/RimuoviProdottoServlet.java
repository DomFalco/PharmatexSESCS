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
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
            return;
        }
        Utente admin = (Utente) sessione.getAttribute("Amministratore");
        if (admin == null || !admin.isAmministratore()) {
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti.");
            return;
        }

        Prodotto p = (Prodotto) sessione.getAttribute("idModificaPrezzo");
        if (p == null || p.getIdProdotto() == null) {
            ServletErrorHelper.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Nessun prodotto selezionato per la rimozione.");
            return;
        }

        ProdottoDAO.cancellaProdotto(p.getIdProdotto());
        ArrayList<Prodotto> tuttiProdotti = ProdottoDAO.doRetriveAll();
        request.setAttribute("tuttiProdotti", tuttiProdotti);
        RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp");
        ds.forward(request, response);
    }
}