package security.unit.inputvalidation;

import controller.*;
import model.AcquistoProdotti;
import model.Prodotto;
import model.Utente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test di copertura estesa per far superare l'85% su SonarQube.
 * Rimossi i test doGet che richiedevano il DB, mantenuti quelli sui blocchi catch.
 */
public class CoverageBoosterTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
    }

    // =========================================================================
    // Copertura dei blocchi catch interni a inviaErrore()
    // =========================================================================

    @Test
    @DisplayName("Copertura catch inviaErrore in AggiuntaProdottoServlet")
    void testAggiuntaProdottoInviaErroreCatch() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        // Forza NumberFormatException
        when(request.getParameter("larghezza")).thenReturn("abc");

        // Forza sendError a lanciare IOException, facendo scattare il catch interno a inviaErrore
        doThrow(new IOException("Simulazione errore")).when(response).sendError(anyInt(), anyString());

        new AggiuntaProdottoServlet().service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Copertura catch inviaErrore in PagamentoServlet")
    void testPagamentoServletInviaErroreCatch() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        // Forza la sessione a null per scatenare l'errore 403
        when(request.getSession(false)).thenReturn(null);

        doThrow(new IOException("Simulazione errore")).when(response).sendError(anyInt(), anyString());

        new PagamentoServlet().service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Copertura catch inviaErrore in RimuoviProdottoServlet")
    void testRimuoviProdottoInviaErroreCatch() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        Utente admin = new Utente();
        admin.setAmministratore(true);
        when(session.getAttribute("Amministratore")).thenReturn(admin);
        when(session.getAttribute("idModificaPrezzo")).thenReturn(null); // Forza errore 400

        doThrow(new IOException("Simulazione errore")).when(response).sendError(anyInt(), anyString());

        new RimuoviProdottoServlet().service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Copertura catch inviaErrore in CercaProdottoPerModificaServlet")
    void testCercaProdottoInviaErroreCatch() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(null); // Forza errore 403

        doThrow(new IOException("Simulazione errore")).when(response).sendError(anyInt(), anyString());

        new CercaProdottoPerModificaServlet().service(request, response);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    // =========================================================================
    // Copertura extra per il model AcquistoProdotti
    // =========================================================================

    @Test
    @DisplayName("Copertura getter e setter di AcquistoProdotti")
    void testAcquistoProdottiModel() {
        AcquistoProdotti ap = new AcquistoProdotti(new Utente(), new Prodotto());
        ap.setNome("Mario");
        ap.setCognome("Rossi");
        ap.setIdProdotto("P001");
        ap.setNomeProd("Materasso");
        ap.setPrezzo(100.0);
        ap.setQuantitaAcquistata(2);

        assertEquals("Mario", ap.getNome());
        assertEquals("Rossi", ap.getCognome());
        assertEquals("P001", ap.getIdProdotto());
        assertEquals("Materasso", ap.getNomeProd());
        assertEquals(100.0, ap.getPrezzo());
        assertEquals(2, ap.getQuantitaAcquistata());
        assertNotNull(ap.toString());
    }
}