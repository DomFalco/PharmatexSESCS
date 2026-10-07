package security.functional;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

/**
 * Classe base per i test funzionali delle Servlet con H2 in-memory.
 * Estende BaseH2Test (che configura il DB H2) e aggiunge i mock della
 * Servlet API (request, response, session, dispatcher).
 * I test che ereditano possono:
 *  - popolare il DB con executeSql(...)
 *  - configurare i mock con Mockito.when(...)
 *  - invocare la Servlet con service(...) (metodo pubblico di HttpServlet)
 *  - verificare il comportamento con Mockito.verify(...)
 */
public abstract class BaseServletH2Test extends BaseH2Test {

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected HttpSession session;
    protected RequestDispatcher dispatcher;

    /**
     * Configura i mock comuni. Le sottoclassi devono chiamare questo metodo
     * in un proprio @BeforeEach.
     */
    protected void setUpServletMocks() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        // Configurazione base comune a tutte le Servlet
        Mockito.when(request.getSession()).thenReturn(session);
        Mockito.when(request.getSession(false)).thenReturn(session);
        Mockito.when(request.getMethod()).thenReturn("POST");
        Mockito.when(request.getProtocol()).thenReturn("HTTP/1.1");
        Mockito.when(request.getContextPath()).thenReturn("/PharmatexSESCS");
    }
}