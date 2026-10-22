package controller;

import model.*;
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

@WebServlet(name = "DatiPagamentoServlet", value = "/DatiPagamentoServlet")
public class DatiPagamentoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DatiPagamentoServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                inviaErrore(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
                return;
            }
            Utente u = (Utente) session.getAttribute("Utente");
            if (u == null) {
                inviaErrore(response, HttpServletResponse.SC_FORBIDDEN, "Accesso negato: utente non autenticato.");
                return;
            }

            ArrayList<Prodotto> cart_list = (ArrayList<Prodotto>) session.getAttribute("cart-list");
            ArrayList<Integer> qList = (ArrayList<Integer>) session.getAttribute("quantitaArticoli");

            if (cart_list == null || qList == null || cart_list.isEmpty() || qList.isEmpty()) {
                inviaErrore(response, HttpServletResponse.SC_BAD_REQUEST, "Carrello vuoto.");
                return;
            }

            int quantitaAggiornata;
            for (int i = 0; i < cart_list.size(); i++) {
                AcquistoProdottiDAO.acquistaProdotto(u.getEmail(), cart_list.get(i).getIdProdotto(), qList.get(i));
                quantitaAggiornata = cart_list.get(i).getQuantita() - qList.get(i);
                ProdottoDAO.doUpdateQuantita(quantitaAggiornata, cart_list.get(i).getIdProdotto());
            }

            Carta c = new Carta(u);
            c.setNumeroCarta(request.getParameter("NCarta"));
            c.setNomeIntestatario(request.getParameter("credenziali"));
            c.setDataScadenza(request.getParameter("dataScadenza"));
            c.setCvv(request.getParameter("cvv"));
            c.setEmailProprietario(u.getEmail());
            CartaDAO.aggiuntaCredenzialiPagamento(c, u.getEmail());

            cart_list.clear();
            qList.clear();
            session.setAttribute("cart-list", cart_list);
            session.setAttribute("quantitaArticoli", qList);

            RequestDispatcher dispatcher = request.getRequestDispatcher("HomePage");
            dispatcher.forward(request, response);

        } catch (ServletException | IOException e) {
            inviaErrore(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'inoltro dopo il pagamento.");
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