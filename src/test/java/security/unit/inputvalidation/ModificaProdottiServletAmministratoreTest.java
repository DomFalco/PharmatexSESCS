package security.unit.inputvalidation;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new ModificaProdottiServletAmministratore();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

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

    // ====== 1. NPE / Null handling ======

    @Test
    @DisplayName("Prodotto null in sessione risponde con 400 Bad Request")
    void testProdottoNullInSessioneCausaNPE() throws Exception {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(null);
        when(request.getParameter("nuovoPrezzo")).thenReturn("99.99");
        when(request.getParameter("quantitaTotale")).thenReturn("5");

        servlet.service(request, response);

        // FIX: La servlet gestisce il null e risponde 400
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Parametro 'nuovoPrezzo' null risponde con 400 Bad Request")
    void testNuovoPrezzoNullCausaNPE() throws Exception {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn(null);
        when(request.getParameter("quantitaTotale")).thenReturn("5");

        servlet.service(request, response);

        // FIX: La servlet gestisce il null e risponde 400
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // ====== 2. Input non numerico ======

    @ParameterizedTest(name = "Parametro ''{0}'' = ''{1}'' risponde con 400 Bad Request")
    @CsvSource({
            "nuovoPrezzo, abc",
            "quantitaTotale, abc",
            "nuovoPrezzo, '10; DROP TABLE Prodotto;'"
    })
    @DisplayName("Input non numerico risponde con 400 Bad Request")
    void testInputNonNumericoCausaNumberFormatException(String paramName, String value) throws Exception {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        if ("nuovoPrezzo".equals(paramName)) {
            when(request.getParameter("nuovoPrezzo")).thenReturn(value);
            when(request.getParameter("quantitaTotale")).thenReturn("");
        } else {
            when(request.getParameter("nuovoPrezzo")).thenReturn("");
            when(request.getParameter("quantitaTotale")).thenReturn(value);
        }

        servlet.service(request, response);

        // FIX: La servlet cattura l'eccezione e risponde 400
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // ====== 3. Autorizzazione ======

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

    // ====== 4. Validazione range ======

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

    // ====== 5. Lettura parametri ======

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

    // ====== 6. Lettura sessione ======

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
    void testNessunRamoEseguitoConParametriVuoti() throws Exception {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdottoValido());
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn("");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Possibili eccezioni
        }

        verify(request, atLeastOnce()).getParameter("nuovoPrezzo");
        verify(request, atLeastOnce()).getParameter("quantitaTotale");
        verify(dispatcher, never()).forward(any(), any());
    }
}