package security.unit.authorization;

import controller.LoginServlet;
import model.Utente;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Authorization & Access Control.
 */
@DisplayName("Authorization - LoginServlet")
class LoginServletTest {

    private LoginServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new LoginServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getMethod()).thenReturn("POST");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @ParameterizedTest(name = "Azione: {0}, Email: {1}, Password: {2} -> Errore gestito")
    @CsvSource({
            "logout, null, null",
            "carrello, null, null",
            "null, null, null",
            "logout, user@example.com, password"
    })
    @DisplayName("Test parametrizzato per azioni con errori o parametri null")
    void testParametriInvalidoOErroreDb(String action, String email, String password) throws Exception {
        when(request.getParameter("action")).thenReturn(action);
        when(request.getParameter("Email")).thenReturn(email);
        when(request.getParameter("Password")).thenReturn(password);

        servlet.service(request, response);

        // In questi casi il DB lancia IllegalStateException (mancata variabile d'ambiente)
        // e la Servlet la cattura con sendError(500). Verifichiamo che non vada in crash.
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    @DisplayName("Riepilogo senza utente loggato reindirizza al Login (Fix NPE)")
    void testRiepilogoSenzaUtenteLoggatoRedirigeAlLogin() throws Exception {
        when(request.getParameter("action")).thenReturn("riepilogo");
        when(session.getAttribute("Utente")).thenReturn(null);

        servlet.service(request, response);

        // FIX: Ora la Servlet non va in NPE, ma fa un forward al Login
        verify(dispatcher).forward(request, response);
        verify(response, never()).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    @DisplayName("Nessuna protezione contro brute-force (vulnerabilità documentata)")
    void testNessunaProtezioneBruteForce() {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("Email")).thenReturn("attacker@example.com");
        when(request.getParameter("Password")).thenReturn("wrongpassword");

        for (int i = 0; i < 100; i++) {
            try {
                servlet.service(request, response);
            } catch (Exception e) {
                // Eccezione attesa
            }
        }

        verify(session, never()).invalidate();
    }

    @Test
    @DisplayName("Nessun controllo CSRF token presente (vulnerabilità documentata)")
    void testNessunControlloCsrf() {
        when(request.getParameter("action")).thenReturn("logout");
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa
        }

        verify(request, never()).getParameter("csrf_token");
        verify(session, never()).getAttribute("csrf_token");
    }

    @Test
    @DisplayName("Session fixation: la sessione non viene rigenerata al login (vulnerabilità documentata)")
    void testSessionFixation() {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("Email")).thenReturn("test@example.com");
        when(request.getParameter("Password")).thenReturn("password");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa
        }

        verify(request, never()).changeSessionId();
    }

    @Test
    @DisplayName("I parametri Email e Password vengono letti dalla richiesta")
    void testEmailPasswordVengonoLetti() {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("Email")).thenReturn("test@example.com");
        when(request.getParameter("Password")).thenReturn("password");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa
        }

        verify(request, atLeastOnce()).getParameter("Email");
        verify(request, atLeastOnce()).getParameter("Password");
    }

    @Test
    @DisplayName("Azione sconosciuta non lancia eccezioni non gestite")
    void testAzioneSconosciutaNonProduceRisposta() throws Exception {
        when(request.getParameter("action")).thenReturn("azione_sconosciuta");
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);

        servlet.service(request, response);

        // FIX: Con l'azione sconosciuta e i parametri null, doLogin restituisce null.
        // La Servlet semplicemente ignora l'azione (nessun forward, nessun sendError).
        // Verifichiamo solo che non ci siano errori 500 lanciati per azioni sconosciute.
        verify(response, never()).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    @DisplayName("Nessun controllo esplicito di autorizzazione admin (documentazione)")
    void testNessunControlloAutorizzazioneAdmin() {
        when(request.getParameter("action")).thenReturn("riepilogo");
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);
        when(session.getAttribute("Utente")).thenReturn(new Utente());

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa
        }

        verify(session, never()).getAttribute("Amministratore");
    }
}