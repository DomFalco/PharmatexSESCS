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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    @SuppressWarnings({"unchecked", "java:S1989"})
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession();
            Utente u = (Utente) session.getAttribute("Utente");

            ArrayList<Prodotto> cartList;
            ArrayList<Integer> qList;

            if (u != null) {
                cartList = (ArrayList<Prodotto>) session.getAttribute(CART_LIST);
                qList = (ArrayList<Integer>) session.getAttribute(QUANTITA_ARTICOLI);
                Prodotto p = (Prodotto) session.getAttribute(PROD);

                String id = p.getIdProdotto();

                if (cartList == null && qList == null) {
                    cartList = new ArrayList<>();
                    qList = new ArrayList<>();
                    cartList.add(p);
                    qList.add(Integer.valueOf(request.getParameter(QUANTITA)));
                    salvaCarrello(session, cartList, qList);
                } else {
                    aggiornaQuantitaEsistente(request, cartList, qList, id);
                }
                forward(request, response, HOME_PAGE);
            } else {
                cartList = new ArrayList<>();
                qList = new ArrayList<>();
                forward(request, response, LOGIN_JSP);
            }

            if (request.getParameter(ACTION) != null && request.getParameter(ACTION).contains(RIMUOVI)) {
                rimuoviProdotto(request, session, cartList, qList);
                forward(request, response, HOME_PAGE);
            } else {
                forward(request, response, LOGIN_JSP);
            }
        } catch (NumberFormatException e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "La quantità deve essere un numero valido.");
            } catch (IOException ex) {
                throw new ServletException("Impossibile inviare la risposta di errore", ex);
            }
        } catch (ServletException | IOException e) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'elaborazione del carrello.");
            } catch (IOException ex) {
                throw new ServletException("Impossibile inviare la risposta di errore", ex);
            }
        }
    }

    private void aggiornaQuantitaEsistente(HttpServletRequest request, ArrayList<Prodotto> cartList, ArrayList<Integer> qList, String id) {
        Prodotto p = (Prodotto) request.getSession().getAttribute(PROD);
        String quantitaParam = request.getParameter(QUANTITA);
        boolean esiste = false;

        for (int i = 0; i < cartList.size(); i++) {
            if (cartList.get(i).getIdProdotto().equals(id) && quantitaParam != null) {
                esiste = true;
                int qval = qList.get(i);
                qval += Integer.parseInt(quantitaParam);
                qList.set(i, qval);
            }
        }

        if (!esiste && quantitaParam != null) {
            cartList.add(p);
            qList.add(Integer.parseInt(quantitaParam));
        }
    }

    private void rimuoviProdotto(HttpServletRequest request, HttpSession session, ArrayList<Prodotto> cartList, ArrayList<Integer> qList) {
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

    private void salvaCarrello(HttpSession session, ArrayList<Prodotto> cartList, ArrayList<Integer> qList) {
        session.setAttribute(CART_LIST, cartList);
        session.setAttribute(QUANTITA_ARTICOLI, qList);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        dispatcher.forward(request, response);
    }
}