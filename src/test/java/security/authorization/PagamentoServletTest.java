package security.authorization;

import Controller.PagamentoServlet;
import Model.Utente;
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

/**
 * Test dell'area OWASP: Authorization & Access Control.
 * Verifica che PagamentoServlet richieda un utente autenticato per accedere
 * alla pagina di pagamento, dopo il fix della vulnerabilità:
 * - CWE-862: Missing Authorization
 */
@DisplayName("Authorization - PagamentoServlet (post-fix)")
class PagamentoServletTest {

    private PagamentoServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new PagamentoServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession(false)).thenReturn(session);
        when(request.getMethod()).thenReturn("POST");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private Utente creaUtenteLoggato() {
        Utente u = new Utente();
        u.setEmail("cliente@example.com");
        return u;
    }

    // =================================================================
    // TEST DI SICUREZZA (verificano il fix di autenticazione)
    // =================================================================

    @Test
    @DisplayName("Utente anonimo (sessione null) riceve 403 Forbidden")
    void testAnonimoSessioneNullRiceve403() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("Sessione senza attributo 'Utente' riceve 403")
    void testSessioneSenzaUtenteRiceve403() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("Utente anonimo non raggiunge il dispatcher")
    void testAnonimoNonRaggiungePagamentoJsp() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(request, never()).getRequestDispatcher("/WEB-INF/results/Pagamento.jsp");
    }

    // =================================================================
    // TEST DI COMPORTAMENTO (utente loggato)
    // =================================================================

    @Test
    @DisplayName("Utente loggato accede alla pagina di pagamento")
    void testUtenteLoggatoAccede() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(creaUtenteLoggato());

        servlet.service(request, response);

        verify(dispatcher).forward(request, response);
        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Utente loggato viene forwardato a Pagamento.jsp")
    void testUtenteLoggatoForwardatoAPagamentoJsp() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(creaUtenteLoggato());
        when(request.getRequestDispatcher("/WEB-INF/results/Pagamento.jsp")).thenReturn(dispatcher);

        servlet.service(request, response);

        verify(request, atLeastOnce()).getRequestDispatcher("/WEB-INF/results/Pagamento.jsp");
    }

    // =================================================================
    // TEST: la Servlet non legge parametri (semplice, senza input)
    // =================================================================

    @Test
    @DisplayName("La Servlet non legge alcun parametro dalla richiesta")
    void testNessunParametroLetto() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(creaUtenteLoggato());

        servlet.service(request, response);

        // La Servlet non ha parametri: non chiama mai getParameter()
        verify(request, never()).getParameter(anyString());
    }
}