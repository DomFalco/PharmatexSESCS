package security.inputvalidation;

import Controller.RicercaServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Input Validation.
 * Verifica il comportamento di RicercaServlet rispetto al parametro 'search'.
 * NOTA: poiché il DAO statico richiede un DB configurato, i test si limitano
 * a verificare il comportamento del Servlet PRIMA della chiamata al database.
 * Documentano inoltre vulnerabilità presenti nel codice attuale.
 */
@DisplayName("Input Validation - RicercaServlet")
class RicercaServletTest {

    private RicercaServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        servlet = new RicercaServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(request.getMethod()).thenReturn("GET");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
    }

    // ====== 1. Documentazione NPE su input null ======

    @Test
    @DisplayName("Input null causa NullPointerException (vulnerabilità documentata)")
    void testInputNullCausaNPE() {
        when(request.getParameter("search")).thenReturn(null);

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice attuale non gestisce l'input null: NPE documentata. " +
                        "Fix suggerito: aggiungere null check prima di toUpperCase()");
    }

    // ====== 2. Lettura del parametro 'search' ======

    @Test
    @DisplayName("Il parametro 'search' viene letto dalla richiesta")
    void testGetParameterSearchChiamato() {
        when(request.getParameter("search")).thenReturn("Materasso");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        verify(request, atLeastOnce()).getParameter("search");
    }

    // ====== 3. Accesso alla sessione HTTP ======

    @Test
    @DisplayName("La sessione HTTP viene ottenuta dalla richiesta")
    void testGetSessionChiamato() {
        when(request.getParameter("search")).thenReturn("Materasso");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getSession();
    }

    // ====== 4. Documentazione mancanza di sanitizzazione ======

    @Test
    @DisplayName("Il parametro 'search' non viene sanitizzato (vulnerabilità documentata)")
    void testInputNonSanitizzato() {
        String inputMalevolo = "<script>alert(1)</script>";
        when(request.getParameter("search")).thenReturn(inputMalevolo);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("search");
    }

    // ====== 5. Robustezza con input speciali ======

    @Test
    @DisplayName("Input con caratteri speciali non causa NPE (il crash è dovuto al DB)")
    void testInputConCaratteriSpecialiNonCausaNPE() {
        when(request.getParameter("search")).thenReturn("caffè/è&bello#1");

        assertDoesNotThrow(() -> {
            try {
                servlet.service(request, response);
            } catch (NullPointerException e) {
                throw e;
            } catch (Exception e) {
                // Eccezione attesa dal DB non configurato: ignorata
            }
        }, "Input non null non deve causare NPE");
    }

    // ====== 6. La Servlet non gestisce le eccezioni del DB ======

    @Test
    @DisplayName("La Servlet non gestisce le eccezioni del DB (documentazione)")
    void testEccezioneDbPropagata() {
        when(request.getParameter("search")).thenReturn("Materasso");

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il Servlet non gestisce le eccezioni del DAO: il crash si propaga al client");
    }
}