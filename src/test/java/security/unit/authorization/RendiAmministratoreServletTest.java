package security.unit.authorization;

import Controller.RendiAmministratoreServlet;
import Model.Utente;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Authorization & Access Control.
 * Verifica che RendiAmministratoreServlet applichi i controlli di
 * autorizzazione dopo il fix delle vulnerabilità critiche:
 * - CWE-862: Missing Authorization (privilege escalation)
 * - CWE-20: Improper Input Validation (parsing di 'action')
 */
@DisplayName("Authorization - RendiAmministratoreServlet (post-fix)")
class RendiAmministratoreServletTest {

    private RendiAmministratoreServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new RendiAmministratoreServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

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
    @DisplayName("Utente anonimo (sessione null) non può promuovere admin")
    void testAnonimoNonPuoPromuovereAdmin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(request, never()).getParameter("action");
    }

    @Test
    @DisplayName("Utente normale non può promuovere se stesso ad admin")
    void testUtenteNormaleNonPuoPromuoversi() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(request, never()).getParameter("action");
    }

    @Test
    @DisplayName("Utente con isAmministratore()=false non può promuovere admin")
    void testUtenteFlagFalseNonPuoPromuovere() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaUtenteNormale());

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Utente normale non può rimuovere i permessi ad altri")
    void testUtenteNormaleNonPuoRimuoverePermessi() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    // =================================================================
    // TEST DI VALIDAZIONE INPUT (post-fix) - Parameterized
    // =================================================================

    @ParameterizedTest(name = "Admin: azione ''{0}'' restituisce 400")
    @ValueSource(strings = {
            "azione_bizzarra",
            "superamministratoreX",
            "finto_amministratoreX",
            "amministratore",
            "rimuovipermessi"
    })
    @DisplayName("Admin: azione non valida o senza email restituisce 400")
    void testAdminAzioneNonValidaRestituisce400(String action) throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("action")).thenReturn(action);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin: parametro 'action' null restituisce 400")
    void testAdminActionNullRestituisce400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("action")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // =================================================================
    // TEST DI COMPORTAMENTO (admin autorizzato)
    // =================================================================

    @Test
    @DisplayName("Admin può promuovere un utente (azione 'amministratoreXXX')")
    void testAdminPuoPromuovere() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("action")).thenReturn("amministratoreutente@example.com");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(response, never()).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin può rimuovere permessi (azione 'rimuovipermessiXXX')")
    void testAdminPuoRimuoverePermessi() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("action")).thenReturn("rimuovipermessiutente@example.com");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }
}