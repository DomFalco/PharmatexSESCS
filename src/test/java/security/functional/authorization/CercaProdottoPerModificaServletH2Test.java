package security.functional.authorization;

import Controller.CercaProdottoPerModificaServlet;
import Model.Prodotto;
import Model.Utente;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di CercaProdottoPerModificaServlet con H2 in-memory.
 * Verifica:
 * - Autorizzazione: sessione mancante / admin mancante / non-admin → 403
 * - Validazione input: search null o invalido → 400
 * - Ricerca prodotto: cerca in DB reale con LIKE case-insensitive
 * - FINDING (documentato): il controllo `pmod == null` è codice morto,
 *   perché doRetriveBySearch ritorna sempre un oggetto (eventualmente vuoto)
 */
@DisplayName("Functional - CercaProdottoPerModificaServlet (Authorization)")
class CercaProdottoPerModificaServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(CercaProdottoPerModificaServlet servlet) throws Exception {
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
    // Autorizzazione (403)
    // ==================================================================

    @Test
    @DisplayName("Sessione mancante → 403")
    void testSessioneNullRiceve403() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        invokeService(new CercaProdottoPerModificaServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Admin null in sessione → 403")
    void testAdminNullRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(null);

        invokeService(new CercaProdottoPerModificaServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Utente non amministratore → 403")
    void testUtenteNonAdminRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(utenteNormale());

        invokeService(new CercaProdottoPerModificaServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    // ==================================================================
    // Validazione input (400)
    // ==================================================================

    @Test
    @DisplayName("Search null → 400")
    void testSearchNullRiceve400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("search")).thenReturn(null);

        invokeService(new CercaProdottoPerModificaServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("Search con caratteri invalidi → 400")
    void testSearchInvalidoRiceve400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("search")).thenReturn("'; DROP TABLE--");

        invokeService(new CercaProdottoPerModificaServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    // ==================================================================
    // Ricerca valida (forward)
    // ==================================================================

    @Test
    @DisplayName("Search valido + prodotto esistente → forward a ModificaProdotto.jsp")
    void testRicercaProdottoEsistente() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("search")).thenReturn("Nuvola");
        when(request.getRequestDispatcher("/WEB-INF/amministratore/ModificaProdotto.jsp"))
                .thenReturn(dispatcher);

        invokeService(new CercaProdottoPerModificaServlet());

        verify(request).setAttribute(eq("prodottoModifica"), any(Prodotto.class));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Search case-insensitive: 'nuvola' trova 'Nuvola' (grazie al fix SEC-DAO-01)")
    void testRicercaCaseInsensitive() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("search")).thenReturn("nuvola");
        when(request.getRequestDispatcher("/WEB-INF/amministratore/ModificaProdotto.jsp"))
                .thenReturn(dispatcher);

        invokeService(new CercaProdottoPerModificaServlet());

        // Grazie al fix SEC-DAO-01, "nuvola" trova "Nuvola"
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // FINDING: il 404 è codice morto
    // ==================================================================

    /**
     * FINDING (documentato): la Servlet non restituisce mai 404.
     * Il metodo ProdottoDAO.doRetriveBySearch ritorna SEMPRE un oggetto
     * Prodotto (anche se non trova nulla: in quel caso è un oggetto vuoto
     * con campi a null). Quindi il controllo `if (pmod == null)` è codice
     * morto: non viene mai eseguito.
     * Conseguenza: quando il prodotto non esiste, la Servlet fa comunque
     * forward alla JSP con un prodotto vuoto, mostrando una pagina vuota
     * invece di un errore 404.
     */
    @Test
    @DisplayName("FINDING: prodotto non trovato → NON restituisce 404 (il controllo è codice morto)")
    void testProdottoNonTrovato_BugDocumentato() throws Exception {
        // DB vuoto
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(request.getParameter("search")).thenReturn("NonEsiste");
        when(request.getRequestDispatcher("/WEB-INF/amministratore/ModificaProdotto.jsp"))
                .thenReturn(dispatcher);

        invokeService(new CercaProdottoPerModificaServlet());

        // Non viene inviato 404 (bug: il controllo pmod == null non funziona)
        verify(response, never()).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
        // Invece: forward con prodotto vuoto
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Helper: INSERT prodotto
    // ==================================================================

    private String insertProdotto(String id, String categoria, String nome,
                                  double prezzo, int quantita) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita) VALUES ('"
                + id + "', '" + categoria + "', '" + nome + "', "
                + "'Descrizione di test', " + prezzo + ", " + quantita + ")";
    }
}