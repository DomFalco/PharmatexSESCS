package Controller;

import Model.Prodotto;
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

@WebServlet("/CarrelloServlet")
public class CarrelloServlet extends HttpServlet {

    // ===== Costanti =====
    private static final String CART_LIST = "cart-list";
    private static final String QUANTITA_ARTICOLI = "quantitaArticoli";
    private static final String QUANTITA = "quantita";
    private static final String HOME_PAGE = "HomePage";
    private static final String LOGIN_JSP = "/WEB-INF/results/Login.jsp";
    private static final String ACTION = "action";
    private static final String RIMUOVI = "rimuovi";
    private static final String PROD = "prod";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    @SuppressWarnings({"unchecked", "java:S1989"})
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Utente u = (Utente) session.getAttribute("Utente");

        ArrayList<Prodotto> cartList;
        ArrayList<Integer> qList;

        if (u != null) {
            cartList = (ArrayList<Prodotto>) session.getAttribute(CART_LIST);
            qList = (ArrayList<Integer>) session.getAttribute(QUANTITA_ARTICOLI);
            Prodotto p = (Prodotto) session.getAttribute(PROD);

            // NPE documentata: se p è null, p.getIdProdotto() lancia NPE.
            // Il comportamento è preservato rispetto al codice originale.
            String id = p.getIdProdotto();

            if (cartList == null && qList == null) {
                // Primo inserimento: crea un nuovo carrello
                cartList = new ArrayList<>();
                qList = new ArrayList<>();
                cartList.add(p);
                qList.add(Integer.valueOf(request.getParameter(QUANTITA)));
                salvaCarrello(session, cartList, qList);
            } else {
                // Carrello già esistente: aggiorna quantità o aggiungi prodotto
                aggiornaQuantitaEsistente(request, cartList, qList, id);
            }
            forward(request, response, HOME_PAGE);
        } else {
            // Utente non loggato: liste vuote per il blocco "rimuovi" sottostante
            cartList = new ArrayList<>();
            qList = new ArrayList<>();
            forward(request, response, LOGIN_JSP);
        }

        // Blocco "rimuovi" (eseguito sempre dopo il blocco precedente)
        if (request.getParameter(ACTION).contains(RIMUOVI)) {
            rimuoviProdotto(request, session, cartList, qList);
            forward(request, response, HOME_PAGE);
        } else {
            forward(request, response, LOGIN_JSP);
        }
    }

    /**
     * Cerca il prodotto nel carrello: se esiste aggiorna la quantità,
     * altrimenti lo aggiunge al carrello.
     */
    @SuppressWarnings("java:S1989")
    private void aggiornaQuantitaEsistente(HttpServletRequest request,
                                           ArrayList<Prodotto> cartList,
                                           ArrayList<Integer> qList,
                                           String id) {
        Prodotto p = (Prodotto) request.getSession().getAttribute(PROD);
        String quantitaParam = request.getParameter(QUANTITA);
        boolean esiste = false;

        for (int i = 0; i < cartList.size(); i++) {
            if (cartList.get(i).getIdProdotto().equals(id) && quantitaParam != null) {
                esiste = true;
                int qval = qList.get(i);
                qval += Integer.valueOf(quantitaParam);
                qList.set(i, qval);
            }
        }

        if (!esiste && quantitaParam != null) {
            cartList.add(p);
            qList.add(Integer.valueOf(quantitaParam));
        }
    }

    /**
     * Rimuove dal carrello il prodotto il cui id è specificato nell'action
     * (formato atteso: "rimuovi<idProdotto>").
     */
    private void rimuoviProdotto(HttpServletRequest request, HttpSession session,
                                 ArrayList<Prodotto> cartList, ArrayList<Integer> qList) {
        String[] parti = request.getParameter(ACTION).split(RIMUOVI);
        String idDaRimuovere = parti[1];

        for (int i = 0; i < cartList.size(); i++) {
            if (cartList.get(i).getIdProdotto().equalsIgnoreCase(idDaRimuovere)) {
                cartList.remove(i);
                qList.remove(i);
                salvaCarrello(session, cartList, qList);
            }
        }
    }

    /**
     * Salva il carrello e le quantità in sessione.
     */
    private void salvaCarrello(HttpSession session, ArrayList<Prodotto> cartList, ArrayList<Integer> qList) {
        session.setAttribute(CART_LIST, cartList);
        session.setAttribute(QUANTITA_ARTICOLI, qList);
    }

    /**
     * Forward verso la pagina specificata.
     */
    private void forward(HttpServletRequest request, HttpServletResponse response, String path)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        dispatcher.forward(request, response);
    }
}