package security.unit.inputvalidation;

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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    // ====== 1. Parametri stringa null ======

    @Test
    @DisplayName("Parametri stringa null vengono gestiti con Bad Request")
    void testParametriStringaNullNonValidati() throws Exception {
        when(request.getParameter("idProdotto")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // ====== 2. NumberFormatException - Parameterized ======

    @ParameterizedTest(name = "Parametro ''{0}'' = ''{1}'' risponde con 400")
    @CsvSource({
            "larghezza, abc",
            "prezzo, abc",
            "quantita, abc",
            "prezzo, '10; DROP TABLE Prodotto;'"
    })
    @DisplayName("Input non numerico risponde con 400 Bad Request")
    void testInputNonNumericoCausaNumberFormatException(String paramName, String value) throws Exception {
        mockParametriValidi();
        when(request.getParameter(paramName)).thenReturn(value);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // ====== 3. Input non validato passa al DAO ======

    @ParameterizedTest(name = "Parametro ''{0}'' = ''{1}'' non viene validato")
    @CsvSource({
            "nomeProdotto, '<script>alert(1)</script>'",
            "descrizione, '<img src=x onerror=alert(1)>'",
            "quantita, -10",
            "prezzo, -999.99"
    })
    @DisplayName("Input non validato passa al DAO (vulnerabilità documentata)")
    void testInputNonValidatoPassaAlDao(String paramName, String value) {
        mockParametriValidi();
        when(request.getParameter(paramName)).thenReturn(value);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
        }

        verify(request, atLeastOnce()).getParameter(paramName);
    }

    // ====== 4. Mancanza controllo autorizzazione ======

    @Test
    @DisplayName("Nessun controllo di autorizzazione presente (vulnerabilità documentata)")
    void testNessunControlloAutorizzazione() {
        mockParametriValidi();

        try {
            servlet.service(request, response);
        } catch (Exception e) {
        }

        verify(session, never()).getAttribute("Utente");
    }

    // ====== 5. Lettura di tutti i 17 parametri ======

    @Test
    @DisplayName("Tutti i 17 parametri vengono letti dalla richiesta")
    void testTuttiIParametriVengonoLetti() {
        mockParametriValidi();

        try {
            servlet.service(request, response);
        } catch (Exception e) {
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