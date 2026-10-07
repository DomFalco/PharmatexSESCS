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

@WebServlet("/ModificaProdottiServletAmministratore")
public class ModificaProdottiServletAmministratore extends HttpServlet {

    // ===== Costanti =====
    private static final String PARAM_NUOVO_PREZZO = "nuovoPrezzo";
    private static final String PARAM_QUANTITA_TOTALE = "quantitaTotale";
    private static final String ATTR_ID_MODIFICA_PREZZO = "idModificaPrezzo";
    private static final String HOME_SERVLET_AMMINISTRATORE = "HomeServletAmministratore";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    @SuppressWarnings("java:S1989")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession sessione = request.getSession();
        Prodotto p = (Prodotto) sessione.getAttribute(ATTR_ID_MODIFICA_PREZZO);

        String nuovoPrezzo = request.getParameter(PARAM_NUOVO_PREZZO);
        String quantitaTotale = request.getParameter(PARAM_QUANTITA_TOTALE);

        if (!nuovoPrezzo.equals("") && !quantitaTotale.equals("")) {
            // Ramo 1: aggiorna sia prezzo che quantità (quantità incrementale)
            double prezzo = Double.parseDouble(nuovoPrezzo);
            ProdottoDAO.doSetNewPrezzo(prezzo, p.getIdProdotto());
            int q = Integer.parseInt(quantitaTotale);
            int quantita = p.getQuantita() + q;
            ProdottoDAO.doUpdateQuantita(quantita, p.getIdProdotto());
            forward(request, response);
        } else if (!nuovoPrezzo.equals("")) {
            // Ramo 2: aggiorna solo il prezzo
            double prezzo = Double.parseDouble(nuovoPrezzo);
            ProdottoDAO.doSetNewPrezzo(prezzo, p.getIdProdotto());
            forward(request, response);
        } else if (quantitaTotale != null) {
            // Ramo 3: aggiorna solo la quantità (quantità incrementale)
            int q = Integer.parseInt(quantitaTotale);
            int quantita = p.getQuantita() + q;
            ProdottoDAO.doUpdateQuantita(quantita, p.getIdProdotto());
            forward(request, response);
        }
        // Nota: se nuovoPrezzo="" e quantitaTotale=null, nessun ramo viene eseguito
        // (silent drop, comportamento documentato)
    }

    /**
     * Forward verso HomeServletAmministratore.
     */
    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher ds = request.getRequestDispatcher(HOME_SERVLET_AMMINISTRATORE);
        ds.forward(request, response);
    }
}