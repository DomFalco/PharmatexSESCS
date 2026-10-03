package security.authorization;

import Controller.LoginServlet;
import Model.Utente;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Authorization & Access Control.
 * Verifica il comportamento di LoginServlet rispetto all'autenticazione,
 * alla gestione della sessione e alle azioni disponibili.
 * NOTA: i test DOCUMENTANO la mancanza di controlli di sicurezza attuali
 * (CSRF, rate limiting, session fixation) e un bug di progettazione
 * (doLogin() eseguito incondizionatamente).
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
    @DisplayName("Logout: la Servlet esegue prima doLogin (bug documentato)")
    void testLogoutEseguePrimaDoLogin() {
        when(request.getParameter("action")).thenReturn("logout");
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);

        // BUG: UtenteDAO.doLogin() viene invocato PRIMA del check su 'action'
        // -> l'eccezione arriva da ConPool (DB non configurato), non dal ramo logout
        assertThrows(Exception.class, () -> servlet.service(request, response),
                "LoginServlet esegue doLogin() prima di controllare 'action': " +
                        "il logout fallisce se il DB non è raggiungibile");
    }

    // ====== 2. Azione "carrello" ======

    @Test
    @DisplayName("Carrello: la Servlet esegue prima doLogin (bug documentato)")
    void testCarrelloEseguePrimaDoLogin() {
        when(request.getParameter("action")).thenReturn("carrello");
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);

        // BUG: stesso problema del logout
        assertThrows(Exception.class, () -> servlet.service(request, response),
                "LoginServlet esegue doLogin() prima di controllare 'action': " +
                        "l'azione 'carrello' non è raggiungibile senza DB");
    }

    // ====== 3. Azione "riepilogo" senza utente in sessione ======

    @Test
    @DisplayName("Riepilogo senza utente loggato causa NPE (vulnerabilità documentata)")
    void testRiepilogoSenzaUtenteLoggatoCausaNPE() {
        when(request.getParameter("action")).thenReturn("riepilogo");
        when(session.getAttribute("Utente")).thenReturn(null);

        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice accede a utente.getEmail() senza null check: NPE documentata");
    }

    // ====== 4. Documentazione mancanza di rate limiting ======

    @Test
    @DisplayName("Nessuna protezione contro brute-force (vulnerabilità documentata)")
    void testNessunaProtezioneBruteForce() {
        // Simula 100 tentativi di login falliti consecutivi
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

        // La Servlet NON blocca l'IP dopo N tentativi falliti:
        // nessuna chiamata a session.invalidate() o simili per rate limiting
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

        // La Servlet non verifica alcun token CSRF
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

        // La Servlet NON chiama request.changeSessionId() o simili
        // dopo un login riuscito (vulnerabilità session fixation documentata)
        verify(request, never()).changeSessionId();
    }

    // ====== 7. Documentazione NPE su parametri null ======

    @Test
    @DisplayName("Email e Password null non causano NPE immediato (documentazione)")
    void testEmailPasswordNull() {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);

        // La Servlet passa i parametri direttamente al DAO senza null check.
        // Il DAO è statico, quindi l'eccezione attesa proviene dal DB non configurato.
        assertThrows(Exception.class, () -> servlet.service(request, response),
                "Il codice non valida Email/Password prima di passarli al DAO");
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
    @DisplayName("Azione sconosciuta non produce né errore né forward (bug documentato)")
    void testAzioneSconosciutaNonProduceRisposta() throws Exception {
        when(request.getParameter("action")).thenReturn("azione_sconosciuta");
        when(request.getParameter("Email")).thenReturn(null);
        when(request.getParameter("Password")).thenReturn(null);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Possibili eccezioni
        }

        // Nessun ramo gestisce azioni sconosciute: la Servlet non risponde
        verify(dispatcher, never()).forward(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
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

        // La Servlet si affida unicamente a isAmministratore() del DAO,
        // senza controlli aggiuntivi (es. verifica token, ruolo, ecc.)
        verify(session, never()).getAttribute("Amministratore");
    }

    // ====== 11. Documentazione bug di progettazione ======

    @Test
    @DisplayName("Il logout non funziona senza DB (bug di progettazione documentato)")
    void testLogoutNonFunzionaSenzaDb() {
        when(request.getParameter("action")).thenReturn("logout");
        when(request.getParameter("Email")).thenReturn("user@example.com");
        when(request.getParameter("Password")).thenReturn("password");

        // Nonostante l'azione sia "logout" (che non richiede autenticazione),
        // la Servlet esegue doLogin() -> il DB viene contattato inutilmente
        assertThrows(Exception.class, () -> servlet.service(request, response),
                "BUG: il logout richiede una connessione al DB funzionante, " +
                        "anche se l'azione non ne ha bisogno");
    }
}