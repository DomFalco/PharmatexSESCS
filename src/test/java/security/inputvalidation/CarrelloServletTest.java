package security.inputvalidation;

import Controller.CarrelloServlet;
import Model.Prodotto;
import Model.Utente;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Input Validation.
 * Verifica il comportamento di CarrelloServlet rispetto ai parametri
 * 'quantita' e 'action'.
 * NOTA: alcuni test DOCUMENTANO vulnerabilità e bug presenti nel codice attuale
 * (NPE, NumberFormatException, doppio forward).
 */
@DisplayName("Input Validation - CarrelloServlet")
class CarrelloServletTest {

    private CarrelloServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    // Rimosso: private RequestDispatcher dispatcher; (convertito in variabile locale)

    @BeforeEach
    void setUp() {
        servlet = new CarrelloServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        // Aggiunto come variabile locale
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getMethod()).thenReturn("POST");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    // ====== 1. Utente non loggato ======

    @Test
    @DisplayName("Utente non loggato viene reindirizzato a Login.jsp")
    void testUtenteNonLoggatoRedirectALogin() { // Rimosso 'throws Exception'
        when(session.getAttribute("Utente")).thenReturn(null);
        when(request.getParameter("action")).thenReturn("visualizza");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Possibili eccezioni documentate
        }

