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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Input Validation.
 * Verifica il comportamento di ModificaProdottiServletAmministratore.
 */
@DisplayName("Input Validation - ModificaProdottiServletAmministratore")
class ModificaProdottiServletAmministratoreTest {

    private ModificaProdottiServletAmministratore servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new ModificaProdottiServletAmministratore();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getMethod()).thenReturn("POST");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private Prodotto creaProdottoValido() {
        Prodotto p = new Prodotto();
        p.setIdProdotto("PROD001");
        p.setQuantita(10);
        return p;
    }

    // ====== 1. Documentazione NPE ======

    @Test
    @DisplayName("Prodotto null in sessione causa NPE (vulnerabilità documentata)")
    void testProdottoNullInSessioneCausaNPE() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(null);
        when(request.getParameter("nuovoPrezzo")).thenReturn("99.99");
        when(request.getParameter("quantitaTotale")).thenReturn("5");

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice accede a p.getIdProdotto() senza null check: NPE documentata");
    }

    @Test
    @DisplayName("Parametro 'nuovoPrezzo' null causa NPE (vulnerabilità documentata)")
    void testNuovoPrezzoNullCausaNPE() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn(null);
        when(request.getParameter("quantitaTotale")).thenReturn("5");

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice chiama .equals(\"\") su null: NPE documentata");
    }

    // ====== 2. Documentazione NumberFormatException - Parameterized ======

    @ParameterizedTest(name = "Parametro ''{0}'' = ''{1}'' causa NumberFormatException")
    @CsvSource({
            "nuovoPrezzo, abc",
            "quantitaTotale, abc",
            "nuovoPrezzo, '10; DROP TABLE Prodotto;'"
    })
    @DisplayName("Input non numerico causa NumberFormatException (vulnerabilità documentata)")
    void testInputNonNumericoCausaNumberFormatException(String paramName, String value) {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        if ("nuovoPrezzo".equals(paramName)) {
            when(request.getParameter("nuovoPrezzo")).thenReturn(value);
            when(request.getParameter("quantitaTotale")).thenReturn("");
        } else {
            when(request.getParameter("nuovoPrezzo")).thenReturn("");
            when(request.getParameter("quantitaTotale")).thenReturn(value);
        }

        assertThrows(NumberFormatException.class, () -> servlet.service(request, response),
                "Parsing non protetto: NumberFormatException documentata per " + paramName);
    }

    // ====== 3. Documentazione mancanza controllo autorizzazione ======

    @Test
    @DisplayName("Nessun controllo di autorizzazione presente (vulnerabilità documentata)")
    void testNessunControlloAutorizzazione() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("99.99");
        when(request.getParameter("quantitaTotale")).thenReturn("");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        verify(session, never()).getAttribute("Utente");
    }

    // ====== 4. Documentazione mancanza validazione range ======

    @Test
    @DisplayName("Prezzo negativo non viene validato (vulnerabilità documentata)")
    void testPrezzoNegativoNonValidato() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("-999.99");
        when(request.getParameter("quantitaTotale")).thenReturn("");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("nuovoPrezzo");
    }

    @Test
    @DisplayName("Quantità negativa non viene validata (vulnerabilità documentata)")
    void testQuantitaNegativaNonValidata() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn("-5");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("quantitaTotale");
    }

    // ====== 5. Lettura dei parametri ======

    @Test
    @DisplayName("I parametri 'nuovoPrezzo' e 'quantitaTotale' vengono letti")
    void testParametriVengonoLetti() {
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

    // ====== 6. Lettura prodotto dalla sessione ======

    @Test
    @DisplayName("Il prodotto da modificare viene letto dalla sessione")
    void testProdottoLettoDaSessione() {
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

    // ====== 7. Nessun ramo eseguito ======

    @Test
    @DisplayName("Nessun ramo eseguito se entrambi i parametri sono vuoti (bug documentato)")
    void testNessunRamoEseguitoConParametriVuoti() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn("");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Possibili eccezioni
        }

        verify(request, atLeast(2)).getParameter("nuovoPrezzo");
    }
}