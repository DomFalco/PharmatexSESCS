package security.functional.authorization;

import Controller.PagamentoServlet;
import Model.Utente;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di PagamentoServlet con H2 in-memory.
 * Verifica:
 * - Autorizzazione: sessione mancante / utente non autenticato → 403
 * - Utente autenticato → forward a Pagamento.jsp
 * - FINDING: un admin (attributo "Amministratore") non può pagare,
 *   perché la Servlet cerca solo l'attributo "Utente"
 */
@DisplayName("Functional - PagamentoServlet (Authorization)")
class PagamentoServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(PagamentoServlet servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    private Utente utenteNormale() {
        Utente u = new Utente();
        u.setEmail("mario@test.com");
        u.setAmministratore(false);
        return u;
    }

    private Utente admin() {
        Utente u = new Utente();
        u.setEmail("admin@test.com");
        u.setAmministratore(true);
        return u;
    }

    // ==================================================================
    // Autorizzazione (403)
    // ==================================================================

    @Test
    @DisplayName("Sessione mancante → 403")
    void testSessioneNullRiceve403() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        invokeService(new PagamentoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Utente non autenticato (attributo 'Utente' null) → 403")
    void testUtenteNullRiceve403() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(null);

        invokeService(new PagamentoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    // ==================================================================
    // Utente autenticato → forward
    // ==================================================================

    @Test
    @DisplayName("Utente autenticato → forward a Pagamento.jsp")
    void testUtenteAutenticatoForward() throws Exception {
        when(session.getAttribute("Utente")).thenReturn(utenteNormale());
        when(request.getRequestDispatcher("/WEB-INF/results/Pagamento.jsp"))
                .thenReturn(dispatcher);

        invokeService(new PagamentoServlet());

        verify(dispatcher).forward(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    // ==================================================================
    // FINDING: admin non può pagare
    // ==================================================================

    /**
     * FINDING (documentato): un admin non può pagare.
     * La Servlet controlla solo l'attributo "Utente" in sessione. Ma quando
     * un admin fa login, LoginServlet imposta l'attributo "Amministratore"
     * (non "Utente"). Quindi un admin che visita /PagamentoServlet viene
     * respinto con 403 "utente non autenticato" — anche se è loggato.
     * Conseguenza: l'admin, che è anche un utente, non può acquistare
     * prodotti. Bug di design: la Servlet dovrebbe accettare entrambi
     * gli attributi ("Utente" o "Amministratore").
     * Fix suggerito:
     *   Utente utente = (Utente) session.getAttribute("Utente");
     *   if (utente == null) utente = (Utente) session.getAttribute("Amministratore");
     *   if (utente == null) { sendError 403; return; }
     */
    @Test
    @DisplayName("FINDING: admin loggato non può pagare (cerca solo 'Utente')")
    void testAdminNonPuoPagare_BugDocumentato() throws Exception {
        // Simula admin loggato: attributo "Amministratore" presente, "Utente" assente
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(session.getAttribute("Utente")).thenReturn(null);

        invokeService(new PagamentoServlet());

        // L'admin riceve 403 "utente non autenticato" (bug)
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN),
                eq("Accesso negato: utente non autenticato."));
    }

    // ==================================================================
    // Verifica path doGet → doPost
    // ==================================================================

    @Test
    @DisplayName("doGet inoltra a doPost (comportamento trasparente)")
    void testDoGetInoltraADoPost() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(session.getAttribute("Utente")).thenReturn(utenteNormale());
        when(request.getRequestDispatcher("/WEB-INF/results/Pagamento.jsp"))
                .thenReturn(dispatcher);

        invokeService(new PagamentoServlet());

        verify(dispatcher).forward(request, response);
    }
}