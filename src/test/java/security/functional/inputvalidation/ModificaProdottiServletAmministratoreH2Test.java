package security.functional.inputvalidation;

import Controller.ModificaProdottiServletAmministratore;
import Model.ConPool;
import Model.Prodotto;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import security.functional.BaseServletH2Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Test funzionali di ModificaProdottiServletAmministratore con H2 in-memory.
 *
 * Verifica:
 * - Modifica prezzo + quantita (quantita INCREMENTALE: p.quantita + q)
 * - Solo prezzo
 * - Solo quantita
 * - FINDING CWE-862: nessun controllo autorizzazione
 * - FINDING CWE-476: NPE su nuovoPrezzo null
 * - FINDING CWE-476: NPE su idModificaPrezzo null
 * - FINDING: NumberFormatException su input non numerico
 * - FINDING: silent drop se nuovoPrezzo="" e quantitaTotale=null
 */
@DisplayName("Functional - ModificaProdottiServletAmministratore (Input Validation)")
class ModificaProdottiServletAmministratoreH2Test extends BaseServletH2Test {

    @BeforeEach
    void setUp() {
        setUpServletMocks();
        // La Servlet implementa doGet + doPost (delega) -> POST default ok
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

    // ==================================================================
    // Modifica prezzo + quantita
    // ==================================================================

    @Test
    @DisplayName("nuovoPrezzo + quantitaTotale -> aggiorna entrambi + forward")
    void testModificaPrezzoEQuantita() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("350.0");
        when(request.getParameter("quantitaTotale")).thenReturn("3");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        // Prezzo aggiornato a 350
        assertThat(getPrezzo("P0001")).isEqualTo(350.0);
        // Quantita INCREMENTALE: 5 + 3 = 8
        assertThat(getQuantita("P0001")).isEqualTo(8);
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Solo prezzo
    // ==================================================================

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
        // Quantita invariata
        assertThat(getQuantita("P0001")).isEqualTo(5);
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // Solo quantita
    // ==================================================================

    @Test
    @DisplayName("Solo quantitaTotale -> aggiorna solo la quantita (incrementale)")
    void testSoloQuantita() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn("10");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        // Prezzo invariato
        assertThat(getPrezzo("P0001")).isEqualTo(400.0);
        // Quantita: 5 + 10 = 15
        assertThat(getQuantita("P0001")).isEqualTo(15);
        verify(dispatcher).forward(request, response);
    }

    // ==================================================================
    // FINDING CWE-862: nessun controllo autorizzazione
    // ==================================================================

    /**
     * FINDING (documentato): CWE-862 - Missing Authorization.
     *
     * La Servlet NON controlla ne' la sessione ne' il ruolo admin.
     * Chiunque puo' modificare il prezzo o la quantita di un prodotto,
     * anche un utente anonimo o un utente non amministratore.
     *
     * Fix suggerito: aggiungere il controllo sessione + ruolo admin
     * come nelle altre Servlet (es. CercaProdottoPerModificaServlet).
     */
    @Test
    @DisplayName("FINDING CWE-862: nessun controllo autorizzazione (utente anonimo puo' modificare)")
    void testNessunControlloAutorizzazione_BugDocumentato() throws Exception {
        executeSql(insertProdotto("P0001", "Nuvola", 400.0, 5));
        // Nessun attributo "Amministratore" in sessione
        when(session.getAttribute("Amministratore")).thenReturn(null);
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("999.99");
        when(request.getParameter("quantitaTotale")).thenReturn("");
        when(request.getRequestDispatcher("HomeServletAmministratore")).thenReturn(dispatcher);

        invokeService(new ModificaProdottiServletAmministratore());

        // Il prezzo E' STATO modificato NONOSTANTE l'assenza di autenticazione
        assertThat(getPrezzo("P0001")).isEqualTo(999.99);
        verify(response, never()).sendError(eq(403), anyString());
    }

