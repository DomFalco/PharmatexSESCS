package security.functional.authorization;

import Controller.RimuoviProdottoServlet;
import Model.ConPool;
import Model.Prodotto;
import Model.Utente;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di RimuoviProdottoServlet con H2 in-memory.
 *
 * Verifica:
 * - Autorizzazione: sessione mancante / non-admin → 403
 * - Validazione: prodotto in sessione null / idProdotto null → 400
 * - Rimozione: DELETE dal DB + forward + attributo tuttiProdotti
 * - FINDING: prodotto in sessione ma non nel DB → RuntimeException (500)
 */
@DisplayName("Functional - RimuoviProdottoServlet (Authorization)")
class RimuoviProdottoServletH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(RimuoviProdottoServlet servlet) throws Exception {
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

    private Prodotto prodottoInSessione(String id) {
        Prodotto p = new Prodotto();
        p.setIdProdotto(id);
        return p;
    }

    // ==================================================================
    // Autorizzazione (403)
    // ==================================================================

    @Test
    @DisplayName("Sessione mancante → 403")
    void testSessioneNullRiceve403() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        invokeService(new RimuoviProdottoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Admin null → 403")
    void testAdminNullRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(null);

        invokeService(new RimuoviProdottoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    @DisplayName("Utente non-admin → 403")
    void testUtenteNonAdminRiceve403() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(utenteNormale());

        invokeService(new RimuoviProdottoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    // ==================================================================
    // Validazione input (400)
    // ==================================================================

    @Test
    @DisplayName("Prodotto in sessione null → 400")
    void testProdottoNullRiceve400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(session.getAttribute("idModificaPrezzo")).thenReturn(null);

        invokeService(new RimuoviProdottoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST),
                eq("Nessun prodotto selezionato per la rimozione."));
    }

    @Test
    @DisplayName("Prodotto con idProdotto null → 400")
    void testIdProdottoNullRiceve400() throws Exception {
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        Prodotto p = new Prodotto();
        p.setIdProdotto(null);
        when(session.getAttribute("idModificaPrezzo")).thenReturn(p);

        invokeService(new RimuoviProdottoServlet());

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST),
                eq("Nessun prodotto selezionato per la rimozione."));
    }

    // ==================================================================
    // Rimozione riuscita
    // ==================================================================

    @Test
    @DisplayName("Prodotto esistente → DELETE + forward a VediTuttiIProdotti.jsp")
    void testRimozioneSuccesso() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        executeSql(insertProdotto("P0002", "Letto", "Dublino", 300.0, 3));
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001"));
        when(request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp"))
                .thenReturn(dispatcher);

        invokeService(new RimuoviProdottoServlet());

        // P0001 eliminato, P0002 ancora presente
        assertThat(countProdotti("P0001")).isZero();
        assertThat(countProdotti("P0002")).isEqualTo(1);
        // Attributo tuttiProdotti contiene solo P0002
        verify(request).setAttribute(eq("tuttiProdotti"), argThat(o -> {
            java.util.ArrayList<?> list = (java.util.ArrayList<?>) o;
            return list.size() == 1;
        }));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Rimozione dell'ultimo prodotto → lista vuota passata alla JSP")
    void testRimozioneUltimoProdotto() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001"));
        when(request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp"))
                .thenReturn(dispatcher);

        invokeService(new RimuoviProdottoServlet());

        verify(request).setAttribute(eq("tuttiProdotti"), argThat(o -> {
            java.util.ArrayList<?> list = (java.util.ArrayList<?>) o;
            return list.isEmpty();
        }));
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // FINDING: prodotto in sessione ma non nel DB
    // ==================================================================

    /**
     * FINDING (documentato): prodotto in sessione ma non nel DB → 500.
     *
     * Se il prodotto salvato in sessione ("idModificaPrezzo") non esiste più
     * nel database (es. cancellato da un altro admin, o id manomesso), la
     * chiamata a ProdottoDAO.cancellaProdotto lancia RuntimeException
     * ("DELETE error.") perché executeUpdate() ritorna 0.
     *
     * La Servlet non gestisce questa eccezione, quindi risponde con 500
     * (Internal Server Error) invece di un 404 più appropriato.
     *
     * Fix suggerito: gestire l'eccezione e restituire SC_NOT_FOUND con un
     * messaggio "Prodotto non trovato".
     */
    @Test
    @DisplayName("FINDING: prodotto in sessione ma non nel DB → RuntimeException (500)")
    void testProdottoNonNelDb_BugDocumentato() {
        // DB vuoto, ma in sessione c'è un prodotto con id inesistente
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P9999"));

        assertThatThrownBy(() -> invokeService(new RimuoviProdottoServlet()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DELETE error");
    }

    // ==================================================================
    // doGet → doPost
    // ==================================================================

    @Test
    @DisplayName("doGet inoltra a doPost (comportamento trasparente)")
    void testDoGetInoltraADoPost() throws Exception {
        executeSql(insertProdotto("P0001", "Materasso", "Nuvola", 400.0, 5));
        when(request.getMethod()).thenReturn("GET");
        when(session.getAttribute("Amministratore")).thenReturn(admin());
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001"));
        when(request.getRequestDispatcher("/WEB-INF/amministratore/VediTuttiIProdotti.jsp"))
                .thenReturn(dispatcher);

        invokeService(new RimuoviProdottoServlet());

        assertThat(countProdotti("P0001")).isZero();
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Helper
    // ==================================================================

    private int countProdotti(String id) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM Prodotto WHERE idProdotto = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private String insertProdotto(String id, String categoria, String nome,
                                  double prezzo, int quantita) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita) VALUES ('"
                + id + "', '" + categoria + "', '" + nome + "', "
                + "'Descrizione di test', " + prezzo + ", " + quantita + ")";
    }
}