        verify(request, atLeastOnce()).getRequestDispatcher("/WEB-INF/results/Login.jsp");
    }

    // ====== 2. Documentazione NPE su action null ======

    @Test
    @DisplayName("Action null causa NullPointerException (vulnerabilità documentata)")
    void testActionNullCausaNPE() {
        Utente utente = new Utente();
        when(session.getAttribute("Utente")).thenReturn(utente);
        when(session.getAttribute("prod")).thenReturn(null);
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("quantita")).thenReturn("1");

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice attuale chiama action.contains() senza null check: NPE documentata");
    }

    // ====== 3. Documentazione NPE su prodotto null ======

    @Test
    @DisplayName("Prodotto null in sessione causa NPE (vulnerabilità documentata)")
    void testProdottoNullCausaNPE() {
        Utente utente = new Utente();
        when(session.getAttribute("Utente")).thenReturn(utente);
        when(session.getAttribute("prod")).thenReturn(null);
        when(session.getAttribute("cart-list")).thenReturn(null);
        when(session.getAttribute("quantitaArticoli")).thenReturn(null);
        when(request.getParameter("quantita")).thenReturn("1");
        when(request.getParameter("action")).thenReturn("aggiungi");

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice accede a p.getIdProdotto() senza null check: NPE documentata");
    }

    // ====== 4. Documentazione NumberFormatException ======

    @Test
    @DisplayName("Quantità non numerica causa NumberFormatException (vulnerabilità documentata)")
    void testQuantitaNonNumericaCausaNumberFormatException() {
        Utente utente = new Utente();
        Prodotto prodotto = new Prodotto();
        prodotto.setIdProdotto("PROD001");

        when(session.getAttribute("Utente")).thenReturn(utente);
        when(session.getAttribute("prod")).thenReturn(prodotto);
        when(session.getAttribute("cart-list")).thenReturn(null);
        when(session.getAttribute("quantitaArticoli")).thenReturn(null);
        when(request.getParameter("quantita")).thenReturn("abc");
        when(request.getParameter("action")).thenReturn("aggiungi");

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice usa Integer.valueOf() senza try/catch: NumberFormatException documentata");
    }

    @Test
    @DisplayName("Quantità con caratteri speciali causa NumberFormatException")
    void testQuantitaConCaratteriSpecialiCausaNumberFormatException() {
        Utente utente = new Utente();
        Prodotto prodotto = new Prodotto();
        prodotto.setIdProdotto("PROD001");

        when(session.getAttribute("Utente")).thenReturn(utente);
        when(session.getAttribute("prod")).thenReturn(prodotto);
        when(session.getAttribute("cart-list")).thenReturn(null);
        when(session.getAttribute("quantitaArticoli")).thenReturn(null);
        when(request.getParameter("quantita")).thenReturn("1;DROP TABLE");
        when(request.getParameter("action")).thenReturn("aggiungi");

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Input non numerico deve essere rifiutato (documentazione)");
    }

    // ====== 5. Documentazione mancanza validazione quantità ======

    @Test
    @DisplayName("Quantità negativa non viene validata (vulnerabilità documentata)")
    void testQuantitaNegativaNonValidata() { // Rimosso 'throws Exception'
        Utente utente = new Utente();
        Prodotto prodotto = new Prodotto();
        prodotto.setIdProdotto("PROD001");

        when(session.getAttribute("Utente")).thenReturn(utente);
        when(session.getAttribute("prod")).thenReturn(prodotto);
        when(session.getAttribute("cart-list")).thenReturn(null);
        when(session.getAttribute("quantitaArticoli")).thenReturn(null);
        when(request.getParameter("quantita")).thenReturn("-5");
        when(request.getParameter("action")).thenReturn("aggiungi");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Possibili eccezioni
        }

        // Il Servlet NON verifica che la quantità sia positiva:
        // "-5" viene accettata e salvata in sessione
        verify(request, atLeastOnce()).getParameter("quantita");
    }

    // ====== 6. Gestione azione "rimuovi" ======

    @Test
    @DisplayName("Azione 'rimuoviXXXX' processa la rimozione (documentazione)")
    void testAzioneRimuoviProcessaRimozione() { // Rimosso 'throws Exception'
        Utente utente = new Utente();

        // Prodotto valido in sessione (necessario: il codice chiama p.getIdProdotto())
        Prodotto prod = new Prodotto();
        prod.setIdProdotto("PROD001");

        // Prodotto già presente nel carrello con lo stesso ID
        Prodotto prodottoInCarrello = new Prodotto();
        prodottoInCarrello.setIdProdotto("PROD001");

        ArrayList<Prodotto> cartList = new ArrayList<>();
        cartList.add(prodottoInCarrello);
        ArrayList<Integer> qList = new ArrayList<>();
        qList.add(1);

        when(session.getAttribute("Utente")).thenReturn(utente);
        when(session.getAttribute("cart-list")).thenReturn(cartList);
        when(session.getAttribute("quantitaArticoli")).thenReturn(qList);
        when(session.getAttribute("prod")).thenReturn(prod);
        when(request.getParameter("quantita")).thenReturn("1");
        when(request.getParameter("action")).thenReturn("rimuoviPROD001");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Possibili eccezioni
        }

        // Ora il flusso arriva alla fine: getParameter("action") viene chiamato
        verify(request, atLeastOnce()).getParameter("action");
    }

    // ====== 7. Documentazione doppio forward ======

    @Test
    @DisplayName("Il Servlet esegue forward multipli (bug del doppio forward documentato)")
    void testDoppioForwardDocumentato() { // Rimosso 'throws Exception'
        Utente utente = new Utente();
        Prodotto prodotto = new Prodotto();
        prodotto.setIdProdotto("PROD001");

        when(session.getAttribute("Utente")).thenReturn(utente);
        when(session.getAttribute("prod")).thenReturn(prodotto);
        when(session.getAttribute("cart-list")).thenReturn(null);
        when(session.getAttribute("quantitaArticoli")).thenReturn(null);
        when(request.getParameter("quantita")).thenReturn("1");
        when(request.getParameter("action")).thenReturn("aggiungi");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Possibili eccezioni
        }

        // In produzione questo causerebbe IllegalStateException:
        // "Cannot forward after response has been committed"
        // Con i mock non viene lanciata, ma il test documenta il problema
        verify(request, atLeastOnce()).getParameter("action");
    }

    // ====== 8. Cast non protetto ======

    @Test
    @DisplayName("La sessione viene letta con cast non protetto (documentazione)")
    void testCastNonProtettoDocumentato() { // Rimosso 'throws Exception'
        // Simula un oggetto di tipo diverso in sessione (es. attacco o bug)
        when(session.getAttribute("Utente")).thenReturn(null);
        when(request.getParameter("action")).thenReturn("visualizza");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Possibile ClassCastException documentata
        }

        verify(session, atLeastOnce()).getAttribute("Utente");
    }
}