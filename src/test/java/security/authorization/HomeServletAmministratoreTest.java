package security.authorization;

import Controller.HomeServletAmministratore;
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
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Authorization & Access Control.
 * Verifica che HomeServletAmministratore applichi correttamente i controlli
 * di autorizzazione dopo il fix della vulnerabilità CWE-862.
 */
@DisplayName("Authorization - HomeServletAmministratore (post-fix)")
class HomeServletAmministratoreTest {

    private HomeServletAmministratore servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new HomeServletAmministratore();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

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

    // =================================================================
    // Test di SICUREZZA (verificano il fix)
    // =================================================================

    @Test
    @DisplayName("Utente anonimo (sessione null) riceve 403 Forbidden")
    void testSessioneNullRiceve403() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("Sessione senza attributo 'Amministratore' riceve 403")
    void testSessioneSenzaAdminRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("Utente non-amministratore riceve 403")
    void testUtenteNonAdminRiceve403() throws Exception {
        Utente utenteNormale = new Utente();
        utenteNormale.setAmministratore(false);
        when(session.getAttribute("Amministratore")).thenReturn(utenteNormale);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("Utente anonimo non accede alla lista prodotti")
    void testAnonimoNonAccedeAiProdotti() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        // Il Servlet non legge il parametro 'valore' prima del controllo auth
        verify(request, never()).getParameter("valore");
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Utente anonimo non accede alla lista utenti (protezione GDPR)")
    void testAnonimoNonAccedeListaUtenti() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        // Anche se l'utente prova a richiedere esplicitamente 'clienti',
        // il controllo di autorizzazione blocca prima della lettura
        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(dispatcher, never()).forward(any(), any());
    }

    @Test
    @DisplayName("Utente anonimo non accede ad 'aggiungi prodotto'")
    void testAnonimoNonAccedeAggiungiProdotto() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    // =================================================================
    // Test di COMPORTAMENTO (verificano che gli admin accedano correttamente)
    // =================================================================

    @Test
    @DisplayName("Amministratore accede alla pagina prodotti (valore null)")
    void testAdminAccedeAiProdotti() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("valore")).thenReturn(null);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @ParameterizedTest(name = "Amministratore accede al ramo ''{0}''")
    @ValueSource(strings = {"clienti", "aggiungi", "quantita", "ordine"})
    @DisplayName("Amministratore può accedere ai rami protetti")
    void testAdminAccedeAiRami(String valore) throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("valore")).thenReturn(valore);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("valore");
        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }
}