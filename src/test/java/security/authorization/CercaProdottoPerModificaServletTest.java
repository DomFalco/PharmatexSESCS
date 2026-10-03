package security.authorization;

import Controller.CercaProdottoPerModificaServlet;
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
 * Verifica che CercaProdottoPerModificaServlet applichi i controlli di
 * autorizzazione admin e di validazione input dopo il fix (CWE-862, CWE-20).
 */
@DisplayName("Authorization - CercaProdottoPerModificaServlet (post-fix)")
class CercaProdottoPerModificaServletTest {

    private CercaProdottoPerModificaServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    // Rimosso: private RequestDispatcher dispatcher; (convertito in variabile locale)

    @BeforeEach
    void setUp() {
        servlet = new CercaProdottoPerModificaServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        // Aggiunto come variabile locale
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getSession(false)).thenReturn(session);
        when(request.getMethod()).thenReturn("POST");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private Utente creaAdmin() {
        Utente u = new Utente();
        u.setAmministratore(true);
        return u;
    }

    private Utente creaUtenteNormale() {
        Utente u = new Utente();
        u.setAmministratore(false);
        return u;
    }

    // =================================================================
    // TEST DI SICUREZZA (fix Broken Access Control)
    // =================================================================

    @Test
    @DisplayName("Utente anonimo (sessione null) riceve 403 Forbidden")
    void testAnonimoSessioneNullRiceve403() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(request, never()).getParameter("search");
    }

    @Test
    @DisplayName("Utente normale riceve 403 Forbidden")
    void testUtenteNormaleRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(request, never()).getParameter("search");
    }

    @Test
    @DisplayName("Utente con isAmministratore=false riceve 403")
    void testUtenteFlagFalseRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaUtenteNormale());

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Anonimo non raggiunge il dispatcher admin")
    void testAnonimoNonRaggiungeDispatcher() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(request, never()).getRequestDispatcher("/WEB-INF/amministratore/ModificaProdotto.jsp");
    }

    // =================================================================
    // TEST DI VALIDAZIONE INPUT (fix CWE-20)
    // =================================================================

    @Test
    @DisplayName("Admin: 'search' null restituisce 400 Bad Request")
    void testAdminSearchNullRestituisce400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("search")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin: 'search' con <script> restituisce 400")
    void testAdminSearchScriptRestituisce400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("search")).thenReturn("<script>alert(1)</script>");

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin: 'search' con SQL injection restituisce 400")
    void testAdminSearchSqlInjectionRestituisce400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("search")).thenReturn("' OR '1'='1");

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin: 'search' con simboli restituisce 400")
    void testAdminSearchSimboliRestituisce400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("search")).thenReturn("cat/../etc");

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // =================================================================
    // TEST DI COMPORTAMENTO (admin + input valido)
    // =================================================================

    @Test
    @DisplayName("Admin con input valido supera i controlli di guardia")
    void testAdminInputValidoSuperaControlli() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("search")).thenReturn("Materasso");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        // Non deve ricevere 403 né 400
        // NOTA: sendError() dichiara throws IOException, quindi serve 'throws Exception'
        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(response, never()).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin con input valido legge il parametro 'search'")
    void testAdminLeggeParametroSearch() { // Rimosso 'throws Exception'
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("search")).thenReturn("Materasso");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("search");
    }
}