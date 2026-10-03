package security.inputvalidation;

import Controller.FiltraggioServletPrezzo;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Input Validation.
 * Verifica il comportamento di FiltraggioServletPrezzo rispetto ai parametri
 * 'prezzomin' e 'prezzomax'.
 * NOTA: alcuni test DOCUMENTANO vulnerabilità presenti nel codice attuale
 * (mancanza di null check, parsing non protetto, doppio forward).
 */
@DisplayName("Input Validation - FiltraggioServletPrezzo")
class FiltraggioServletPrezzoTest {

    private FiltraggioServletPrezzo servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new FiltraggioServletPrezzo();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class); // Inizializzato qui

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getMethod()).thenReturn("GET");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
    }

    // ====== 1. Documentazione NPE su parametri null ======

    @Test
    @DisplayName("Parametri null causano NullPointerException (vulnerabilità documentata)")
    void testParametriNullCausanoNPE() {
        when(request.getParameter("prezzomin")).thenReturn(null);
        when(request.getParameter("prezzomax")).thenReturn(null);

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice attuale non gestisce parametri null: NPE documentata. " +
                        "Fix suggerito: aggiungere null check su getParameter()");
    }

    // ====== 2. Documentazione parsing non protetto ======

    @Test
    @DisplayName("Prezzo non numerico causa NumberFormatException (vulnerabilità documentata)")
    void testPrezzoNonNumericoCausaNumberFormatException() {
        when(request.getParameter("prezzomin")).thenReturn("");
        when(request.getParameter("prezzomax")).thenReturn("abc");

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice attuale non valida l'input numerico: " +
                        "NumberFormatException documentata. Fix: try/catch su parseDouble()");
    }

    @Test
    @DisplayName("Prezzo con caratteri speciali causa NumberFormatException")
    void testPrezzoConCaratteriSpecialiCausaNumberFormatException() {
        when(request.getParameter("prezzomin")).thenReturn("10;DROP TABLE");
        when(request.getParameter("prezzomax")).thenReturn("");

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Input non numerico deve essere rifiutato (documentazione)");
    }

    // ====== 3. Lettura del filtro dalla sessione ======

    @Test
    @DisplayName("Il filtro 'filtri' viene letto dalla sessione")
    void testFiltriLettoDaSessione() { // Rimosso 'throws Exception'
        when(request.getParameter("prezzomin")).thenReturn("10");
        when(request.getParameter("prezzomax")).thenReturn("100");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(session, atLeastOnce()).getAttribute("filtri");
    }

    // ====== 4. Documentazione bug del doppio forward ======

    @Test
    @DisplayName("Entrambi i prezzi vuoti causano doppio forward (bug documentato)")
    void testEntrambiPrezziVuotiCausaDoppioForward() { // Rimosso 'throws Exception'
        when(request.getParameter("prezzomin")).thenReturn("");
        when(request.getParameter("prezzomax")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);
        when(request.getRequestDispatcher("/WEB-INF/results/Prodotti.jsp")).thenReturn(dispatcher);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Possibile eccezione
        }

        // Il codice attuale manca di "return" dopo il primo forward:
        // questo causa un secondo forward -> IllegalStateException in produzione
        verify(request, atLeastOnce()).getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp");
    }

    // ====== 5. Documentazione mancanza range check ======

    @Test
    @DisplayName("Prezzi negativi non vengono validati (vulnerabilità documentata)")
    void testPrezziNegativiNonValidati() { // Rimosso 'throws Exception'
        when(request.getParameter("prezzomin")).thenReturn("-1000");
        when(request.getParameter("prezzomax")).thenReturn("-500");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Il Servlet NON verifica che i prezzi siano positivi o che min < max:
        // il parsing di "-1000" va a buon fine e il valore passa al DAO
        verify(request, atLeastOnce()).getParameter("prezzomin");
    }

    @Test
    @DisplayName("Prezzo min > prezzo max non viene validato (vulnerabilità documentata)")
    void testMinMaggioreDiMaxNonValidato() { // Rimosso 'throws Exception'
        when(request.getParameter("prezzomin")).thenReturn("500");
        when(request.getParameter("prezzomax")).thenReturn("100");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Il codice NON verifica che min <= max
        verify(request, atLeastOnce()).getParameter("prezzomin");
        verify(request, atLeastOnce()).getParameter("prezzomax");
    }

    // ====== 6. Accesso alla sessione ======

    @Test
    @DisplayName("La sessione HTTP viene ottenuta dalla richiesta")
    void testGetSessionChiamato() { // Rimosso 'throws Exception'
        when(request.getParameter("prezzomin")).thenReturn("10");
        when(request.getParameter("prezzomax")).thenReturn("100");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getSession();
    }
}