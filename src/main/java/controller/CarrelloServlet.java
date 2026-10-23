package controller;

import model.Prodotto;
import model.Utente;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/CarrelloServlet")
public class CarrelloServlet extends HttpServlet {

    private static final String CART_LIST = "cart-list";
    private static final String QUANTITA_ARTICOLI = "quantitaArticoli";
    private static final String QUANTITA = "quantita";
    private static final String HOME_PAGE = "HomePage";
    private static final String LOGIN_JSP = "/WEB-INF/results/Login.jsp";
    private static final String ACTION = "action";
    private static final String RIMUOVI = "rimuovi";
    private static final String PROD = "prod";

    private static final Logger LOGGER = Logger.getLogger(CarrelloServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    @Override
    @SuppressWarnings({"unchecked", "java:S1989"})
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession();
            Utente u = (Utente) session.getAttribute("Utente");

            // 1. Se l'utente non è loggato, vai al login e FERMATI
            if (u == null) {
                forward(request, response, LOGIN_JSP);
                return;
            }

            // Recupera o crea le liste del carrello
            ArrayList<Prodotto> cartList = (ArrayList<Prodotto>) session.getAttribute(CART_LIST);
            ArrayList<Integer> qList = (ArrayList<Integer>) session.getAttribute(QUANTITA_ARTICOLI);

            if (cartList == null) cartList = new ArrayList<>();
            if (qList == null) qList = new ArrayList<>();

            String action = request.getParameter(ACTION);

            // 2. Gestione RIMOZIONE
            if (action != null && action.contains(RIMUOVI)) {
                rimuoviProdotto(request, session, cartList, qList);
                forward(request, response, HOME_PAGE);
                return; // FERMATI QUI
            }

            // 3. Gestione AGGIUNTA
            Prodotto p = (Prodotto) session.getAttribute(PROD);

            // Se non c'è nessun prodotto in sessione, torna alla home
            if (p == null) {
                forward(request, response, HOME_PAGE);
                return;
            }

            String id = p.getIdProdotto();
            String quantitaParam = request.getParameter(QUANTITA);

            if (quantitaParam != null) {
                boolean esiste = false;
                for (int i = 0; i < cartList.size(); i++) {
                    if (cartList.get(i).getIdProdotto().equals(id)) {
                        esiste = true;
                        int qval = qList.get(i) + Integer.parseInt(quantitaParam);
                        qList.set(i, qval);
                        break;
                    }
                }
                if (!esiste) {
                    cartList.add(p);
                    qList.add(Integer.parseInt(quantitaParam));
                }
            }

            salvaCarrello(session, cartList, qList);
            forward(request, response, HOME_PAGE);

        } catch (NumberFormatException e) {
            inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "La quantità deve essere un numero valido.");
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'elaborazione del carrello.", e);
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'elaborazione del carrello.");
        }
    }

    private void rimuoviProdotto(HttpServletRequest request, HttpSession session, ArrayList<Prodotto> cartList, ArrayList<Integer> qList) {
        String[] parti = request.getParameter(ACTION).split(RIMUOVI);
        if (parti.length > 1) {
            String idDaRimuovere = parti[1];
            for (int i = 0; i < cartList.size(); i++) {
                if (cartList.get(i).getIdProdotto().equalsIgnoreCase(idDaRimuovere)) {
                    cartList.remove(i);
                    qList.remove(i);
                    salvaCarrello(session, cartList, qList);
                    break; // Esci dal ciclo dopo aver rimosso
                }
            }
        }
    }

    private void salvaCarrello(HttpSession session, ArrayList<Prodotto> cartList, ArrayList<Integer> qList) {
        session.setAttribute(CART_LIST, cartList);
        session.setAttribute(QUANTITA_ARTICOLI, qList);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        dispatcher.forward(request, response);
    }

    private void inviaErrore(HttpServletResponse response, int status, String messaggio) {
        try {
            response.sendError(status, messaggio);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Impossibile inviare la risposta di errore", e);
        }
    }
}