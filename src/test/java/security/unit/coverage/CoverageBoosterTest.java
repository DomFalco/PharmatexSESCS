package security.unit.coverage;

import controller.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test per massimizzare la copertura dei blocchi catch delle Servlet.
 */
public class CoverageBoosterTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private void forceError() throws IOException {
        doThrow(new IOException("Simulazione errore")).when(response).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("Catch AggiuntaProdottoServlet")
    void testAggiunta() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("larghezza")).thenReturn("abc");
        forceError();
        new AggiuntaProdottoServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Catch CarrelloServlet")
    void testCarrello() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        doThrow(new IOException("Simulazione")).when(dispatcher).forward(any(), any());
        forceError();
        new CarrelloServlet().service(request, response);
        // FIX: Aggiunta verifica per evitare il Blocker
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    @DisplayName("Catch CercaProdottoPerModificaServlet")
    void testCerca() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(null);
        forceError();
        new CercaProdottoPerModificaServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Catch DatiPagamentoServlet")
    void testDatiPagamento() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(null);
        forceError();
        new DatiPagamentoServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Catch HomeServlet")
    void testHome() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("valore")).thenReturn("home");
        when(request.getSession(false)).thenReturn(null);
        forceError();
        new HomeServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Catch HomeServletAmministratore")
    void testHomeAdmin() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(null);
        forceError();
        new HomeServletAmministratore().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Catch InizioServlet")
    void testInizio() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("valore")).thenReturn("!!!");
        forceError();
        new InizioServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Catch LoginServlet")
    void testLogin() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("carrello");
        doThrow(new IOException("Simulazione")).when(dispatcher).forward(any(), any());
        forceError();
        new LoginServlet().service(request, response);
        // FIX: Aggiunta verifica per evitare il Blocker
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    @DisplayName("Catch PagamentoServlet")
    void testPagamento() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(null);
        forceError();
        new PagamentoServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Catch Registrazione")
    void testRegistrazione() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        doThrow(new IOException("Simulazione")).when(dispatcher).forward(any(), any());
        forceError();
        new Registrazione().service(request, response);
        // FIX: Aggiunta verifica per evitare il Blocker
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    @DisplayName("Catch RendiAmministratoreServlet")
    void testRendi() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(null);
        forceError();
        new RendiAmministratoreServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Catch RimuoviProdottoServlet")
    void testRimuovi() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(null);
        forceError();
        new RimuoviProdottoServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Catch ModificaProdottiServletAmministratore")
    void testModifica() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(session.getAttribute("idModificaPrezzo")).thenReturn(null);
        forceError();
        new ModificaProdottiServletAmministratore().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }
}