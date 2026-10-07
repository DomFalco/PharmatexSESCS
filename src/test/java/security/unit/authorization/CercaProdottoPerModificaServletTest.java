package security.unit.authorization;

import Controller.CercaProdottoPerModificaServlet;
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
 * Verifica che CercaProdottoPerModificaServlet applichi i controlli di
 * autorizzazione admin e di validazione input dopo il fix (CWE-862, CWE-20).
 */
@DisplayName("Authorization - CercaProdottoPerModificaServlet (post-fix)")
class CercaProdottoPerModificaServletTest {

    private CercaProdottoPerModificaServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new CercaProdottoPerModificaServlet();
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
    // TEST DI VALIDAZIONE INPUT (fix CWE-20) - Parameterized
    // =================================================================

    @ParameterizedTest(name = "Admin: search=''{0}'' restituisce 400")
    @ValueSource(strings = {
            "<script>alert(1)</script>",
            "' OR '1'='1",
            "cat/../etc",
            "test;drop",
            "@#$%"
    })
    @DisplayName("Admin: input malevolo su 'search' restituisce 400 Bad Request")
    void testAdminSearchMalevoloRestituisce400(String inputMalevolo) throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("search")).thenReturn(inputMalevolo);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin: 'search' null restituisce 400 Bad Request")
    void testAdminSearchNullRestituisce400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getParameter("search")).thenReturn(null);

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

        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(response, never()).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Admin con input valido legge il parametro 'search'")
    void testAdminLeggeParametroSearch() {
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