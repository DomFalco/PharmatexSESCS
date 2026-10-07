package security.unit.authorization;

import Controller.RimuoviProdottoServlet;
import Model.Prodotto;
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
 * Verifica che RimuoviProdottoServlet applichi il controllo di autorizzazione
 * admin e il controllo del prodotto in sessione dopo il fix (CWE-862).
 */
@DisplayName("Authorization - RimuoviProdottoServlet (post-fix)")
class RimuoviProdottoServletTest {

    private RimuoviProdottoServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new RimuoviProdottoServlet();
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

    private Utente creaUtenteNormale() {
        Utente u = new Utente();
        u.setAmministratore(false);
        return u;
    }

    private Prodotto creaProdotto() {
        Prodotto p = new Prodotto();
        p.setIdProdotto("MAT001");
        return p;
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
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("Utente normale riceve 403 Forbidden")
    void testUtenteNormaleRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
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

        verify(request, never()).getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp");
    }

    // =================================================================
    // TEST DI SICUREZZA (fix controllo prodotto in sessione)
    // =================================================================

    @Test
    @DisplayName("Admin senza prodotto in sessione riceve 400 Bad Request")
    void testAdminSenzaProdottoRiceve400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(session.getAttribute("idModificaPrezzo")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin con prodotto senza id riceve 400 Bad Request")
    void testAdminProdottoSenzaIdRiceve400() throws Exception {
        Prodotto p = new Prodotto();  // idProdotto è null
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(session.getAttribute("idModificaPrezzo")).thenReturn(p);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // =================================================================
    // TEST DI COMPORTAMENTO (admin + prodotto valido)
    // =================================================================

    @Test
    @DisplayName("Admin con prodotto valido supera i controlli di guardia")
    void testAdminProdottoValidoSuperaControlli() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdotto());

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(response, never()).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin con prodotto valido legge la sessione correttamente")
    void testAdminLeggeProdottoDallaSessione() { // Rimosso 'throws Exception'
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdotto());

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(session, atLeastOnce()).getAttribute("idModificaPrezzo");
    }

    // =================================================================
    // DOCUMENTAZIONE: mancanza di conferma e audit log
    // =================================================================

    @Test
    @DisplayName("Nessuna conferma prima della cancellazione (documentazione)")
    void testNessunaConfermaPrimaDellaCancellazione() { // Rimosso 'throws Exception'
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdotto());

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // La Servlet non chiede conferma: la cancellazione è immediata
        // (fix suggerito: pagina di conferma o doppio click)
        verify(request, never()).getParameter("conferma");
    }

    @Test
    @DisplayName("Nessun log di audit della cancellazione (documentazione)")
    void testNessunAuditLog() { // Rimosso 'throws Exception'
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(session.getAttribute("idModificaPrezzo")).thenReturn(creaProdotto());

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // La Servlet non registra alcun log: impossibile tracciare chi
        // ha cancellato cosa (fix suggerito: log dell'admin, prodotto, timestamp)
        verify(session, never()).setAttribute(eq("auditLog"), any());
    }
}