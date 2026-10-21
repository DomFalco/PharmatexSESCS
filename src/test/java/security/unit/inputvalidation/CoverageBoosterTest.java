package security.unit.inputvalidation;

import Controller.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test di copertura per i blocchi catch delle Servlet.
 * Simulano errori per far scattare i catch senza aver bisogno del database.
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

    @Test
    @DisplayName("Copertura HomeServlet catch 500")
    void testHomeServletCatch() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        // Evita la chiamata al DB usando il ramo "home"
        when(request.getParameter("valore")).thenReturn("home");
        // Rende valida la sessione
        Model.Utente admin = new Model.Utente();
        admin.setAmministratore(true);
        when(session.getAttribute("Amministratore")).thenReturn(admin);

        // Fa fallire il forward per scatenare il catch
        doThrow(new IOException("Errore simulato")).when(dispatcher).forward(any(), any());

        new HomeServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    @DisplayName("Copertura InizioServlet catch 500")
    void testInizioServletCatch() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        // Evita il DB usando il ramo "login"
        when(request.getParameter("action")).thenReturn("login");

        doThrow(new IOException("Errore simulato")).when(dispatcher).forward(any(), any());

        new InizioServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    @DisplayName("Copertura MaterialeServlet catch 500")
    void testMaterialeServletCatch() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        // Evita il DB facendo restituire null (va nel ramo RicercaErrata)
        when(session.getAttribute("mat")).thenReturn(null);
        when(session.getAttribute("materiale")).thenReturn(null);

        doThrow(new IOException("Errore simulato")).when(dispatcher).forward(any(), any());

        new MaterialeServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    @DisplayName("Copertura PagamentoServlet catch 500")
    void testPagamentoServletCatch() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        // Rende valida la sessione
        Model.Utente u = new Model.Utente();
        when(session.getAttribute("Utente")).thenReturn(u);

        doThrow(new IOException("Errore simulato")).when(dispatcher).forward(any(), any());

        new PagamentoServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }

    @Test
    @DisplayName("Copertura Registrazione catch 500")
    void testRegistrazioneCatch() throws Exception {
        when(request.getMethod()).thenReturn("GET");

        doThrow(new IOException("Errore simulato")).when(dispatcher).forward(any(), any());

        new Registrazione().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR), anyString());
    }
}