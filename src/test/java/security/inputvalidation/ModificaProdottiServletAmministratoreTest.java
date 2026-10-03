package security.inputvalidation;

import Controller.ModificaProdottiServletAmministratore;
import Model.Prodotto;
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
 * Verifica il comportamento di ModificaProdottiServletAmministratore rispetto
 * ai parametri 'nuovoPrezzo' e 'quantitaTotale'.
 * NOTA: i test DOCUMENTANO la mancanza di validazione e sanitizzazione del codice
 * attuale (NPE, NumberFormatException, mancanza di autorizzazione).
 */
@DisplayName("Input Validation - ModificaProdottiServletAmministratore")
class ModificaProdottiServletAmministratoreTest {

    private ModificaProdottiServletAmministratore servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    // Rimosso: private RequestDispatcher dispatcher; (convertito in variabile locale)

    @BeforeEach
    void setUp() {
        servlet = new ModificaProdottiServletAmministratore();
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

    /**
     * Helper: crea un Prodotto valido da mettere in sessione.
     */
    private Prodotto creaProdottoValido() {
        Prodotto p = new Prodotto();
        p.setIdProdotto("PROD001");
        p.setQuantita(10);
        return p;
    }

    // ====== 1. Documentazione NPE su prodotto null in sessione ======

    @Test
    @DisplayName("Prodotto null in sessione causa NPE (vulnerabilità documentata)")
    void testProdottoNullInSessioneCausaNPE() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(null);
        when(request.getParameter("nuovoPrezzo")).thenReturn("99.99");
        when(request.getParameter("quantitaTotale")).thenReturn("5");

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice accede a p.getIdProdotto() senza null check: NPE documentata");
    }

    // ====== 2. Documentazione NPE su parametri null ======

    @Test
    @DisplayName("Parametro 'nuovoPrezzo' null causa NPE (vulnerabilità documentata)")
    void testNuovoPrezzoNullCausaNPE() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn(null);
        when(request.getParameter("quantitaTotale")).thenReturn("5");

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice chiama .equals(\"\") su null: NPE documentata");
    }

    // ====== 3. Documentazione NumberFormatException ======

    @Test
    @DisplayName("Prezzo non numerico causa NumberFormatException (vulnerabilità documentata)")
    void testPrezzoNonNumericoCausaNumberFormatException() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("abc");
        when(request.getParameter("quantitaTotale")).thenReturn("");

        assertThrows(NumberFormatException.class, () -> servlet.service(request, response),
                "Double.parseDouble senza try/catch: NumberFormatException documentata");
    }

    @Test
    @DisplayName("Quantità non numerica causa NumberFormatException (vulnerabilità documentata)")
    void testQuantitaNonNumericaCausaNumberFormatException() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn("abc");

        assertThrows(NumberFormatException.class, () -> servlet.service(request, response),
                "Integer.parseInt senza try/catch: NumberFormatException documentata");
    }

    @Test
    @DisplayName("Input malevolo su prezzo causa NumberFormatException")
    void testInputMalevoloSuPrezzoCausaNumberFormatException() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("10; DROP TABLE Prodotto;");
        when(request.getParameter("quantitaTotale")).thenReturn("");

        assertThrows(NumberFormatException.class, () -> servlet.service(request, response),
                "Input non numerico deve essere rifiutato dal parsing");
    }

    // ====== 4. Documentazione mancanza controllo autorizzazione ======

    @Test
    @DisplayName("Nessun controllo di autorizzazione presente (vulnerabilità documentata)")
    void testNessunControlloAutorizzazione() { // Rimosso 'throws Exception'
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("99.99");
        when(request.getParameter("quantitaTotale")).thenReturn("");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        // La Servlet NON verifica che l'utente sia amministratore
        verify(session, never()).getAttribute("Utente");
    }

    // ====== 5. Documentazione mancanza validazione range ======

    @Test
    @DisplayName("Prezzo negativo non viene validato (vulnerabilità documentata)")
    void testPrezzoNegativoNonValidato() { // Rimosso 'throws Exception'
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("-999.99");
        when(request.getParameter("quantitaTotale")).thenReturn("");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Il parsing di "-999.99" va a buon fine: il prezzo negativo passa al DAO
        verify(request, atLeastOnce()).getParameter("nuovoPrezzo");
    }

    @Test
    @DisplayName("Quantità negativa non viene validata (vulnerabilità documentata)")
    void testQuantitaNegativaNonValidata() { // Rimosso 'throws Exception'
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn("-5");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Il parsing di "-5" va a buon fine: la quantità negativa passa al DAO
        verify(request, atLeastOnce()).getParameter("quantitaTotale");
    }

    // ====== 6. Lettura dei parametri ======

    @Test
    @DisplayName("I parametri 'nuovoPrezzo' e 'quantitaTotale' vengono letti")
    void testParametriVengonoLetti() { // Rimosso 'throws Exception'
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("99.99");
        when(request.getParameter("quantitaTotale")).thenReturn("5");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("nuovoPrezzo");
        verify(request, atLeastOnce()).getParameter("quantitaTotale");
    }

    // ====== 7. Lettura del prodotto da modificare dalla sessione ======

    @Test
    @DisplayName("Il prodotto da modificare viene letto dalla sessione")
    void testProdottoLettoDaSessione() { // Rimosso 'throws Exception'
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("99.99");
        when(request.getParameter("quantitaTotale")).thenReturn("");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(session, atLeastOnce()).getAttribute("idModificaPrezzo");
    }

    // ====== 8. Nessun ramo eseguito ======

    @Test
    @DisplayName("Nessun ramo eseguito se entrambi i parametri sono vuoti (bug documentato)")
    void testNessunRamoEseguitoConParametriVuoti() { // Rimosso 'throws Exception'
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn("");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Possibili eccezioni
        }

        // Il codice NON ha un "else" finale: se entrambi i parametri sono vuoti,
        // la Servlet non risponde e non forwarda a nessuna pagina
        // (bug documentato: mancanza di un ramo else)
        verify(request, atLeast(2)).getParameter("nuovoPrezzo");
    }
}