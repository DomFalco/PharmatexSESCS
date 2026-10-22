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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Authorization & Access Control.
 * Verifica il comportamento di LoginServlet rispetto all'autenticazione,
 * alla gestione della sessione e alle azioni disponibili.
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

    // ====== 1. Azione "logout" ======

    @Test
    @DisplayName("Logout: la Servlet esegue prima doLogin, che fallisce senza DB e risponde 500")
    void testLogoutEseguePrimaDoLogin() throws Exception {
        when(request.getParameter("action")).thenReturn("logout");
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);

        servlet.service(request, response);

        // FIX: La Servlet ora cattura l'eccezione e risponde 500
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    // ====== 2. Azione "carrello" ======

    @Test
    @DisplayName("Carrello: la Servlet esegue prima doLogin, che fallisce senza DB e risponde 500")
    void testCarrelloEseguePrimaDoLogin() throws Exception {
        when(request.getParameter("action")).thenReturn("carrello");
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);

        servlet.service(request, response);

        // FIX: La Servlet ora cattura l'eccezione e risponde 500
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    // ====== 3. Azione "riepilogo" senza utente in sessione ======

    @Test
    @DisplayName("Riepilogo senza utente loggato risponde 500 (NPE gestita)")
    void testRiepilogoSenzaUtenteLoggatoCausaNPE() throws Exception {
        when(request.getParameter("action")).thenReturn("riepilogo");
        when(session.getAttribute("Utente")).thenReturn(null);

        servlet.service(request, response);

        // FIX: La Servlet ora cattura la NPE e risponde 500
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    // ====== 4. Documentazione mancanza di rate limiting ======

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
                // Eccezione attesa dal DB non configurato
            }
        }

        verify(session, never()).invalidate();
    }

    // ====== 5. Documentazione mancanza di protezione CSRF ======

    @Test
    @DisplayName("Nessun controllo CSRF token presente (vulnerabilità documentata)")
    void testNessunControlloCsrf() {
        when(request.getParameter("action")).thenReturn("logout");
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa: il codice crasha prima di controllare CSRF
        }

        verify(request, never()).getParameter("csrf_token");
        verify(session, never()).getAttribute("csrf_token");
    }

    // ====== 6. Documentazione session fixation ======

    @Test
    @DisplayName("Session fixation: la sessione non viene rigenerata al login (vulnerabilità documentata)")
    void testSessionFixation() {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("Email")).thenReturn("test@example.com");
        when(request.getParameter("Password")).thenReturn("password");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, never()).changeSessionId();
    }

    // ====== 7. Email e Password null ======

    @Test
    @DisplayName("Email e Password null rispondono 500 (eccezione gestita)")
    void testEmailPasswordNull() throws Exception {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);

        servlet.service(request, response);

        // FIX: La Servlet ora cattura l'eccezione e risponde 500
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    // ====== 8. Lettura dei parametri Email e Password ======

    @Test
    @DisplayName("I parametri Email e Password vengono letti dalla richiesta")
    void testEmailPasswordVengonoLetti() {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("Email")).thenReturn("test@example.com");
        when(request.getParameter("Password")).thenReturn("password");

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(request, atLeastOnce()).getParameter("Email");
        verify(request, atLeastOnce()).getParameter("Password");
    }

    // ====== 9. Azione non riconosciuta ======

    @Test
    @DisplayName("Azione sconosciuta con doLogin fallito risponde 500")
    void testAzioneSconosciutaNonProduceRisposta() throws Exception {
        when(request.getParameter("action")).thenReturn("azione_sconosciuta");
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);

        servlet.service(request, response);

        // FIX: La Servlet esegue doLogin (fallisce) e cattura l'eccezione, rispondendo 500
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
        verify(dispatcher, never()).forward(request, response);
    }

    // ====== 10. La Servlet non controlla mai l'autorizzazione admin ======

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
            // Eccezione attesa dal DB
        }

        verify(session, never()).getAttribute("Amministratore");
    }

    // ====== 11. Documentazione bug di progettazione ======

    @Test
    @DisplayName("Il logout senza DB risponde 500 (eccezione gestita)")
    void testLogoutNonFunzionaSenzaDb() throws Exception {
        when(request.getParameter("action")).thenReturn("logout");
        when(request.getParameter("Email")).thenReturn("user@example.com");
        when(request.getParameter("Password")).thenReturn("password");

        servlet.service(request, response);

        // FIX: La Servlet ora cattura l'eccezione e risponde 500
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }
}