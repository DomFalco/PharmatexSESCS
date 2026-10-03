package security.businesslogic;

import Controller.Registrazione;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test dell'area OWASP: Business Logic.
 * Verifica il comportamento di Registrazione (Servlet che mostra il form
 * di registrazione pubblico). Essendo una Servlet di sola presentazione,
 * i test verificano solo il forward corretto al JSP.
 */
@DisplayName("Business Logic - Registrazione (Servlet form)")
class RegistrazioneTest {

    private Registrazione servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new Registrazione();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("La Servlet forwarda a RegisterUser.jsp")
    void testForwardARegisterUserJsp() throws Exception {
        servlet.service(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("La Servlet richiede il dispatcher per il path corretto")
    void testRichiedeDispatcherCorretto() throws Exception {
        servlet.service(request, response);

        verify(request, atLeastOnce()).getRequestDispatcher("/WEB-INF/results/RegisterUser.jsp");
    }

    @Test
    @DisplayName("La Servlet non legge parametri di input")
    void testNessunParametroLetto() throws Exception {
        servlet.service(request, response);

        verify(request, never()).getParameter(anyString());
    }

    @Test
    @DisplayName("La Servlet non scrive nella sessione")
    void testNessunaScritturaInSessione() throws Exception {
        servlet.service(request, response);

        verify(request, never()).getSession();
        verify(request, never()).getSession(anyBoolean());
    }

    @Test
    @DisplayName("La Servlet e' accessibile senza autenticazione (pagina pubblica)")
    void testAccessibileSenzaAutenticazione() throws Exception {
        // Non essendoci controlli di autenticazione, il forward avviene sempre
        servlet.service(request, response);

        verify(dispatcher).forward(request, response);
    }
}