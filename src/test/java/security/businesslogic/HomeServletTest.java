package security.businesslogic;

import Controller.HomeServlet;
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
 * Test dell'area OWASP: Business Logic / Authorization.
 * Verifica il comportamento di HomeServlet dopo il fix del controllo
 * di autorizzazione admin sulla pagina "home" (ex vulnerabilità CWE-862).
 */
@DisplayName("Business Logic - HomeServlet (post-fix)")
class HomeServletTest {

    private HomeServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new HomeServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession(false)).thenReturn(session);
        when(request.getMethod()).thenReturn("GET");
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
    // TEST DI SICUREZZA (fix del Broken Access Control)
    // =================================================================

    @Test
    @DisplayName("Utente anonimo con 'valore=home' riceve 403 Forbidden")
    void testAnonimoConValoreHomeRiceve403() throws Exception {
        when(request.getParameter("valore")).thenReturn("home");
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("Utente normale con 'valore=home' riceve 403 Forbidden")
    void testUtenteNormaleConValoreHomeRiceve403() throws Exception {
        when(request.getParameter("valore")).thenReturn("home");
        when(session.getAttribute("Amministratore")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Utente con isAmministratore=false riceve 403")
    void testUtenteFlagFalseRiceve403() throws Exception {
        when(request.getParameter("valore")).thenReturn("home");
        when(session.getAttribute("Amministratore")).thenReturn(creaUtenteNormale());

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Anonimo non raggiunge il dispatcher admin")
    void testAnonimoNonRaggiungeDispatcherAdmin() throws Exception {
        when(request.getParameter("valore")).thenReturn("home");
        when(request.getSession(false)).thenReturn(null);

        servlet.service(request, response);

        verify(request, never()).getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp");
    }

    // =================================================================
    // TEST DI COMPORTAMENTO (utente admin)
    // =================================================================

    @Test
    @DisplayName("Admin con 'valore=home' accede alla pagina amministratore")
    void testAdminConValoreHomeAccede() throws Exception {
        when(request.getParameter("valore")).thenReturn("home");
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());
        when(request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp")).thenReturn(dispatcher);

        servlet.service(request, response);

        verify(dispatcher).forward(request, response);
        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Admin viene forwardato al JSP corretto")
    void testAdminForwardatoAlJspAmministratore() throws Exception {
        when(request.getParameter("valore")).thenReturn("home");
        when(session.getAttribute("Amministratore")).thenReturn(creaAdmin());

        servlet.service(request, response);

        verify(request, atLeastOnce()).getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp");
    }

    // =================================================================
    // TEST DI COMPORTAMENTO (home pubblica - valore null)
    // =================================================================

    @Test
    @DisplayName("Home pubblica (valore null) non richiede autenticazione")
    void testHomePubblicaNonRichiedeAutenticazione() throws Exception {
        when(request.getParameter("valore")).thenReturn(null);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        // Non deve ricevere 403: la home pubblica è accessibile a tutti
        // NOTA: sendError() dichiara throws IOException, quindi serve 'throws Exception'
        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Home pubblica imposta l'attributo 'Valore' (numero casuale)")
    void testHomePubblicaImpostaAttributoValore() { // Rimosso 'throws Exception'
        when(request.getParameter("valore")).thenReturn(null);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // Verifica che il Servlet imposti l'attributo "Valore" (numero casuale 5-48)
        // NOTA: setAttribute() non dichiara eccezioni checked → no throws
        verify(request, atLeastOnce()).setAttribute(eq("Valore"), anyInt());
    }

    // =================================================================
    // DOCUMENTAZIONE: azione sconosciuta
    // =================================================================

    @Test
    @DisplayName("Azione sconosciuta restituisce 400 Bad Request")
    void testAzioneSconosciutaRestituisce400() throws Exception {
        when(request.getParameter("valore")).thenReturn("azione_bizzarra");

        servlet.service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // =================================================================
    // DOCUMENTAZIONE: assenza di protezione CSRF (non applicabile)
    // =================================================================

    @Test
    @DisplayName("La Servlet non legge parametri di input (solo 'valore')")
    void testSoloParametroValoreLetto() { // Rimosso 'throws Exception'
        when(request.getParameter("valore")).thenReturn(null);

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        // La Servlet legge solo "valore": non ci sono altri input utente
        // NOTA: getParameter() non dichiara eccezioni checked → no throws
        verify(request, never()).getParameter("prezzo");
        verify(request, never()).getParameter("categoria");
    }
}