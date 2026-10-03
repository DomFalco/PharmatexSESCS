package Controller;

import Model.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "DatiPagamentoServlet", value = "/DatiPagamentoServlet")
public class DatiPagamentoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    @Override
    @SuppressWarnings("unchecked") // <-- Aggiunto per gestire i cast da Object ad ArrayList<T>
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // ===== FIX 1: controllo autenticazione utente =====
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: sessione mancante.");
            return;
        }
        Utente u = (Utente) session.getAttribute("Utente");
        if (u == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: utente non autenticato.");
            return;
        }
        // ===== FINE FIX 1 =====

        // ===== FIX 2: controllo carrello non vuoto =====
        ArrayList<Prodotto> cart_list = (ArrayList<Prodotto>) session.getAttribute("cart-list");
        ArrayList<Integer> qList = (ArrayList<Integer>) session.getAttribute("quantitaArticoli");
        if (cart_list == null || qList == null || cart_list.isEmpty() || qList.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Carrello vuoto.");
            return;
        }
        // ===== FINE FIX 2 =====

        // ===== Logica di pagamento (invariata) =====
        int x;
        for (int i = 0; i < cart_list.size(); i++) {
            AcquistoProdottiDAO.acquistaProdotto(u.getEmail(), cart_list.get(i).getIdProdotto(), qList.get(i));
            x = cart_list.get(i).getQuantita() - qList.get(i);
            ProdottoDAO.doUpdateQuantita(x, cart_list.get(i).getIdProdotto());
        }

        Carta c = new Carta(u);
        c.setNumeroCarta(request.getParameter("NCarta"));
        c.setNomeIntestario(request.getParameter("credenziali"));
        c.setDataScadenza(request.getParameter("dataScadenza"));
        c.setCVV(request.getParameter("cvv"));
        c.setEmailProprietario(u.getEmail());
        CartaDAO.aggiuntaCredenzialiPagamento(c, u.getEmail());

        cart_list.clear();
        qList.clear();
        session.setAttribute("cart-list", cart_list);
        session.setAttribute("quantitaArticoli", qList);

        RequestDispatcher dispatcher = request.getRequestDispatcher("HomePage");
        dispatcher.forward(request, response);
    }
}