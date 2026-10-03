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

    /**
     * Helper: crea un Utente amministratore valido.
     */
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

        verify(request, never()).getParameter("valore");
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Utente anonimo non accede alla lista utenti (GDPR)")
    void testAnonimoNonAccedeListaUtenti() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(request, never()).getParameter("valore");
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
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

        // L'admin supera il controllo e arriva al ramo di codice
        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Amministratore può raggiungere il ramo 'clienti'")
    void testAdminAccedeClienti() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("valore")).thenReturn("clienti");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // L'admin legge il parametro 'valore' (supera il controllo di autorizzazione)
        verify(request, atLeastOnce()).getParameter("valore");
        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Amministratore può raggiungere il ramo 'aggiungi'")
    void testAdminAccedeAggiungi() { // Rimosso 'throws Exception'
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("valore")).thenReturn("aggiungi");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa
        }

        verify(request, atLeastOnce()).getParameter("valore");
    }

    @Test
    @DisplayName("Amministratore può raggiungere il ramo 'quantita'")
    void testAdminAccedeQuantita() { // Rimosso 'throws Exception'
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("valore")).thenReturn("quantita");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa
        }

        verify(request, atLeastOnce()).getParameter("valore");
    }

    @Test
    @DisplayName("Amministratore può raggiungere il ramo 'ordine'")
    void testAdminAccedeOrdine() { // Rimosso 'throws Exception'
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("valore")).thenReturn("ordine");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa
        }

        verify(request, atLeastOnce()).getParameter("valore");
    }
}