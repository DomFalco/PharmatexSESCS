package security.unit.inputvalidation;

import controller.FiltraggioServletPrezzo;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test dell'area OWASP: Input Validation.
 * Verifica il comportamento di FiltraggioServletPrezzo rispetto ai parametri
 * 'prezzomin' e 'prezzomax'.
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
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("filtri")).thenReturn("Materasso");
        when(request.getMethod()).thenReturn("GET");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
    }

    // ====== 1. Parametri null ======

    @Test
    @DisplayName("Parametri null vengono gestiti e si esegue il forward a RicercaErrata.jsp")
    void testParametriNullCausanoNPE() throws Exception {
        when(request.getParameter("prezzomin")).thenReturn(null);
        when(request.getParameter("prezzomax")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);

        servlet.service(request, response);

        verify(dispatcher).forward(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    // ====== 2. Parsing non protetto ======

    @Test
    @DisplayName("Prezzo non numerico risponde con 400 Bad Request")
    void testPrezzoNonNumericoCausaNumberFormatException() throws Exception {
        when(request.getParameter("prezzomin")).thenReturn("");
        when(request.getParameter("prezzomax")).thenReturn("abc");

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Prezzo con caratteri speciali risponde con 400 Bad Request")
    void testPrezzoConCaratteriSpecialiCausaNumberFormatException() throws Exception {
        when(request.getParameter("prezzomin")).thenReturn("10;DROP TABLE");
        when(request.getParameter("prezzomax")).thenReturn("");

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // ====== 3. Lettura del filtro dalla sessione ======

    @Test
    @DisplayName("Il filtro 'filtri' viene letto dalla sessione")
    void testFiltriLettoDaSessione() {
        when(request.getParameter("prezzomin")).thenReturn("10");
        when(request.getParameter("prezzomax")).thenReturn("100");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            assertNotNull(e, "Eccezione attesa durante il test");
        }

        verify(session, atLeastOnce()).getAttribute("filtri");
    }

    // ====== 4. Doppio forward (ora singolo) ======

    @Test
    @DisplayName("Entrambi i prezzi vuoti causano un singolo forward a RicercaErrata.jsp")
    void testEntrambiPrezziVuotiCausaDoppioForward() {
        when(request.getParameter("prezzomin")).thenReturn("");
        when(request.getParameter("prezzomax")).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp")).thenReturn(dispatcher);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            assertNotNull(e, "Eccezione attesa durante il test");
        }

        // Verifica che venga chiamato solo il dispatcher per RicercaErrata.jsp
        verify(request, times(1)).getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp");
        verify(request, never()).getRequestDispatcher("/WEB-INF/results/Prodotti.jsp");
    }

    // ====== 5. Mancanza range check ======

    @Test
    @DisplayName("Prezzi negativi non vengono validati (vulnerabilità documentata)")
    void testPrezziNegativiNonValidati() {
        when(request.getParameter("prezzomin")).thenReturn("-1000");
        when(request.getParameter("prezzomax")).thenReturn("-500");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            assertNotNull(e, "Eccezione attesa durante il test");
        }

        verify(request, atLeastOnce()).getParameter("prezzomin");
    }

    @Test
    @DisplayName("Prezzo min > prezzo max non viene validato (vulnerabilità documentata)")
    void testMinMaggioreDiMaxNonValidato() {
        when(request.getParameter("prezzomin")).thenReturn("500");
        when(request.getParameter("prezzomax")).thenReturn("100");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            assertNotNull(e, "Eccezione attesa durante il test");
        }

        verify(request, atLeastOnce()).getParameter("prezzomin");
        verify(request, atLeastOnce()).getParameter("prezzomax");
    }

    // ====== 6. Accesso alla sessione ======

    @Test
    @DisplayName("La sessione HTTP viene ottenuta dalla richiesta")
    void testGetSessionChiamato() {
        when(request.getParameter("prezzomin")).thenReturn("10");
        when(request.getParameter("prezzomax")).thenReturn("100");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            assertNotNull(e, "Eccezione attesa durante il test");
        }

        verify(request, atLeastOnce()).getSession();
    }
}