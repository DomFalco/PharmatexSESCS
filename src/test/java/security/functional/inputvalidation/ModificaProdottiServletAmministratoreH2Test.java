package security.functional.inputvalidation;

import Controller.ModificaProdottiServletAmministratore;
import Model.ConPool;
import Model.Prodotto;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di ModificaProdottiServletAmministratore con H2 in-memory.
 */
@DisplayName("Functional - ModificaProdottiServletAmministratore (Input Validation)")
class ModificaProdottiServletAmministratoreH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
    }

    private void invokeService(ModificaProdottiServletAmministratore servlet) throws Exception {
        servlet.service((ServletRequest) request, (ServletResponse) response);
    }

    private Prodotto prodottoInSessione(String id, int quantita) {
        Prodotto p = new Prodotto();
        p.setIdProdotto(id);
        p.setQuantita(quantita);
        return p;
    }

    @Test
    @DisplayName("nuovoPrezzo + quantitaTotale -> aggiorna entrambi + forward")
    void testModificaPrezzoEQuantita() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("350.0");
        when(request.getParameter("quantitaTotale")).thenReturn("3");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        assertThat(getPrezzo("P0001")).isEqualTo(350.0);
        assertThat(getQuantita("P0001")).isEqualTo(8);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Solo nuovoPrezzo -> aggiorna solo il prezzo")
    void testSoloPrezzo() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("299.99");
        when(request.getParameter("quantitaTotale")).thenReturn("");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        assertThat(getPrezzo("P0001")).isEqualTo(299.99);
        assertThat(getQuantita("P0001")).isEqualTo(5);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Solo quantitaTotale -> aggiorna solo la quantita (incrementale)")
    void testSoloQuantita() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn("10");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        assertThat(getPrezzo("P0001")).isEqualTo(400.0);
        assertThat(getQuantita("P0001")).isEqualTo(15);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("FINDING CWE-862: nessun controllo autorizzazione (utente anonimo puo' modificare)")
    void testNessunControlloAutorizzazione_BugDocumentato() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(session.getAttribute("Amministratore")).thenReturn(null);
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("999.99");
        when(request.getParameter("quantitaTotale")).thenReturn("");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        assertThat(getPrezzo("P0001")).isEqualTo(999.99);
        verify(response, never()).sendError(eq(403), anyString());
    }

    @Test
    @DisplayName("FIX CWE-476: nuovoPrezzo null -> Bad Request 400")
    void testNuovoPrezzoNull() throws Exception {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn(null);
        when(request.getParameter("quantitaTotale")).thenReturn("3");
        ModificaProdottiServletAmministratore servlet = new ModificaProdottiServletAmministratore();

        invokeService(servlet);

        // FIX: La servlet ora cattura l'eccezione e risponde 400
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("FIX CWE-476: idModificaPrezzo null -> Bad Request 400")
    void testIdModificaPrezzoNull() throws Exception {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(null);
        when(request.getParameter("nuovoPrezzo")).thenReturn("350.0");
        when(request.getParameter("quantitaTotale")).thenReturn("3");
        ModificaProdottiServletAmministratore servlet = new ModificaProdottiServletAmministratore();

        invokeService(servlet);

        // FIX: La servlet ora cattura l'eccezione e risponde 400
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("nuovoPrezzo non numerico -> Bad Request 400")
    void testNuovoPrezzoNonNumerico() throws Exception {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("abc");
        when(request.getParameter("quantitaTotale")).thenReturn("3");
        ModificaProdottiServletAmministratore servlet = new ModificaProdottiServletAmministratore();

        invokeService(servlet);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("quantitaTotale non numerica -> Bad Request 400")
    void testQuantitaTotaleNonNumerica() throws Exception {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn("dieci");
        ModificaProdottiServletAmministratore servlet = new ModificaProdottiServletAmministratore();

        invokeService(servlet);

        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("FIX: nuovoPrezzo='' e quantitaTotale=null -> Bad Request 400 (invece di silent drop)")
    void testSilentDrop() throws Exception {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn(null);

        invokeService(new ModificaProdottiServletAmministratore());

        // FIX: Non c'è più il silent drop, ora la Servlet blocca l'esecuzione
        verify(dispatcher, never()).forward(any(), any());
        verify(response).sendError(eq(HttpServletResponse.SC_BAD_REQUEST), anyString());
    }

    @Test
    @DisplayName("doGet inoltra a doPost (comportamento trasparente)")
    void testDoGetInoltraADoPost() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(request.getMethod()).thenReturn("GET");
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("300.0");
        when(request.getParameter("quantitaTotale")).thenReturn("");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        assertThat(getPrezzo("P0001")).isEqualTo(300.0);
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Helper
    // ==================================================================

    private double getPrezzo(String id) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT prezzo FROM Prodotto WHERE idProdotto = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    private int getQuantita(String id) throws Exception {
        try (Connection conn = ConPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT quantita FROM Prodotto WHERE idProdotto = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private String insertProdotto(String id, String nome, double prezzo, int quantita) {
        return "INSERT INTO Prodotto (idProdotto, nomeCategoria, nomeProd, descrizione, "
                + "prezzo, quantita) VALUES ('"
                + id + "', 'Test', '" + nome + "', 'Desc', " + prezzo + ", " + quantita + ")";
    }
}