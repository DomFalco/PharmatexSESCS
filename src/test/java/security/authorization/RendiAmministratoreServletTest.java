package security.authorization;

import Controller.RendiAmministratoreServlet;
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
    // Rimosso: private RequestDispatcher dispatcher; (convertito in variabile locale)

    @BeforeEach
    void setUp() {
        servlet = new RendiAmministratoreServlet();
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
    // TEST DI VALIDAZIONE INPUT (post-fix)
    // =================================================================

    @Test
    @DisplayName("Admin: parametro 'action' null restituisce 400")
    void testAdminActionNullRestituisce400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("action")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin: azione sconosciuta restituisce 400")
    void testAdminAzioneSconosciutaRestituisce400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("action")).thenReturn("azione_bizzarra");

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin: 'amministratore' senza email restituisce 400")
    void testAdminAmministratoreSenzaEmailRestituisce400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("action")).thenReturn("amministratore");

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin: 'rimuovipermessi' senza email restituisce 400")
    void testAdminRimuoviPermessiSenzaEmailRestituisce400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("action")).thenReturn("rimuovipermessi");

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

        // L'admin supera il controllo e non riceve 403
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

    // =================================================================
    // TEST DI SICUREZZA: matching permissivo
    // =================================================================

    @Test
    @DisplayName("'superamministratore' NON viene interpretato come 'amministratore'")
    void testMatchingPermissivoBloccato() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        // Con il vecchio codice, "superamministratoreX" matchava "amministratore"
        // Con startsWith("amministratore"), NON matcha più
        when(request.getParameter("action")).thenReturn("superamministratoreX");

        servlet.service(request, response);

        // Deve essere riconosciuto come azione sconosciuta
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("'finto_amministratore' NON viene interpretato come valido")
    void testStringaConAmministratoreNelMezzoBloccata() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("action")).thenReturn("finto_amministratoreX");

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }
}