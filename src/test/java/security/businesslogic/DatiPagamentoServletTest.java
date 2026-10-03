package security.businesslogic;

import Controller.DatiPagamentoServlet;
import Model.Prodotto;
import Model.Utente;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;        // <-- Aggiunto
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Business Logic.
 * Verifica il comportamento di DatiPagamentoServlet dopo il fix
 * del controllo di autenticazione e del carrello vuoto.
 * NOTA: la logica di pagamento (ciclo for, chiamate al DAO) non è testabile
 * senza un DB configurato. I test si concentrano sui controlli di guardia
 * introdotti con il fix e documentano i problemi residui.
 */
@DisplayName("Business Logic - DatiPagamentoServlet (post-fix)")
class DatiPagamentoServletTest {

    private DatiPagamentoServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new DatiPagamentoServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession(false)).thenReturn(session);
        when(request.getMethod()).thenReturn("POST");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private Utente creaUtente() {
        Utente u = new Utente();
        u.setEmail("cliente@example.com");
        return u;
    }

    private ArrayList<Prodotto> creaCarrello() {
        ArrayList<Prodotto> lista = new ArrayList<>();
        Prodotto p = new Prodotto();
        p.setIdProdotto("MAT001");
        p.setQuantita(10);
        lista.add(p);
        return lista;
    }

    private ArrayList<Integer> creaQuantita() {
        ArrayList<Integer> lista = new ArrayList<>();
        lista.add(2);
        return lista;
    }

    // =================================================================
    // TEST DI SICUREZZA (verificano il fix di autenticazione)
    // =================================================================

    @Test
    @DisplayName("Utente anonimo (sessione null) riceve 403 Forbidden")
    void testAnonimoSessioneNullRiceve403() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("Sessione senza attributo 'Utente' riceve 403")
    void testSessioneSenzaUtenteRiceve403() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("Carrello null riceve 400 Bad Request")
    void testCarrelloNullRiceve400() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(creaUtente());
        when(session.getAttribute("cart-list")).thenReturn(null);
        when(session.getAttribute("quantitaArticoli")).thenReturn(creaQuantita());

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Carrello vuoto riceve 400 Bad Request")
    void testCarrelloVuotoRiceve400() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(creaUtente());
        when(session.getAttribute("cart-list")).thenReturn(new ArrayList<>());
        when(session.getAttribute("quantitaArticoli")).thenReturn(new ArrayList<>());

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("QuantitaArticoli null riceve 400 Bad Request")
    void testQuantitaNullRiceve400() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(creaUtente());
        when(session.getAttribute("cart-list")).thenReturn(creaCarrello());
        when(session.getAttribute("quantitaArticoli")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // =================================================================
    // TEST DI COMPORTAMENTO (utente valido - passa i controlli di guardia)
    // =================================================================

    @Test
    @DisplayName("Utente con carrello valido supera i controlli di guardia")
    void testUtenteValidoSuperaControlliGuardia() throws IOException { // <-- throws IOException
        when(session.getAttribute("Utente")).thenReturn(creaUtente());
        when(session.getAttribute("cart-list")).thenReturn(creaCarrello());
        when(session.getAttribute("quantitaArticoli")).thenReturn(creaQuantita());

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        // L'utente valido NON deve ricevere 403 né 400
        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(response, never()).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Utente valido non viene reindirizzato al login")
    void testUtenteValidoNonRedirettoALogin() {
        when(session.getAttribute("Utente")).thenReturn(creaUtente());
        when(session.getAttribute("cart-list")).thenReturn(creaCarrello());
        when(session.getAttribute("quantitaArticoli")).thenReturn(creaQuantita());

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Non deve mai reindirizzare al login (l'utente è già autenticato)
        verify(request, never()).getRequestDispatcher("/WEB-INF/results/Login.jsp");
    }

    // =================================================================
    // DOCUMENTAZIONE: parametri carta non validati
    // =================================================================

    @Test
    @DisplayName("Nessuna validazione sui parametri carta (vulnerabilità documentata)")
    void testNessunaValidazioneParametriCarta() throws IOException { // <-- throws IOException
        when(session.getAttribute("Utente")).thenReturn(creaUtente());
        when(session.getAttribute("cart-list")).thenReturn(creaCarrello());
        when(session.getAttribute("quantitaArticoli")).thenReturn(creaQuantita());
        // Parametri volutamente errati
        when(request.getParameter("NCarta")).thenReturn("abc");
        when(request.getParameter("credenziali")).thenReturn("");
        when(request.getParameter("dataScadenza")).thenReturn("xx/xx");
        when(request.getParameter("cvv")).thenReturn("x");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // La Servlet non valida i parametri: passano direttamente al modello Carta
        // (il fix dovrebbe aggiungere una validazione formato prima di CartaDAO)
        verify(response, never()).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // =================================================================
    // DOCUMENTAZIONE: mancanza di transazione atomica
    // =================================================================

    @Test
    @DisplayName("Nessuna transazione atomica (documentazione)")
    void testNessunaTransazioneAtomica() {
        when(session.getAttribute("Utente")).thenReturn(creaUtente());
        when(session.getAttribute("cart-list")).thenReturn(creaCarrello());
        when(session.getAttribute("quantitaArticoli")).thenReturn(creaQuantita());

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Il codice esegue in sequenza:
        //   1. AcquistoProdottiDAO.acquistaProdotto()  -> INSERT acquisto
        //   2. ProdottoDAO.doUpdateQuantita()           -> UPDATE quantità
        //   3. CartaDAO.aggiuntaCredenzialiPagamento()  -> INSERT carta
        //   4. session.getAttribute("cart-list").clear() -> svuota carrello
        // Se uno dei passi fallisce, gli altri NON vengono annullati.
        // Fix suggerito: usare una transazione JDBC (commit/rollback).

        verify(session, atLeastOnce()).getAttribute("cart-list");
    }

    // =================================================================
    // DOCUMENTAZIONE: mancanza di protezione CSRF
    // =================================================================

    @Test
    @DisplayName("Nessun controllo CSRF token presente (vulnerabilità documentata)")
    void testNessunControlloCsrf() {
        when(session.getAttribute("Utente")).thenReturn(creaUtente());
        when(session.getAttribute("cart-list")).thenReturn(creaCarrello());
        when(session.getAttribute("quantitaArticoli")).thenReturn(creaQuantita());

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Nessun token CSRF verificato prima di processare il pagamento
        verify(request, never()).getParameter("csrf_token");
        verify(session, never()).getAttribute("csrf_token");
    }
}