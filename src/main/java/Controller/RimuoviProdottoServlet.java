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

@WebServlet(name = "RimuoviProdottoServlet", value = "/RimuoviProdottoServlet")
public class RimuoviProdottoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // ===== FIX 1: controllo autorizzazione admin =====
        HttpSession sessione = request.getSession(false);
        if (sessione == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
            return;
        }
        Utente admin = (Utente) sessione.getAttribute("Amministratore");
        if (admin == null || !admin.isAmministratore()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti.");
            return;
        }
        // ===== FINE FIX 1 =====

        // ===== FIX 2: controllo prodotto in sessione =====
        Prodotto p = (Prodotto) sessione.getAttribute("idModificaPrezzo");
        if (p == null || p.getIdProdotto() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nessun prodotto selezionato per la rimozione.");
            return;
        }
        // ===== FINE FIX 2 =====

        ProdottoDAO.cancellaProdotto(p.getIdProdotto());
        ArrayList<Prodotto> tuttiProdotti = ProdottoDAO.doRetriveAll();
        request.setAttribute("tuttiProdotti", tuttiProdotti);
        RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp");
        ds.forward(request, response);
    }
}