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
        try {
            HttpSession sessione = request.getSession();
            Prodotto p = (Prodotto) sessione.getAttribute(ATTR_ID_MODIFICA_PREZZO);

            if (p == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nessun prodotto selezionato.");
                return;
            }

            String nuovoPrezzo = request.getParameter(PARAM_NUOVO_PREZZO);
            String quantitaTotale = request.getParameter(PARAM_QUANTITA_TOTALE);

            if (nuovoPrezzo == null || quantitaTotale == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti.");
                return;
            }

            if (!nuovoPrezzo.equals("") && !quantitaTotale.equals("")) {
                double prezzo = Double.parseDouble(nuovoPrezzo);
                ProdottoDAO.doSetNewPrezzo(prezzo, p.getIdProdotto());
                int q = Integer.parseInt(quantitaTotale);
                int quantita = p.getQuantita() + q;
                ProdottoDAO.doUpdateQuantita(quantita, p.getIdProdotto());
                forward(request, response);
            } else if (!nuovoPrezzo.equals("")) {
                double prezzo = Double.parseDouble(nuovoPrezzo);
                ProdottoDAO.doSetNewPrezzo(prezzo, p.getIdProdotto());
                forward(request, response);
            } else if (!quantitaTotale.equals("")) {
                int q = Integer.parseInt(quantitaTotale);
                int quantita = p.getQuantita() + q;
                ProdottoDAO.doUpdateQuantita(quantita, p.getIdProdotto());
                forward(request, response);
            }
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato numerico non valido per prezzo o quantità.");
        } catch (ServletException | IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante la modifica del prodotto.");
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher ds = request.getRequestDispatcher(HOME_SERVLET_AMMINISTRATORE);
        ds.forward(request, response);
    }
}