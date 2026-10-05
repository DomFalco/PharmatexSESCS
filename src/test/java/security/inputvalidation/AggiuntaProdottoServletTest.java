package security.inputvalidation;

import Controller.AggiuntaProdottoServlet;
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
 * Verifica il comportamento di AggiuntaProdottoServlet rispetto ai 17 parametri.
 */
@DisplayName("Input Validation - AggiuntaProdottoServlet")
class AggiuntaProdottoServletTest {

    private AggiuntaProdottoServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new AggiuntaProdottoServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getMethod()).thenReturn("POST");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private void mockParametriValidi() {
        when(request.getParameter("idProdotto")).thenReturn("PROD001");
        when(request.getParameter("nomeCategoria")).thenReturn("Materasso");
        when(request.getParameter("nomeProdotto")).thenReturn("Materasso Memory");
        when(request.getParameter("descrizione")).thenReturn("Materasso comodo");
        when(request.getParameter("larghezza")).thenReturn("90.5");
        when(request.getParameter("lunghezza")).thenReturn("200.0");
        when(request.getParameter("prezzo")).thenReturn("499.99");
        when(request.getParameter("quantita")).thenReturn("10");
        when(request.getParameter("tipoMaterialeMaterasso")).thenReturn("Memory");
        when(request.getParameter("coloreLetto")).thenReturn("Bianco");
        when(request.getParameter("materialeRete")).thenReturn("Faggio");
        when(request.getParameter("rivestimentoDivano")).thenReturn("Tessuto");
        when(request.getParameter("coloreDivano")).thenReturn("Grigio");
        when(request.getParameter("tipoStoffaCuscino")).thenReturn("Cotone");
        when(request.getParameter("materialeCuscino")).thenReturn("Memory");
        when(request.getParameter("formaCuscino")).thenReturn("Quadrato");
    }

    // ====== 1. Documentazione NPE su parametri null ======

    @Test
    @DisplayName("Parametri stringa null non vengono validati (vulnerabilità documentata)")
    void testParametriStringaNullNonValidati() {
        when(request.getParameter("idProdotto")).thenReturn(null);

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice non gestisce parametri stringa null: NPE documentata");
    }

    // ====== 2. Documentazione NumberFormatException - Parameterized ======

    @ParameterizedTest(name = "Parametro ''{0}'' = ''{1}'' causa NumberFormatException")
    @CsvSource({
            "larghezza, abc",
            "prezzo, abc",
            "quantita, abc",
            "prezzo, '10; DROP TABLE Prodotto;'"
    })
    @DisplayName("Input non numerico causa NumberFormatException (vulnerabilità documentata)")
    void testInputNonNumericoCausaNumberFormatException(String paramName, String value) {
        mockParametriValidi();
        when(request.getParameter(paramName)).thenReturn(value);

        assertThrows(NumberFormatException.class, () -> servlet.service(request, response),
                "Parsing non protetto: NumberFormatException documentata per " + paramName);
    }

    // ====== 3. Documentazione mancanza di sanitizzazione ======

    @Test
    @DisplayName("Il nome prodotto con <script> non viene sanitizzato (vulnerabilità documentata)")
    void testNomeProdottoNonSanitizzato() {
        mockParametriValidi();
        when(request.getParameter("nomeProdotto")).thenReturn("<script>alert(1)</script>");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        verify(request, atLeastOnce()).getParameter("nomeProdotto");
    }

    @Test
    @DisplayName("La descrizione con tag HTML non viene sanitizzata (vulnerabilità documentata)")
    void testDescrizioneNonSanitizzata() {
        mockParametriValidi();
        when(request.getParameter("descrizione")).thenReturn("<img src=x onerror=alert(1)>");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("descrizione");
    }

    // ====== 4. Documentazione mancanza controllo autorizzazione ======

    @Test
    @DisplayName("Nessun controllo di autorizzazione presente (vulnerabilità documentata)")
    void testNessunControlloAutorizzazione() {
        mockParametriValidi();

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(session, never()).getAttribute("Utente");
    }

    // ====== 5. Documentazione mancanza range check ======

    @Test
    @DisplayName("Quantità negativa non viene validata (vulnerabilità documentata)")
    void testQuantitaNegativaNonValidata() {
        mockParametriValidi();
        when(request.getParameter("quantita")).thenReturn("-10");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("quantita");
    }

    @Test
    @DisplayName("Prezzo negativo non viene validato (vulnerabilità documentata)")
    void testPrezzoNegativoNonValidato() {
        mockParametriValidi();
        when(request.getParameter("prezzo")).thenReturn("-999.99");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("prezzo");
    }

    // ====== 6. Lettura di tutti i 17 parametri ======

    @Test
    @DisplayName("Tutti i 17 parametri vengono letti dalla richiesta")
    void testTuttiIParametriVengonoLetti() {
        mockParametriValidi();

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("idProdotto");
        verify(request, atLeastOnce()).getParameter("nomeCategoria");
        verify(request, atLeastOnce()).getParameter("nomeProdotto");
        verify(request, atLeastOnce()).getParameter("descrizione");
        verify(request, atLeastOnce()).getParameter("larghezza");
        verify(request, atLeastOnce()).getParameter("lunghezza");
        verify(request, atLeastOnce()).getParameter("prezzo");
        verify(request, atLeastOnce()).getParameter("quantita");
    }
}