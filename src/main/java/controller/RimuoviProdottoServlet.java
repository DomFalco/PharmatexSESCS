package controller;

import model.Prodotto;
import model.ProdottoDAO;
import model.Utente;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "RimuoviProdottoServlet", value = "/RimuoviProdottoServlet")
public class RimuoviProdottoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RimuoviProdottoServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession sessione = request.getSession(false);
            if (sessione == null) {
                inviaErrore(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
                return;
            }
            Utente admin = (Utente) sessione.getAttribute("Amministratore");
            if (admin == null || !admin.isAmministratore()) {
                inviaErrore(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: privilegi insufficienti.");
                return;
            }

            Prodotto p = (Prodotto) sessione.getAttribute("idModificaPrezzo");
            if (p == null || p.getIdProdotto() == null) {
                inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Nessun prodotto selezionato per la rimozione.");
                return;
            }

            ProdottoDAO.cancellaProdotto(p.getIdProdotto());
            ArrayList<Prodotto> tuttiProdotti = (ArrayList<Prodotto>) ProdottoDAO.doRetriveAll();
            request.setAttribute("tuttiProdotti", tuttiProdotti);
            RequestDispatcher ds = request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp");
            ds.forward(request, response);

        } catch (ServletException | IOException e) {
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la rimozione del prodotto.");
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