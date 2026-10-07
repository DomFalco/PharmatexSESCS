package security.functional.businesslogic;

import Controller.HomeServlet;
import Model.Prodotto;
import Model.Utente;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di HomeServlet con H2 in-memory.
 * HomeServlet ha 3 comportamenti:
 * 1. valore == null  → homepage pubblica (random + tutti i prodotti)
 * 2. valore == "home" → dashboard admin (solo admin autenticati)
 * 3. valore sconosciuto → 400
 */
@DisplayName("Functional - HomeServlet (Business Logic)")
class HomeServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(HomeServlet servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    private Utente admin() {
        Utente u = new Utente();
        u.setEmail("admin@test.com");
        u.setAmministratore(true);
        return u;
    }

    private Utente utenteNormale() {
        Utente u = new Utente();
        u.setEmail("mario@test.com");
        u.setAmministratore(false);
        return u;
    }

    // ==================================================================
    // Ramo 1: valore == null → homepage pubblica
    // ==================================================================

    @Test
    @DisplayName("valore=null → imposta 'Valore' random + carica tutti i prodotti + forward HomePage.jsp")
    void testHomepagePubblica() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Letto", "Dublino", 300.0, 3));
        when(request.getParameter("valore")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/results/HomePage.jsp")).thenReturn(dispatcher);

        invokeService(new HomeServlet());

        verify(request).setAttribute(eq("Valore"), argThat(o -> {
            int v = (int) o;
            return v >= 5 && v <= 48;
        }));
        verify(request).setAttribute(eq("prodotti"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.size() == 2;
        }));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("valore=null con DB vuoto → lista prodotti vuota")
    void testHomepagePubblicaDbVuoto() throws Exception {
        when(request.getParameter("valore")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/results/HomePage.jsp")).thenReturn(dispatcher);

        invokeService(new HomeServlet());

        verify(request).setAttribute(eq("prodotti"), argThat(o -> {
            @SuppressWarnings("unchecked")
            ArrayList<Prodotto> list = (ArrayList<Prodotto>) o;
            return list.isEmpty();
        }));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("valore=null è accessibile anche senza sessione (pagina pubblica)")
    void testHomepagePubblicaSenzaSessione() throws Exception {
        when(request.getParameter("valore")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/results/HomePage.jsp")).thenReturn(dispatcher);

        invokeService(new HomeServlet());

        verify(response, never()).sendError(anyInt(), anyString());
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Ramo 2: valore == "home" → dashboard admin
    // ==================================================================

    @Test
    @DisplayName("valore=home senza sessione → 403")
    void testHomeAdminSessioneNullRiceve403() throws Exception {
        when(request.getParameter("valore")).thenReturn("home");
        when(request.getSession(false)).thenReturn(null);

        invokeService(new HomeServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("valore=home con admin null → 403")
    void testHomeAdminNullRiceve403() throws Exception {
        when(request.getParameter("valore")).thenReturn("home");
        when(session.getAttribute("Amministratore")).thenReturn(null);

        invokeService(new HomeServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("valore=home con utente non-admin → 403")
    void testHomeAdminUtenteNonAdminRiceve403() throws Exception {
        when(request.getParameter("valore")).thenReturn("home");
        when(session.getAttribute("Amministratore")).thenReturn(utenteNormale());

        invokeService(new HomeServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("valore=home con admin autenticato → forward a VediTuttiIProdotti.jsp")
    void testHomeAdminSuccesso() throws Exception {
        when(request.getParameter("valore")).thenReturn("home");
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp"))
                .thenReturn(dispatcher);

        invokeService(new HomeServlet());

        verify(dispatcher).forward(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    // ==================================================================
    // Ramo 3: valore sconosciuto → 400
    // ==================================================================

    @Test
    @DisplayName("valore sconosciuto → 400")
    void testValoreSconosciutoRiceve400() throws Exception {
        when(request.getParameter("valore")).thenReturn("xyz");

        invokeService(new HomeServlet());

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione non riconosciuta.");
    }

    // ==================================================================
    // doPost → doGet
    // ==================================================================

    @Test
    @DisplayName("doPost inoltra a doGet (comportamento trasparente)")
    void testDoPostInoltraADoGet() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("valore")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/results/HomePage.jsp")).thenReturn(dispatcher);

        invokeService(new HomeServlet());

        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Helper
    // ==================================================================

    private String insertProdotto(String id, String categoria, String nome,
                                  double prezzo, int quantita) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita) VALUES ('"
                + id + "', '" + categoria + "', '" + nome + "', "
                + "'Descrizione di test', " + prezzo + ", " + quantita + ")";
    }
}