    // ==================================================================
    // FINDING CWE-476: nuovoPrezzo null
    // ==================================================================

    /**
     * FINDING (documentato): CWE-476 - NPE su nuovoPrezzo null.
     *
     * La Servlet chiama `request.getParameter("nuovoPrezzo").equals("")`
     * senza verificare che il parametro non sia null. Se il client non invia
     * il parametro, getParameter ritorna null -> NPE.
     */
    @Test
    @DisplayName("FINDING CWE-476: nuovoPrezzo null -> NullPointerException")
    void testNuovoPrezzoNull_BugDocumentato() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn(null);
        when(request.getParameter("quantitaTotale")).thenReturn("3");

        assertThatThrownBy(() -> invokeService(new ModificaProdottiServletAmministratore()))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================================================================
    // FINDING CWE-476: idModificaPrezzo null
    // ==================================================================

    /**
     * FINDING (documentato): CWE-476 - NPE su idModificaPrezzo null.
     *
     * Se l'attributo "idModificaPrezzo" non e' in sessione (es. l'admin
     * non ha selezionato un prodotto prima), la Servlet chiama
     * `p.getIdProdotto()` su null -> NPE.
     */
    @Test
    @DisplayName("FINDING CWE-476: idModificaPrezzo null -> NullPointerException")
    void testIdModificaPrezzoNull_BugDocumentato() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(null);
        when(request.getParameter("nuovoPrezzo")).thenReturn("350.0");
        when(request.getParameter("quantitaTotale")).thenReturn("3");

        assertThatThrownBy(() -> invokeService(new ModificaProdottiServletAmministratore()))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================================================================
    // FINDING: NumberFormatException
    // ==================================================================

    @Test
    @DisplayName("FINDING: nuovoPrezzo non numerico -> NumberFormatException")
    void testNuovoPrezzoNonNumerico_BugDocumentato() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("abc");
        when(request.getParameter("quantitaTotale")).thenReturn("3");

        assertThatThrownBy(() -> invokeService(new ModificaProdottiServletAmministratore()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("FINDING: quantitaTotale non numerica -> NumberFormatException")
    void testQuantitaTotaleNonNumerica_BugDocumentato() {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn("dieci");

        assertThatThrownBy(() -> invokeService(new ModificaProdottiServletAmministratore()))
                .isInstanceOf(NumberFormatException.class);
    }

    // ==================================================================
    // FINDING: silent drop
    // ==================================================================

    /**
     * FINDING (documentato): silent drop se nuovoPrezzo="" e quantitaTotale=null.
     *
     * I tre rami della Servlet sono:
     *   1. nuovoPrezzo != "" AND quantitaTotale != ""  (entrambi valorizzati)
     *   2. nuovoPrezzo != ""                            (solo prezzo)
     *   3. quantitaTotale != null                       (solo quantita)
     *
     * Se nuovoPrezzo="" e quantitaTotale=null:
     *   - Ramo 1: nuovoPrezzo != "" ? NO
     *   - Ramo 2: nuovoPrezzo != "" ? NO
     *   - Ramo 3: quantitaTotale != null ? NO
     *
     * Nessun ramo viene eseguito -> nessun forward -> risposta vuota.
     */
    @Test
    @DisplayName("FINDING: nuovoPrezzo='' e quantitaTotale=null -> silent drop")
    void testSilentDrop_BugDocumentato() throws Exception {
        when(session.getAttribute("idModificaPrezzo")).thenReturn(prodottoInSessione("P0001", 5));
        when(request.getParameter("nuovoPrezzo")).thenReturn("");
        when(request.getParameter("quantitaTotale")).thenReturn(null);

        invokeService(new ModificaProdottiServletAmministratore());

        // Nessun forward, nessun errore
        verify(dispatcher, never()).forward(any(), any());
        verify(response, never()).sendError(anyInt(), anyString());
    }

    // ==================================================================
    // doGet -> doPost
    // ==================================================================

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