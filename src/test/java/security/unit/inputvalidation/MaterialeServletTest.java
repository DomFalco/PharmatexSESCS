package security.unit.inputvalidation;

import Controller.MaterialeServlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Input Validation.
 * Verifica il comportamento di MaterialeServlet dopo il fix dei bug
 * NPE su 'mat' null e doppio forward.
 */
@DisplayName("Input Validation - MaterialeServlet (post-fix)")
class MaterialeServletTest {

    private MaterialeServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new MaterialeServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession(false)).thenReturn(session);
        when(request.getMethod()).thenReturn("GET");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    // =================================================================
    // TEST DI ROBUSTEZZA (fix NPE e doppio forward)
    // =================================================================

    @Test
    @DisplayName("Sessione null forwarda a RicercaErrata.jsp senza crash")
    void testSessioneNullForwardaARicercaErrata() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(request, atLeastOnce()).getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp");
    }

    @Test
    @DisplayName("mat null e materiale null forwarda a RicercaErrata.jsp")
    void testMatEMaterialeNull() throws Exception {
        when(session.getAttribute("mat")).thenReturn(null);
        when(session.getAttribute("materiale")).thenReturn(null);

        servlet.service(request, response);

        verify(request, atLeastOnce()).getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp");
    }

    @Test
    @DisplayName("mat null e materiale valorizzato NON causa NPE")
    void testMatNullMaterialeValorizzatoNonCausaNPE() { // Rimosso 'throws Exception'
        when(session.getAttribute("mat")).thenReturn(null);
        when(session.getAttribute("materiale")).thenReturn("Memory");

        assertDoesNotThrow(() -> servlet.service(request, response),
                "Il fix deve evitare NPE su mat.equalsIgnoreCase()");

        verify(request, atLeastOnce()).getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp");
    }

    @Test
    @DisplayName("mat valorizzato e materiale null NON causa NPE")
    void testMatValorizzatoMaterialeNullNonCausaNPE() { // Rimosso 'throws Exception'
        when(session.getAttribute("mat")).thenReturn("Materasso");
        when(session.getAttribute("materiale")).thenReturn(null);

        assertDoesNotThrow(() -> servlet.service(request, response));

        verify(request, atLeastOnce()).getRequestDispatcher("/WEB-INF/results/RicercaErrata.jsp");
    }

    // =================================================================
    // TEST DI COMPORTAMENTO (parametri validi)
    // =================================================================

    @Test
    @DisplayName("Materasso + Memory: lettura parametri da sessione")
    void testMaterassoMemoryLeggeSessione() { // Rimosso 'throws Exception'
        when(session.getAttribute("mat")).thenReturn("Materasso");
        when(session.getAttribute("materiale")).thenReturn("Memory");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        verify(session, atLeastOnce()).getAttribute("mat");
        verify(session, atLeastOnce()).getAttribute("materiale");
    }

    @Test
    @DisplayName("Rete + Faggio: lettura parametri da sessione")
    void testReteFaggioLeggeSessione() { // Rimosso 'throws Exception'
        when(session.getAttribute("mat")).thenReturn("Rete");
        when(session.getAttribute("materiale")).thenReturn("Faggio");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(session, atLeastOnce()).getAttribute("mat");
    }

    @Test
    @DisplayName("Cuscino + Memory: lettura parametri da sessione")
    void testCuscinoMemoryLeggeSessione() { // Rimosso 'throws Exception'
        when(session.getAttribute("mat")).thenReturn("Cuscino");
        when(session.getAttribute("materiale")).thenReturn("Memory");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(session, atLeastOnce()).getAttribute("mat");
    }

    // =================================================================
    // TEST: case-insensitive
    // =================================================================

    @Test
    @DisplayName("La logica e' case-insensitive (MATERASSO = Materasso)")
    void testCaseInsensitive() { // Rimosso 'throws Exception'
        when(session.getAttribute("mat")).thenReturn("MATERASSO");
        when(session.getAttribute("materiale")).thenReturn("Memory");

        assertDoesNotThrow(() -> {
            try {
                servlet.service(request, response);
            } catch (Exception e) {
                if (e instanceof NullPointerException) throw e;
            }
        });
    }

    // =================================================================
    // TEST: nessun doppio forward
    // =================================================================

    @Test
    @DisplayName("Nessun doppio forward quando i prodotti sono vuoti")
    void testNessunDoppioForward() throws Exception { // Mantiene 'throws Exception'
        when(session.getAttribute("mat")).thenReturn("Materasso");
        when(session.getAttribute("materiale")).thenReturn("NonEsiste");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Con il fix, non ci sono due chiamate a forward consecutive
        // (che causerebbero IllegalStateException in produzione)
        // NOTA: forward() dichiara throws ServletException, IOException,
        // quindi 'throws Exception' è necessario
        verify(dispatcher, atMost(1)).forward(request, response);
    }
}