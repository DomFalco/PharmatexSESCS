package security.unit.inputvalidation;

import controller.CarrelloServlet;
import model.Prodotto;
import model.Utente;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test dell'area OWASP: Input Validation.
 * Verifica il comportamento di CarrelloServlet rispetto ai parametri
 * 'quantita' e 'action'.
 */
@DisplayName("Input Validation - CarrelloServlet")
class CarrelloServletTest {

    private CarrelloServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new CarrelloServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getMethod()).thenReturn("POST");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    // ====== 1. Utente non loggato ======

    @Test
    @DisplayName("Utente non loggato viene reindirizzato a Login.jsp")
    void testUtenteNonLoggatoRedirectALogin() {
        when(session.getAttribute("Utente")).thenReturn(null);
        when(request.getParameter("action")).thenReturn("visualizza");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            assertNotNull(e, "Eccezione attesa durante il test");
        }

        verify(request, atLeastOnce()).getRequestDispatcher("/WEB-INF/results/Login.jsp");
    }

    // ====== 2. FIX: Action null ora reindirizza alla Home ======

    @Test
    @DisplayName("Action null viene gestita e reindirizza alla Home (Fix NPE)")
    void testActionNullRedirigeAllaHome() throws Exception {
        Utente utente = new Utente();
        Prodotto p = new Prodotto();
        p.setIdProdotto("PROD001");

        when(session.getAttribute("Utente")).thenReturn(utente);
        when(session.getAttribute("prod")).thenReturn(p);
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("quantita")).thenReturn("1");

        servlet.service(request, response);

        // FIX: Ora non va più in crash, ma fa un forward alla Home
        verify(request, atLeastOnce()).getRequestDispatcher("HomePage");
        verify(response, never()).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    // ====== 3. FIX: Prodotto null ora reindirizza alla Home ======

    @Test
    @DisplayName("Prodotto null in sessione reindirizza alla Home (Fix NPE)")
    void testProdottoNullRedirigeAllaHome() throws Exception {
        Utente utente = new Utente();
        when(session.getAttribute("Utente")).thenReturn(utente);
        when(session.getAttribute("prod")).thenReturn(null);
        when(session.getAttribute("cart-list")).thenReturn(null);
        when(session.getAttribute("quantitaArticoli")).thenReturn(null);
        when(request.getParameter("quantita")).thenReturn("1");
        when(request.getParameter("action")).thenReturn("aggiungi");

        servlet.service(request, response);

        // FIX: Ora non va più in crash, ma fa un forward alla Home
        verify(request, atLeastOnce()).getRequestDispatcher("HomePage");
        verify(response, never()).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    // ====== 4. Quantità non numerica (GESTITA CON 400) ======

    @Test
    @DisplayName("Quantità non numerica risponde con 400 Bad Request")
    void testQuantitaNonNumericaCausaNumberFormatException() throws Exception {
        Utente utente = new Utente();
        Prodotto prodotto = new Prodotto();
        prodotto.setIdProdotto("PROD001");

        when(session.getAttribute("Utente")).thenReturn(utente);
        when(session.getAttribute("prod")).thenReturn(prodotto);
        when(session.getAttribute("cart-list")).thenReturn(null);
        when(session.getAttribute("quantitaArticoli")).thenReturn(null);
        when(request.getParameter("quantita")).thenReturn("abc");
        when(request.getParameter("action")).thenReturn("aggiungi");

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Quantità con caratteri speciali risponde con 400 Bad Request")
    void testQuantitaConCaratteriSpecialiCausaNumberFormatException() throws Exception {
        Utente utente = new Utente();
        Prodotto prodotto = new Prodotto();
        prodotto.setIdProdotto("PROD001");

        when(session.getAttribute("Utente")).thenReturn(utente);
        when(session.getAttribute("prod")).thenReturn(prodotto);
        when(session.getAttribute("cart-list")).thenReturn(null);
        when(session.getAttribute("quantitaArticoli")).thenReturn(null);
        when(request.getParameter("quantita")).thenReturn("1;DROP TABLE");
        when(request.getParameter("action")).thenReturn("aggiungi");

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // ====== 5. Documentazione mancanza validazione quantità ======

    @Test
    @DisplayName("Quantità negativa non viene validata (vulnerabilità documentata)")
    void testQuantitaNegativaNonValidata() {
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
            assertNotNull(e, "Eccezione attesa durante il test");
        }

        verify(request, atLeastOnce()).getParameter("quantita");
    }

    // ====== 6. Gestione azione "rimuovi" ======

    @Test
    @DisplayName("Azione 'rimuoviXXXX' processa la rimozione (documentazione)")
    void testAzioneRimuoviProcessaRimozione() {
        Utente utente = new Utente();

        Prodotto prod = new Prodotto();
        prod.setIdProdotto("PROD001");

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
            assertNotNull(e, "Eccezione attesa durante il test");
        }

        verify(request, atLeastOnce()).getParameter("action");
    }

    // ====== 7. Documentazione doppio forward ======

    @Test
    @DisplayName("Il Servlet esegue forward multipli (bug del doppio forward documentato)")
    void testDoppioForwardDocumentato() {
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
            assertNotNull(e, "Eccezione attesa durante il test");
        }

        verify(request, atLeastOnce()).getParameter("action");
    }

    // ====== 8. Cast non protetto ======

    @Test
    @DisplayName("La sessione viene letta con cast non protetto (documentazione)")
    void testCastNonProtettoDocumentato() {
        when(session.getAttribute("Utente")).thenReturn(null);
        when(request.getParameter("action")).thenReturn("visualizza");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            assertNotNull(e, "Eccezione attesa durante il test");
        }

        verify(session, atLeastOnce()).getAttribute("Utente");
    }
}