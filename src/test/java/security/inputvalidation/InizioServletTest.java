package security.inputvalidation;

import Controller.InizioServlet;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Input Validation.
 * Verifica che InizioServlet rifiuti input con caratteri speciali,
 * gestisca il routing e non produca errori 400 per input validi.
 */
@DisplayName("Input Validation - InizioServlet")
class InizioServletTest {

    private InizioServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new InizioServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);

        HttpSession session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
    }

    // ====== 1. Validazione del parametro 'action' - Parameterized ======

    @ParameterizedTest(name = "action=''{0}'' restituisce 400")
    @ValueSource(strings = {
            "<script>alert(1)</script>",
            "' OR '1'='1",
            "cat/../etc/passwd",
            "test;drop",
            "a<b>c"
    })
    @DisplayName("Action con caratteri malevoli restituisce 400 Bad Request")
    void testActionMalevolaRestituisce400(String action) throws Exception {
        when(request.getParameter("action")).thenReturn(action);

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // ====== 2. Gestione parametri null (action + valore) ======

    @Test
    @DisplayName("Action null e valore null restituisce 400 Bad Request")
    void testActionNullValoreNullRestituisce400() throws Exception {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("valore")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Action null e valore con <script> restituisce 400")
    void testActionNullValoreScriptRestituisce400() throws Exception {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("valore")).thenReturn("<script>alert(1)</script>");

        servlet.doGet(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // ====== 3. Routing verso pagine statiche ======

    @Test
    @DisplayName("Action 'login' forwarda a Login.jsp")
    void testLoginForwardaALoginJsp() throws Exception {
        when(request.getParameter("action")).thenReturn("login");
        when(request.getRequestDispatcher("/WEB-INF/results/Login.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("Action 'contatti' forwarda a Contatti.jsp")
    void testContattiForwardaAContattiJsp() throws Exception {
        when(request.getParameter("action")).thenReturn("contatti");
        when(request.getRequestDispatcher("/WEB-INF/results/Contatti.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    // ====== 4. Input validi superano la validazione ======

    @Test
    @DisplayName("Action valida ('Elettronica') non produce errore 400")
    void testActionValidaNonProduceErrore() throws Exception {
        when(request.getParameter("action")).thenReturn("Elettronica");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        try {
            servlet.doGet(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB non configurato
        }

        verify(response, never()).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Valore valido ('Cuscino') non produce errore 400")
    void testValoreValidoNonProduceErrore() throws Exception {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("valore")).thenReturn("Cuscino");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        try {
            servlet.doGet(request, response);
        } catch (Exception e) {
            // Eccezione attesa dal DB
        }

        verify(response, never()).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }
}